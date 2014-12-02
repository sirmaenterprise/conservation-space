package com.sirma.itt.cmf.services.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.ContextNotActiveException;
import javax.inject.Inject;

import org.apache.log4j.Logger;

import com.sirma.itt.cmf.beans.model.CaseInstance;
import com.sirma.itt.cmf.beans.model.DocumentInstance;
import com.sirma.itt.cmf.beans.model.SectionInstance;
import com.sirma.itt.cmf.constants.CaseProperties;
import com.sirma.itt.cmf.constants.CommonProperties;
import com.sirma.itt.cmf.constants.DocumentProperties;
import com.sirma.itt.cmf.constants.LinkConstantsCmf;
import com.sirma.itt.cmf.domain.ObjectTypesCmf;
import com.sirma.itt.cmf.search.DmsSearchEngineExtension;
import com.sirma.itt.cmf.search.SearchType;
import com.sirma.itt.cmf.services.CaseService;
import com.sirma.itt.cmf.services.adapter.CMFSearchAdapterService;
import com.sirma.itt.cmf.states.PrimaryStates;
import com.sirma.itt.emf.adapter.DMSException;
import com.sirma.itt.emf.adapter.FileDescriptor;
import com.sirma.itt.emf.configuration.Config;
import com.sirma.itt.emf.configuration.EmfConfigurationProperties;
import com.sirma.itt.emf.db.RelationalDb;
import com.sirma.itt.emf.event.EventService;
import com.sirma.itt.emf.exceptions.SearchException;
import com.sirma.itt.emf.instance.dao.BatchEntityLoader;
import com.sirma.itt.emf.instance.dao.InstanceDao;
import com.sirma.itt.emf.instance.dao.InstanceType;
import com.sirma.itt.emf.instance.dao.ServiceRegister;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.instance.model.InstanceReference;
import com.sirma.itt.emf.instance.model.OwnedModel;
import com.sirma.itt.emf.link.LinkConstants;
import com.sirma.itt.emf.link.LinkReference;
import com.sirma.itt.emf.link.LinkService;
import com.sirma.itt.emf.plugin.Extension;
import com.sirma.itt.emf.search.Query;
import com.sirma.itt.emf.search.Query.QueryBoost;
import com.sirma.itt.emf.search.event.SearchEventBinding;
import com.sirma.itt.emf.search.event.SearchEventObject;
import com.sirma.itt.emf.search.event.SearchEventType;
import com.sirma.itt.emf.search.model.SearchArguments;
import com.sirma.itt.emf.search.model.SearchArgumentsMap;
import com.sirma.itt.emf.security.AuthenticationService;
import com.sirma.itt.emf.security.context.SecurityContextManager;
import com.sirma.itt.emf.security.model.User;
import com.sirma.itt.emf.state.StateService;
import com.sirma.itt.emf.time.TimeTracker;
import com.sirma.itt.emf.util.CollectionUtils;
import com.sirma.itt.emf.util.LinkIterable;

/**
 * Extension for {@link com.sirma.itt.cmf.search.DmsSearchEngine} that handles searching for cases
 * and documents from cases.
 * 
 * @author BBonev
 */
@ApplicationScoped
@Extension(target = DmsSearchEngineExtension.TARGET_NAME, order = 10)
public class CaseDocumentDmsSearchEngineExtension implements DmsSearchEngineExtension {

	/** The Constant CASE_ASPECTS. */
	private final static HashSet<String> CASE_ASPECTS = new HashSet<String>(1);

	/** The Constant DOCUMENT_ASPECTS. */
	private final static HashSet<String> DOCUMENT_ASPECTS = new HashSet<String>(2);

	/** The Constant SUPPORTED_OBJECTS. */
	private static final List<Class<?>> SUPPORTED_OBJECTS = new ArrayList<Class<?>>(Arrays.asList(
			CaseInstance.class, DocumentInstance.class));

