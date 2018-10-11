package com.sirma.itt.seip.annotations;

import static com.sirma.itt.emf.semantic.queries.SPARQLQueryHelper.APPLY_INSTANCES_BY_TYPE_FILTER_FLAG;
import static com.sirma.itt.seip.collections.CollectionUtils.addNonNullValue;
import static com.sirma.itt.seip.collections.CollectionUtils.createHashMap;
import static com.sirma.itt.seip.collections.CollectionUtils.isEmpty;
import static com.sirma.itt.seip.collections.CollectionUtils.isNotEmpty;
import static com.sirma.itt.seip.util.EqualsHelper.getOrDefault;
import static com.sirma.itt.seip.util.EqualsHelper.nullSafeEquals;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.rdf4j.RDF4JException;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.query.Update;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.emf.semantic.exception.SemanticPersistenceException;
import com.sirma.itt.emf.semantic.persistence.SemanticPersistenceHelper;
import com.sirma.itt.emf.semantic.persistence.ValueConverter;
import com.sirma.itt.emf.semantic.queries.SPARQLQueryHelper;
import com.sirma.itt.emf.semantic.search.SemanticQueryVisitor;
import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.Uri;
import com.sirma.itt.seip.adapters.iiif.ImageServerConfigurations;
import com.sirma.itt.seip.annotations.model.Annotation;
import com.sirma.itt.seip.annotations.model.AnnotationProperties;
import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.concurrent.FragmentedWork;
import com.sirma.itt.seip.configuration.Options;
import com.sirma.itt.seip.db.DatabaseIdManager;
import com.sirma.itt.seip.db.DbDao;
import com.sirma.itt.seip.db.SemanticDb;
import com.sirma.itt.seip.domain.definition.label.LabelProvider;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceType;
import com.sirma.itt.seip.domain.search.Query;
import com.sirma.itt.seip.domain.search.Query.QueryBoost;
import com.sirma.itt.seip.domain.search.SearchArguments;
import com.sirma.itt.seip.domain.search.SearchArguments.QueryResultPermissionFilter;
import com.sirma.itt.seip.domain.search.SearchDialects;
import com.sirma.itt.seip.domain.search.Sorter;
import com.sirma.itt.seip.domain.security.Action;
import com.sirma.itt.seip.domain.security.ActionTypeConstants;
import com.sirma.itt.seip.exception.RollbackedRuntimeException;
import com.sirma.itt.seip.instance.InstanceTypes;
import com.sirma.itt.seip.instance.state.StateTransitionManager;
import com.sirma.itt.seip.io.ResourceLoadUtil;
import com.sirma.itt.seip.monitor.Statistics;
import com.sirma.itt.seip.permissions.action.AuthorityService;
import com.sirma.itt.seip.permissions.action.EmfAction;
import com.sirma.itt.seip.resources.ResourceService;
import com.sirma.itt.seip.search.NamedQueries;
import com.sirma.itt.seip.search.SearchService;
import com.sirma.itt.seip.security.context.SecurityContext;
import com.sirma.itt.seip.time.DateRange;
import com.sirma.itt.seip.time.TimeTracker;
import com.sirma.itt.semantic.NamespaceRegistryService;
import com.sirma.itt.semantic.model.vocabulary.EMF;
import com.sirma.itt.semantic.model.vocabulary.OA;
import com.sirma.itt.semantic.queries.QueryBuilder;
import com.sirma.itt.semantic.search.FTSQueryParser;

/**
 * Provides basic logic for persisting and retrieving of annotations from the semantic repository When the annotation
 * JSON needs to be parsed then use:
 *
 * <pre>
 * <code>URL contexts = SemanticAnnotationService.class.getClassLoader().getResource("annotations/contexts.jsonld");
 Model annotationModel = RepositoryCreatorUtils.parseRDFFile(new StringReader(tempData), RDFFormat.JSONLD,
				contexts.toString());</code>
 * </pre>
 *
 * @author kirq4e
 */
@ApplicationScoped
class SemanticAnnotationService implements AnnotationService {

	private static final String STATUS = EMF.STATUS.getLocalName();
	private static final String EMF_STATUS = EMF.PREFIX + ":" + EMF.STATUS.getLocalName();
	private static final String MODIFIED_ON = EMF.MODIFIED_ON.getLocalName();
	private static final String EMF_MODIFIED_ON = EMF.PREFIX + ":" + MODIFIED_ON;
	private static final String MODIFIED_BY = EMF.MODIFIED_BY.getLocalName();
	private static final String CREATED_ON = EMF.CREATED_ON.getLocalName();
	private static final String CREATED_BY = EMF.CREATED_BY.getLocalName();
	private static final String EMF_CREATED_BY = EMF.PREFIX + ":" + CREATED_BY;
	private static final String HAS_BODY = OA.HAS_BODY.getLocalName();
	private static final String HAS_TARGET = OA.HAS_TARGET.getLocalName();
	private static final String OA_HAS_TARGET = OA.PREFIX + ":" + HAS_TARGET;
	private static final String CONTENT = EMF.CONTENT.getLocalName();
	private static final String REPLY_TO = EMF.REPLY_TO.getLocalName();
	private static final String OA_HAS_BODY = OA.PREFIX + ":" + HAS_BODY;

	private static final String COMMENTS_ON = EMF.COMMENTS_ON.getLocalName();
	private static final String REPLY_KEY = EMF.PREFIX + ":" + REPLY_TO;

	private static final String ANNOTATION_PARAM = "annotation";
	private static final String ANNOTATION_GRAPH_PARAM = "annotationsDataGraph";

