/*
 * Copyright (C) 2017 The Android Open Source Project
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

package com.android.launcher3.config;

import static com.android.launcher3.uioverrides.flags.FlagsFactory.getDebugFlag;
import static com.android.launcher3.uioverrides.flags.FlagsFactory.getReleaseFlag;

import android.content.Context;

import androidx.annotation.VisibleForTesting;

import com.android.launcher3.BuildConfig;
import com.android.launcher3.Utilities;
import com.saggitt.omega.preferences.NeoPrefs;

import java.util.function.Predicate;
import java.util.function.ToIntFunction;

/**
 * Defines a set of flags used to control various launcher behaviors.
 *
 * <p>All the flags should be defined here with appropriate default values.
 */
public final class FeatureFlags {

    @VisibleForTesting
    public static Predicate<BooleanFlag> sBooleanReader = f -> f.mCurrentValue;
    @VisibleForTesting
    public static ToIntFunction<IntFlag> sIntReader = f -> f.mCurrentValue;

    public static final String FLAGS_PREF_NAME = "featureFlags";

    private FeatureFlags() {
    }

    public static boolean showFlagTogglerUi(Context context) {
        return Utilities.IS_DEBUG_DEVICE && Utilities.isDevelopersOptionsEnabled(context);
    }

    /**
     * True when the build has come from Android Studio and is being used for local debugging.
     */
    public static final boolean IS_STUDIO_BUILD = BuildConfig.DEBUG;

    /**
     * Enable moving the QSB on the 0th screen of the workspace. This is not a configuration feature
     * and should be modified at a project level.
     */
    //public static final boolean QSB_ON_FIRST_SCREEN = true;
    public static boolean QSbOnFirstScreen(Context context) {
        return NeoPrefs.getInstance(context).getSmartspaceEnable().getValue();
    }

    /**
     * Feature flag to handle define config changes dynamically instead of killing the process.
     * <p>
     * <p>
     * To add a new flag that can be toggled through the flags UI:
     * <p>
     * Declare a new ToggleableFlag below. Give it a unique key (e.g. "QSB_ON_FIRST_SCREEN"),
     * and set a default value for the flag. This will be the default value on Debug builds.
     */

    // When enabled the promise icon is visible in all apps while installation an app.
    public static final BooleanFlag PROMISE_APPS_IN_ALL_APPS = getDebugFlag(270390012,
            "PROMISE_APPS_IN_ALL_APPS", false, "Add promise icon in all-apps");

    public static final BooleanFlag KEYGUARD_ANIMATION = getDebugFlag(270390904,
            "KEYGUARD_ANIMATION", true, "Enable animation for keyguard going away on wallpaper");

    public static final BooleanFlag ENABLE_DEVICE_SEARCH = getReleaseFlag(270390907,
            "ENABLE_DEVICE_SEARCH", true, "Allows on device search in all apps");

    public static final BooleanFlag ENABLE_FLOATING_SEARCH_BAR =
            getDebugFlag(270390286, "ENABLE_FLOATING_SEARCH_BAR", false,
                    "Keep All Apps search bar at the bottom (but above keyboard if open)");

    public static final BooleanFlag ENABLE_EXPANDING_PAUSE_WORK_BUTTON = getReleaseFlag(270390779,
            "ENABLE_EXPANDING_PAUSE_WORK_BUTTON", false,
            "Expand and collapse pause work button while scrolling");

    public static final BooleanFlag COLLECT_SEARCH_HISTORY = getReleaseFlag(270391455,
            "COLLECT_SEARCH_HISTORY", false, "Allow launcher to collect search history for log");

    public static final BooleanFlag ENABLE_TWOLINE_ALLAPPS = getDebugFlag(270390937,
            "ENABLE_TWOLINE_ALLAPPS", false, "Enables two line label inside all apps.");

    public static final BooleanFlag ENABLE_TWOLINE_DEVICESEARCH = getDebugFlag(201388851,
            "ENABLE_TWOLINE_DEVICESEARCH", false,
            "Enable two line label for icons with labels on device search.");

    public static final BooleanFlag ENABLE_DEVICE_SEARCH_PERFORMANCE_LOGGING = getReleaseFlag(
            270391397, "ENABLE_DEVICE_SEARCH_PERFORMANCE_LOGGING", false,
            "Allows on device search in all apps logging");

    public static final BooleanFlag IME_STICKY_SNACKBAR_EDU = getDebugFlag(270391693,
            "IME_STICKY_SNACKBAR_EDU", true, "Show sticky IME edu in AllApps");

    public static final BooleanFlag ENABLE_PEOPLE_TILE_PREVIEW = getDebugFlag(270391653,
            "ENABLE_PEOPLE_TILE_PREVIEW", false,
            "Experimental: Shows conversation shortcuts on home screen as search results");

