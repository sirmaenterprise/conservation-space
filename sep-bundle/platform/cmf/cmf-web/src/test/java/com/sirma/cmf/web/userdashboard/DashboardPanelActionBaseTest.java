package com.sirma.cmf.web.userdashboard;

import java.util.ArrayList;
import java.util.List;

import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.sirma.cmf.CMFTest;
import com.sirma.cmf.web.DocumentContext;
import com.sirma.itt.commons.utils.reflection.ReflectionUtils;
import com.sirma.itt.emf.converter.TypeConverter;
import com.sirma.itt.emf.converter.extensions.UriConverterProvider.StringUriProxy;
import com.sirma.itt.emf.domain.Context;
import com.sirma.itt.emf.domain.model.Uri;
import com.sirma.itt.emf.event.EventService;
import com.sirma.itt.emf.search.SearchService;
import com.sirma.itt.emf.search.model.SearchArguments;
import com.sirma.itt.emf.search.model.SearchFilter;
import com.sirma.itt.emf.search.model.SearchFilterConfig;
import com.sirma.itt.emf.search.model.SearchInstance;
import com.sirma.itt.emf.time.TimeTracker;

/**
 * Tests for DashboardPanelActionBase.
 * 
 * @author svelikov
 */
public class DashboardPanelActionBaseTest extends CMFTest {

	private static final String EMF_USERID = "emf:userid";

	private static final String DASHLET_NAME = "dashlet_name";

	/** The action. */
	private final DashboardPanelActionBaseMock action;

	/** The execute search. */
	private boolean executeSearch;

	private SearchService searchService;

	private TypeConverter typeConverter;

	private EventService eventService;

	/**
	 * Instantiates a new dashboard panel action base test.
	 */
	public DashboardPanelActionBaseTest() {
		action = new DashboardPanelActionBaseMock() {

			private DocumentContext docContext = new DocumentContext();

			@Override
			public DocumentContext getDocumentContext() {
				return docContext;
			}

			@Override
			public void setDocumentContext(DocumentContext documentContext) {
				docContext = documentContext;
			}

			@Override
			public void initialize() {
				// User currentLoggedUser = authenticationService.getCurrentUser();
				// userId = currentLoggedUser.getIdentifier();
				userURI = EMF_USERID;
				// Instance currentInstance = getDocumentContext().getCurrentInstance();
				// if (currentInstance != null) {
				// currentInstanceId = currentInstance.getId();
				// }
				// debug = log.isDebugEnabled();
				timeTracker = new TimeTracker();
			}

			@Override
			protected SearchArguments getSearchArguments(SearchFilter filter, Context context) {
				SearchArguments arguments = new SearchArguments();
				return arguments;
			}

			@Override
			public void searchCriteriaChanged() {
				if (executeSearch) {
					super.searchCriteriaChanged();
				}
			}

			@Override
			public void executeDefaultFiltersAsync() {

			}

			@Override
			public void executeDefaultFilter() {

			}

			@Override
			public String targetDashletName() {
				return DASHLET_NAME;
			}
		};

		searchService = Mockito.mock(SearchService.class);
		ReflectionUtils.setField(action, "searchService", searchService);

		typeConverter = Mockito.mock(TypeConverter.class);
		ReflectionUtils.setField(action, "typeConverter", typeConverter);

		eventService = Mockito.mock(EventService.class);
		ReflectionUtils.setField(action, "eventService", eventService);

		ReflectionUtils.setField(action, "log", LOG);
	}

	/**
	 * Inits the action class.
	 */
	@BeforeMethod
	public void initActionClass() {
		action.initialize();
	}

	/**
	 * Test for onOpen method.
	 */
	@Test
	public void onOpenTest() {
		List<SearchFilter> filters = new ArrayList<SearchFilter>();
		SearchFilter filter = new SearchFilter("filter1", "Filter 1", "", null);
		filters.add(filter);
		List<SearchFilter> sorters = new ArrayList<SearchFilter>();
		SearchFilter sorter = new SearchFilter("sorter1", "Sorter 1", "", null);
		sorters.add(sorter);
		SearchFilterConfig searchConfig = new SearchFilterConfig(filters, sorters);
		Mockito.when(searchService.getFilterConfiguration(DASHLET_NAME, SearchInstance.class))
				.thenReturn(searchConfig);
		action.onOpen();

		Assert.assertEquals(action.getFilters(), filters);
		Assert.assertEquals(action.getSorters(), sorters);
		Assert.assertEquals(action.getDefaultFilter(), "filter1");
		Assert.assertEquals(action.getSelectedFilter(), "filter1");
		Assert.assertEquals(action.getDefaultSorter(), "sorter1");
		Assert.assertEquals(action.getSelectedSorter(), "sorter1");
	}

	/**
	 * Search criteria changed test.
	 */
	public void searchCriteriaChangedTest() {
		executeSearch = true;
		Mockito.when(typeConverter.convert(Uri.class, EMF_USERID)).thenReturn(
				new StringUriProxy(EMF_USERID));

		// SearchFilter filter = new SearchFilter("filter1", "Filter 1", "", null);
		// Context<String, Object> context = new Context<String, Object>();
		// SearchArguments arguments = new SearchArguments();
		// Mockito.when(searchService.buildSearchArguments(filter, SearchInstance.class, context))
		// .thenReturn(arguments);
		// TODO: complete
		action.searchCriteriaChanged();
	}

	/**
	 * Change order test.
	 */
	@Test
	public void changeOrderTest() {
		executeSearch = false;
		boolean defaultOrder = action.isOrderAscending();
		action.changeOrder();
		Assert.assertTrue(action.isOrderAscending() != defaultOrder);
	}

	/**
	 * Checks if is default filter test.
	 */
	@Test
	public void isDefaultFilterTest() {
		boolean isDefaultFilter = action.isDefaultFilter(null, null);
		Assert.assertFalse(isDefaultFilter);

		String filter1 = "filter1";
		String filter2 = "filter2";

		isDefaultFilter = action.isDefaultFilter(filter1, null);
		Assert.assertFalse(isDefaultFilter);

		isDefaultFilter = action.isDefaultFilter(null, filter2);
		Assert.assertFalse(isDefaultFilter);

		isDefaultFilter = action.isDefaultFilter(filter1, filter2);
		Assert.assertFalse(isDefaultFilter);

		isDefaultFilter = action.isDefaultFilter(filter1, filter1);
		Assert.assertTrue(isDefaultFilter);
	}
}