	/**
	 * Query that fetches all annotations with their replies for instance id
	 * <p>
	 *
	 * <pre>
	 * SELECT ?instance ?instanceType ?hasBody ?content ?createdBy ?createdOn ?modifiedBy ?modifiedOn ?replyTo ?status WHERE {
	 *   ?instance a oa:Annotation ;
	 *       emf:instanceType ?instanceType ;
	 *       emf:modifiedBy ?modifiedBy ;
	 *       emf:modifiedOn ?modifiedOn ;
	 *       emf:createdBy ?createdBy ;
	 *       emf:createdOn ?createdOn ;
	 *       emf:content ?content ;
	 *       oa:hasBody ?hasBody ;
	 *       emf:isDeleted "false"^^xsd:boolean ;
	 *       ems:status ?status.
	 *   ?instance oa:hasTarget ?hasTarget.
	 *	 ?instance emf:commentsOn ?commentsOn.
	 *   optional { ?instance emf:replyTo ?replyTo. }
	 * }
	 * </pre>
	 */
	private static final String SEARCH_INSTANCE_ANNOTATIONS_QUERY = ResourceLoadUtil
			.loadResource(SemanticAnnotationService.class, "SearchInstanceAnnotationQuery.sparql");


	/**
	 * Query that fetches all annotations with their replies for instance id
	 * <p>
	 *
	 * <pre>
	 * SELECT ?instance ?instanceType ?hasBody ?content ?createdBy ?createdOn ?modifiedBy ?modifiedOn ?replyTo ?status WHERE {
	 *   ?instance a oa:Annotation ;
	 *       emf:instanceType ?instanceType ;
	 *       emf:modifiedBy ?modifiedBy ;
	 *       emf:modifiedOn ?modifiedOn ;
	 *       emf:createdBy ?createdBy ;
	 *       emf:createdOn ?createdOn ;
	 *       emf:content ?content ;
	 *       oa:hasBody ?hasBody ;
	 *       emf:isDeleted "false"^^xsd:boolean ;
	 *       ems:status ?status.
	 *   ?instance oa:hasTarget ?hasTarget.
	 *   optional { ?instance emf:replyTo ?replyTo. }
	 * }
	 * </pre>
	 */
	private static final String SEARCH_INSTANCE_ANNOTATIONS_ONLY_BY_TARGET_QUERY = ResourceLoadUtil.
			loadResource(SemanticAnnotationService.class, "SearchInstanceAnnotationOnlyByTargetQuery.sparql");

	/**
	 * Query that fetches all replies for instance id
	 * <p>
	 *
	 * <pre>
	 * SELECT DISTINCT ?instance ?instanceType ?hasBody ?content ?createdBy ?createdOn ?modifiedBy ?modifiedOn ?replyTo ?status WHERE {
	 *    {
	 *   	?instance a oa:Annotation ;
	 *            emf:instanceType ?instanceType ;
	 *            emf:modifiedBy ?modifiedBy ;
	 *            emf:modifiedOn ?modifiedOn ;
	 *            emf:createdBy ?createdBy ;
	 *            emf:createdOn ?createdOn ;
	 *            emf:content ?content ;
	 *            oa:hasBody ?hasBody ;
	 *            emf:isDeleted "false"^^xsd:boolean ;
	 *       	  emf:replyTo ?replyTo;
	 *       	  emf:status ?status.
	 *    } UNION {
	 *   	?replyTo a oa:Annotation ;
	 *            emf:instanceType ?instanceType ;
	 *            emf:modifiedBy ?modifiedBy ;
	 *            emf:modifiedOn ?modifiedOn ;
	 *            emf:createdBy ?createdBy ;
	 *            emf:createdOn ?createdOn ;
	 *            emf:content ?content ;
	 *            oa:hasBody ?hasBody ;
	 *            emf:isDeleted "false"^^xsd:boolean;
	 *       	  emf:status ?status.
	 *  	 BIND(?replyTo as ?instance).
	 *    }
	 * }
	 * </pre>
	 */
	private static final String LOAD_ANNOTATIONS_REPLIES = ResourceLoadUtil
			.loadResource(SemanticAnnotationService.class, "LoadAnnotationReplies.sparql");

	/**
	 * Query that deletes annotations by their target or commons on id
	 *
	 * <pre>
	 * DELETE {
	 *    GRAPH ?annotationsDataGraph {
	 *       ?annotation ?p ?o.
	 *    }
	 * }
	 * WHERE {
	 *    GRAPH ?annotationsDataGraph {
	 *       ?annotation a oa:Annotation.
	 *       ?annotation ?p ?o.
	 *       ?annotation emf:commentsOn ?commentsOn.
	 *       ?annotation oa:hasTarget ?hasTarget.
	 *    }
	 * }
	 * </pre>
	 */
	private static final String DELETE_ANNOTATION =  ResourceLoadUtil
			.loadResource(SemanticAnnotationService.class, "DeleteAnnotation.sparql");

