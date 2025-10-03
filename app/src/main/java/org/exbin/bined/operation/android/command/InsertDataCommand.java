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
package org.exbin.bined.operation.android.command;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.exbin.auxiliary.binary_data.BinaryData;
import org.exbin.bined.capability.CaretCapable;
import org.exbin.bined.operation.android.InsertDataOperation;
import org.exbin.bined.android.CodeAreaCore;

/**
 * Command for inserting data.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class InsertDataCommand extends OpCodeAreaCommand {

    protected final long position;
    protected final long dataLength;

    public InsertDataCommand(CodeAreaCore codeArea, long position, int codeOffset, BinaryData data) {
        super(codeArea);
        this.position = position;
        dataLength = data.getDataSize();
        super.setOperation(new InsertDataOperation(position, codeOffset, data));
    }

    @Nonnull
    @Override
    public CodeAreaCommandType getType() {
        return CodeAreaCommandType.DATA_INSERTED;
    }

    @Override
    public void performExecute() {
        super.performExecute();
        ((CaretCapable) codeArea).setActiveCaretPosition(position + dataLength);
    }
}
