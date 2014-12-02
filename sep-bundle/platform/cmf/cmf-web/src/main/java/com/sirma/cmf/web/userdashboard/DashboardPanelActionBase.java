package com.sirma.cmf.web.userdashboard;

import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.enterprise.event.Event;
import javax.inject.Inject;

import com.sirma.cmf.web.ActionsPlaceholders;
import com.sirma.cmf.web.EntityAction;
import com.sirma.cmf.web.userdashboard.event.DashletToolbarActionBinding;
import com.sirma.cmf.web.userdashboard.event.DashletToolbarActionEvent;
import com.sirma.cmf.web.util.DashboardPanelLoader;
import com.sirma.itt.emf.concurrent.NonTxAsyncCallableEvent;
import com.sirma.itt.emf.configuration.Config;
import com.sirma.itt.emf.domain.Context;
import com.sirma.itt.emf.domain.model.Uri;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.search.SearchService;
import com.sirma.itt.emf.search.event.AfterSearchQueryBuildEvent;
import com.sirma.itt.emf.search.event.BeforeSearchQueryBuildEvent;
import com.sirma.itt.emf.search.event.SearchFilterEventBinding;
import com.sirma.itt.emf.search.model.SearchArguments;
import com.sirma.itt.emf.search.model.SearchFilter;
import com.sirma.itt.emf.search.model.SearchFilterConfig;
import com.sirma.itt.emf.search.model.SearchInstance;
import com.sirma.itt.emf.search.model.Sorter;
import com.sirma.itt.emf.security.AuthorityService;
import com.sirma.itt.emf.security.Secure;
import com.sirma.itt.emf.security.context.SecurityContextManager;
import com.sirma.itt.emf.security.model.Action;
import com.sirma.itt.emf.security.model.User;
import com.sirma.itt.emf.time.TimeTracker;
import com.sirma.itt.emf.web.config.EmfWebConfigurationProperties;

/**
 * The Class DashboardPanelActionBase.
 * 
 * @author svelikov
 * @param <E>
 *            the element type
 */