	/**
	 * Query that deletes annotations by their target or commons on id
	 *
	 * <pre>
	 * DELETE {
	 *    GRAPH ?annotationsDataGraph {
	 *      ?instance ?p ?v.
	 *   }
	 * }
	 * WHERE {
	 *   GRAPH ?annotationsDataGraph {
	 *     {
	 *         ?annotation a oa:Annotation.
	 *         ?annotation ?p ?v.
	 *         BIND(?annotation as ?instance).
	 *     } UNION {
	 *         ?reply a oa:Annotation.
	 *         ?reply ?p ?v.
	 *         ?reply emf:replyTo ?annotation.
	 *         BIND(?reply as ?instance).
	 *     }
	 *   }
	 * }
	 * </pre>
	 */
	private static final String DELETE_ANNOTATION_AND_REPLIES_QUERY = "DELETE { GRAPH ?" + ANNOTATION_GRAPH_PARAM
			+ " { ?instance ?p ?v. } " + "} WHERE { GRAPH ?" + ANNOTATION_GRAPH_PARAM + " { { ?" + ANNOTATION_PARAM
			+ " a <" + OA.ANNOTATION + ">. ?" + ANNOTATION_PARAM + " ?p ?v. BIND(?" + ANNOTATION_PARAM
			+ " as ?instance). } UNION { ?reply a <" + OA.ANNOTATION + ">. ?reply ?p ?v. " + " ?reply emf:replyTo ?"
			+ ANNOTATION_PARAM + ". BIND(?reply as ?instance). } } }";

	/**
	 * Query that counts the annotations for the given target
	 *
	 * <pre>
	 * select (count(DISTINCT ?annotation) as ?count) WHERE {
	 *    ?annotation a oa:Annotation.
	 *    {
	 *        ?annotation emf:commentsOn ?hasTarget.
	 *    } UNION {
	 *        ?annotation oa:hasTarget ?hasTarget.
	 *    }
	 *    optional {
	 *        ?annotation emf:replyTo ?reply.
	 *        ?annotation emf:isDeleted ?check.
	 *    }
	 *    FILTER(! BOUND(?check)).
	 * }
	 * </pre>
	 */
	private static final String COUNT_ANNOTATIONS = ResourceLoadUtil
			.loadResource(SemanticAnnotationService.class, "CountAnnotations.sparql");
	/**
	 * Query that counts the replies of all annotations for the given target
	 *
	 * <pre>
	 * select ?instance ("annotation" as ?instanceType) (count(?reply) as ?count) WHERE {
	 *    ?instance a oa:Annotation .
	 *    {
	 *       ?instance emf:commentsOn ?hasTarget.
	 *    } UNION {
	 *       ?instance oa:hasTarget ?hasTarget.
	 *    }
	 *    optional {
	 *       ?instance emf:replyTo ?r.
	 *       ?instance emf:isDeleted ?check.
	 *    }
	 *    FILTER(! BOUND(?check)).
	 *    optional  {
	 *       ?reply emf:replyTo ?instance
	 *    }
	 * } group by ?instance
	 * </pre>
	 */
	private static final String COUNT_ANNOTATION_REPLIES = "select distinct ?instance (\"annotation\" as ?"
			+ EMF.INSTANCE_TYPE.getLocalName() + ") (count(DISTINCT ?reply) as ?count) WHERE { ?instance a <"
			+ OA.ANNOTATION + ">. { ?instance " + EMF.PREFIX + ":" + EMF.COMMENTS_ON.getLocalName() + " ?" + HAS_TARGET
			+ ". } UNION { ?instance " + OA_HAS_TARGET + " ?" + HAS_TARGET + "." + "} OPTIONAL { ?instance "
			+ EMF.PREFIX + ":" + REPLY_TO + " ?r. ?instance " + EMF.PREFIX + ":" + EMF.IS_DELETED.getLocalName()
			+ " ?check. } FILTER(! BOUND(?check)). OPTIONAL { ?reply " + EMF.PREFIX + ":" + REPLY_TO
			+ " ?instance. } } GROUP BY ?instance ";

	private static final String SEARCH_ANNOTATIONS_QUERY_PREFIX = "select distinct ?instance (\"annotation\" as ?instanceType) where { {\n";
	private static final String SEARCH_ANNOTATIONS_COUNT_QUERY_PREFIX = "select (count(distinct ?instance) as ?count) where { {\n";
	private static final String SEARCH_ANNOTATIONS_QUERY_SUFFIX = " }\n" + "    {\n"
			+ "        # found annotation is reply return the topic\n" + "    	?annotation emf:replyTo ?instance.\n"
			+ "    } UNION {\n" + "        # the found annotation is a topic with replies\n"
			+ "        ?reply emf:replyTo ?annotation.\n" + "        BIND(?annotation as ?instance).\n"
			+ "    } UNION {\n" + "        # the found annotation is a topic without replies\n"
			+ "        ?annotation emf:instanceType ?atype .\n" + "        optional {\n"
			+ "            ?annotation emf:replyTo ?a.\n" + "            ?annotation emf:isDeleted ?aCheck.\n"
			+ "        }\n" + "        FILTER(!BOUND(?aCheck)).\n" + "        BIND(?annotation as ?instance).\n"
			+ "    }\n" + "}";

	public static final String ID = "@id";

	private static final Logger LOGGER = LoggerFactory.getLogger(SemanticAnnotationService.class);

	private static final Map<String, String> PROJECTION_MAPPING = new HashMap<>();
	// used in statistics
	private static final String SEMANTIC_SEARCH = "semanticSearch";

	static {
		PROJECTION_MAPPING.put(STATUS, EMF.PREFIX + ":" + STATUS);
		PROJECTION_MAPPING.put(CONTENT, EMF.PREFIX + ":" + CONTENT);
		PROJECTION_MAPPING.put(HAS_BODY, OA.PREFIX + ":" + HAS_BODY);
		PROJECTION_MAPPING.put(HAS_TARGET, OA_HAS_TARGET);
		PROJECTION_MAPPING.put(CREATED_BY, EMF_CREATED_BY);
		PROJECTION_MAPPING.put(CREATED_ON, EMF.PREFIX + ":" + CREATED_ON);
		PROJECTION_MAPPING.put(MODIFIED_BY, EMF.PREFIX + ":" + MODIFIED_BY);
		PROJECTION_MAPPING.put(MODIFIED_ON, EMF_MODIFIED_ON);
		PROJECTION_MAPPING.put(REPLY_TO, REPLY_KEY);
	}

