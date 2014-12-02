package com.sirma.itt.emf.comment;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;

import org.apache.log4j.Logger;

import com.sirma.itt.emf.cache.CacheConfiguration;
import com.sirma.itt.emf.cache.Eviction;
import com.sirma.itt.emf.cache.Expiration;
import com.sirma.itt.emf.converter.TypeConverterUtil;
import com.sirma.itt.emf.db.DbDao;
import com.sirma.itt.emf.db.SemanticDb;
import com.sirma.itt.emf.db.SequenceEntityGenerator;
import com.sirma.itt.emf.domain.Context;
import com.sirma.itt.emf.forum.AbstractCommentService;
import com.sirma.itt.emf.forum.CommentService;
import com.sirma.itt.emf.forum.model.CommentInstance;
import com.sirma.itt.emf.forum.model.ImageAnnotation;
import com.sirma.itt.emf.forum.model.TopicInstance;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.instance.model.InstanceReference;
import com.sirma.itt.emf.plugin.ExtensionPoint;
import com.sirma.itt.emf.properties.dao.SemanticNonPersistentPropertiesExtension;
import com.sirma.itt.emf.search.SearchService;
import com.sirma.itt.emf.search.model.SearchArguments;
import com.sirma.itt.emf.search.model.Sorter;
import com.sirma.itt.emf.security.model.User;
import com.sirma.itt.emf.time.TimeTracker;
import com.sirma.itt.emf.util.CollectionUtils;
import com.sirma.itt.emf.util.Documentation;
import com.sirma.itt.emf.util.EqualsHelper;
import com.sirma.itt.semantic.NamespaceRegistryService;
import com.sirma.itt.semantic.model.vocabulary.EMF;
import com.sirma.itt.semantic.search.SemanticQueries;

/**
 * Implements the methods defined in {@link CommentService}.
 *
 * @author Adrian Mitev
 */
@Stateless
public class CommentServiceImpl extends AbstractCommentService {

	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = 6502878793761243439L;

	/** The namespace registry service. */
	@Inject
	private NamespaceRegistryService namespaceRegistryService;

	@Inject
	private SearchService searchService;

	@Inject
	@SemanticDb
	private DbDao dbDao;

	/** The log. */
	private static final Logger LOG = Logger.getLogger(CommentService.class);

	/** The Constant SEMANTIC_TOPIC_ENTITY_ID_BY_TARGET_CACHE. */
	@CacheConfiguration(container = "cmf", eviction = @Eviction(maxEntries = 40000, strategy = "LRU"), expiration = @Expiration(interval = 10000, lifespan = 600000, maxIdle = 300000), doc = @Documentation(""))
	private static final String SEMANTIC_TOPIC_ENTITY_ID_BY_TARGET_CACHE = "SEMANTIC_TOPIC_ENTITY_ID_BY_TARGET_CACHE";

	/** The Constant SEMANTIC_TOPIC_ENTITY_ID_BY_COMMENT_CACHE. */
	@CacheConfiguration(container = "cmf", eviction = @Eviction(maxEntries = 400000, strategy = "LRU"), expiration = @Expiration(interval = 10000, lifespan = 600000, maxIdle = 300000), doc = @Documentation(""))
	private static final String SEMANTIC_TOPIC_ENTITY_ID_BY_COMMENT_CACHE = "SEMANTIC_TOPIC_ENTITY_ID_BY_COMMENT_CACHE";

	/** The Constant SEMANTIC_TOPIC_ENTITY_BY_ID_CACHE. */
	@CacheConfiguration(container = "cmf", eviction = @Eviction(maxEntries = 10000, strategy = "LRU"), expiration = @Expiration(interval = 10000, lifespan = 600000, maxIdle = 300000), doc = @Documentation(""
			+ "Cache that stores topic instances with all it's comments with a key as the topic db id/uri. The cache "))
	private static final String SEMANTIC_TOPIC_ENTITY_BY_ID_CACHE = "SEMANTIC_TOPIC_ENTITY_BY_ID_CACHE";

