package com.sirma.itt.emf.help.history;

import java.util.ArrayList;
import java.util.List;

/**
 * Manager of history for visited page.
 * 
 * @author Boyan Tonchev
 */
public class HistoryManagerImpl implements HistoryManagerInterface {

	/**
	 * Store position of help in history store.
	 */
	private int currentPage;

	/**
	 * History of all visited pages during the session.
	 */
	private List<PageUrl> history;

	/**
	 * Initialize history manager.
	 */
	public HistoryManagerImpl() {
		history = new ArrayList<PageUrl>();
		currentPage = 0;
	}

	/**
	 * Initialize history manager.
	 */
	public void init() {
		history = new ArrayList<PageUrl>();
		currentPage = 0;
	}

	@Override
	public PageUrl forward() {
		PageUrl result = null;
		if (history.size() > 0) {
			if (currentPage < history.size() - 1) {
				result = history.get(++currentPage);
			} else {
				result = history.get(currentPage);
			}
		}
		return result;
	}

	@Override
	public PageUrl back() {
		PageUrl result = null;
		if (currentPage > 0) {
			if (history.size() > 0) {
				result = history.get(--currentPage);
			}
		} else {
			result = history.get(currentPage);
		}
		return result;
	}

	@Override
	public void addPageUrl(PageUrl pageUrl) {
		if (history.size() == 0) {
			history.add(pageUrl);
		} else {
			removeLastPages(currentPage);
			history.add(++currentPage, pageUrl);
		}
	}

	/**
	 * Remove all pages from history manager after index <code>from</code>.
	 * 
	 * @param from
	 *            index after which pages have to be removed from history
	 *            manager.
	 */
	private void removeLastPages(int from) {
		// if (history.size() > 1) {
		for (int i = (history.size() - 1); i > from; i--) {
			history.remove(i);
		}
		// }
	}

	@Override
	public String getCurrentPageContentUrl() {
		if (history.size() > 0) {
			return history.get(currentPage).getContentUrl();
		}
		return "";
	}

	@Override
	public String getCurentPageNavigatorState() {
		if (history.size() > 0) {
			return history.get(currentPage).getNavigationState();
		}
		return "";
	}

	@Override
	public Boolean hasMoreBack() {
		
		return currentPage > 0;
	}

	@Override
	public Boolean hasMoreForward() {
		return currentPage < (history.size() - 1);
	}
}