	@Inject
	private RepositoryConnection repositoryConnection;

	@Inject
	private NamespaceRegistryService namespaceRegistryService;

	@Inject
	private SecurityContext securityContext;

	@Inject
	@SemanticDb
	private DbDao dbDao;

	@Inject
	private DatabaseIdManager idManager;

	@Inject
	private Statistics statistics;
	@Inject
	private SearchService searchService;
	@Inject
	private ResourceService resourceService;
	@Inject
	private AuthorityService authorityService;
	@Inject
	private QueryBuilder queryBuilder;
	@Inject
	private StateTransitionManager stateTransitionManager;
	@Inject
	private LabelProvider labelProvider;

	@Inject
	private FTSQueryParser parser;

	@Inject
	private ImageServerConfigurations imageServerConfig;

	@Inject
	private InstanceTypes instanceTypes;

	@Override
	public Annotation saveAnnotation(Annotation annotation) {
		if (annotation == null) {
			return null;
		}
		List<Annotation> annotations = Collections.singletonList(annotation);
		saveInternal(annotations);
		loadUserData(annotations);
		return annotation;
	}

	@Override
	public Collection<Annotation> saveAnnotation(Collection<Annotation> annotations) {
		if (isEmpty(annotations)) {
			return Collections.emptyList();
		}
		saveInternal(annotations);
		loadUserData(annotations);
		return annotations;
	}

	private void saveInternal(Collection<Annotation> annotations) {
		List<Annotation> modified = annotations
				.stream()
				.flatMap(Annotation::stream)
				.filter(a -> a.isNew() || a.isForEdit() || a.isHasAction())
				.peek(this::fillDefaultProperties)
				.collect(Collectors.toList());

		Options.USE_CUSTOM_GRAPH.set(EMF.ANNOTATIONS_CONTEXT);
		try {
			if (isNotEmpty(modified)) {
				persistAnnotation(modified);
			}
		} finally {
			Options.USE_CUSTOM_GRAPH.clear();
		}
	}

	private void fillDefaultProperties(Annotation annotation) {
		if (annotation.isNew() || annotation.isHasAction()) {
			setNewStatus(annotation);
		}
		idManager.generateStringId(annotation, false);

		// link replies to the parent annotation
		IRI uri = namespaceRegistryService.buildUri(annotation.getId().toString());
		annotation.getReplies().forEach(reply -> reply.addIfNotPresent(EMF.REPLY_TO.toString(), uri));

		Serializable currentTime = new Date();
		annotation.add(EMF.MODIFIED_ON.toString(), currentTime);
		// if present then the value is from the converters so it's still Value
		annotation.getProperties().computeIfPresent(EMF.CREATED_ON.toString(),
				(key, date) -> ValueConverter.convertValue((Value) date));
		annotation.addIfNotPresent(EMF.CREATED_ON.toString(), currentTime);

		Serializable userid = securityContext.getAuthenticated().getSystemId();
		IRI userUri = namespaceRegistryService.buildUri(userid.toString());
		annotation.add(EMF.MODIFIED_BY.toString(), userUri);
		annotation.addIfNotPresent(EMF.CREATED_BY.toString(), userUri);
		annotation.addIfNotPresent(EMF.IS_DELETED.toString(), Boolean.FALSE);

	}

	/**
	 * Annotation new status setter.
	 *
	 * @param annotation
	 *            annotation
	 */
	private void setNewStatus(Annotation annotation) {
		annotation.getProperties().computeIfPresent(EMF.STATUS.toString(),
				(key, status) -> ValueConverter.convertValue((Value) status));

		annotation.getProperties().computeIfPresent(AnnotationProperties.ACTION.toString(),
				(key, action) -> ValueConverter.convertValue((Value) action));

		String nextState = stateTransitionManager.getNextState(annotation, annotation.getCurrentStatus(),
				annotation.getTransition());

		// sets the next annotation state in the correct format, depending on if the annotation is new or returned from
		// UI
		if (annotation.isNew()) {
			annotation.addIfNotNullOrEmpty(EMF_STATUS, nextState);
		} else {
			annotation.addIfNotNullOrEmpty(EMF.STATUS.toString(), nextState);
		}
	}

	@SuppressWarnings("resource")
	private void persistAnnotation(Collection<Annotation> annotations) {

		try {
			for (Annotation annotation : annotations) {
				IRI annotationId = namespaceRegistryService.buildUri(annotation.getId().toString());
				for (String key : annotation.getProperties().keySet()) {
					// remove previous annotation data without deleting information about replies
					repositoryConnection.remove(annotationId, namespaceRegistryService.buildUri(key), null,
							EMF.ANNOTATIONS_CONTEXT);
				}

				dbDao.saveOrUpdate(annotation);
			}
		} catch (RepositoryException e) {
			throw new RollbackedRuntimeException(e);
		}
	}

