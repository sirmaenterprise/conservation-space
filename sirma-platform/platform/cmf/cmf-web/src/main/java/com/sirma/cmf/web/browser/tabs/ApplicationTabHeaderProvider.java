package com.sirma.cmf.web.browser.tabs;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.PostConstruct;
import javax.faces.bean.ApplicationScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;

import com.sirma.cmf.web.Action;
import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.seip.definition.DictionaryService;
import com.sirma.itt.seip.domain.codelist.CodelistService;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.domain.definition.label.LabelProvider;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.Instance;

/**
 * Class that manage label and icons for browser tab, retrieving based on current context instance. When the context is
 * not initialize, tab data will be generated based on page path.
 *
 * @author cdimitrov
 */
@Named
@ApplicationScoped
public class ApplicationTabHeaderProvider extends Action {

	/** Image prefix for tab icon. */
	private static final String ICON_PREFIX = "images:";

	/** Image suffix for tab icon. */
	private static final String ICON_SUFFIX = "-icon-16.png";

	/** Container that will holds supported browser tabs in the application. */
	private Map<String, BrowserTab> browserTabContainer;

	/** Browser tab object that will holds supported data. */
	private BrowserTab pageBrowserTab;

	/**
	 * The label provider that will use for loading labels based on bundle key.
	 */
	@Inject
	private LabelProvider labelProvider;

	/**
	 * Service that will be used for retrieving property definition.
	 */
	@Inject
	private DictionaryService dictionaryService;

	/**
	 * Service that will be used for retrieving values from specific code-lists.
	 */
	@Inject
	private CodelistService codelistService;

	/**
	 * On bean initialization, will be generated container that will holds all supported browser tabs based on URL. Will
	 * be used when there is no context instance available.
	 */
	@PostConstruct
	public void init() {

		browserTabContainer = new HashMap<String, BrowserTab>();

		browserTabContainer.put(ApplicationTabConstants.CMF_PAGE_USER_DASHBOARD, createBrowserTabObject(
				ApplicationTabConstants.CMF_ICON_USER_DASHBOARD, ApplicationTabConstants.CMF_TAB_USER_DASHBOARD));

		browserTabContainer.put(ApplicationTabConstants.CMF_PAGE_PROJECT, createBrowserTabObject(
				ApplicationTabConstants.CMF_ICON_PROJECT, ApplicationTabConstants.CMF_TAB_CREATE_PROJECT));

		browserTabContainer.put(ApplicationTabConstants.CMF_PAGE_BASIC_SEARCH, createBrowserTabObject(
				ApplicationTabConstants.CMF_ICON_BASIC_SEARCH, ApplicationTabConstants.CMF_TAB_BASIC_SEARCH));

		browserTabContainer.put(ApplicationTabConstants.CMF_PAGE_CASE_LIST, createBrowserTabObject(
				ApplicationTabConstants.CMF_ICON_SEARCH, ApplicationTabConstants.CMF_TAB_CASE_SEARCH));

		browserTabContainer.put(ApplicationTabConstants.CMF_PAGE_SEARCH, createBrowserTabObject(
				ApplicationTabConstants.CMF_ICON_SEARCH, ApplicationTabConstants.CMF_TAB_CASE_SEARCH));

		browserTabContainer.put(ApplicationTabConstants.CMF_PAGE_TASK_LIST, createBrowserTabObject(
				ApplicationTabConstants.CMF_ICON_SEARCH, ApplicationTabConstants.CMF_TAB_TASK_SEARCH));

		browserTabContainer.put(ApplicationTabConstants.CMF_PAGE_DOCUMENT_LIST, createBrowserTabObject(
				ApplicationTabConstants.CMF_ICON_SEARCH, ApplicationTabConstants.CMF_TAB_DOCUMENT_SEARCH));

		browserTabContainer.put(ApplicationTabConstants.CMF_PAGE_BASIC_SEARCH, createBrowserTabObject(
				ApplicationTabConstants.CMF_ICON_PROJECT, ApplicationTabConstants.CMF_TAB_BASIC_SEARCH));

		browserTabContainer.put(ApplicationTabConstants.CMF_PAGE_RESOURCE_ALLOCATION,
				createBrowserTabObject(ApplicationTabConstants.CMF_ICON_RESOURCE_ALLOCATION,
						ApplicationTabConstants.CMF_TAB_RESOURCE_ALLOCATION));

		browserTabContainer.put(ApplicationTabConstants.CMF_PAGE_HELP_REQUEST, createBrowserTabObject(
				ApplicationTabConstants.CMF_ICON_HELP_REQUEST, ApplicationTabConstants.CMF_TAB_HELP_REQUEST));
	}

