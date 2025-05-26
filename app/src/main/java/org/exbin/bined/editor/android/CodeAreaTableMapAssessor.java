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
package org.exbin.bined.editor.android;

import android.content.ContentResolver;
import android.net.Uri;

import org.exbin.bined.CodeAreaSection;
import org.exbin.bined.android.CodeAreaCharAssessor;
import org.exbin.bined.android.CodeAreaPaintState;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CoderMalfunctionError;
import java.nio.charset.CodingErrorAction;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Table map character assessor.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class CodeAreaTableMapAssessor implements CodeAreaCharAssessor {

    protected final CodeAreaCharAssessor parentAssessor;

    protected char[] charMapping = null;

    protected long dataSize;
    protected int maxBytesPerChar;
    protected byte[] rowData;
    protected Charset charset;
    private CharsetDecoder decoder;
    private ByteBuffer byteBuffer;
    private CharBuffer charBuffer = null;

    protected boolean useTable = false;
    protected final Map<Integer, Character> characterTable = new HashMap<>();
    protected final Map<Character, Integer> keyPressTable = new HashMap<>();

    public CodeAreaTableMapAssessor() {
        parentAssessor = null;
    }

    public CodeAreaTableMapAssessor(@Nullable CodeAreaCharAssessor parentAssessor) {
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
        rowData = codeAreaPainterState.getRowData();
    }

    @Override
    public char getPreviewCharacter(long rowDataPosition, int byteOnRow, int charOnRow, CodeAreaSection section) {
        if (byteOnRow > rowData.length - maxBytesPerChar) {
            return ' ';
        }

        if (maxBytesPerChar > 1) {
            decoder.reset();

            if (rowDataPosition + maxBytesPerChar > dataSize) {
                byteBuffer.clear();
                byteBuffer.put(rowData, byteOnRow, (int) (dataSize - rowDataPosition));
            } else {
                byteBuffer.rewind();
                byteBuffer.put(rowData, byteOnRow, maxBytesPerChar);
            }

            if (useTable) {
                byteBuffer.rewind();
                int value0 = byteBuffer.get(0) & 0xff;
                int value1 = ((byteBuffer.get(1) & 0xff) << 8) + value0;
                Character character = characterTable.get(value1);
                if (character != null) {
                    return character;
                }
                character = characterTable.get(value0);
                if (character != null) {
                    return character;
                }
            }

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
            if (useTable) {
                Character character = characterTable.get(rowData[byteOnRow] & 0xff);
                if (character != null) {
                    return character;
                }
            }

            if (charMapping == null) {
                buildCharMapping();
            }

            return charMapping[rowData[byteOnRow] & 0xff];
        }

        return ' ';
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

            if (useTable) {
                byteBuffer.rewind();
                int value0 = byteBuffer.get(0) & 0xff;
                int value1 = ((byteBuffer.get(1) & 0xff) << 8) + value0;
                Character character = characterTable.get(value1);
                if (character != null) {
                    return character;
                }
                character = characterTable.get(value0);
                if (character != null) {
                    return character;
                }
            }

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
            if (useTable) {
                Character character = characterTable.get(cursorData[0] & 0xff);
                if (character != null) {
                    return character;
                }
            }

            if (charMapping == null) {
                buildCharMapping();
            }

            return charMapping[cursorData[0] & 0xff];
        }

        return ' ';
    }

    @Nonnull
    @Override
    public Optional<CodeAreaCharAssessor> getParentCharAssessor() {
        return Optional.ofNullable(parentAssessor);
    }

    public boolean isUseTable() {
        return useTable;
    }

    public void setUseTable(boolean useTable) {
        this.useTable = useTable;
    }

    public void openFile(ContentResolver contentResolver, Uri fileUri) {
        characterTable.clear();
        try {
            try (InputStream inputStream = contentResolver.openInputStream(fileUri); BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"))) {
                String line = reader.readLine();
                while (line != null) {
                    int valuePos = line.indexOf("=");
                    if (valuePos >= 0) {
                        valuePos++;
                    } else {
                        line = reader.readLine();
                        continue;
                    }

                    Integer code = 0;
                    for (int i = 0; i < valuePos - 1; i++) {
                        code = code << 4;
                        char codeChar = line.charAt(i);
                        if (codeChar >= '0' && codeChar <= '9') {
                            code |= (byte) (codeChar - '0');
                        } else if (codeChar >= 'a' && codeChar <= 'f') {
                            code |= (byte) (codeChar + 10 - 'a');
                        } else if (codeChar >= 'A' && codeChar <= 'F') {
                            code |= (byte) (codeChar + 10 - 'A');
                        } else {
                            throw new IllegalArgumentException("Invalid character " + codeChar);
                        }
                    }

                    if (line.length() > valuePos) {
                        characterTable.put(code, line.charAt(valuePos));
                        if (line.length() == valuePos + 1) {
                            keyPressTable.put(line.charAt(valuePos), code);
                        }
                    }
                    line = reader.readLine();
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(CodeAreaTableMapAssessor.class.getName()).log(Level.SEVERE, null, ex);
        }
        useTable = true;
    }

    @Nonnull
    public Map<Integer, Character> getCharacterTable() {
        return characterTable;
    }

    @Nullable
    public byte[] translateKey(char key) {
        Integer code = keyPressTable.get(key);
        if (code == null) {
            return null;
        }

        byte[] result;
        if (code > 256) {
            result = new byte[2];
            result[1] = (byte) ((code >> 8) & 0xff);
        }  else {
            result = new byte[1];
        }
        result[0] = (byte) (code & 0xff);

        return result;
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