	private Collection<Annotation> loadUserData(Collection<Annotation> annotations) {
		// the method collects all users from all annotations
		// loads the resources for all unique one
		// builds a mapping for them by user system id
		// replaces the user values with the loaded users

		Set<String> users = annotations
				.stream()
				.flatMap(Annotation::stream)
				.flatMap(Annotation::getUsers)
				.map(this::convertUserId)
				.filter(Objects::nonNull)
				.collect(Collectors.toSet());

		List<String> usersList = new ArrayList<>(users);

		Map<Serializable, Instance> userMapping = resourceService
				.loadByDbId(usersList)
				.stream()
				.collect(CollectionUtils.toIdentityMap(Instance::getId));

		Function<Serializable, Instance> resourceProvider = id -> userMapping.get(convertUserId(id));

		annotations
		.stream()
		.flatMap(Annotation::stream)
		.forEach(annotation -> annotation.expandUsers(resourceProvider));
		return annotations;
	}

	private String convertUserId(Serializable id) {
		if (id instanceof String) {
			return namespaceRegistryService.getShortUri((String) id);
		} else if (id instanceof IRI) {
			return namespaceRegistryService.getShortUri((IRI) id);
		} else if (id instanceof Uri) {
			return namespaceRegistryService.getShortUri((Uri) id);
		}
		return null;
	}

	@Override
	public void deleteAnnotation(String annotationId) {
		if (StringUtils.isBlank(annotationId)) {
			return;
		}
		LOGGER.debug("Deleting annotation with id: {}", annotationId);
		Value targetIdValue = SemanticPersistenceHelper.createValue(annotationId, !annotationId.contains(":"),
																	namespaceRegistryService);

		Map<String, Serializable> bindings = CollectionUtils.createHashMap(2);
		bindings.put(ANNOTATION_PARAM, targetIdValue);
		bindings.put(ANNOTATION_GRAPH_PARAM, EMF.ANNOTATIONS_CONTEXT);
		deleteAllAnnotationsInternal(bindings, DELETE_ANNOTATION_AND_REPLIES_QUERY);
	}

	@Override
	public void deleteAllAnnotations(String targetId, String tabId) {
		if (StringUtils.isBlank(targetId) || StringUtils.isBlank(tabId)) {
			return;
		}
		LOGGER.debug("Deleting all annotations for instance: {} and tab: {}", targetId, tabId);
		Value targetIdValue = SemanticPersistenceHelper.createValue(targetId, !targetId.contains(":"),
																	namespaceRegistryService);
		Value tabIdValue = SemanticPersistenceHelper.createValue(tabId, !tabId.contains(":"),
																	namespaceRegistryService);

		Map<String, Serializable> bindings = CollectionUtils.createHashMap(2);
		bindings.put(HAS_TARGET, targetIdValue);
		bindings.put(COMMENTS_ON, tabIdValue);
		bindings.put(ANNOTATION_GRAPH_PARAM, EMF.ANNOTATIONS_CONTEXT);
		deleteAllAnnotationsInternal(bindings, DELETE_ANNOTATION);
	}

	@Override
	public void deleteAllAnnotations(String targetId) {
		if (StringUtils.isBlank(targetId)) {
			return;
		}
		LOGGER.debug("Deleting all annotations for instance: {}", targetId);
		Value targetIdValue = SemanticPersistenceHelper.createValue(targetId, !targetId.contains(":"),
																	namespaceRegistryService);

		Map<String, Serializable> bindings = CollectionUtils.createHashMap(2);
		bindings.put(HAS_TARGET, targetIdValue);
		bindings.put(ANNOTATION_GRAPH_PARAM, EMF.ANNOTATIONS_CONTEXT);
		deleteAllAnnotationsInternal(bindings, DELETE_ANNOTATION);
	}

	private void deleteAllAnnotationsInternal(Map<String, Serializable> bindings, String query) {
		try {
			Update update = SPARQLQueryHelper.prepareUpdateQuery(repositoryConnection, query, bindings, false);
			update.execute();
		} catch (RDF4JException e) {
			LOGGER.error("Error occurred when deleting the annotation with params: {} : {}", bindings, e.getMessage());
			throw new SemanticPersistenceException(e);
		}
	}

	@Override
	public Optional<Annotation> loadAnnotation(String annotationId) {
		if (StringUtils.isBlank(annotationId)) {
			return Optional.empty();
		}
		Serializable id = idManager.getValidId(annotationId);
		Collection<Annotation> annotations = searchAnnotations(id.toString(), REPLY_TO, LOAD_ANNOTATIONS_REPLIES, null);
		if (annotations.isEmpty()) {
			return Optional.empty();
		}
		if (annotations.size() > 1) {
			LOGGER.warn("Found more than one annotation for the given id: {}", annotationId);
		}
		setAllowedActions(annotations);
		return Optional.of(annotations.iterator().next());
	}

	@Override
	public Collection<Annotation> loadAnnotations(Collection<? extends Serializable> annotationIds) {
		if (isEmpty(annotationIds)) {
			return Collections.emptyList();
		}
		return FragmentedWork.doWorkWithResult(annotationIds, 256, this::loadAnnotationsInternal);
	}

	private Collection<Annotation> loadAnnotationsInternal(Collection<? extends Serializable> annotationIds) {
		SearchArguments<Annotation> arguments = new SearchArguments<>();
		arguments.setPermissionsType(QueryResultPermissionFilter.NONE);
		arguments.setDialect(SearchDialects.SPARQL);
		String query = queryBuilder.buildQueryByName(NamedQueries.LOAD_ANNOTATIONS, Collections.singletonList(
				new Pair<>(NamedQueries.Params.URIS, annotationIds)));
		arguments.setStringQuery(query);
		arguments.setFaceted(false);
		// Setting the max size here, because there is a problem with the memory repo in the tests and sometimes
		// returns numerous copies of the same instance, even if only 1 ID is provided
		arguments.setPageSize(annotationIds.size());
		arguments.setMaxSize(annotationIds.size());
		arguments.getQueryConfigurations().put(APPLY_INSTANCES_BY_TYPE_FILTER_FLAG, Boolean.FALSE);

		searchService.search(Annotation.class, arguments);

		return loadUserData(remapProperties(arguments.getResult()));
	}

