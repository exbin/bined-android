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
package org.exbin.auxiliary.binary_data.android_jna;

import android.app.ActivityManager;

import com.sun.jna.Memory;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.nio.ByteBuffer;

/**
 * JNA byte buffer allocation.
 */
@NullMarked
public class JnaBuffer {

    private static final long MEMORY_LIMIT = 10_000_000L;
    private static @Nullable ActivityManager activityManager;
    private static ActivityManager.@Nullable MemoryInfo memoryInfo;

    private JnaBuffer() {
        // Utility class, don't create instances
    }

    public static ByteBuffer allocateBufferInt(int capacity) {
        if (activityManager != null) {
            activityManager.getMemoryInfo(memoryInfo);
            if (memoryInfo.availMem < MEMORY_LIMIT || memoryInfo.lowMemory) {
                throw new OutOfMemoryError("Failed to allocate memory");
            }
        }

        try {
            return new Memory(capacity).getByteBuffer(0, capacity);
        } catch (Throwable tw) {
            // Fallback to regular byte buffer
            return ByteBuffer.allocateDirect(capacity);
        }
    }

    public static ByteBuffer allocateBufferInt(byte @Nullable [] data) {
        if (data == null) {
            return JnaBuffer.allocateBufferInt(0);
        } else {
            ByteBuffer buffer = JnaBuffer.allocateBufferInt(data.length);
            buffer.put(data);
            buffer.clear();
            return buffer;
        }
    }

    /**
     * Sets activity manager for memory limit;
     */
    public static void setActivityManager(ActivityManager activityManager) {
        memoryInfo = new ActivityManager.MemoryInfo();
        JnaBuffer.activityManager = activityManager;
    }
}