public abstract class DashboardPanelActionBase<E extends Instance> extends EntityAction implements
		ActionsPlaceholders {

	/** The Constant DASHLET_WAIT_FOR_DATA_INTERVAL. */
	private static final int DASHLET_WAIT_FOR_DATA_INTERVAL = 5;

	/** The Constant DASHLET_NOT_LOADED_DATA_MSG. */
	private static final String DASHLET_NOT_LOADED_DATA_MSG = "Data was not recieved in on time for ";

	/** The debug. */
	protected boolean debug;

	/** The user id. */
	protected String userId;

	/** The user uri. */
	protected String userURI;

	/** The time tracker. */
	protected TimeTracker timeTracker;

	/** The dashlet actions. */
	private List<Action> dashletActions;

	/** The semaphore. */
	protected Semaphore semaphore = new Semaphore(1);

	/** The loading flag shows if data for dashlets is still loading. */
	protected boolean loading = false;

	/** The search service. */
	@Inject
	protected SearchService searchService;

	/** The authority service. */
	@Inject
	private AuthorityService authorityService;

	/** The dashlet toolbar action event. */
	@Inject
	private Event<DashletToolbarActionEvent> dashletToolbarActionEvent;

	/** Maximum elements to be displayed in user dashlet. */
	@Inject
	@Config(name = EmfWebConfigurationProperties.DASHLET_SEARCH_RESULT_PAGESIZE, defaultValue = "25")
	protected int maxSize = 0;

	private Serializable currentInstanceId;

	private List<E> result;

	private List<SearchFilter> filters;
	private String selectedFilter;
	private String defaultFilter;

	private List<SearchFilter> sorters;
	private String selectedSorter;
	private String defaultSorter;

	private boolean orderAscending = false;

	private int totalCount;

	/** Cached search criteria for given dashlet. */
	private SearchFilterConfig searchCriteria;

	/**
	 * Initialize.
	 */
	@PostConstruct
	public void initialize() {
		User currentLoggedUser = authenticationService.getCurrentUser();
		userId = currentLoggedUser.getIdentifier();
		userURI = (String) currentLoggedUser.getId();
		Instance currentInstance = getDocumentContext().getCurrentInstance();
		if (currentInstance != null) {
			currentInstanceId = currentInstance.getId();
		}
		debug = log.isDebugEnabled();
		timeTracker = new TimeTracker();
	}

	/**
	 * Wait for data to load when asynchronous of data is done.
	 */
	protected void waitForDataToLoad() {
		// if the asynchronous loading is in progress then we need to check if the data is present
		if (loading) {
			try {
				// REVIEW: change to trace level
				if (debug) {
					timeTracker.begin();
					log.debug(this.getClass().getSimpleName() + " checking if data is ready");
				}
				// does not enter again to block indefinitely because we have only one available
				// token and other thread enters here it could be blocked. Also it resets the state
				// for asynchronous loading so that other calls of this method does not block
				loading = false;
				// check if the data is available if so continue
				if (!semaphore.tryAcquire(DASHLET_WAIT_FOR_DATA_INTERVAL, TimeUnit.SECONDS)) {
					String msg = DASHLET_NOT_LOADED_DATA_MSG + " ["
							+ this.getClass().getSimpleName() + "]";
					log.warn(msg);
					// TODO: should check because this doesn't work as expected
					// notificationSupport.addMessage(new NotificationMessage(msg,
					// MessageLevel.WARN));
				}
				if (debug) {
					log.debug(this.getClass().getSimpleName() + " data available in "
							+ timeTracker.stopInSeconds() + " s");
				}
			} catch (InterruptedException e) {
				log.warn("", e);
			}
		}
	}

	/**
	 * Notify for loaded data.
	 */
	protected void notifyForLoadedData() {
		// release a token to notify for data availability
		semaphore.release();
	}

	/**
	 * Called when the dashboard is opened. Load defined filters for given dashlet and executes the
	 * default filter.
	 */
	@Secure
	public void onOpen() {
		// don't load filters and re-apply default and selected filters if already loaded in same
		// view
		if (searchCriteria == null) {
			searchCriteria = searchService.getFilterConfiguration(targetDashletName(),
					SearchInstance.class);
			List<SearchFilter> filters = searchCriteria.getFilters();
			List<SearchFilter> sorters = searchCriteria.getSorterFields();
			setLoadedFilters(filters);
			setLoadedSorters(sorters);
			if ((filters != null) && !filters.isEmpty()) {
				String firstFilter = filters.get(0).getValue();
				setSelectedFilter(firstFilter);
				setDefaultFilter(firstFilter);
			}
			if ((sorters != null) && !sorters.isEmpty()) {
				String firstSorter = sorters.get(0).getValue();
				setSelectedSorter(firstSorter);
				setDefaultSorter(firstSorter);
			}
		}

		// If asynchronous loading is supported then we will call it. If not, we call the filter.
		if (isAsynchronousLoadingSupported()) {
			executeDefaultFiltersAsync();
		} else {
			executeDefaultFilter();
		}
	}

	/**
	 * Execute default filters asynchronously.
	 */
	public void executeDefaultFiltersAsync() {
		// initialize before begin
		initializeForAsynchronousInvocation();

		// set loading start
		semaphore.drainPermits();
		loading = true;

		try {
			// fire non transactional event to execute dashboard loading on a separate thread.
			eventService.fire(new NonTxAsyncCallableEvent(new DashboardPanelLoader(this),
					SecurityContextManager.getCurrentSecurityContext()));
		} catch (RuntimeException e) {
			// if the loading failed for some reason, release a token to unblock any waiting threads
			semaphore.release();
			log.error("Dashlets data loading failed for some reason!", e);
			throw e;
		}
	}

	/**
	 * Loads all allowed actions trough authority service and converts to collection with names.
	 * 
	 * @return the actions for current instance
	 */
	protected Set<String> getActionsForCurrentInstance() {
		timeTracker.begin();
		Set<String> instanceActions = new LinkedHashSet<String>();
		Instance dashletActionsTargetInstance = dashletActionsTarget();
		// refresh instance to ensure we work with last properties version
		instanceService.refresh(dashletActionsTargetInstance);
		Set<Action> allowedActions = authorityService.getAllowedActions(
				dashletActionsTargetInstance, targetDashletName());
		for (Action action : allowedActions) {
			instanceActions.add(action.getActionId());
		}
		if (debug) {
			log.debug("Loading actions for instance type["
					+ dashletActionsTargetInstance.getClass().getSimpleName() + "] with id["
					+ dashletActionsTargetInstance.getId() + "] took "
					+ timeTracker.stopInSeconds() + " s");
		}
		return instanceActions;
	}

	/**
	 * Search criteria changed is called from dashlet search toolbar menus.
	 */
	public void searchCriteriaChanged() {
		timeTracker.begin();
		// - get selected filter
		List<SearchFilter> dashletFilters = getDashletFilters();
		String selectedFilterName = getSelectedFilterName();
		SearchFilter selectedSearchFilter = findSearchFilter(dashletFilters, selectedFilterName);

		// - get selected sorter
		List<SearchFilter> dashletSorters = getDashletSorters();
		String selectedSorterName = getSelectedSorterName();
		SearchFilter selectedSearchSorter = findSearchFilter(dashletSorters, selectedSorterName);

		log.debug("Selected search criteria for [" + this.getClass().getSimpleName()
				+ "] with criteria: filter[" + selectedSearchFilter + "] sorter["
				+ selectedSearchSorter + "] isOrderAscending[" + isOrderAscending() + "]");

		// - create and populate search context
		Context<String, Object> context = getSearchContext();
		updateSearchContext(context);

		// - fire event to get search context updates if any
		BeforeSearchQueryBuildEvent beforeSearchQueryBuildEvent = new BeforeSearchQueryBuildEvent(
				context, selectedFilterName, selectedSearchFilter.getDefinition());
		SearchFilterEventBinding searchFilterEventBinding = new SearchFilterEventBinding(
				selectedFilterName);
		eventService.fire(beforeSearchQueryBuildEvent, searchFilterEventBinding);

		// - get search arguments from search service
		SearchArguments<E> searchArguments = getSearchArguments(selectedSearchFilter, context);

		// - call implementors to update search arguments object if necessary
		updateSearchArguments(searchArguments, selectedSearchFilter);

		if (log.isTraceEnabled()) {
			log.trace("Executing filter: " + searchArguments.getStringQuery());
		}

		// - fire event to allow search arguments to be updated
		AfterSearchQueryBuildEvent afterSearchQueryBuildEvent = new AfterSearchQueryBuildEvent(
				searchArguments, selectedFilterName);
		eventService.fire(afterSearchQueryBuildEvent, searchFilterEventBinding);

		// - execute filter
		searchService.search(Instance.class, searchArguments);
		setResult(searchArguments.getResult());
		setTotalCount(searchArguments.getTotalItems());
		notifyForLoadedData();
		if (debug) {
			log.debug("Executing filter for dashlet[" + this.getClass().getSimpleName() + "] took "
					+ timeTracker.stopInSeconds());
		}
	}

	/**
	 * Change result order.
	 */
	public void changeOrder() {
		boolean orderAscending = isOrderAscending();
		setOrderAscending(!orderAscending);
		searchCriteriaChanged();
	}

	/**
	 * Execute toolbar action.
	 * 
	 * @param dashlet
	 *            the dashlet
	 * @param action
	 *            the action
	 * @return the string
	 */
	public String executeToolbarAction(String dashlet, String action) {
		timeTracker.begin();
		log.debug("Executing dashlet toolbar action[" + action + "] dashlet [" + dashlet + "]");
		DashletToolbarActionBinding binding = new DashletToolbarActionBinding(dashlet, action);
		DashletToolbarActionEvent event = new DashletToolbarActionEvent();
		dashletToolbarActionEvent.select(binding).fire(event);
		log.debug("Dashlet toolbar action took " + timeTracker.stopInSeconds() + " s");
		return event.getNavigation();
	}

	/**
	 * Gets the search arguments.
	 * 
	 * @param filter
	 *            the filter
	 * @param context
	 *            the context
	 * @return the search arguments
	 */
	protected SearchArguments<E> getSearchArguments(SearchFilter filter,
			Context<String, Object> context) {
		SearchArguments<?> arguments = searchService.buildSearchArguments(filter,
				SearchInstance.class, context);
		arguments.setMaxSize(maxSize);
		arguments.setPageSize(maxSize);
		return (SearchArguments<E>) arguments;
	}

	/**
	 * Gets the search context.
	 * 
	 * @return the search context
	 */
	protected Context<String, Object> getSearchContext() {
		// TODO: set some default values
		Context<String, Object> context = new Context<String, Object>();
		context.put("sorter", new Sorter(getSelectedSorterName(), isOrderAscending()));

		Uri userid = typeConverter.convert(Uri.class, userURI);
		String useridString = userid.toString();
		context.put("userid", useridString);
		context.put("assignee", useridString);
		context.put("owner", useridString);
		// TODO probably wont be used
		context.put("userAuthorities", "\"" + useridString + "\"");
		// TODO from config
		context.put("highpriority", "0006-000085");
		// TODO only for cases, projects
		context.put("contexturi", typeConverter.convert(Uri.class, currentInstanceId));

		return context;
	}

	/**
	 * Find search filter by name.
	 * 
	 * @param list
	 *            the list
	 * @param target
	 *            the target
	 * @return the search filter
	 */
	private SearchFilter findSearchFilter(List<SearchFilter> list, String target) {
		for (SearchFilter item : list) {
			if (item.getValue().equals(target)) {
				return item;
			}
		}
		return null;
	}

	/**
	 * Checks if is default filter.
	 * 
	 * @param selected
	 *            the selected
	 * @param defaultFilter
	 *            the default filter
	 * @return true, if is default filter
	 */
	public boolean isDefaultFilter(String selected, String defaultFilter) {
		if ((selected != null) && (defaultFilter != null)) {
			return selected.equals(defaultFilter);
		}
		return false;
	}

	/**
	 * Update search arguments.
	 * 
	 * @param searchArguments
	 *            the search arguments
	 * @param selectedSearchFilter
	 *            the selected search filter
	 */
	public abstract void updateSearchArguments(SearchArguments<E> searchArguments,
			SearchFilter selectedSearchFilter);

	/**
	 * Update search context.
	 * 
	 * @param context
	 *            the context
	 */
	public abstract void updateSearchContext(Context<String, Object> context);

	/**
	 * Dashlet action ids.
	 * 
	 * @return the sets the
	 */
	public abstract Set<String> dashletActionIds();

	/**
	 * Target dashlet name as css class.
	 * 
	 * @return the string.
	 */
	public abstract String targetDashletName();

	/**
	 * Dashlet actions target.
	 * 
	 * @return the instance.
	 */
	public abstract Instance dashletActionsTarget();

	/**
	 * Execute default filter for given panel.
	 */
	public abstract void executeDefaultFilter();

	/**
	 * Getter method for dashletActions.
	 * 
	 * @return the dashletActions.
	 */
	public List<Action> getDashletActions() {
		return dashletActions;
	}

	/**
	 * Setter method for dashletActions.
	 * 
	 * @param dashletActions
	 *            the dashletActions to set.
	 */
	public void setDashletActions(List<Action> dashletActions) {
		this.dashletActions = dashletActions;
	}

	/**
	 * Checks if is asynchronous loading supported. If not the overriding class should return
	 * <code>false</code>
	 * 
	 * @return true, if is asynchronous loading supported
	 */
	protected boolean isAsynchronousLoadingSupported() {
		return true;
	}

	/**
	 * Method called before beginning the asynchronous invocation.
	 */
	protected void initializeForAsynchronousInvocation() {
		// can be overridden in successors
	}

	/**
	 * This method can be overriden when dashlet actions should be filtered one by one as provided
	 * from the dashlet implementation or to be loaded all.
	 * 
	 * @return the filter actions
	 */
	public boolean getFilterActions() {
		return true;
	}

	/**
	 * Sets the loaded filters.
	 * 
	 * @param filters
	 *            the new loaded filters
	 */
	public void setLoadedFilters(List<SearchFilter> filters) {
		this.filters = filters;
	}

	/**
	 * Sets the loaded sorters.
	 * 
	 * @param sorters
	 *            the new loaded sorters
	 */
	public void setLoadedSorters(List<SearchFilter> sorters) {
		this.sorters = sorters;
	}

	/**
	 * Sets the default filter.
	 * 
	 * @param filter
	 *            the new default filter
	 */
	public void setDefaultFilter(String filter) {
		defaultFilter = filter;
	}

	/**
	 * Sets the default sorter.
	 * 
	 * @param sorter
	 *            the new default sorter
	 */
	public void setDefaultSorter(String sorter) {
		defaultSorter = sorter;
	}

	/**
	 * Getter method for defaultFilter.
	 * 
	 * @return the defaultFilter
	 */
	public String getDefaultFilter() {
		return defaultFilter;
	}

	/**
	 * Gets the selected filter name.
	 * 
	 * @return the selected filter name
	 */
	public String getSelectedFilterName() {
		return selectedFilter;
	}

	/**
	 * Gets the dashlet filters.
	 * 
	 * @return the dashlet filters
	 */
	public List<SearchFilter> getDashletFilters() {
		return filters;
	}

	/**
	 * Gets the dashlet sorters.
	 * 
	 * @return the dashlet sorters
	 */
	public List<SearchFilter> getDashletSorters() {
		return sorters;
	}

	/**
	 * Gets the selected sorter name.
	 * 
	 * @return the selected sorter name
	 */
	public String getSelectedSorterName() {
		return selectedSorter;
	}

	/**
	 * Getter method for selectedFilter.
	 * 
	 * @return the selectedFilter
	 */
	public String getSelectedFilter() {
		return selectedFilter;
	}

	/**
	 * Setter method for selectedFilter.
	 * 
	 * @param selectedFilter
	 *            the selectedFilter to set
	 */
	public void setSelectedFilter(String selectedFilter) {
		this.selectedFilter = selectedFilter;
	}

	/**
	 * Getter method for filters.
	 * 
	 * @return the filters
	 */
	public List<SearchFilter> getFilters() {
		return filters;
	}

	/**
	 * Setter method for filters.
	 * 
	 * @param filters
	 *            the filters to set
	 */
	public void setFilters(List<SearchFilter> filters) {
		this.filters = filters;
	}

	/**
	 * Getter method for sorters.
	 * 
	 * @return the sorters
	 */
	public List<SearchFilter> getSorters() {
		return sorters;
	}

	/**
	 * Setter method for sorters.
	 * 
	 * @param sorters
	 *            the sorters to set
	 */
	public void setSorters(List<SearchFilter> sorters) {
		this.sorters = sorters;
	}

	/**
	 * Getter method for selectedSorter.
	 * 
	 * @return the selectedSorter
	 */
	public String getSelectedSorter() {
		return selectedSorter;
	}

	/**
	 * Setter method for selectedSorter.
	 * 
	 * @param selectedSorter
	 *            the selectedSorter to set
	 */
	public void setSelectedSorter(String selectedSorter) {
		this.selectedSorter = selectedSorter;
	}

	/**
	 * Checks if is order ascending.
	 * 
	 * @return true, if is order ascending
	 */
	public boolean isOrderAscending() {
		return orderAscending;
	}

	/**
	 * Sets the order ascending.
	 * 
	 * @param orderAscending
	 *            the new order ascending
	 */
	public void setOrderAscending(boolean orderAscending) {
		this.orderAscending = orderAscending;
	}

	/**
	 * Getter method for defaultSorter.
	 * 
	 * @return the defaultSorter
	 */
	public String getDefaultSorter() {
		return defaultSorter;
	}

	/**
	 * Sets the result.
	 * 
	 * @param result
	 *            the new result
	 */
	public void setResult(List<E> result) {
		this.result = result;
	}

	/**
	 * Getter method for result.
	 * 
	 * @return the result
	 */
	public List<E> getResult() {
		waitForDataToLoad();
		return result;
	}

	/**
	 * Gets the total count.
	 * 
	 * @return the total count
	 */
	public int getTotalCount() {
		return totalCount;
	}

	/**
	 * Sets the total count.
	 * 
	 * @param count
	 *            the new total count
	 */
	public void setTotalCount(int count) {
		this.totalCount = count;
	}
}