	@Override
	public Collection<Annotation> loadAnnotations(String targetId, Integer limit) {
		if (StringUtils.isBlank(targetId)) {
			return Collections.emptyList();
		}
		Serializable id = idManager.getValidId(targetId);
		return searchAnnotations(id.toString(), HAS_TARGET, SEARCH_INSTANCE_ANNOTATIONS_ONLY_BY_TARGET_QUERY, limit);
	}

	@Override
	public Collection<Annotation> searchAnnotation(String targetId, String tabId, Integer limit) {
		if (StringUtils.isBlank(targetId) || imageServerConfig.getNoContentImageName().get().equals(targetId)) {
			return Collections.emptyList();
		}
		LOGGER.debug("Executing search for annotations for object id: {} and tab id: {}", targetId, tabId);
		TimeTracker tracker = statistics.createTimeStatistics(getClass(), SEMANTIC_SEARCH).begin();

		SearchArguments<Annotation> arguments = new SearchArguments<>();
		arguments.setPermissionsType(QueryResultPermissionFilter.NONE);
		arguments.setDialect(SearchDialects.SPARQL);
		arguments.setFaceted(false);
		arguments.setMaxSize(limit == null ? -1 : limit);
		arguments.setPageSize(0);

		Map<String, Serializable> bindings = new HashMap<>();
		bindings.put(HAS_TARGET, namespaceRegistryService.buildUri(targetId));

		// CMF-23619
		// The tab id parameter is optional, and requires the use of different queries.
		if (StringUtils.isNotBlank(tabId)) {
			arguments.setStringQuery(SEARCH_INSTANCE_ANNOTATIONS_QUERY);
			Serializable id = idManager.getValidId(tabId);
			bindings.put(COMMENTS_ON, namespaceRegistryService.buildUri(String.valueOf(id)));
		} else {
			arguments.setStringQuery(SEARCH_INSTANCE_ANNOTATIONS_ONLY_BY_TARGET_QUERY);
		}
		arguments.setArguments(bindings);
		try {
			searchService.search(Annotation.class, arguments);
			Collection<Annotation> annotations = remapProperties(arguments.getResult());
			loadUserData(annotations);
			return annotations;
		} catch (Exception e) {
			LOGGER.error("Error executing search for annotations for object id and tab id: {}", targetId, tabId, e.getMessage(), e);
			return Collections.emptyList();
		} finally {
			LOGGER.debug("Annotation search took {} s", tracker.stopInSeconds());
		}
	}

	@Override
	public Collection<Annotation> searchAnnotations(AnnotationSearchRequest searchRequest) {

		SearchArguments<Annotation> arguments = new SearchArguments<>();
		String filterQuery = buildSearchFilterQuery(searchRequest, arguments, true);

		String query = SEARCH_ANNOTATIONS_QUERY_PREFIX + filterQuery + SEARCH_ANNOTATIONS_QUERY_SUFFIX;
		arguments.setStringQuery(query);

		searchService.search(Annotation.class, arguments);

		List<Serializable> foundAnnotationIds = arguments
				.getResult()
				.stream()
				.map(Annotation::getId)
				.collect(Collectors.toList());

		return loadAnnotations(foundAnnotationIds);
	}

	@SuppressWarnings("boxing")
	private String buildSearchFilterQuery(AnnotationSearchRequest searchRequest, SearchArguments<Annotation> arguments,
			boolean addPaginationInfo) {
		arguments.setPermissionsType(QueryResultPermissionFilter.NONE);
		arguments.setDialect(SearchDialects.SPARQL);
		arguments.setFaceted(false);
		if (addPaginationInfo) {
			arguments.setPageSize(getOrDefault(searchRequest.getLimit(), -1));
			arguments.setSkipCount(getOrDefault(searchRequest.getOffset(), 0));
		} else {
			arguments.setSkipCount(0);
			arguments.setPageSize(-1);
			arguments.setMaxSize(-1);
		}
		arguments.getQueryConfigurations().put(APPLY_INSTANCES_BY_TYPE_FILTER_FLAG, Boolean.FALSE);
		if (arguments.isOrdered()) {
			arguments.addSorter(Sorter.descendingSorter(EMF_MODIFIED_ON));
		} else {
			arguments.getSorters().clear();
		}

		Map<String, Serializable> properties = CollectionUtils.createHashMap(4);
		fillSearchProperties(searchRequest, properties);
		arguments.setArguments(properties);

		Map<String, Serializable> bindings = new HashMap<>();
		String filterQuery = prepareQuery(arguments, bindings);

		arguments.getArguments().clear();
		arguments.getArguments().putAll(bindings);

		// This is temporary solution until we migrate to solr
		// we first generate sub query based on the search criteria then we return only topics for found replies
		// so this generated query will be included in an external query as sub query
		return filterQuery.replaceAll("\\?instance", "?annotation").replaceAll("\\?instanceType", "?annotationType");
	}