	static {
		CASE_ASPECTS.add(CaseProperties.TYPE_CASE_INSTANCE);
		DOCUMENT_ASPECTS.add(DocumentProperties.TYPE_DOCUMENT_ATTACHMENT);
		DOCUMENT_ASPECTS.add(DocumentProperties.TYPE_DOCUMENT_STRUCTURED);
	}

	/** The case instance service. */
	@Inject
	private CaseService caseInstanceService;

	/** The search adapter service. */
	@Inject
	private CMFSearchAdapterService searchAdapterService;

	/** The state service instance. */
	@Inject
	private StateService stateServiceInstance;

	/** The logger. */
	@Inject
	private Logger LOGGER;

	/** The search event. */
	@Inject
	private EventService eventService;

	/** The document instance dao. */
	@Inject
	@InstanceType(type = ObjectTypesCmf.DOCUMENT)
	private InstanceDao<DocumentInstance> documentInstanceDao;

	/** The max results. */
	@Inject
	@Config(name = EmfConfigurationProperties.SEARCH_RESULT_MAXSIZE)
	private Integer maxResults;

	/** The authentication service. */
	@Inject
	private javax.enterprise.inject.Instance<AuthenticationService> authenticationService;

	@Inject
	@RelationalDb
	private LinkService linkService;

	@Inject
	private ServiceRegister serviceRegister;

	/**
	 * Search cases.
	 * 
	 * @param <E>
	 *            the element type
	 * @param arguments
	 *            the arguments
	 * @return the search arguments
	 */
	@SuppressWarnings("unchecked")
	public <E extends CaseInstance> SearchArguments<E> searchCases(SearchArguments<E> arguments) {

		fireSearchEvent(SearchEventType.BEFORE_SEARCH, SearchType.CASE, arguments);

		SearchArguments<FileDescriptor> searchArguments = new SearchArguments<FileDescriptor>();

		Query query;
		if (arguments.getQuery() == null) {
			Map<String, Serializable> map = arguments.getArguments();
			map.put(CommonProperties.KEY_SEARCHED_ASPECT, CASE_ASPECTS);
			query = Query.fromMap(map, QueryBoost.INCLUDE_AND);
			query = query.andNot(CaseProperties.STATUS, getDeletedState());
		} else {
			query = arguments.getQuery();
			// if someone added additional arguments to already builded query or
			// if the event builded query and we need to add the arguments to it
			if (!arguments.getArguments().isEmpty()) {
				Query parameters = Query.fromMap(arguments.getArguments(), QueryBoost.INCLUDE_AND)
						.andNot(CaseProperties.STATUS, getDeletedState());
				query = parameters.and(query);
			}
		}
		// add container filtering
		// uncomment to enable site filtering
		/* .and(CaseProperties.CONTAINER, getCurrentContainer()) */
		searchArguments.setQuery(query);

		searchArguments.setOrdered(arguments.isOrdered());
		searchArguments.setPageSize(arguments.getPageSize());
		searchArguments.setSkipCount(arguments.getSkipCount());
		searchArguments.setSorter(arguments.getSorter());
		searchArguments.setTotalItems(arguments.getTotalItems());
		searchArguments.setContext(getCurrentTenant());
		List<CaseInstance> search = performSearch(searchArguments, SearchType.CASE);

		arguments.setResult((List<E>) search);
		arguments.setTotalItems(searchArguments.getTotalItems());

		fireSearchEvent(SearchEventType.AFTER_SEARCH, SearchType.CASE, arguments);

		return arguments;
	}