    public static final BooleanFlag FOLDER_NAME_MAJORITY_RANKING = getDebugFlag(270391638,
            "FOLDER_NAME_MAJORITY_RANKING", true,
            "Suggests folder names based on majority based ranking.");

    public static final BooleanFlag ASSISTANT_GIVES_LAUNCHER_FOCUS = getDebugFlag(270391641,
            "ASSISTANT_GIVES_LAUNCHER_FOCUS", false,
            "Allow Launcher to handle nav bar gestures while Assistant is running over it");

    public static final BooleanFlag ENABLE_BULK_WORKSPACE_ICON_LOADING = getDebugFlag(270392203,
            "ENABLE_BULK_WORKSPACE_ICON_LOADING",
            true,
            "Enable loading workspace icons in bulk.");

    public static final BooleanFlag ENABLE_BULK_ALL_APPS_ICON_LOADING = getDebugFlag(270392465,
            "ENABLE_BULK_ALL_APPS_ICON_LOADING",
            true,
            "Enable loading all apps icons in bulk.");

    public static final BooleanFlag ENABLE_DATABASE_RESTORE = getDebugFlag(270392706,
            "ENABLE_DATABASE_RESTORE", true,
            "Enable database restore when new restore session is created");

    public static final BooleanFlag ENABLE_SMARTSPACE_DISMISS = getDebugFlag(270391664,
            "ENABLE_SMARTSPACE_DISMISS", true,
            "Adds a menu option to dismiss the current Enhanced Smartspace card.");

    public static final BooleanFlag ALWAYS_USE_HARDWARE_OPTIMIZATION_FOR_FOLDER_ANIMATIONS =
            getDebugFlag(270393096,
                    "ALWAYS_USE_HARDWARE_OPTIMIZATION_FOR_FOLDER_ANIMATIONS", false,
                    "Always use hardware optimization for folder animations.");

    public static final BooleanFlag SEPARATE_RECENTS_ACTIVITY = getDebugFlag(270392980,
            "SEPARATE_RECENTS_ACTIVITY", false,
            "Uses a separate recents activity instead of using the integrated recents+Launcher UI");

    public static final BooleanFlag ENABLE_MINIMAL_DEVICE = getDebugFlag(270392984,
            "ENABLE_MINIMAL_DEVICE", false,
            "Allow user to toggle minimal device mode in launcher.");

    public static final BooleanFlag ENABLE_TASKBAR_POPUP_MENU = getDebugFlag(
            270392477, "ENABLE_TASKBAR_POPUP_MENU", true,
            "Enables long pressing taskbar icons to show the popup menu.");

    public static final BooleanFlag ENABLE_TWO_PANEL_HOME = getDebugFlag(270392643,
            "ENABLE_TWO_PANEL_HOME", true,
            "Uses two panel on home screen. Only applicable on large screen devices.");

    public static final BooleanFlag ENABLE_SCRIM_FOR_APP_LAUNCH = getDebugFlag(270393276,
            "ENABLE_SCRIM_FOR_APP_LAUNCH", false,
            "Enables scrim during app launch animation.");

    public static final BooleanFlag ENABLE_ENFORCED_ROUNDED_CORNERS = getReleaseFlag(270393258,
            "ENABLE_ENFORCED_ROUNDED_CORNERS", true, "Enforce rounded corners on all App Widgets");

    public static final BooleanFlag NOTIFY_CRASHES = getDebugFlag(
            270393108, "NOTIFY_CRASHES", true,
            "Sends a notification whenever launcher encounters an uncaught exception.");

    public static final BooleanFlag ENABLE_WALLPAPER_SCRIM = getDebugFlag(270393604,
            "ENABLE_WALLPAPER_SCRIM", false,
            "Enables scrim over wallpaper for text protection.");

    public static final BooleanFlag WIDGETS_IN_LAUNCHER_PREVIEW = getDebugFlag(270393268,
            "WIDGETS_IN_LAUNCHER_PREVIEW", true,
            "Enables widgets in Launcher preview for the Wallpaper app.");

    public static final BooleanFlag QUICK_WALLPAPER_PICKER = getDebugFlag(270393112,
            "QUICK_WALLPAPER_PICKER", true,
            "Shows quick wallpaper picker in long-press menu");

    public static final BooleanFlag ENABLE_BACK_SWIPE_HOME_ANIMATION = getDebugFlag(270393426,
            "ENABLE_BACK_SWIPE_HOME_ANIMATION", true,
            "Enables home animation to icon when user swipes back.");

    public static final BooleanFlag ENABLE_ICON_LABEL_AUTO_SCALING = getDebugFlag(270393294,
            "ENABLE_ICON_LABEL_AUTO_SCALING", true,
            "Enables scaling/spacing for icon labels to make more characters visible");