	@Override
	public int searchAnnotationsCountOnly(AnnotationSearchRequest searchRequest) {
		SearchArguments<Annotation> arguments = new SearchArguments<>();
		arguments.setOrdered(false);
		String filterQuery = buildSearchFilterQuery(searchRequest, arguments, false);

		String query = SEARCH_ANNOTATIONS_COUNT_QUERY_PREFIX + filterQuery + SEARCH_ANNOTATIONS_QUERY_SUFFIX;
		arguments.setStringQuery(query);
		arguments.setCountOnly(true);

		searchService.search(Annotation.class, arguments);
		return arguments.getTotalItems();
	}

	private void fillSearchProperties(AnnotationSearchRequest searchRequest, Map<String, Serializable> properties) {
		String annotationUri = namespaceRegistryService.getShortUri(OA.ANNOTATION.stringValue());
		properties.put(DefaultProperties.SEMANTIC_TYPE, annotationUri);

		if (isNotEmpty(searchRequest.getInstanceIds())) {
			properties.put(OA_HAS_TARGET, (Serializable) searchRequest.getInstanceIds());
		}

		if (isNotEmpty(searchRequest.getUserIds())) {
			properties.put(EMF_CREATED_BY, (Serializable) searchRequest.getUserIds());
		}
		DateRange dateRange = searchRequest.getDateRange();
		if (dateRange != null && (dateRange.getFirst() != null || dateRange.getSecond() != null)) {
			properties.put(EMF_MODIFIED_ON, dateRange);
		}
		if (searchRequest.getStatus() != null) {
			properties.put(EMF_STATUS, searchRequest.getStatus());
		}
		if (StringUtils.isNotBlank(searchRequest.getText())) {
			properties.put(OA_HAS_BODY, searchRequest.getText());
		}
	}

	private <E extends Instance, S extends SearchArguments<E>> String prepareQuery(S arguments,
			Map<String, Serializable> bindings) {

		Map<String, Serializable> argumentsMap = arguments.getArguments();
		Map<String, Serializable> queryArguments = CollectionUtils.createHashMap(argumentsMap.size());

		queryArguments.putAll(argumentsMap);
		Query searchQuery = Query.getEmpty();
		searchQuery.and(Query.fromMap(queryArguments, QueryBoost.INCLUDE_AND));

		SemanticQueryVisitor visitor = prepareQueryVisitor(arguments);
		try {
			searchQuery.visit(visitor);
		} catch (Exception e) {
			LOGGER.error("Error parsing the search query", e);
		}

		String query = visitor.getQuery().toString();
		bindings.putAll(visitor.getBindings());

		query = SPARQLQueryHelper.appendOrderByToQuery(query, arguments.getSorters(), true);

		return query;
	}

	/**
	 * Builds SemanticQueryVisitor and initializes it
	 *
	 * @param arguments
	 *            Search arguments
	 * @return Initialized SemanticQueryVisitor
	 */
	private <S extends SearchArguments<?>> SemanticQueryVisitor prepareQueryVisitor(S arguments) {
		SemanticQueryVisitor visitor = new SemanticQueryVisitor();
		visitor.setMaxResultLimit(arguments.getMaxSize());

		Serializable applyTypeFilter = arguments.getQueryConfigurations().get(APPLY_INSTANCES_BY_TYPE_FILTER_FLAG);
		boolean applyTypeFilterFlag = true;
		if (applyTypeFilter != null) {
			applyTypeFilterFlag = (boolean) applyTypeFilter;
		}
		visitor.setApplyFilterForType(applyTypeFilterFlag);
		visitor.setIgnoreInstancesForType("");
		visitor.setFTSParser(parser);
		visitor.setProjection(arguments.getProjection());
		return visitor;
	}

	@SuppressWarnings("boxing")
	private Collection<Annotation> searchAnnotations(String selectId, String bindingName, String query, Integer limit) {
		LOGGER.debug("Executing search for annotations for object id: {}", selectId);
		TimeTracker tracker = statistics.createTimeStatistics(getClass(), SEMANTIC_SEARCH).begin();

		SearchArguments<Annotation> arguments = new SearchArguments<>();
		arguments.setPermissionsType(QueryResultPermissionFilter.NONE);
		arguments.setDialect(SearchDialects.SPARQL);
		arguments.setStringQuery(query);
		arguments.setFaceted(false);
		arguments.setMaxSize(limit == null ? -1 : limit);
		arguments.setPageSize(0);

		Map<String, Serializable> bindings = new HashMap<>();
		bindings.put(bindingName, namespaceRegistryService.buildUri(selectId));
		arguments.setArguments(bindings);

		try {
			searchService.search(Annotation.class, arguments);
			Collection<Annotation> annotations = remapProperties(arguments.getResult());
			loadUserData(annotations);
			return annotations;
		} catch (Exception e) {
			LOGGER.error("Error executing search for annotations for image: {}", selectId, e.getMessage(), e);
			return Collections.emptyList();
		} finally {
			LOGGER.debug("Annotation search took {} s", tracker.stopInSeconds());
		}
	}

	private static Collection<Annotation> remapProperties(List<Annotation> result) {
		Map<Serializable, Annotation> mapping = new HashMap<>();
		List<Annotation> resultCopy = new LinkedList<>();
		for (Annotation annotation : result) {
			Map<String, Serializable> properties = annotation.getProperties();
			Map<String, Serializable> remapped = CollectionUtils.createHashMap(properties.size());
			for (Entry<String, String> entry : PROJECTION_MAPPING.entrySet()) {
				addNonNullValue(remapped, entry.getValue(), properties.remove(entry.getKey()));
			}
			annotation.setProperties(remapped);
			mapping.put(annotation.getId(), annotation);

			resultCopy.add(annotation);
		}

		return linkReplies(mapping, resultCopy);
	}