	/**
	 * This method create new browser tab object based on current page path and label bundle.
	 *
	 * @param pagePath
	 *            page URL
	 * @param labelBundle
	 *            label bundle
	 * @return specific browser tab
	 */
	private BrowserTab createBrowserTabObject(String pagePath, String labelBundle) {
		BrowserTab browserTab = new BrowserTab();
		browserTab.setBrowserTabTitle(labelProvider.getValue(labelBundle));
		browserTab.setBrowserTabIcon(generateIcon(pagePath));
		return browserTab;
	}

	/**
	 * Getter for browser tab based on context and URL path. If the context is available will load the data from the
	 * instance. If the context is not available will retrieve the browser tab data from the container(
	 * <b>browserTabContainer</b>).
	 *
	 * @return current browser tab object
	 */
	public BrowserTab getPageBrowserTab() {
		Instance currentInstance = getDocumentContext().getCurrentInstance();
		if (currentInstance != null) {
			pageBrowserTab = retriveTabDetailsBasedOnContext(currentInstance);
		} else {
			pageBrowserTab = getBrowserTabBasedPagePath();
		}
		return pageBrowserTab;
	}

	/**
	 * Method that generate title and icon based on current context instance.
	 *
	 * @param instance
	 *            current context instance
	 * @return browser tab object
	 */
	private BrowserTab retriveTabDetailsBasedOnContext(Instance instance) {
		BrowserTab currentBrowserTab = null;
		if (instance != null) {
			currentBrowserTab = new BrowserTab();
			String instanceType = instance.getClass().getSimpleName().toLowerCase();
			currentBrowserTab.setBrowserTabIcon(generateIcon(instanceType));
			String instanceTitle = null;
			if (isCodelistLocated(instance)) {
				instanceTitle = getTabTitleFromCodelist(instance);
			} else {
				instanceTitle = (String) instance.getProperties().get(DefaultProperties.TITLE);
				if (!StringUtils.isNotNull(instanceTitle)) {
					instanceTitle = labelProvider.getValue(ApplicationTabConstants.CMF_TAB_NEW_OBJECT)
							+ (String) instance.getProperties().get(DefaultProperties.TYPE);
				}
			}
			currentBrowserTab.setBrowserTabTitle(instanceTitle);
		}
		return currentBrowserTab;
	}

	/**
	 * Retrieve instance type value from code-list.
	 *
	 * @param instance
	 *            current instance
	 * @return instance type value from code-list
	 */
	private String getTabTitleFromCodelist(Instance instance) {
		String tabTitle = null;

		PropertyDefinition typeDefinition = dictionaryService.getProperty(DefaultProperties.TYPE, instance);

		if (typeDefinition != null && typeDefinition.getCodelist() != null) {

			String instanceType = (String) instance.getProperties().get(DefaultProperties.TYPE);
			Integer codelistNumber = typeDefinition.getCodelist();

			tabTitle = codelistService.getDescription(codelistNumber, instanceType);
		}
		return tabTitle;
	}

	/**
	 * Retrieve tab titles from code-list only for {@link WorkflowInstanceContext} and {@link TaskInstance}.
	 *
	 * @param instance
	 *            current instance
	 * @return true, if supported instance
	 */
	protected boolean isCodelistLocated(Instance instance) {
		boolean fromCodelist = false;
		// check instance is available
		if (instance == null) {
			return fromCodelist;
		}
		return fromCodelist;
	}

	/**
	 * Method that construct icon based on specific name. The specific name can be the current object type.
	 *
	 * @param iconName
	 *            icon name
	 * @return full icon name
	 */
	protected String generateIcon(String iconName) {
		StringBuilder iconHolder = new StringBuilder(ICON_PREFIX);
		iconHolder.append(iconName).append(ICON_SUFFIX);
		return iconHolder.toString();
	}

	/**
	 * Check current page match to specific, see {@link ApplicationTabConstants}.
	 *
	 * @param searchedPage
	 *            page that we search
	 * @param currentPage
	 *            current page
	 * @return available status
	 */
	private boolean isCurrentPage(String searchedPage, String currentPage) {
		if (StringUtils.isNotNullOrEmpty(searchedPage) && StringUtils.isNotNullOrEmpty(currentPage)
				&& currentPage.matches(".*\\b" + searchedPage + "\\b.*")) {
			return true;
		}
		return false;
	}

	/**
	 * Search for browser tab object based on current URL page.
	 *
	 * @return supported browser tab object
	 */
	protected BrowserTab getBrowserTabBasedPagePath() {
		String currentPage = FacesContext.getCurrentInstance().getViewRoot().getViewId();
		for (Entry<String, BrowserTab> entry : browserTabContainer.entrySet()) {
			if (isCurrentPage(entry.getKey(), currentPage)) {
				return browserTabContainer.get(entry.getKey());
			}
		}
		return null;
	}

}
