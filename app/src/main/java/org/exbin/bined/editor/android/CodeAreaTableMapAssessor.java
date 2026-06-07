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
    
    // 用于缓存已分析的行字符位置
    private long lastRowDataPosition = -1;
    private char[] rowCharacters = null;
    
    // UTF-8 字节序列分析
    private static final int UTF8_CONTINUE_MASK = 0xC0;
    private static final int UTF8_CONTINUE = 0x80;
    
    // 多字节编码检测阈值
    private static final int HIGH_BYTE_THRESHOLD = 0x80;

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
        // 每次绘制都清除缓存，确保数据修改后能刷新显示
        lastRowDataPosition = -1;
        rowData = codeAreaPainterState.getRowData();
    }

    /**
     * 预先分析一行数据，确定每个位置应该显示什么字符
     */
    private void analyzeRow(long rowDataPosition) {
        if (rowCharacters == null || rowCharacters.length < rowData.length) {
            rowCharacters = new char[rowData.length];
        }
        
        // 初始化为空格
        for (int i = 0; i < rowData.length; i++) {
            rowCharacters[i] = ' ';
        }
        
        String charsetName = charset.name();
        int byteIndex = 0;
        boolean isInDoubleByte = false; // 跟踪是否在双字节字符中
        
        while (byteIndex < rowData.length && rowDataPosition + byteIndex < dataSize) {
            byte currentByte = rowData[byteIndex];
            int unsignedByte = currentByte & 0xFF;
            
            boolean isStartByte = false;
            
            // UTF-8 处理
            if (charsetName.equals("UTF-8")) {
                if ((unsignedByte & UTF8_CONTINUE_MASK) != UTF8_CONTINUE) {
                    isStartByte = true;
                    isInDoubleByte = false;
                } else {
                    isStartByte = false;
                }
            }
            // UTF-16LE 处理 - 固定2字节
            else if (charsetName.equals("UTF-16LE")) {
                if ((byteIndex & 1) == 0) {
                    isStartByte = true;
                    isInDoubleByte = false;
                } else {
                    isStartByte = false;
                }
            }
            // UTF-16BE 处理 - 固定2字节
            else if (charsetName.equals("UTF-16BE")) {
                if ((byteIndex & 1) == 0) {
                    isStartByte = true;
                    isInDoubleByte = false;
                } else {
                    isStartByte = false;
                }
            }
            // GBK/GB2312/GB18030 处理
            else if (charsetName.startsWith("GB") || charsetName.equals("GB18030")) {
                if (isInDoubleByte) {
                    // 前一个字节是起始字节，这个是第二个字节
                    isStartByte = false;
                    isInDoubleByte = false;
                } else if (unsignedByte >= 0x81 && unsignedByte <= 0xFE) {
                    // GBK第一字节范围是0x81-0xFE
                    isStartByte = true;
                    isInDoubleByte = true;
                } else {
                    // ASCII单字节字符
                    isStartByte = true;
                    isInDoubleByte = false;
                }
            }
            // Big5/Shift_JIS/EUC-KR等处理
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
            // 默认处理
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
                
                Character character = null;
                if (useTable) {
                    byteBuffer.rewind();
                    int value0 = byteBuffer.get(0) & 0xff;
                    int value1 = availableBytes >= 2 ? ((byteBuffer.get(1) & 0xff) << 8) + value0 : 0;
                    if (availableBytes >= 2) {
                        character = characterTable.get(value1);
                    }
                    if (character == null) {
                        character = characterTable.get(value0);
                    }
                }
                
                if (character != null) {
                    rowCharacters[byteIndex] = character;
                } else {
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
            // 缓存优化：仅当行改变时重新分析
            if (lastRowDataPosition != rowDataPosition) {
                analyzeRow(rowDataPosition);
            }
            return rowCharacters[byteOnRow];
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
        keyPressTable.clear();
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