	private static Collection<Annotation> linkReplies(Map<Serializable, Annotation> mapping,
			List<Annotation> resultCopy) {

		for (Iterator<Annotation> it = resultCopy.iterator(); it.hasNext();) {
			Annotation annotation = it.next();
			Serializable parentKey = annotation.get(REPLY_KEY);
			if (parentKey == null || nullSafeEquals(annotation.getId(), parentKey)) {
				// when the reply id is the same as the current annotation it should be removed
				// this is only valid for when we are loading replies for a single annotation
				annotation.remove(REPLY_KEY);
				continue;
			}
			Annotation parent = mapping.get(parentKey);
			if (parent != null) {
				parent.getReplies().add(annotation);
				it.remove();
			}
		}
		return resultCopy;
	}

	/**
	 * Annotations actions setter.
	 *
	 * @param annotations
	 *            Collection of annotations
	 */
	private void setAllowedActions(Collection<Annotation> annotations) {
		Map<Serializable, Set<Action>> userActions = new HashMap<>();
		InstanceType annotationType = instanceTypes.from(OA.ANNOTATION)
				.orElseThrow(() -> new IllegalStateException("Missing Annotation type"));

		for (Annotation annotation : annotations) {
			// make sure the annotations has their type set
			annotation.setType(annotationType);
			annotation.setActions(getAllowedActions(annotation));
			for (Annotation reply : annotation.getReplies()) {
				reply.setType(annotationType);
				reply.setTopic(annotation);
				Set<Action> allowedActions = userActions.computeIfAbsent(reply.getCreatedBy(),
						actions -> getAllowedActions(reply));
				reply.setActions(allowedActions);
			}
		}
	}

	/**
	 * Gets the current user allowed actions on the given annotation.
	 *
	 * @param annotation
	 *            to get actions for
	 * @return collection of allowed actions
	 */
	private Set<Action> getAllowedActions(Annotation annotation) {
		if (annotation == null) {
			return Collections.emptySet();
		}

		Set<Action> actions = authorityService.getAllowedActions(annotation, "");

		// Should set and return NO_ACTIONS_ALLOWED action if no actions are calculated to ensure UI functionality.
		if (CollectionUtils.isEmpty(actions)) {
			EmfAction noAllowed = new EmfAction(ActionTypeConstants.NO_ACTIONS_ALLOWED);
			noAllowed.setPurpose(ActionTypeConstants.NO_ACTIONS_ALLOWED);
			noAllowed.setLabel(labelProvider.getValue("cmf.btn.actions.not_allowed"));
			noAllowed.setDisabled(false);
			return Collections.singleton(noAllowed);
		}

		return actions;
	}

	@Override
	@SuppressWarnings("boxing")
	public int countAnnotations(String targetId, String tabId) {
		if (StringUtils.isBlank(targetId) || StringUtils.isBlank(tabId)) {
			return -1;
		}
		LOGGER.debug("Executing annotation count for target id: {}", targetId);
		TimeTracker tracker = statistics.createTimeStatistics(getClass(), SEMANTIC_SEARCH).begin();
		Serializable id = idManager.getValidId(tabId);
		SearchArguments<Annotation> arguments = new SearchArguments<>();
		arguments.setPermissionsType(QueryResultPermissionFilter.NONE);
		arguments.setDialect(SearchDialects.SPARQL);
		arguments.setStringQuery(COUNT_ANNOTATIONS);
		arguments.setFaceted(false);
		arguments.setCountOnly(true);
		arguments.setMaxSize(-1);

		Map<String, Serializable> bindings = new HashMap<>();
		bindings.put(HAS_TARGET, targetId);
		bindings.put(COMMENTS_ON, String.valueOf(id));
		arguments.setArguments(bindings);

		try {
			searchService.search(Annotation.class, arguments);
			return arguments.getTotalItems();
		} catch (Exception e) {
			LOGGER.error("Error executing annotation counting target id: {}", targetId, e.getMessage(), e);
			return -1;
		} finally {
			LOGGER.debug("Annotation count took {} s", tracker.stopInSeconds());
		}
	}

	@Override
	@SuppressWarnings("boxing")
	public Map<String, Integer> countAnnotationReplies(String targetId) {
		if (StringUtils.isBlank(targetId)) {
			return Collections.emptyMap();
		}
		LOGGER.debug("Executing annotation reply count for target id: {}", targetId);
		TimeTracker tracker = statistics.createTimeStatistics(getClass(), SEMANTIC_SEARCH).begin();

		SearchArguments<Annotation> arguments = new SearchArguments<>();
		arguments.setPermissionsType(QueryResultPermissionFilter.NONE);
		arguments.setDialect(SearchDialects.SPARQL);
		arguments.setStringQuery(COUNT_ANNOTATION_REPLIES);
		arguments.setFaceted(false);
		arguments.setMaxSize(-1);
		arguments.setPageSize(0);

		Map<String, Serializable> bindings = new HashMap<>();
		bindings.put(HAS_TARGET, targetId);
		arguments.setArguments(bindings);

		try {
			searchService.search(Annotation.class, arguments);
			Map<String, Integer> mapping = createHashMap(arguments.getResult().size());
			for (Annotation annotation : arguments.getResult()) {
				mapping.put(annotation.getId().toString(), annotation.get("count", Integer.class, () -> 0));
			}
			return mapping;
		} catch (Exception e) {
			LOGGER.error("Error executing annotation reply counting target id: {}", targetId, e.getMessage(), e);
			return Collections.emptyMap();
		} finally {
			LOGGER.debug("Annotation reply count took {} s", tracker.stopInSeconds());
		}
	}

}
