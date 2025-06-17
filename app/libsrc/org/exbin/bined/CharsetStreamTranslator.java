/*
 * Copyright (C) ExBin Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.exbin.bined;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;
import java.nio.charset.CodingErrorAction;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Input stream translation class which converts from input charset to target
 * charset.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class CharsetStreamTranslator extends InputStream {

    private static final String UNMAPPABLE_CHARACTER_ERROR = "Unmappable character should be handled automatically";
    private static final String MALFORMED_CHARACTER_ERROR = "Malformed character should be handled automatically";

    public static final String DEFAULT_ENCODING = "UTF-8";
    public static final int DEFAULT_MAX_BYTES_PER_CHAR = 8;
    public static final int BYTE_BUFFER_SIZE = 16;

    @Nonnull
    protected final CharsetEncoder encoder;
    @Nonnull
    protected final CharsetDecoder decoder;
    @Nonnull
    protected final InputStream source;

    @Nonnull
    protected final ByteBuffer inputBuffer;
    @Nonnull
    protected final ByteBuffer outputBuffer;
    @Nonnull
    protected final CharBuffer charBuffer;
    protected boolean endOfInput = false;

    protected int maxInputCharSize;
    protected int maxOutputCharSize;

    public CharsetStreamTranslator(Charset inputCharset, Charset outputCharset, InputStream source, int bufferSize) {
        this.source = source;
        decoder = inputCharset.newDecoder();
        decoder.onUnmappableCharacter(CodingErrorAction.REPLACE);
        decoder.onMalformedInput(CodingErrorAction.REPLACE);
        CharsetEncoder outputEncoder;
        try {
            outputEncoder = outputCharset.newEncoder();
        } catch (UnsupportedOperationException ex) {
            outputEncoder = Charset.forName(DEFAULT_ENCODING).newEncoder();
        }
        encoder = outputEncoder;
        encoder.onUnmappableCharacter(CodingErrorAction.REPLACE);
        encoder.onMalformedInput(CodingErrorAction.REPLACE);
        maxInputCharSize = (int) decoder.maxCharsPerByte();
        if (maxInputCharSize < 0) {
            maxInputCharSize = 1;
        }
        maxOutputCharSize = (int) encoder.maxBytesPerChar();
        if (maxOutputCharSize < 0) {
            maxOutputCharSize = 1;
        }

        charBuffer = CharBuffer.allocate(bufferSize * 8);

        inputBuffer = ByteBuffer.allocate(bufferSize);
        inputBuffer.limit(0);

        outputBuffer = ByteBuffer.allocate(bufferSize * maxOutputCharSize * 8);
        outputBuffer.limit(0);
    }

    public CharsetStreamTranslator(Charset inputCharset, Charset outputCharset, InputStream source) {
        this(inputCharset, outputCharset, source, BYTE_BUFFER_SIZE);
    }

    @Override
    public int read() throws IOException {
        boolean dataReady = outputBuffer.remaining() > 0;
        if (!dataReady) {
            if (endOfInput) {
                return -1;
            } else {
                processNext();
                if (outputBuffer.remaining() == 0) {
                    return -1;
                }
            }
        }

        return outputBuffer.get() & 0xFF;
    }

    @Override
    public int read(byte[] buffer, int offset, int length) throws IOException {
        int processed = 0;

        while (processed < length) {
            int remaining = outputBuffer.remaining();
            if (remaining == 0) {
                if (endOfInput) {
                    return processed > 0 ? processed : -1;
                } else {
                    processNext();
                    remaining = outputBuffer.remaining();
                    if (remaining == 0) {
                        return processed > 0 ? processed : -1;
                    }
                }
            }

            int toProcess = Math.min(length, remaining);
            outputBuffer.get(buffer, offset, toProcess);
            offset += toProcess;
            length -= toProcess;
            processed += toProcess;
        }

        return processed;
    }

    public void processNext() {
        charBuffer.rewind();
        charBuffer.limit(charBuffer.capacity());

        do {
            loadFromInput();
            if (inputBuffer.remaining() == 0) {
                return;
            }

            decoder.reset();
            CoderResult decodeResult = decoder.decode(inputBuffer, charBuffer, endOfInput);
            if (decodeResult.isOverflow()) {
                // Can't fix partial character on the end of the stream -> ignore
                break;
            } else if (decodeResult.isError()) {
                // Skip byte or ignore
                if (charBuffer.position() == 0 && inputBuffer.remaining() > 0) {
                    inputBuffer.position(inputBuffer.position() + 1);
                }
            } else if (decodeResult.isUnmappable()) {
                throw new IllegalStateException(UNMAPPABLE_CHARACTER_ERROR);
            } else if (decodeResult.isMalformed()) {
                throw new IllegalStateException(MALFORMED_CHARACTER_ERROR);
            }
        } while (charBuffer.position() == 0);

        int chars = charBuffer.position();
        charBuffer.rewind();
        charBuffer.limit(chars);

        outputBuffer.limit(outputBuffer.capacity());
        outputBuffer.clear();
        while (charBuffer.remaining() > 0) {
            encoder.reset();
            CoderResult encodeResult = encoder.encode(charBuffer, outputBuffer, endOfInput);
            if (encodeResult.isOverflow()) {
                throw new IllegalStateException("Unexpected overflow character");
            } else if (encodeResult.isError()) {
                throw new IllegalStateException("Unexpected error character");
            } else if (encodeResult.isUnmappable()) {
                throw new IllegalStateException(UNMAPPABLE_CHARACTER_ERROR);
            } else if (encodeResult.isMalformed()) {
                throw new IllegalStateException(MALFORMED_CHARACTER_ERROR);
            }
        }

        int length = outputBuffer.position();
        outputBuffer.rewind();
        outputBuffer.limit(length);
    }

    @Override
    public int available() throws IOException {
        int remaining = outputBuffer.remaining();
        if (remaining > 0) {
            return remaining;
        }

        return endOfInput ? 1 : 0;
    }

    private void loadFromInput() {
        byte[] buffer = inputBuffer.array();
        int remainingLength = inputBuffer.remaining();
        if (remainingLength > 0) {
            // Copy remaining data from previous processing
            System.arraycopy(buffer, inputBuffer.position(), buffer, 0, remainingLength);
            inputBuffer.rewind();
            inputBuffer.limit(remainingLength);
            inputBuffer.position(remainingLength);
        } else {
            inputBuffer.rewind();
            inputBuffer.limit(0);
        }

        int position = inputBuffer.position();
        int toRead = inputBuffer.capacity() - inputBuffer.position();
        inputBuffer.limit(position + toRead);
        int offset = position;
        while (toRead > 0) {
            try {
                int read = source.read(buffer, offset, toRead);
                if (read < 0) {
                    inputBuffer.limit(offset);
                    endOfInput = true;
                    break;
                }

                offset += read;
                toRead -= read;
            } catch (IOException ex) {
                Logger.getLogger(CharsetStreamTranslator.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        inputBuffer.rewind();
    }
}
