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
package org.exbin.bined.editor.android.search;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Binary search.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class BinarySearch {

    private static final int DEFAULT_DELAY = 500;

    private InvokeSearchThread invokeSearchThread;
    private SearchThread searchThread;

    private SearchOperation currentSearchOperation = SearchOperation.FIND;
    private SearchParameters.SearchDirection currentSearchDirection = SearchParameters.SearchDirection.FORWARD;
    private final SearchParameters currentSearchParameters = new SearchParameters();
    private final ReplaceParameters currentReplaceParameters = new ReplaceParameters();

//    private final List<SearchCondition> searchHistory = new ArrayList<>();
//    private final List<SearchCondition> replaceHistory = new ArrayList<>();

//    private CodeAreaPopupMenuHandler codeAreaPopupMenuHandler;
    private PanelClosingListener panelClosingListener = null;
    private BinarySearchService binarySearchService;
    private BinarySearchService.SearchStatusListener searchStatusListener;

    public BinarySearch() {
    }

    public void setBinarySearchService(BinarySearchService binarySearchService) {
        this.binarySearchService = binarySearchService;
    }

    public void setPanelClosingListener(PanelClosingListener panelClosingListener) {
        this.panelClosingListener = panelClosingListener;
    }

//    public void setCodeAreaPopupMenuHandler(CodeAreaPopupMenuHandler codeAreaPopupMenuHandler) {
//        this.codeAreaPopupMenuHandler = codeAreaPopupMenuHandler;
//        binarySearchPanel.setCodeAreaPopupMenuHandler(codeAreaPopupMenuHandler);
//    }

    @Nonnull
    public BinarySearchService.SearchStatusListener getSearchStatusListener() {
        return searchStatusListener;
    }

    private void invokeSearch(SearchOperation searchOperation) {
        invokeSearch(searchOperation, currentSearchParameters, currentReplaceParameters, 0);
    }

    private void invokeSearch(SearchOperation searchOperation, final int delay) {
        invokeSearch(searchOperation,currentSearchParameters, currentReplaceParameters, delay);
    }

    private void invokeSearch(SearchOperation searchOperation, SearchParameters searchParameters, @Nullable ReplaceParameters replaceParameters) {
        invokeSearch(searchOperation, searchParameters, replaceParameters, 0);
    }

    private void invokeSearch(SearchOperation searchOperation, SearchParameters searchParameters, @Nullable ReplaceParameters replaceParameters, final int delay) {
        if (invokeSearchThread != null) {
            invokeSearchThread.interrupt();
        }
        invokeSearchThread = new InvokeSearchThread();
        invokeSearchThread.delay = delay;
        currentSearchOperation = searchOperation;
        currentSearchParameters.setFromParameters(searchParameters);
        if (replaceParameters != null) {
            currentReplaceParameters.setFromParameters(replaceParameters);
        }
        invokeSearchThread.start();
    }

    // TODO Move to search panel
    public void performFind(SearchParameters searchParameters, BinarySearchService.SearchStatusListener searchStatusListener) {
        this.searchStatusListener = searchStatusListener;
        invokeSearch(SearchOperation.FIND, searchParameters, null, 0);
    }

    // TODO Move to search panel
    public void performFindAgain(BinarySearchService.SearchStatusListener searchStatusListener) {
        this.searchStatusListener = searchStatusListener;
        invokeSearch(SearchOperation.FIND_AGAIN, currentSearchParameters, currentReplaceParameters, 0);
    }

    public void cancelSearch() {
        if (invokeSearchThread != null) {
            invokeSearchThread.interrupt();
        }
        if (searchThread != null) {
            searchThread.interrupt();
        }
    }

    public void clearSearch() {
        SearchCondition condition = currentSearchParameters.getCondition();
        condition.clear();
        binarySearchService.clearMatches();
        if (searchStatusListener != null) {
            searchStatusListener.clearStatus();
        }
    }

    public void dataChanged() {
        binarySearchService.clearMatches();
        invokeSearch(currentSearchOperation, DEFAULT_DELAY);
    }

    private class InvokeSearchThread extends Thread {

        private int delay = DEFAULT_DELAY;

        public InvokeSearchThread() {
            super("InvokeSearchThread");
        }

        @Override
        public void run() {
            try {
                Thread.sleep(delay);
                if (searchThread != null) {
                    searchThread.interrupt();
                }
                searchThread = new SearchThread();
                searchThread.start();
            } catch (InterruptedException ex) {
                // don't search
            }
        }
    }

    private class SearchThread extends Thread {

        public SearchThread() {
            super("SearchThread");
        }

        @Override
        public void run() {
            switch (currentSearchOperation) {
                case FIND:
                    binarySearchService.performFind(currentSearchParameters, searchStatusListener);
                    break;
                case FIND_AGAIN:
                    binarySearchService.performFindAgain(searchStatusListener);
                    break;
                case REPLACE:
                    binarySearchService.performReplace(currentSearchParameters, currentReplaceParameters);
                    break;
                default:
                    throw new UnsupportedOperationException("Not supported yet.");
            }
        }
    }

    public interface PanelClosingListener {

        void closed();
    }

    private enum SearchOperation {
        FIND,
        FIND_AGAIN,
        REPLACE,
        REPLACE_ALL
    }
}
