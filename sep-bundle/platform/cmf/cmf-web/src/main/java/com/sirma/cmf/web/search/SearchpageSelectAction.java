package com.sirma.cmf.web.search;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.myfaces.extensions.cdi.core.api.scope.conversation.ViewAccessScoped;

import com.sirma.cmf.web.Action;
import com.sirma.cmf.web.constants.NavigationActionConstants;
import com.sirma.cmf.web.constants.NavigationConstants;
import com.sirma.cmf.web.menu.NavigationMenu;
import com.sirma.cmf.web.menu.NavigationMenuEvent;
import com.sirma.cmf.web.navigation.history.event.NavigationHistoryEvent;
import com.sirma.cmf.web.navigation.history.event.NavigationHistoryType;
import com.sirma.cmf.web.search.event.SearchTypeMenuInitializedEvent;
import com.sirma.cmf.web.util.LabelConstants;
import com.sirma.itt.emf.label.LabelProvider;

/**
 * The Class SearchpageSelectAction.
 * 
 * @author svelikov
 */
@Named
@ViewAccessScoped
public class SearchpageSelectAction extends Action implements Serializable {

	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = -8120336439747872666L;

	/** The selected search page. */
	private String selectedSearchPage;

	/** The label provider. */
	@Inject
	private LabelProvider labelProvider;

	/** The search page initialize event. */
	@Inject
	private Event<SearchTypeSelectedEvent> searchPageSelectedEvent;

	/** The search page initialize event. */
	@Inject
	private Event<SearchTypeMenuInitializedEvent> searchPageInitializeEvent;

	/**
	 * Inits the search page.
	 * 
	 * @param navigationEvent
	 *            the navigation event
	 */
	public void initSearchPage(
			@Observes @NavigationMenu(NavigationActionConstants.SEARCH) NavigationMenuEvent navigationEvent) {
		log.debug("CMFWeb: Executing Observer SearchpageSelectAction.initSearchPage");
		setSelectedSearchPage(SearchConstants.CASE_SEARCH);
	}

	/**
	 * Navigation history event observer for restoring search page.
	 * 
	 * @param event
	 *            the event
	 */
	public void historyOpenSearchPageObserver(
			@Observes @NavigationHistoryType(NavigationConstants.NAVIGATE_MENU_SEARCH) NavigationHistoryEvent event) {
		log.debug("CMFWeb: Executing observer SearchpageSelectAction.historyOpenSearchPageObserver");
		setSelectedSearchPage(SearchConstants.CASE_SEARCH);
	}

	/**
	 * Gets the search pages. This fires an event with the pages list to allow
	 * it to be extended in third party systems.
	 * 
	 * @return the search pages
	 */
	public List<SearchPage> getSearchPages() {
		List<SearchPage> searchPages = new ArrayList<SearchPage>();
		searchPages.add(new SearchPage(SearchConstants.CASE_SEARCH, labelProvider
				.getValue(SearchConstants.SEARCHPAGE_LABEL_PREF + SearchConstants.CASE_SEARCH)));
		searchPages.add(new SearchPage(SearchConstants.TASK_SEARCH, labelProvider
				.getValue(SearchConstants.SEARCHPAGE_LABEL_PREF + SearchConstants.TASK_SEARCH)));
		searchPages
				.add(new SearchPage(SearchConstants.DOCUMENT_SEARCH, labelProvider
						.getValue(SearchConstants.SEARCHPAGE_LABEL_PREF
								+ SearchConstants.DOCUMENT_SEARCH)));
		// searchPageTypes.add(new
		// SearchPageType(SearchConstants.MESSAGE_SEARCH,labelProvider.getValue(SearchConstants.SEARCHPAGE_LABEL_PREF
		// + SearchConstants.MESSAGE_SEARCH)));

		SearchTypeMenuInitializedEvent event = new SearchTypeMenuInitializedEvent(searchPages);
		searchPageInitializeEvent.fire(event);

		return searchPages;
	}

	/**
	 * Retrieve date format hint label for date-pickers.
	 * 
	 * @param configDatePattern
	 *            date/time format from configuration
	 * @return combine message for date/time format
	 */
	public String getDateFormatHintLabel(String configDatePattern) {
		String label = LabelConstants.DATEPICKER_DATEFORMAT_HINT;
		Date date = new Date();
		String formatedDate = new SimpleDateFormat(configDatePattern).format(date);
		label = labelProvider.getValue(label) + formatedDate;
		return label;
	}

	/**
	 * Setter method for selectedSearchPage. An event is fired when page is
	 * selected from the menu.
	 * 
	 * @param selectedSearchPage
	 *            the selectedSearchPage to set
	 */
	public void setSelectedSearchPage(String selectedSearchPage) {
		this.selectedSearchPage = selectedSearchPage;

		SearchTypeSelectedEvent event = new SearchTypeSelectedEvent(selectedSearchPage);
		SearchPageTypeBinding typeBinding = new SearchPageTypeBinding(selectedSearchPage);
		searchPageSelectedEvent.select(typeBinding).fire(event);
	}

	/**
	 * Getter method for selectedSearchPage.
	 * 
	 * @return the selectedSearchPage
	 */
	public String getSelectedSearchPage() {
		return selectedSearchPage;
	}

}
