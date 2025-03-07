/*
 * Copyright (C) 2023 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.android.launcher3.uioverrides.flags;

import com.android.launcher3.Utilities;
import com.android.launcher3.config.FeatureFlags.BooleanFlag;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FlagsFactory {

    private static final List<DebugFlag> sDebugFlags = new ArrayList<>();

    /**
     * Creates a new debug flag
     */
    public static BooleanFlag getDebugFlag(
            int bugId, String key, boolean defaultValue, String description) {
        return new BooleanFlag(defaultValue);
    }

    /**
     * Creates a new debug flag
     */
    public static BooleanFlag getReleaseFlag(
            int bugId, String key, boolean defaultValueInCode, String description) {
        return new BooleanFlag(defaultValueInCode);
    }

    public static List<DebugFlag> getDebugFlags() {
        if (!Utilities.IS_DEBUG_DEVICE) {
            return Collections.emptyList();
        }
        synchronized (sDebugFlags) {
            return new ArrayList<>(sDebugFlags);
        }
    }
}
