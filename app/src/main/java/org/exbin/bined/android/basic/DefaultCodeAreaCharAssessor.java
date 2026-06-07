/*
 * Copyright (C) ExBin Project, https://exbin.org
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
package org.exbin.bined.android.basic;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CoderMalfunctionError;
import java.nio.charset.CodingErrorAction;
import java.util.Optional;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.exbin.bined.CodeAreaSection;
import org.exbin.bined.android.CodeAreaCharAssessor;
import org.exbin.bined.android.CodeAreaPaintState;

/**
 * Default code area character assessor.
 */
@ParametersAreNonnullByDefault
public class DefaultCodeAreaCharAssessor implements CodeAreaCharAssessor {

    protected final CodeAreaCharAssessor parentAssessor;

    protected char[] charMapping = null;

    protected long dataSize;
    protected int maxBytesPerChar;
    protected byte[] rowData;
    protected Charset charset;
    private CharsetDecoder decoder;
    private ByteBuffer byteBuffer;
    private CharBuffer charBuffer = null;
    
    // Cache for analyzed row character positions
    private long lastRowDataPosition = -1;
    private char[] rowCharacters = null;
    
    // UTF-8 byte sequence analysis
    private static final int UTF8_CONTINUE_MASK = 0xC0;
    private static final int UTF8_CONTINUE = 0x80;
    
    // Multi-byte encoding detection threshold
    private static final int HIGH_BYTE_THRESHOLD = 0x80;

    public DefaultCodeAreaCharAssessor() {
        parentAssessor = null;
    }

    public DefaultCodeAreaCharAssessor(@Nullable CodeAreaCharAssessor parentAssessor) {
        this.parentAssessor = parentAssessor;
    }

    @Override
    public void startPaint(CodeAreaPaintState codeAreaPainterState) {
        dataSize = codeAreaPainterState.getDataSize();
        Charset painterCharset = codeAreaPainterState.getCharset();
        maxBytesPerChar = codeAreaPainterState.getMaxBytesPerChar();
        if (charBuffer == null) {
            charBuffer = CharBuffer.allocate(8);
        }
        if (charset != painterCharset) {
            charMapping = null;
            decoder = painterCharset.newDecoder();
            decoder.onUnmappableCharacter(CodingErrorAction.REPLACE);
            decoder.onMalformedInput(CodingErrorAction.REPLACE);
            byteBuffer = ByteBuffer.allocate(maxBytesPerChar);
            this.charset = painterCharset;
        }
        // Clear cache on each paint to ensure refreshed display after data modification
        lastRowDataPosition = -1;
        rowData = codeAreaPainterState.getRowData();
    }