	/** The non persistent properties. */
	@Inject
	@ExtensionPoint(value = SemanticNonPersistentPropertiesExtension.TARGET_NAME)
	private Iterable<SemanticNonPersistentPropertiesExtension> semanticNonPersistentProperties;

	/**
	 * The default sorter. It's not good to link the property to a constants that is not related to
	 * the query.
	 */
	private static Sorter defaultSorter = new Sorter("createdOn", Sorter.SORT_ASCENDING);

	/** The debug. */
	private static boolean debug = LOG.isDebugEnabled();

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected String getTopicIdByTargetCacheName() {
		return SEMANTIC_TOPIC_ENTITY_ID_BY_TARGET_CACHE;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected String getTopicIdByCommentCacheName() {
		return SEMANTIC_TOPIC_ENTITY_ID_BY_COMMENT_CACHE;
	}

	@Override
	protected Set<String> getForbiddenProperties() {
		Set<String> properties = new HashSet<String>();
		for (SemanticNonPersistentPropertiesExtension extension : semanticNonPersistentProperties) {
			properties.addAll(extension.getNonPersistentProperties());
		}
		return properties;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected String getTopicByIdCacheName() {
		return SEMANTIC_TOPIC_ENTITY_BY_ID_CACHE;
	}

	@Override
	protected void persistTopic(TopicInstance topic, TopicInstance oldInstance) {
		TimeTracker tracker = null;
		if (debug) {
			tracker = new TimeTracker().begin();
			LOG.debug("CommentService.persistTopic topic start");
		}

		SequenceEntityGenerator.generateStringId(topic, false);
		if (topic.getImageAnnotation() != null) {
			SequenceEntityGenerator.generateStringId(topic.getImageAnnotation(), false);
		}

		dbDao.saveOrUpdate(topic, oldInstance);

		if (debug) {
			LOG.debug("CommentService.persistTopic topic in " + tracker.stopInSeconds() + " s");
		}
	}

	@Override
	protected void persistComment(CommentInstance comment, CommentInstance oldInstance) {
		TimeTracker tracker = null;
		if (debug) {
			tracker = new TimeTracker().begin();
			LOG.debug("CommentService.persistComment comment start");
		}

		// generate id
		SequenceEntityGenerator.generateStringId(comment, false);

		dbDao.saveOrUpdate(comment, oldInstance);

		if (debug) {
			LOG.debug("CommentService.persistComment comment in " + tracker.stopInSeconds() + " s");
		}
	}

	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	protected List<TopicInstance> loadTopicByDbId(Serializable key) {
		TimeTracker tracker = null;
		if (debug) {
			tracker = new TimeTracker().begin();
			LOG.debug("CommentService.loadTopicByDbId comments by object id [" + key + "]");
		}

		String localUri = key.toString();
		if (!localUri.contains(":")) {
			localUri = EMF.PREFIX + ":" + localUri;
			LOG.debug("CommentService.loadTopicByDbId KEY DOESN'T HAVE PREFFIX! [" + key + "]");
		}

		SearchArguments<Instance> filter = searchService.getFilter(
				SemanticQueries.QUERY_GET_TOPICS_BY_OBJECT_ID.name(), Instance.class,
				buildFilterContext(defaultSorter, -1, null));

		Map<String, Serializable> arguments = CollectionUtils.createHashMap(2);
		arguments.put("instance", localUri);
		arguments.put("includeInferred", Boolean.TRUE);

		filter.setArguments(arguments);

		Map<String, TopicInstance> topics = executeTopicSearch(filter);

		if (debug) {
			LOG.debug("CommentService.loadTopicByDbId comments by object id [" + key
					+ "] in " + tracker.stopInSeconds() + " s");
			LOG.debug("CommentService.loadTopicByDbId response size:" + topics.size());
		}

		return new ArrayList<>(topics.values());
	}

	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	protected TopicInstance loadTopicByCommentDbId(Serializable key) {
		TimeTracker tracker = null;
		if (debug) {
			tracker = new TimeTracker().begin();
			LOG.debug("CommentService.loadTopicByCommentDbId comments by object id [" + key + "]");
		}

		String localUri = key.toString();
		if (!localUri.contains(":")) {
			localUri = EMF.PREFIX + ":" + localUri;
		}

		SearchArguments<Instance> filter = searchService.getFilter(
				SemanticQueries.QUERY_GET_COMMENT_BY_OBJECT_ID.name(), Instance.class, null);

		Map<String, Serializable> arguments = CollectionUtils.createHashMap(2);
		arguments.put("instance", localUri);
		arguments.put("includeInferred", Boolean.FALSE);

		filter.setArguments(arguments);

		Map<String, TopicInstance> topics = executeTopicSearch(filter);

		if (debug) {
			LOG.debug("CommentService.loadTopicByCommentDbId comments by object id [" + key
					+ "] in " + tracker.stopInSeconds() + " s");
			LOG.debug("CommentService.loadTopicByCommentDbId response size:" + topics.size());
		}
		Iterator<TopicInstance> iterator = topics.values().iterator();
		if (iterator.hasNext()) {
			return iterator.next();
		}

		return null;
	}

	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	protected CommentInstance getCommentByDbId(Serializable id) {
		TimeTracker tracker = null;
		if (debug) {
			tracker = TimeTracker.createAndStart();
			LOG.debug("getCommentByDbId id [" + id + "]");
		}

		Context<String, Object> context = new Context<String, Object>(1);
		addLimit(context, 1);
		SearchArguments<Instance> filter = searchService.getFilter(
				SemanticQueries.QUERY_GET_COMMENT_BY_ID.name(), Instance.class, context);

		Map<String, Serializable> arguments = CollectionUtils.createHashMap(1);
		arguments.put("instance", id);
		arguments.put("includeInferred", Boolean.FALSE);
		filter.setArguments(arguments);

		Map<String, TopicInstance> topics = executeTopicSearch(filter);
		List<CommentInstance> comments = Collections.emptyList();
		if (topics.size() > 0) {
			TopicInstance topicInstance = topics.values().iterator().next();
			if (topicInstance != null) {
				comments = topicInstance.getComments();
			}
		}

		if (debug) {
			LOG.debug("getCommentByDbId id [" + id + "] took " + tracker.stopInSeconds()
					+ " s and found " + comments.size());
		}

		if (comments.isEmpty()) {
			return null;
		}

		if (EqualsHelper.nullSafeEquals(comments.get(0).getId(), id)) {
			return comments.get(0);
		}
		return null;
	}

	@Override
	protected List<TopicInstance> loadTopicByTopicAbout(InstanceReference key, Date lastKnownDate,
			int limit, Sorter sorter, Map<String, Serializable> filterParams) {
		TimeTracker tracker = null;
		if (debug) {
			tracker = new TimeTracker().begin();
			LOG.debug("loadTopicByTopicAbout comments by object id ["
					+ key.getIdentifier() + "]");
		}

		String localUri = key.getIdentifier();
		if (!localUri.contains(":")) {
			localUri = EMF.PREFIX + ":" + localUri;
			LOG.debug("loadTopicByTopicAbout KEY DOESN'T HAVE PREFFIX! [" + key + "]");
		}

		Map<String, Serializable> filters = filterParams;
		if (filters == null) {
			filters = new HashMap<String, Serializable>(4);
		}
		filters.put("lastKnownDate", lastKnownDate);

		SearchArguments<Instance> filter = searchService.getFilter(
				SemanticQueries.QUERY_GET_TOPICS_BY_OBJECT_ID.name(), Instance.class,
				buildFilterContext(sorter, limit, filters));

		Map<String, Serializable> arguments = CollectionUtils.createHashMap(2);
		// when object and section are the same then the query will return only topics that are
		// placed on the root object
		arguments.put("object", localUri);
		arguments.put("objectSection", localUri);
		arguments.put("includeInferred", Boolean.TRUE);
		if (filters != null) {
			arguments.putAll(filters);
		}
		filter.setArguments(arguments);
		if (limit > 0) {
			filter.setPageSize(limit);
		}

		Map<String, TopicInstance> topics = executeTopicSearch(filter);

		if (debug) {
			LOG.debug("loadTopicByTopicAbout comments by object id [" + key + "] took "
					+ tracker.stopInSeconds() + " s and returned " + topics.size());
		}

		return new ArrayList<>(topics.values());
	}

	/**
	 * Execute topic search.
	 *
	 * @param filter
	 *            the filter
	 * @return the map
	 */
	private Map<String, TopicInstance> executeTopicSearch(SearchArguments<Instance> filter) {
		searchService.search(Instance.class, filter);

		List<Instance> resultList = filter.getResult();

		Map<String, TopicInstance> topics = new LinkedHashMap<String, TopicInstance>();

		for (Instance instance : resultList) {

			if (instance instanceof TopicInstance) {
				TopicInstance topicInstance = (TopicInstance) instance;

				TopicInstance existingInstance = topics.get(topicInstance.getId());
				if (existingInstance != null) {
					topicInstance.getComments().addAll(existingInstance.getComments());
					topics.put(topicInstance.getId().toString(), topicInstance);
				} else {
					topics.put(topicInstance.getId().toString(), topicInstance);
				}

				Map<String, Serializable> topicProperties = topicInstance.getProperties();

				// fill topic properties

				// object
				String objectURI = (String) topicProperties.remove("object");
				String objectType = (String) topicProperties.remove("objectType");
				if (objectType.indexOf(':') > 0) {
					objectType = namespaceRegistryService.buildFullUri(objectType);
				}

				InstanceReference reference = TypeConverterUtil.getConverter().convert(
						InstanceReference.class, objectType);
				reference.setIdentifier(objectURI);
				topicInstance.setTopicAbout(reference);

				// imageAnnotation
				String immageAnnotationURI = (String) topicProperties.remove("imageAnnotation");
				if (immageAnnotationURI != null) {
					ImageAnnotation imageAnnotation = new ImageAnnotation();
					imageAnnotation.setId(immageAnnotationURI);
					imageAnnotation.setSvgValue((String) topicProperties.remove("svgValue"));
					imageAnnotation.setViewBox((String) topicProperties.remove("viewBox"));
					imageAnnotation.setZoomLevel(TypeConverterUtil.getConverter().convert(
							Integer.class, topicProperties.remove("zoomLevel")));
					topicInstance.setImageAnnotation(imageAnnotation);
				}

				String objectSectionURI = (String) topicProperties.remove("objectSection");
				topicInstance.setSubSectionId(objectSectionURI);

			} else {
				CommentInstance commentInstance = (CommentInstance) instance;
				Map<String, Serializable> commentProperties = commentInstance.getProperties();

				// parent
				String parentURI = (String) commentProperties.remove("parent");
				TopicInstance topicInstance = topics.get(parentURI);
				if (topicInstance == null) {
					topicInstance = new TopicInstance();
					topicInstance.setId(parentURI);
					topics.put(parentURI, topicInstance);
				}
				topicInstance.getComments().add(commentInstance);
				commentInstance.setTopic(topicInstance);
			}
		}
		return topics;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	protected List<TopicInstance> loadTopicBySubSectionId(String key, Date lastKnownDate,
			int limit, Sorter sorter, Map<String, Serializable> filterParams) {
		TimeTracker tracker = null;
		if (debug) {
			tracker = new TimeTracker().begin();
			LOG.debug("loadTopicBySubSectionId comments by object id [" + key + "]");
		}

		String localUri = key.toString();
		if (!localUri.contains(":")) {
			localUri = EMF.PREFIX + ":" + localUri;
			LOG.debug("loadTopicBySubSectionId KEY DOESN'T HAVE PREFFIX! [" + key + "]");
		}

		Map<String, Serializable> filters = filterParams;
		if (filters == null) {
			filters = new HashMap<String, Serializable>(4);
		}
		filters.put("lastKnownDate", lastKnownDate);

		Context<String, Object> context = buildFilterContext(sorter, limit, filters);
		SearchArguments<Instance> filter = searchService.getFilter(
				SemanticQueries.QUERY_GET_TOPICS_BY_OBJECT_ID.name(), Instance.class, context);

		Map<String, Serializable> arguments = CollectionUtils.createHashMap(2);
		arguments.put("objectSection", localUri);
		arguments.put("includeInferred", Boolean.TRUE);
		if (filters != null) {
			arguments.putAll(filters);
		}
		filter.setSorter(sorter);

		filter.setArguments(arguments);

		Map<String, TopicInstance> topics = executeTopicSearch(filter);

		if (debug) {
			LOG.debug("loadTopicBySubSectionId comments by object id [" + key + "] took "
					+ tracker.stopInSeconds() + " s and returned " + topics.size());
		}

		return new ArrayList<>(topics.values());
	}

	/**
	 * Builds the filter context.
	 *
	 * @param sorter
	 *            the sorter
	 * @param limit
	 *            the limit
	 * @param filterParams
	 *            the filter parameters that are passed for additional filtering. We are going to
	 *            use them to build proper parameters for the query builder.
	 * @return the context
	 */
	private Context<String, Object> buildFilterContext(Sorter sorter, int limit,
			Map<String, Serializable> filterParams) {
		Sorter sort = sorter;
		if (sort == null) {
			sort = defaultSorter;
		}
		Context<String, Object> context = new Context<String, Object>(4);
		context.put("filter", sort.getSortField());
		context.put("sort", sort);
		applyFilters(context, filterParams);
		addLimit(context, limit);
		return context;
	}

	/**
	 * Apply filters to context parameters. Update the context based on the provided filters
	 *
	 * @param context
	 *            the context
	 * @param filterParams
	 *            the filter params
	 */
	private void applyFilters(Context<String, Object> context,
			Map<String, Serializable> filterParams) {
		if (filterParams == null) {
			return;
		}
		if (filterParams.containsKey("tagFilter")) {
			context.put("tags",
					"?instance emf:tag ?tags . filter(regex(str(?tags), \"{tagFilter}\",'i')). ");
			context.put("tagFilter", filterParams.remove("tagFilter"));
		}
		if (filterParams.containsKey("categoryFilter")) {
			context.put("category",
					"?instance emf:type ?type . filter(regex(str(?type), \"{categoryFilter}\",'i')).");
			context.put("categoryFilter", filterParams.remove("categoryFilter"));
		}
		// create dates filter
		Serializable value = filterParams.get("lastKnownDate");
		if (filterParams.containsKey("fromDate") || filterParams.containsKey("toDate")
				|| (value != null)) {
			String filter = "";
			if (filterParams.containsKey("fromDate")) {
				filter = " ?{filter} > ?fromDate ";
			}
			if (filterParams.containsKey("toDate")) {
				// if lastKnownDate is not set we ensure is the passed toDate to be used
				long lastKnownDate = Long.MAX_VALUE;
				if (value instanceof Date) {
					lastKnownDate = ((Date) value).getTime();
				}

				long min = Math.min(lastKnownDate, ((Date) filterParams.get("toDate")).getTime());

				// update the last date
				filterParams.put("toDate", new Date(min));
				if (!filter.isEmpty()) {
					filter += "&&";
				}
				filter += " ?{filter} <= ?toDate ";
			}
			// ensure pagination
			if (filter.isEmpty() && (value != null)) {
				filter += " ?{filter} <= ?lastKnownDate ";
			}
			if (!filter.isEmpty()) {
				context.put("datesFilter", "filter ( " + filter + " ).");
			}
		}
		if (filterParams.containsKey("zoomLevel")) {
			context.put(
					"imageAnnotation",
					"?instance emf:hasImageAnnotation ?imageAnnotation . ?imageAnnotation emf:svgValue ?svgValue ; emf:zoomLevel ?zoomLevel ; emf:viewBox ?viewBox.");
		}
		if (filterParams.containsKey("objectTypeParameter")) {
			context.put("objectTypeParameter", "?object a ?objectTypeParameter .");
		}
	}

	/**
	 * Adds the limit.
	 * @param context
	 *            the context
	 * @param limit
	 *            the limit
	 */
	private void addLimit(Context<String, Object> context, int limit) {
		context.put("limit", Integer.valueOf(limit));
	}

	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public List<TopicInstance> getTopicsByUser(User user) {
		return getTopicsByUser(user, new Date(), -1, null, null);
	}

	@Override
	public List<TopicInstance> getInstanceSuccessorsTopics(String uri) {
		return getInstanceSuccessorsTopics(uri, new Date(), -1, null, null);
	}

	@Override
	protected void deleteEntry(CommentInstance instance) {
		if (instance instanceof TopicInstance) {
			// to mark as deleted all comments and topics we collect all ids and push them to DB
			List<CommentInstance> comments = ((TopicInstance) instance).getComments();
			Collection<Serializable> ids = new ArrayList<>(comments.size() + 1);
			ids.add(instance.getId());
			for (CommentInstance commentInstance : comments) {
				ids.add(commentInstance.getId());
			}
			dbDao.delete(CommentInstance.class, (Serializable) ids);
		} else {
			dbDao.delete(CommentInstance.class, instance.getId());
		}
	}

	@Override
	public List<TopicInstance> getTopicsByUser(User user, Date lastKnownDate, int limit,
			Sorter sorter, Map<String, Serializable> filterParams) {
		TimeTracker tracker = null;
		if (debug) {
			tracker = new TimeTracker().begin();
			LOG.debug("getTopicsByUser getting topics for user [" + user + "]");
		}

		Map<String, Serializable> filters = filterParams;
		if ((filters == null) && (lastKnownDate != null)) {
			filters = new HashMap<String, Serializable>(4);
		}
		filters.put("lastKnownDate", lastKnownDate);

		SearchArguments<Instance> filter = searchService.getFilter(
				SemanticQueries.QUERY_GET_USER_TOPICS.name(), Instance.class,
				buildFilterContext(sorter, limit, filters));

		Map<String, Serializable> arguments = CollectionUtils.createHashMap(1);
		arguments.put("owner", user.getId().toString());
		if (filters != null) {
			arguments.putAll(filters);
		}
		filter.setArguments(arguments);
		if (limit > 0) {
			filter.setPageSize(limit);
		}

		Map<String, TopicInstance> topics = executeTopicSearch(filter);

		if (debug) {
			LOG.debug("getTopicsByUser loaded topics for user [" + user + "] took "
					+ tracker.stopInSeconds() + " s and returned " + topics.size());
		}

		return new ArrayList<>(topics.values());
	}

	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public List<TopicInstance> getInstanceSuccessorsTopics(String uri, Date lastKnownDate,
			int limit, Sorter sorter, Map<String, Serializable> filterParams) {
		TimeTracker tracker = null;
		if (debug) {
			tracker = new TimeTracker().begin();
			LOG.debug("getInstanceSuccessorsTopics getting topics for instance "
					+ "with [" + uri + "] and all its children");
		}

		Map<String, Serializable> filters = filterParams;
		if (filters == null) {
			filters = new HashMap<String, Serializable>(4);
		}
		filters.put("lastKnownDate", lastKnownDate);

		SearchArguments<Instance> filter = searchService.getFilter(
				SemanticQueries.QUERY_GET_INSTANCE_SUCCESSORS_TOPICS.name(), Instance.class,
				buildFilterContext(sorter, limit, filters));

		Map<String, Serializable> arguments = CollectionUtils.createHashMap(1);
		arguments.put("objectParameter", uri);
		filter.setArguments(arguments);
		if (filters != null) {
			arguments.putAll(filters);
		}
		if (limit > 0) {
			filter.setPageSize(limit);
		}
		Map<String, TopicInstance> topics = executeTopicSearch(filter);

		if (debug) {
			LOG.debug("getInstanceSuccessorsTopics loaded topics for instance " + "with [" + uri
					+ "] and all its children took " + tracker.stopInSeconds() + " s and returned "
					+ topics.size());
		}

		return new ArrayList<>(topics.values());
	}

	@Override
	protected List<CommentInstance> loadCommentsAfter(Serializable topicId, Date lastKnownDate,
			int limit) {
		// return Collections.emptyList();
		// disabled comments loading due to high query time
		TimeTracker tracker = null;
		if (debug) {
			tracker = TimeTracker.createAndStart();
			LOG.debug("loadCommentsAfter getting comments for topic [" + topicId + "]");
		}

		Context<String, Object> context = new Context<String, Object>();
		context.put("filterBy", "filter (?createdOn > ?lastKnownDate) . ");
		addLimit(context, limit);
		SearchArguments<Instance> filter = searchService.getFilter(
				SemanticQueries.QUERY_GET_COMMENT_FOR_TOPIC_ID.name(), Instance.class, context);

		Map<String, Serializable> arguments = CollectionUtils.createHashMap(1);
		arguments.put("parent", topicId);
		arguments.put("lastKnownDate", lastKnownDate == null ? new Date() : lastKnownDate);
		arguments.put("includeInferred", Boolean.FALSE);
		filter.setArguments(arguments);

		Map<String, TopicInstance> topics = executeTopicSearch(filter);
		List<CommentInstance> comments = Collections.emptyList();
		if (topics.size() > 0) {
			TopicInstance topicInstance = topics.get(topicId);
			if (topicInstance != null) {
				comments = topicInstance.getComments();
			}
		}

		if (debug) {
			LOG.debug("loadCommentsAfter getting comments for topic [" + topicId + "] took "
					+ tracker.stopInSeconds() + " s and found " + comments.size());
		}

		return comments;
	}

	@Override
	protected List<CommentInstance> loadCommentsBefore(Serializable topicId, Date lastKnownDate,
			int limit) {
		TimeTracker tracker = null;
		if (debug) {
			tracker = TimeTracker.createAndStart();
			LOG.debug("loadCommentsBefore getting comments for topic ["
					+ topicId + "]");
		}

		Context<String, Object> context = new Context<String, Object>();
		context.put("filterBy", "filter (?createdOn < ?lastKnownDate) . ");
		addLimit(context, limit);
		SearchArguments<Instance> filter = searchService.getFilter(
				SemanticQueries.QUERY_GET_COMMENT_FOR_TOPIC_ID.name(), Instance.class, context);

		Map<String, Serializable> arguments = CollectionUtils.createHashMap(1);
		arguments.put("parent", topicId);
		arguments.put("lastKnownDate", lastKnownDate == null ? new Date() : lastKnownDate);
		arguments.put("includeInferred", Boolean.FALSE);
		filter.setArguments(arguments);


		Map<String, TopicInstance> topics = executeTopicSearch(filter);
		List<CommentInstance> comments = Collections.emptyList();
		if (topics.size() > 0) {
			TopicInstance topicInstance = topics.get(topicId);
			if (topicInstance != null) {
				comments = topicInstance.getComments();
			}
		}

		if (debug) {
			LOG.debug("loadCommentsBefore getting comments for topic [" + topicId + "] took "
					+ tracker.stopInSeconds() + " s and found " + comments.size());
		}

		return comments;
	}

}
