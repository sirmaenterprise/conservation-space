package com.sirma.itt.pm.search;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.ContextNotActiveException;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.cmf.constants.CommonProperties;
import com.sirma.itt.cmf.search.DmsSearchEngineExtension;
import com.sirma.itt.cmf.services.adapter.CMFSearchAdapterService;
import com.sirma.itt.emf.adapter.DMSException;
import com.sirma.itt.emf.adapter.FileDescriptor;
import com.sirma.itt.emf.configuration.Config;
import com.sirma.itt.emf.configuration.EmfConfigurationProperties;
import com.sirma.itt.emf.exceptions.SearchException;
import com.sirma.itt.emf.plugin.Extension;
import com.sirma.itt.emf.search.Query;
import com.sirma.itt.emf.search.Query.QueryBoost;
import com.sirma.itt.emf.search.model.SearchArguments;
import com.sirma.itt.emf.security.AuthenticationService;
import com.sirma.itt.emf.security.Secure;
import com.sirma.itt.emf.security.context.SecurityContextManager;
import com.sirma.itt.emf.security.model.User;
import com.sirma.itt.emf.time.TimeTracker;
import com.sirma.itt.pm.domain.model.ProjectInstance;
import com.sirma.itt.pm.services.ProjectService;

/**
 * Default project search service implementation.
 * 
 * @author BBonev
 */
@ApplicationScoped
@Extension(target = DmsSearchEngineExtension.TARGET_NAME, order = 30)
public class ProjectDmsSearchEngineExtension implements DmsSearchEngineExtension {

	@Inject
	private ProjectService projectService;

	@Inject
	private CMFSearchAdapterService searchAdapterService;

	@Inject
	@Config(name = EmfConfigurationProperties.SEARCH_RESULT_MAXSIZE)
	private Integer maxResults;

	@Inject
	private Instance<AuthenticationService> authenticationService;

	/** The Constant PROJECT_ASPECTS. */
	private final static HashSet<String> PROJECT_ASPECTS = new HashSet<>(1);

	/** The Constant SUPPORTED_OBJECTS. */
	private static final List<Class<?>> SUPPORTED_OBJECTS = new ArrayList<Class<?>>(
			Arrays.asList(ProjectInstance.class));

	/** The Constant LOGGER. */
	private static final Logger LOGGER = LoggerFactory
			.getLogger(ProjectDmsSearchEngineExtension.class);
	{
		PROJECT_ASPECTS.add("projectInstance");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<Class<?>> getSupportedObjects() {
		return SUPPORTED_OBJECTS;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings("unchecked")
	@Secure
	public <E extends com.sirma.itt.emf.instance.model.Instance, S extends SearchArguments<E>> void search(
			S arguments) {
		SearchArguments<FileDescriptor> searchArguments = new SearchArguments<>();

		Query query;
		if (arguments.getQuery() == null) {
			Map<String, Serializable> map = arguments.getArguments();
			map.put(CommonProperties.KEY_SEARCHED_ASPECT, PROJECT_ASPECTS);
			query = Query.fromMap(map, QueryBoost.INCLUDE_AND);
		} else {
			query = arguments.getQuery();
			// TODO
			// if someone added additional arguments to already builded query or
			// if the event builded query and we need to add the arguments to it
		}
		// add container filtering
		// uncomment to enable site filtering
		/* .and(CaseProperties.CONTAINER, getCurrentContainer()) */
		searchArguments.setQuery(query);
		// searchArguments.setQuery(query);

		searchArguments.setOrdered(arguments.isOrdered());
		searchArguments.setPageSize(arguments.getPageSize());
		searchArguments.setSkipCount(arguments.getSkipCount());
		searchArguments.setSorter(arguments.getSorter());
		searchArguments.setTotalItems(arguments.getTotalItems());
		searchArguments.setMaxSize(maxResults);
		searchArguments.setContext(getCurrentTenant());
		List<E> search = (List<E>) performSearch(searchArguments);

		arguments.setResult(search);
		arguments.setTotalItems(searchArguments.getTotalItems());
	}

	/**
	 * Perform search and maps the {@link FileDescriptor} to specific instances and returns them
	 * as the result.
	 * 
	 * @param <E>
	 *            the element type for descriptor
	 * @param args
	 *            the args to search with
	 * @return the list of found project instances.
	 */
	private <E extends FileDescriptor> List<ProjectInstance> performSearch(
			SearchArguments<E> args) {
		TimeTracker tracker = new TimeTracker();
		tracker.begin();
		boolean debug = LOGGER.isDebugEnabled();
		StringBuilder debugMessage = new StringBuilder();
		try {
			tracker.begin();
			searchAdapterService.search(args, ProjectInstance.class);
			if (debug) {
				debugMessage.append("\nSearch in DMS took ").append(tracker.stopInSeconds())
						.append(" s");
			}
		} catch (DMSException e) {
			LOGGER.error("DMS search failed with " + e.getMessage(), e);
			throw new SearchException(e);
		}
		List<E> list = args.getResult();
		if (list == null) {
			return Collections.emptyList();
		}
		if (debug) {
			debugMessage.append("\nDMS response (").append(list.size()).append(") results");
		}

		List<String> dmsIds = new ArrayList<>(list.size());
		for (E id : list) {
			dmsIds.add(id.getId());
		}
		tracker.begin();
		List<ProjectInstance> result = projectService.load(dmsIds, false);
		if (debug) {
			debugMessage.append("\nPM DB retrieve took ").append(tracker.stopInSeconds())
					.append(" s").append("\nTotal search time for (").append(result.size())
					.append(") results : ").append(tracker.stopInSeconds()).append(" s");
			LOGGER.debug(debugMessage.toString());
		}
		return result;
	}

	/**
	 * Gets the current user.
	 * 
	 * @return the current user or null if authentication service could not retrieve it.
	 */
	private User getCurrentUser() {
		if (authenticationService.isUnsatisfied() || authenticationService.isAmbiguous()) {
			return null;
		}
		try {
			return authenticationService.get().getCurrentUser();
		} catch (ContextNotActiveException e) {
			// logger.debug("No context for fetching current user due to " + e.getMessage());
			return null;
		}
	}

	/**
	 * Gets the current tenant or <code>null</code> if not user is logged in.
	 * 
	 * @return the current tenant
	 */
	private String getCurrentTenant() {
		User currentUser = getCurrentUser();
		if (currentUser == null) {
			// if called from web service where we does not have a context, and user is set using
			// the runAsSystem/Admin we could return that user
			currentUser = SecurityContextManager.getFullAuthentication();
		}
		if (currentUser != null) {
			return currentUser.getTenantId();
		}
		return null;
	}
}