    /**
     * Pre-analyze a row of data to determine what character to show at each position
     */
    private void analyzeRow(long rowDataPosition) {
        if (rowCharacters == null || rowCharacters.length < rowData.length) {
            rowCharacters = new char[rowData.length];
        }
        
        // Initialize with spaces
        for (int i = 0; i < rowData.length; i++) {
            rowCharacters[i] = ' ';
        }
        
        String charsetName = charset.name();
        int byteIndex = 0;
        boolean isInDoubleByte = false; // Track if we're in a double-byte character
        
        while (byteIndex < rowData.length && rowDataPosition + byteIndex < dataSize) {
            byte currentByte = rowData[byteIndex];
            int unsignedByte = currentByte & 0xFF;
            
            boolean isStartByte = false;
            
            // UTF-8 handling
            if (charsetName.equals("UTF-8")) {
                if ((unsignedByte & UTF8_CONTINUE_MASK) != UTF8_CONTINUE) {
                    isStartByte = true;
                    isInDoubleByte = false;
                } else {
                    isStartByte = false;
                }
            }
            // UTF-16LE handling - fixed 2 bytes
            else if (charsetName.equals("UTF-16LE")) {
                if ((byteIndex & 1) == 0) {
                    isStartByte = true;
                    isInDoubleByte = false;
                } else {
                    isStartByte = false;
                }
            }
            // UTF-16BE handling - fixed 2 bytes
            else if (charsetName.equals("UTF-16BE")) {
                if ((byteIndex & 1) == 0) {
                    isStartByte = true;
                    isInDoubleByte = false;
                } else {
                    isStartByte = false;
                }
            }
            // GBK/GB2312/GB18030 handling
            else if (charsetName.startsWith("GB") || charsetName.equals("GB18030")) {
                if (isInDoubleByte) {
                    // Previous byte was start byte, this is second byte
                    isStartByte = false;
                    isInDoubleByte = false;
                } else if (unsignedByte >= 0x81 && unsignedByte <= 0xFE) {
                    // GBK first byte range is 0x81-0xFE
                    isStartByte = true;
                    isInDoubleByte = true;
                } else {
                    // ASCII single-byte character
                    isStartByte = true;
                    isInDoubleByte = false;
                }
            }
            // Big5/Shift_JIS/EUC-KR etc. handling
            else if (charsetName.equals("Big5") || charsetName.equals("Shift_JIS") || charsetName.equals("EUC-KR")) {
                if (isInDoubleByte) {
                    isStartByte = false;
                    isInDoubleByte = false;
                } else if (unsignedByte >= 0x80) {
                    isStartByte = true;
                    isInDoubleByte = true;
                } else {
                    isStartByte = true;
                    isInDoubleByte = false;
                }
            }
            // Default handling
            else {
                isStartByte = true;
                isInDoubleByte = false;
            }
            
            if (isStartByte) {
                decoder.reset();
                
                int availableBytes = Math.min(maxBytesPerChar, (int)(dataSize - (rowDataPosition + byteIndex)));
                availableBytes = Math.min(availableBytes, rowData.length - byteIndex);
                
                byteBuffer.clear();
                byteBuffer.put(rowData, byteIndex, availableBytes);
                byteBuffer.rewind();
                charBuffer.clear();
                try {
                    decoder.decode(byteBuffer, charBuffer, true);
                    if (charBuffer.position() > 0) {
                        charBuffer.rewind();
                        rowCharacters[byteIndex] = charBuffer.get();
                    }
                } catch (CoderMalfunctionError | BufferUnderflowException ex) {
                    // ignore
                }
            }
            
            byteIndex++;
        }
        
        lastRowDataPosition = rowDataPosition;
    }

    @Override
    public char getPreviewCharacter(long rowDataPosition, int byteOnRow, int charOnRow, CodeAreaSection section) {
        if (byteOnRow >= rowData.length || rowDataPosition + byteOnRow >= dataSize) {
            return ' ';
        }

        if (maxBytesPerChar > 1) {
            // Cache optimization: only re-analyze when row changes
            if (lastRowDataPosition != rowDataPosition) {
                analyzeRow(rowDataPosition);
            }
            return rowCharacters[byteOnRow];
        } else {
            if (charMapping == null) {
                buildCharMapping();
            }

            return charMapping[rowData[byteOnRow] & 0xFF];
        }
    }

    @Override
    public char getPreviewCursorCharacter(long rowDataPosition, int byteOnRow, int charOnRow, byte[] cursorData, int cursorDataLength, CodeAreaSection section) {
        if (cursorDataLength == 0) {
            return ' ';
        }

        if (maxBytesPerChar > 1) {
            decoder.reset();
            byteBuffer.rewind();
            byteBuffer.put(cursorData, 0, cursorDataLength);
            byteBuffer.rewind();
            charBuffer.clear();
            try {
                decoder.decode(byteBuffer, charBuffer, true);
                if (charBuffer.position() > 0) {
                    charBuffer.rewind();
                    return charBuffer.get();
                }
            } catch (CoderMalfunctionError | BufferUnderflowException ex) {
                // ignore
            }
        } else {
            if (charMapping == null) {
                buildCharMapping();
            }

            return charMapping[cursorData[0] & 0xFF];
        }

        return ' ';
    }

    @Nonnull
    @Override
    public Optional<CodeAreaCharAssessor> getParentCharAssessor() {
        return Optional.ofNullable(parentAssessor);
    }

    /**
     * Precomputes widths for basic ascii characters.
     */
    private void buildCharMapping() {
        charMapping = new char[256];
        ByteBuffer buffer = ByteBuffer.allocate(1);
        for (int i = 0; i < 256; i++) {
            buffer.rewind();
            buffer.put((byte) i);
            decoder.reset();
            buffer.rewind();
            charBuffer.clear();
            try {
                decoder.decode(buffer, charBuffer, true);
                if (charBuffer.position() > 0) {
                    charBuffer.rewind();
                    charMapping[i] = charBuffer.get();
                } else {
                    charMapping[i] = ' ';
                }
            } catch (CoderMalfunctionError | BufferUnderflowException ex) {
                charMapping[i] = ' ';
            }
        }
    }
}
