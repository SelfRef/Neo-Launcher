/*
 * Copyright (C) 2015 The Android Open Source Project
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
package com.android.launcher3.allapps;

import android.content.Context;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;

import com.android.launcher3.BaseDraggingActivity;
import com.android.launcher3.LauncherAppState;
import com.android.launcher3.LauncherModel;
import com.android.launcher3.Utilities;
import com.android.launcher3.allapps.BaseAllAppsAdapter.AdapterItem;
import com.android.launcher3.celllayout.CellPosMapper;
import com.android.launcher3.model.ModelWriter;
import com.android.launcher3.model.data.AppInfo;
import com.android.launcher3.model.data.ItemInfo;
import com.android.launcher3.util.ComponentKey;
import com.android.launcher3.util.LabelComparator;
import com.android.launcher3.views.ActivityContext;
import com.saggitt.omega.groups.category.DrawerFolderInfo;
import com.saggitt.omega.preferences.NeoPrefs;
import com.saggitt.omega.util.OmegaUtilsKt;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * The alphabetically sorted list of applications.
 *
 * @param <T> Type of context inflating this view.
 */
public class AlphabeticalAppsList<T extends Context & ActivityContext> implements
        AllAppsStore.OnUpdateListener {
    public static final String TAG = "AlphabeticalAppsList";

    private final WorkProfileManager mWorkProviderManager;

    /**
     * Info about a fast scroller section, depending if sections are merged, the fast scroller
     * sections will not be the same set as the section headers.
     */
    public static class FastScrollSectionInfo {
        // The section name
        public final String sectionName;
        // The item position
        public final int position;

        public FastScrollSectionInfo(String sectionName, int position) {
            this.sectionName = sectionName;
            this.position = position;
        }
    }

    private final T mActivityContext;
    // The set of apps from the system
    private final List<AppInfo> mApps = new ArrayList<>();
    @Nullable
    private final AllAppsStore mAllAppsStore;
    // The number of results in current adapter
    private int mAccessibilityResultsCount = 0;
    // The current set of adapter items
    private final ArrayList<AdapterItem> mAdapterItems = new ArrayList<>();
    // The set of sections that we allow fast-scrolling to (includes non-merged sections)
    private final List<FastScrollSectionInfo> mFastScrollerSections = new ArrayList<>();
    // The of ordered component names as a result of a search query
    private final ArrayList<AdapterItem> mSearchResults = new ArrayList<>();
    private BaseAllAppsAdapter<T> mAdapter;
    private AppInfoComparator mAppNameComparator;
    private final int mNumAppsPerRowAllApps;
    private int mNumAppRowsInAdapter;
    private Predicate<ItemInfo> mItemFilter;

    private final NeoPrefs prefs;
    private final BaseDraggingActivity mLauncher;

    public AlphabeticalAppsList(Context context, @Nullable AllAppsStore appsStore,
                                WorkProfileManager workProfileManager) {
        mAllAppsStore = appsStore;
        mActivityContext = ActivityContext.lookupContext(context);
        mAppNameComparator = new AppInfoComparator(context);
        mWorkProviderManager = workProfileManager;
        mNumAppsPerRowAllApps = mActivityContext.getDeviceProfile().inv.numAllAppsColumns;
        if (mAllAppsStore != null) {
            mAllAppsStore.addUpdateListener(this);
        }

        prefs = Utilities.getOmegaPrefs(context);
        mLauncher = BaseDraggingActivity.fromContext(context);
    }

    public void updateItemFilter(Predicate<ItemInfo> itemFilter) {
        this.mItemFilter = itemFilter;
        onAppsUpdated();
    }

    /**
     * Sets the adapter to notify when this dataset changes.
     */
    public void setAdapter(BaseAllAppsAdapter<T> adapter) {
        mAdapter = adapter;
    }

    /**
     * Returns fast scroller sections of all the current filtered applications.
     */
    public List<FastScrollSectionInfo> getFastScrollerSections() {
        return mFastScrollerSections;
    }

    /**
     * Returns the current filtered list of applications broken down into their sections.
     */
    public List<AdapterItem> getAdapterItems() {
        return mAdapterItems;
    }

    /**
     * Returns the child adapter item with IME launch focus.
     */
    public AdapterItem getFocusedChild() {
        if (mAdapterItems.size() == 0 || getFocusedChildIndex() == -1) {
            return null;
        }
        return mAdapterItems.get(getFocusedChildIndex());
    }

    /**
     * Returns the index of the child with IME launch focus.
     */
    public int getFocusedChildIndex() {
        for (AdapterItem item : mAdapterItems) {
            if (item.isCountedForAccessibility()) {
                return mAdapterItems.indexOf(item);
            }
        }
        return -1;
    }

    /**
     * Returns the number of rows of applications
     */
    public int getNumAppRows() {
        return mNumAppRowsInAdapter;
    }

    /**
     * Returns the number of applications in this list.
     */
    public int getNumFilteredApps() {
        return mAccessibilityResultsCount;
    }

    /**
     * Returns whether there are search results which will hide the A-Z list.
     */
    public boolean hasSearchResults() {
        return !mSearchResults.isEmpty();
    }

    /**
     * Returns whether there are no filtered results.
     */
    public boolean hasNoFilteredResults() {
        return hasSearchResults() && mAccessibilityResultsCount == 0;
    }

    /**
     * Sets results list for search
     */
    public boolean setSearchResults(ArrayList<AdapterItem> results) {
        if (Objects.equals(results, mSearchResults)) {
            return false;
        }
        mSearchResults.clear();
        if (results != null) {
            mSearchResults.addAll(results);
        }
        updateAdapterItems();
        return true;
    }

    /**
     * Updates internals when the set of apps are updated.
     */
    @Override
    public void onAppsUpdated() {
        if (mAllAppsStore == null) {
            return;
        }
        // Sort the list of apps
        mApps.clear();

        // Sort the list of apps
        List<AppInfo> unsortedApps = Arrays.asList(mAllAppsStore.getApps());
        OmegaUtilsKt.sortApps(unsortedApps, mLauncher, prefs.getDrawerSortMode().getValue());
        AppInfo[] sortedApps = unsortedApps.toArray(AppInfo.EMPTY_ARRAY);

        Stream<AppInfo> appSteam = Stream.of(sortedApps);
        if (!hasSearchResults() && mItemFilter != null) {
            appSteam = appSteam.filter(mItemFilter);
        }

        //appSteam = appSteam.sorted(mAppNameComparator);

        // As a special case for some languages (currently only Simplified Chinese), we may need to
        // coalesce sections
        Locale curLocale = mActivityContext.getResources().getConfiguration().locale;
        boolean localeRequiresSectionSorting = curLocale.equals(Locale.SIMPLIFIED_CHINESE);
        if (localeRequiresSectionSorting) {
            // Compute the section headers. We use a TreeMap with the section name comparator to
            // ensure that the sections are ordered when we iterate over it later
            appSteam = appSteam.collect(Collectors.groupingBy(
                            info -> info.sectionName,
                            () -> new TreeMap<>(new LabelComparator()),
                            Collectors.toCollection(ArrayList::new)))
                    .values()
                    .stream()
                    .flatMap(ArrayList::stream);
        }
        appSteam.forEachOrdered(mApps::add);
        // Recompose the set of adapter items from the current set of apps
        if (mSearchResults.isEmpty()) {
            updateAdapterItems();
        }
    }

    /**
     * Updates the set of filtered apps with the current filter. At this point, we expect
     * mCachedSectionNames to have been calculated for the set of all apps in mApps.
     */
    public void updateAdapterItems() {
        List<AdapterItem> oldItems = new ArrayList<>(mAdapterItems);
        // Prepare to update the list of sections, filtered apps, etc.
        mFastScrollerSections.clear();
        mAdapterItems.clear();
        mAccessibilityResultsCount = 0;
        // Recreate the filtered and sectioned apps (for convenience for the grid layout) from the
        // ordered set of sections
        if (hasSearchResults()) {
            mAdapterItems.addAll(mSearchResults);
        } else {
            int position = 0;
            if (mWorkProviderManager != null) {
                position += mWorkProviderManager.addWorkItems(mAdapterItems);
                if (!mWorkProviderManager.shouldShowWorkApps()) {
                    return;
                }
            }
            String lastSectionName = null;

            if (mAllAppsStore != null) {
                for (DrawerFolderInfo info : getFolderInfos()) {
                    // Create an folder item
                    mAdapterItems.add(AdapterItem.asFolder(info));
                    String sectionName = "#";

                    // Create a new section if the section names do not match
                    if (!sectionName.equals(lastSectionName)) {
                        lastSectionName = sectionName;
                        mFastScrollerSections.add(new FastScrollSectionInfo(sectionName, position));
                    }

                    info.setAppsStore(mAllAppsStore);
                    position++;
                }
            }

            for (AppInfo info : mApps) {
                mAdapterItems.add(AdapterItem.asApp(info));
                String sectionName = info.sectionName;
                // Create a new section if the section names do not match
                if (!sectionName.equals(lastSectionName)) {
                    lastSectionName = sectionName;
                    mFastScrollerSections.add(new FastScrollSectionInfo(sectionName, position));
                }
                position++;
            }
        }
        mAccessibilityResultsCount = (int) mAdapterItems.stream()
                .filter(AdapterItem::isCountedForAccessibility).count();
        if (mNumAppsPerRowAllApps != 0) {
            // Update the number of rows in the adapter after we do all the merging (otherwise, we
            // would have to shift the values again)
            int numAppsInSection = 0;
            int numAppsInRow = 0;
            int rowIndex = -1;
            for (AdapterItem item : mAdapterItems) {
                item.rowIndex = 0;
                if (BaseAllAppsAdapter.isDividerViewType(item.viewType)) {
                    numAppsInSection = 0;
                } else if (BaseAllAppsAdapter.isIconViewType(item.viewType)) {
                    if (numAppsInSection % mNumAppsPerRowAllApps == 0) {
                        numAppsInRow = 0;
                        rowIndex++;
                    }
                    item.rowIndex = rowIndex;
                    item.rowAppIndex = numAppsInRow;
                    numAppsInSection++;
                    numAppsInRow++;
                }
            }
            mNumAppRowsInAdapter = rowIndex + 1;
        }
        if (mAdapter != null) {
            DiffUtil.calculateDiff(new MyDiffCallback(oldItems, mAdapterItems), false)
                    .dispatchUpdatesTo(mAdapter);
        }
    }

    /**
     * Returns all the apps.
     */
    public List<AppInfo> getApps() {
        return mApps;
    }

    private List<DrawerFolderInfo> getFolderInfos() {
        LauncherAppState app = LauncherAppState.getInstance(mLauncher);
        LauncherModel model = app.getModel();
        ModelWriter modelWriter = model.getWriter(false, true, CellPosMapper.DEFAULT, null);
        return Utilities.getOmegaPrefs(mLauncher)
                .getDrawerAppGroupsManager()
                .getDrawerFolders()
                .getFolderInfos(this, modelWriter);
    }

    private Set<ComponentKey> getFolderFilteredApps() {

        return Utilities.getOmegaPrefs(mLauncher)
                .getDrawerAppGroupsManager()
                .getDrawerFolders()
                .getHiddenComponents();
    }

    private static class MyDiffCallback extends DiffUtil.Callback {
        private final List<AdapterItem> mOldList;
        private final List<AdapterItem> mNewList;

        MyDiffCallback(List<AdapterItem> oldList, List<AdapterItem> newList) {
            mOldList = oldList;
            mNewList = newList;
        }

        @Override
        public int getOldListSize() {
            return mOldList.size();
        }

        @Override
        public int getNewListSize() {
            return mNewList.size();
        }

        @Override
        public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
            return mOldList.get(oldItemPosition).isSameAs(mNewList.get(newItemPosition));
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            return mOldList.get(oldItemPosition).isContentSame(mNewList.get(newItemPosition));
        }
    }
}