    public static final BooleanFlag ENABLE_ALL_APPS_BUTTON_IN_HOTSEAT = getDebugFlag(270393897,
            "ENABLE_ALL_APPS_BUTTON_IN_HOTSEAT", true,
            "Enables displaying the all apps button in the hotseat.");

    public static final BooleanFlag ENABLE_ALL_APPS_ONE_SEARCH_IN_TASKBAR = getDebugFlag(270393900,
            "ENABLE_ALL_APPS_ONE_SEARCH_IN_TASKBAR", false,
            "Enables One Search box in Taskbar All Apps.");

    public static final BooleanFlag ENABLE_SPLIT_FROM_WORKSPACE = getDebugFlag(270393906,
            "ENABLE_SPLIT_FROM_WORKSPACE", true,
            "Enable initiating split screen from workspace.");

    public static final BooleanFlag ENABLE_NEW_MIGRATION_LOGIC = getDebugFlag(270393455,
            "ENABLE_NEW_MIGRATION_LOGIC", true,
            "Enable the new grid migration logic, keeping pages when src < dest");

    public static final BooleanFlag ENABLE_ONE_SEARCH_MOTION = getReleaseFlag(270394223,
            "ENABLE_ONE_SEARCH_MOTION", true, "Enables animations in OneSearch.");

    public static final BooleanFlag ENABLE_SEARCH_RESULT_BACKGROUND_DRAWABLES = getReleaseFlag(
            270394041, "ENABLE_SEARCH_RESULT_BACKGROUND_DRAWABLES", true,
            "Enable option to replace decorator-based search result backgrounds with drawables");

    public static final BooleanFlag ENABLE_SHOW_KEYBOARD_OPTION_IN_ALL_APPS = getReleaseFlag(
            270394468, "ENABLE_SHOW_KEYBOARD_OPTION_IN_ALL_APPS", true,
            "Enable option to show keyboard when going to all-apps");

    public static final BooleanFlag USE_LOCAL_ICON_OVERRIDES = getDebugFlag(270394973,
            "USE_LOCAL_ICON_OVERRIDES", true,
            "Use inbuilt monochrome icons if app doesn't provide one");

    public static final BooleanFlag ENABLE_MATERIAL_U_POPUP = getDebugFlag(270395516,
            "ENABLE_MATERIAL_U_POPUP", true, "Switch popup UX to use material U");

    public static final BooleanFlag ENABLE_SEARCH_UNINSTALLED_APPS = getReleaseFlag(270395269,
            "ENABLE_SEARCH_UNINSTALLED_APPS", false, "Search uninstalled app results.");

    public static final BooleanFlag SECONDARY_DRAG_N_DROP_TO_PIN = getDebugFlag(270395140,
            "SECONDARY_DRAG_N_DROP_TO_PIN", false,
            "Enable dragging and dropping to pin apps within secondary display");

    public static final BooleanFlag SHOW_HOME_GARDENING = getDebugFlag(270395183,
            "SHOW_HOME_GARDENING", true,
            "Show the new home gardening mode");

    public static final BooleanFlag ENABLE_DISMISS_PREDICTION_UNDO = getDebugFlag(270394476,
            "ENABLE_DISMISS_PREDICTION_UNDO", false,
            "Show an 'Undo' snackbar when users dismiss a predicted hotseat item");

    public static final BooleanFlag ENABLE_CACHED_WIDGET = getDebugFlag(270395008,
            "ENABLE_CACHED_WIDGET", true,
            "Show previously cached widgets as opposed to deferred widget where available");

    public static final BooleanFlag FOLDABLE_SINGLE_PAGE = getDebugFlag(270395274,
            "FOLDABLE_SINGLE_PAGE", false,
            "Use a single page for the workspace");

    public static final BooleanFlag LARGE_SCREEN_WIDGET_PICKER = getDebugFlag(270395809,
            "LARGE_SCREEN_WIDGET_PICKER", false, "Enable new widget picker that takes "
                    + "advantage of large screen format");

    public static final BooleanFlag ENABLE_PREMIUM_HAPTICS_ALL_APPS = getDebugFlag(270396358,
            "ENABLE_PREMIUM_HAPTICS_ALL_APPS", false,
            "Enables haptics opening/closing All apps");

    public static class BooleanFlag {

        private final boolean mCurrentValue;

        public BooleanFlag(boolean currentValue) {
            mCurrentValue = currentValue;
        }

        public boolean get() {
            return sBooleanReader.test(this);
        }
    }

    /**
     * Class representing an integer flag
     */
    public static class IntFlag {

        private final int mCurrentValue;

        public IntFlag(int currentValue) {
            mCurrentValue = currentValue;
        }

        public int get() {
            return sIntReader.applyAsInt(this);
        }
    }
}