	/**
	 * Perform the actual search.
	 * 
	 * @param <E>
	 *            the element type
	 * @param args
	 *            the args
	 * @param searchType
	 *            is the search types
	 * @return the list
	 */
	private <E extends FileDescriptor> List<CaseInstance> performSearch(SearchArguments<E> args,
			String searchType) {
		args.setMaxSize(maxResults);

		TimeTracker tracker = new TimeTracker();
		tracker.begin();
		boolean debug = LOGGER.isDebugEnabled();
		StringBuilder debugMessage = new StringBuilder();
		try {
			tracker.begin();
			searchAdapterService.search(args, getClassForType(searchType));
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
		List<String> dmsIds = new ArrayList<String>(list.size());
		for (E id : list) {
			dmsIds.add(id.getId());
		}
		tracker.begin();
		List<CaseInstance> result = caseInstanceService.load(dmsIds, true);
		Map<InstanceReference, CaseInstance> caseMapping = CollectionUtils
				.createLinkedHashMap(result.size());
		Map<InstanceReference, List<LinkReference>> caseToSectionMapping = CollectionUtils
				.createLinkedHashMap(result.size());
		List<InstanceReference> sectionsToLoad = new LinkedList<>();

		for (CaseInstance caseInstance : result) {
			caseMapping.put(caseInstance.toReference(), caseInstance);
			List<LinkReference> links = linkService.getLinks(caseInstance.toReference(),
					LinkConstantsCmf.CASE_TO_SECTION);
			caseToSectionMapping.put(caseInstance.toReference(), links);
			for (InstanceReference reference : new LinkIterable<>(links)) {
				sectionsToLoad.add(reference);
			}
		}

		Map<InstanceReference, Instance> sectionsMapping = BatchEntityLoader
				.loadAsMapFromReferences(sectionsToLoad, serviceRegister, null);
		for (Entry<InstanceReference, CaseInstance> entry : caseMapping.entrySet()) {
			CaseInstance instance = entry.getValue();
			List<LinkReference> sections = caseToSectionMapping.get(instance.toReference());
			for (InstanceReference reference : new LinkIterable<>(sections)) {
				Instance section = sectionsMapping.get(reference);
				if (section instanceof SectionInstance) {
					instance.getSections().add((SectionInstance) section);
				}
			}
		}
		caseMapping.clear();
		caseToSectionMapping.clear();
		sectionsMapping.clear();

		if (debug) {
			debugMessage.append("\nCMF DB retrieve took ").append(tracker.stopInSeconds())
					.append(" s").append("\nTotal search time for (").append(result.size())
					.append(") results : ").append(tracker.stopInSeconds()).append(" s");
			LOGGER.debug(debugMessage);
		}
		return result;
	}

	/**
	 * Helper method to get the class for type.
	 * 
	 * @param model
	 *            the model to get for
	 * @return the class for type or null
	 */
	private Class<? extends com.sirma.itt.emf.instance.model.Instance> getClassForType(String model) {
		if (SearchType.CASE.equals(model)) {
			return CaseInstance.class;
		} else if (SearchType.DOCUMENT.equals(model)) {
			return DocumentInstance.class;
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.sirma.itt.cmf.services.SearchService#searchCasesAndDocuments(com.sirma.itt.cmf.beans.
	 * SearchArgumentsMap)
	 */
	/**
	 * Search cases and documents.
	 * 
	 * @param <C>
	 *            the generic type
	 * @param <D>
	 *            the generic type
	 * @param arguments
	 *            the arguments
	 * @return the search arguments map
	 */
	@SuppressWarnings("unchecked")
	public <C extends CaseInstance, D extends List<DocumentInstance>> SearchArgumentsMap<C, D> searchCasesAndDocuments(
			SearchArgumentsMap<C, D> arguments) {

		fireSearchEvent(SearchEventType.BEFORE_SEARCH, SearchType.CASE_AND_DOCUMENT, arguments);

		SearchArgumentsMap<FileDescriptor, List<FileDescriptor>> searchArguments = new SearchArgumentsMap<FileDescriptor, List<FileDescriptor>>();
		Query query;
		if (arguments.getQuery() == null) {
			Map<String, Serializable> map = arguments.getArguments();
			map.put(CommonProperties.KEY_SEARCHED_ASPECT, DOCUMENT_ASPECTS);
			query = Query.fromMap(map, QueryBoost.INCLUDE_AND);
			query = query.andNot(CaseProperties.STATUS, getDeletedState());
		} else {
			query = arguments.getQuery();
			// if someone added additional arguments to already builded query or
			// if the event builded query and we need to add the arguments to it
			if (!arguments.getArguments().isEmpty()) {
				query = query.and(Query.fromMap(arguments.getArguments(), QueryBoost.INCLUDE_AND));
				query = query.andNot(CaseProperties.STATUS, getDeletedState());
			}
		}
		// add container filtering
		// uncomment to enable site filtering
		// query = query.and(CaseProperties.CONTAINER, getCurrentContainer());
		searchArguments.setQuery(query);

		searchArguments.setOrdered(arguments.isOrdered());
		searchArguments.setPageSize(arguments.getPageSize());
		searchArguments.setSkipCount(arguments.getSkipCount());
		searchArguments.setSorter(arguments.getSorter());
		searchArguments.setTotalItems(arguments.getTotalItems());
		searchArguments.setContext(getCurrentTenant());
		Map<CaseInstance, List<DocumentInstance>> resultMap = performMapSearch(searchArguments);

		for (Entry<CaseInstance, List<DocumentInstance>> caseInstance : resultMap.entrySet()) {
			resultMap.put(caseInstance.getKey(), caseInstance.getValue());
		}
		arguments.setResultMap((Map<C, D>) resultMap);
		arguments.setTotalItems(searchArguments.getTotalItems());

		fireSearchEvent(SearchEventType.AFTER_SEARCH, SearchType.CASE_AND_DOCUMENT, arguments);

		return arguments;

	}

	/**
	 * Perform the actual search with map results.
	 * 
	 * @param <E>
	 *            the element type
	 * @param <D>
	 *            the value type
	 * @param args
	 *            the args
	 * @return the map with result
	 */
	@SuppressWarnings("unchecked")
	<E extends FileDescriptor, D extends FileDescriptor> Map<CaseInstance, List<DocumentInstance>> performMapSearch(
			SearchArgumentsMap<E, List<D>> args) {
		// long startSearch = System.currentTimeMillis();
		// long start = startSearch;
		// boolean debug = LOGGER.isDebugEnabled();
		// StringBuilder debugMessage = new StringBuilder();
		// try {
		// searchAdapterService.searchCaseAndDocuments(args);
		// searchAdapterService.search(args);
		// if (debug) {
		// debugMessage.append("\nSearch in DMS took ")
		// .append((System.currentTimeMillis() - start) / 1000.0).append(" s");
		// }
		// } catch (DMSException e) {
		// LOGGER.error("DMS search failed with " + e.getMessage());
		// return Collections.emptyMap();
		// }
		// Map<E, List<D>> resultMap = args.getResultMap();
		// if (resultMap == null) {
		// return Collections.emptyMap();
		// }
		// if (debug) {
		// debugMessage.append("\nDMS response (").append(resultMap.size()).append(") results");
		// }
		// Map<CaseInstance, List<DocumentInstance>> result = new
		// HashMap<CaseInstance, List<DocumentInstance>>(
		// resultMap.size());
		// for (E id : resultMap.keySet()) {
		// CaseInstance instance =
		// caseInstanceService.loadCaseInstance(id.getId());
		// if (instance != null) {
		// result.put(instance, new ArrayList<DocumentInstance>());
		// }
		// }
		List<CaseInstance> performSearch = performSearch(args, SearchType.DOCUMENT);
		Map<CaseInstance, List<DocumentInstance>> result = new HashMap<CaseInstance, List<DocumentInstance>>(
				performSearch.size());
		for (CaseInstance caseInstance : performSearch) {
			result.put(caseInstance, Collections.EMPTY_LIST);
		}
		return result;
	}

	/**
	 * Fire search event.
	 * 
	 * @param eventType
	 *            the event type
	 * @param searchType
	 *            the search type
	 * @param arguments
	 *            the arguments
	 */
	protected void fireSearchEvent(SearchEventType eventType, String searchType, Object arguments) {
		TimeTracker tracker = new TimeTracker().begin();
		eventService.fire(new SearchEventObject(arguments), new SearchEventBinding(searchType,
				eventType));

		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("Event handling took: " + tracker.stopInSeconds() + " s on " + eventType
					+ " for " + searchType);
		}
	}

	/**
	 * Search documents.
	 * 
	 * @param <C>
	 *            the generic type
	 * @param <D>
	 *            the generic type
	 * @param arguments
	 *            the arguments
	 * @return the search arguments map
	 */
	@SuppressWarnings("unchecked")
	public <C extends CaseInstance, D extends List<DocumentInstance>> SearchArgumentsMap<C, D> searchDocuments(
			SearchArgumentsMap<C, D> arguments) {
		fireSearchEvent(SearchEventType.BEFORE_SEARCH, SearchType.DOCUMENT, arguments);

		SearchArgumentsMap<FileDescriptor, List<FileDescriptor>> searchArguments = new SearchArgumentsMap<FileDescriptor, List<FileDescriptor>>();
		Query query;
		if (arguments.getQuery() == null) {
			Map<String, Serializable> map = arguments.getArguments();
			map.put(CommonProperties.KEY_SEARCHED_ASPECT, DOCUMENT_ASPECTS);
			query = Query.fromMap(map, QueryBoost.INCLUDE_AND);
		} else {
			query = arguments.getQuery();
			// if someone added additional arguments to already builded query or
			// if the event builded query and we need to add the arguments to it
			if (!arguments.getArguments().isEmpty()) {
				query = query.and(Query.fromMap(arguments.getArguments(), QueryBoost.INCLUDE_AND));
			}
		}
		// add container filtering
		// uncomment to enable site filtering
		// query = query.and(CaseProperties.CONTAINER, getCurrentContainer());
		searchArguments.setQuery(query.andNot(CommonProperties.KEY_SEARCHED_ASPECT,
				DocumentProperties.WORKING_COPY));
		// TODO add container filtering
		// /* .and(CaseProperties.CONTAINER, getCurrentContainer()) */);
		searchArguments.setOrdered(arguments.isOrdered());
		searchArguments.setPageSize(arguments.getPageSize());
		searchArguments.setSkipCount(arguments.getSkipCount());
		searchArguments.setSorter(arguments.getSorter());
		searchArguments.setTotalItems(arguments.getTotalItems());
		searchArguments.setContext(getCurrentTenant());
		Map<CaseInstance, List<DocumentInstance>> resultMap = performDocumentMapSearch(searchArguments);

		arguments.setResultMap((Map<C, D>) resultMap);
		// bbanchev FIXME
		arguments.setTotalItems(searchArguments.getTotalItems());

		fireSearchEvent(SearchEventType.AFTER_SEARCH, SearchType.DOCUMENT, arguments);
		return arguments;
	}

	/**
	 * Perform the actual search.
	 * 
	 * @param <E>
	 *            the element type
	 * @param args
	 *            the args
	 * @return the list
	 */
	private <E extends FileDescriptor> Map<CaseInstance, List<DocumentInstance>> performDocumentMapSearch(
			SearchArguments<E> args) {
		TimeTracker tracker = new TimeTracker();
		tracker.begin();
		boolean debug = LOGGER.isDebugEnabled();
		StringBuilder debugMessage = new StringBuilder();
		try {
			if (debug) {
				tracker.begin();
			}
			searchAdapterService.search(args, DocumentInstance.class);
			if (debug) {
				debugMessage.append("\nDocuments search in DMS took ")
						.append(tracker.stopInSeconds()).append(" s");
			}
		} catch (DMSException e) {
			LOGGER.error("DMS search failed with " + e.getMessage(), e);
			throw new SearchException(e);
		}
		List<E> list = args.getResult();
		if (list == null) {
			return Collections.emptyMap();
		}
		if (debug) {
			debugMessage.append("\nDMS response (").append(list.size()).append(") results");
		}

		List<String> dmsIds = new ArrayList<String>(list.size());
		for (E id : list) {
			dmsIds.add(id.getId());
		}
		if (debug) {
			// start DB tracking
			tracker.begin();
		}
		List<DocumentInstance> documents = documentInstanceDao.loadInstances(dmsIds);

		Map<InstanceReference, List<DocumentInstance>> sectionMapping = CollectionUtils
				.createLinkedHashMap(documents.size());

		for (DocumentInstance documentInstance : documents) {
			InstanceReference sectionRef = documentInstance.getOwningReference();
			if (sectionRef == null) {
				List<LinkReference> links = linkService.getLinks(documentInstance.toReference(),
						LinkConstants.PRIMARY_PARENT);
				if (!links.isEmpty()) {
					LinkReference reference = links.get(0);
					sectionRef = reference.getTo();
				}
			}
			if (sectionRef != null) {
				CollectionUtils.addValueToMap(sectionMapping, sectionRef, documentInstance);
			}

		}

		Map<InstanceReference, Instance> sections = BatchEntityLoader.loadAsMapFromReferences(
				sectionMapping.keySet(), serviceRegister, null);

		Set<InstanceReference> caseReferences = CollectionUtils
				.createLinkedHashSet(sections.size());

		for (Instance instance : sections.values()) {
			if (instance instanceof OwnedModel) {
				InstanceReference reference = ((OwnedModel) instance).getOwningReference();
				if (reference != null) {
					caseReferences.add(reference);
				}
			}
		}

		Map<InstanceReference, Instance> cases = BatchEntityLoader.loadAsMapFromReferences(
				caseReferences, serviceRegister, null);

		Map<CaseInstance, List<DocumentInstance>> result = new LinkedHashMap<CaseInstance, List<DocumentInstance>>(
				(int) (cases.size() * 1.1), 0.95f);

		for (Entry<InstanceReference, List<DocumentInstance>> entry : sectionMapping.entrySet()) {
			Instance instance = sections.get(entry.getKey());
			if (instance instanceof SectionInstance) {
				Instance instance2 = cases.get(((SectionInstance) instance).getOwningReference());
				if (instance2 instanceof CaseInstance) {
					((CaseInstance) instance2).getSections().add((SectionInstance) instance);
					((SectionInstance) instance).getContent().addAll(entry.getValue());
					for (DocumentInstance documentInstance : entry.getValue()) {
						CollectionUtils.addValueToMap(result, (CaseInstance) instance2,
								documentInstance);
					}
					((CaseInstance) instance2).initBidirection();
				}
			}
		}

		if (debug) {
			debugMessage.append("\nCMF DB retrieve took ").append(tracker.stopInSeconds())
					.append(" s").append("\nTotal search time for (").append(documents.size())
					.append(") documents in (").append(result.size()).append(") case/s results : ")
					.append(tracker.stopInSeconds()).append(" s");
			LOGGER.debug(debugMessage);
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
			LOGGER.trace("Context not active returing user from thread local", e);
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

	/**
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings("unchecked")
	public <E extends Instance, S extends SearchArguments<E>> void search(S arguments) {
		if (arguments instanceof SearchArgumentsMap) {
			searchDocuments((SearchArgumentsMap<CaseInstance, List<DocumentInstance>>) arguments);
		} else {
			searchCases((SearchArguments<CaseInstance>) arguments);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<Class<?>> getSupportedObjects() {
		return SUPPORTED_OBJECTS;
	}

	/**
	 * Gets the opened state.
	 * 
	 * @return the opened state
	 */
	private String getDeletedState() {
		return stateServiceInstance.getState(PrimaryStates.DELETED, CaseInstance.class);
	}

}
