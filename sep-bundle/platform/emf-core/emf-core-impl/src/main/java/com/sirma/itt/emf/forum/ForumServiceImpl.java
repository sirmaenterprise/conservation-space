/*
 *
 */
package com.sirma.itt.emf.forum;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.persistence.TemporalType;

import org.dozer.Mapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.emf.cache.CacheConfiguration;
import com.sirma.itt.emf.cache.Eviction;
import com.sirma.itt.emf.cache.Expiration;
import com.sirma.itt.emf.cache.lookup.EntityLookupCache;
import com.sirma.itt.emf.cache.lookup.EntityLookupCache.EntityLookupCallbackDAOAdaptor;
import com.sirma.itt.emf.cache.lookup.EntityLookupCacheContext;
import com.sirma.itt.emf.configuration.RuntimeConfiguration;
import com.sirma.itt.emf.configuration.RuntimeConfigurationProperties;
import com.sirma.itt.emf.db.DbDao;
import com.sirma.itt.emf.db.EmfQueries;
import com.sirma.itt.emf.db.SequenceEntityGenerator;
import com.sirma.itt.emf.domain.Pair;
import com.sirma.itt.emf.domain.Triplet;
import com.sirma.itt.emf.dozer.DozerMapper;
import com.sirma.itt.emf.entity.LinkSourceId;
import com.sirma.itt.emf.exceptions.EmfRuntimeException;
import com.sirma.itt.emf.forum.entity.CommentEntity;
import com.sirma.itt.emf.forum.entity.TopicEntity;
import com.sirma.itt.emf.forum.model.ChatInstance;
import com.sirma.itt.emf.forum.model.CommentInstance;
import com.sirma.itt.emf.forum.model.TopicInstance;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.instance.model.InstanceReference;
import com.sirma.itt.emf.properties.PropertiesService;
import com.sirma.itt.emf.security.AuthenticationService;
import com.sirma.itt.emf.time.DateRange;
import com.sirma.itt.emf.time.TimeTracker;
import com.sirma.itt.emf.util.DateRangeUtil;
import com.sirma.itt.emf.util.Documentation;

/**
 * Default implementation for {@link ForumService}.
 * 
 * @author BBonev
 */
@Stateless
public class ForumServiceImpl implements ForumService {

	/** The Constant TOPIC_ENTITY_CACHE. */
	@CacheConfiguration(container = "cmf", eviction = @Eviction(maxEntries = 100), expiration = @Expiration(maxIdle = 600000, interval = 720000), doc = @Documentation(""
			+ "Cache used to store the different topic entries for the different conversations. For each different topic there are 2 entries in the cache. "
			+ "<br>Minimal value expression: users * 10"))
	private static final String TOPIC_ENTITY_CACHE = "TOPIC_ENTITY_CACHE";
	/** The Constant TODAY_COMMENT_ENTITY_CACHE. */
	@CacheConfiguration(container = "cmf", eviction = @Eviction(maxEntries = 50), expiration = @Expiration(maxIdle = 600000, interval = 720000), doc = @Documentation(""
			+ "Cache used to store the list of todays comments per loaded topic. For each different topic there is an entry in the cache. "
			+ "<br>Minimal value expression: users * 5"))
	private static final String TODAY_COMMENT_ENTITY_CACHE = "TODAY_COMMENT_ENTITY_CACHE";

	private static final Logger LOGGER = LoggerFactory.getLogger(ForumServiceImpl.class);

	/** The db dao. */
	@Inject
	private DbDao dbDao;

	/** The cache context. */
	@Inject
	private EntityLookupCacheContext cacheContext;

	@Inject
	private PropertiesService propertiesService;

	@Inject
	private DozerMapper dozerMapper;

	@Inject
	private javax.enterprise.inject.Instance<AuthenticationService> authenticationServiceInst;

	/**
	 * Initialize the caches.
	 */
	@PostConstruct
	public void init() {
		if (!cacheContext.containsCache(TOPIC_ENTITY_CACHE)) {
			cacheContext.createCache(TOPIC_ENTITY_CACHE, new TopicEntityCacheCallback());
		}
		if (!cacheContext.containsCache(TODAY_COMMENT_ENTITY_CACHE)) {
			cacheContext.createCache(TODAY_COMMENT_ENTITY_CACHE, new TodayCommentsCacheCallback());
		}
	}

	/**
	 * Gets the topic cache.
	 * 
	 * @return the topic cache
	 */
	private EntityLookupCache<String, TopicEntity, Pair<String, InstanceReference>> getTopicCache() {
		return cacheContext.getCache(TOPIC_ENTITY_CACHE);
	}

	/**
	 * Gets the today comments cache.
	 * 
	 * @return the today comments cache
	 */
	private EntityLookupCache<Serializable, List<CommentInstance>, Serializable> getTodayCommentsCache() {
		return cacheContext.getCache(TODAY_COMMENT_ENTITY_CACHE);
	}

	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public TopicInstance getOrCreateTopicAbout(Instance instance) {
		if (instance == null) {
			throw new EmfRuntimeException("Cannot create TopicInstance for NULL source instance.");
		}
		// if the instance is not supported then the converter will throw an exception
		LinkSourceId sourceId = convertToLinkSource(instance);
		// create the default topic identifier for the given instance
		String valueIdentifier = createDefaultValueIdentifier(sourceId);
		EntityLookupCache<String, TopicEntity, Pair<String, InstanceReference>> topicCache = getTopicCache();
		TopicEntity result = null;
		// first check if the entity is loaded
		Pair<String, TopicEntity> pair = topicCache.getByKey(valueIdentifier);
		// if not create new one
		boolean loadProps = true;
		if (pair == null) {
			loadProps = false;
			TopicEntity value = new TopicEntity();
			value.setIdentifier(valueIdentifier);
			value.setTopicAbout(sourceId);

			pair = topicCache.getOrCreateByValue(value);
			if (pair != null) {
				result = pair.getSecond();
			}
		} else {
			result = pair.getSecond();
		}
		if (result == null) {
			throw new EmfRuntimeException("Failed to create topic for instance: " + valueIdentifier);
		}
		return convertToInstance(result, loadProps);
	}

	/**
	 * Convert to instance and optionally loads the instance properties
	 * 
	 * @param result
	 *            the result
	 * @param loadProperties
	 *            the load properties
	 * @return the topic instance
	 */
	private TopicInstance convertToInstance(TopicEntity result, boolean loadProperties) {
		TopicInstance instance = dozerMapper.getMapper().map(result, TopicInstance.class);
		if (loadProperties && SequenceEntityGenerator.isPersisted(instance)) {
			propertiesService.loadProperties(instance);
		}
		// initialize the temporary properties
		instance.getProperties().put(ForumProperties.IS_TOPIC_EXPANDED, Boolean.FALSE);
		instance.getProperties().put(ForumProperties.TODAY_COMMENT_COUNT, 0);
		// TODO: check the dozer
		instance.setTopicAbout(result.getTopicAbout());
		return instance;
	}

	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public TopicInstance save(TopicInstance instance) {
		if (instance == null) {
			return instance;
		}

		if (!SequenceEntityGenerator.isPersisted(instance)) {
			TopicEntity entity = dozerMapper.getMapper().map(instance, TopicEntity.class);
			Pair<String, TopicEntity> pair = getTopicCache().getOrCreateByValue(entity);
			if (pair != null) {
				instance.setId(pair.getSecond().getId());
			} else {
				throw new EmfRuntimeException("Cannot save topic instance!");
			}
		}
		setCustomPropertiesSaving(true);
		try {
			propertiesService.saveProperties(instance);
		} finally {
			setCustomPropertiesSaving(false);
		}

		List<CommentInstance> comments = instance.getComments();
		List<CommentInstance> unsaved = new LinkedList<>();
		for (Iterator<CommentInstance> it = comments.iterator(); it.hasNext();) {
			CommentInstance comment = it.next();
			if (!SequenceEntityGenerator.isPersisted(comment)) {
				it.remove();
				unsaved.add(comment);
			}
		}

		// the problem comes when the comments are mixed saved and unsaved
		// but we hope that does not happen
		for (CommentInstance commentInstance : unsaved) {
			postComment(instance, commentInstance);
		}

		return instance;
	}

	/**
	 * Update today cache. The method also updates the comments list if someone else has posted a
	 * comment between the last update of the list
	 * 
	 * @param instance
	 *            the instance
	 * @param singleComment
	 *            the single comment
	 */
	private void syncTodayCache(TopicInstance instance, CommentInstance singleComment) {
		TimeTracker tracker = new TimeTracker().begin();
		Integer currentCount = (Integer) instance.getProperties().get(
				ForumProperties.TODAY_COMMENT_COUNT);
		// int commentsCount = instance.getComments().size();
		if (currentCount == null) {
			currentCount = 0;
		}
		// if we have out of sync currentCount
		if (instance.getComments().size() < currentCount) {
			currentCount = instance.getComments().size();
		}
		// limit the access to the comments cache when adding new comments
		instance.getTopicLock().lock();
		int newSize = 1;
		try {
			EntityLookupCache<Serializable, List<CommentInstance>, Serializable> cache = getTodayCommentsCache();
			Serializable topicId = instance.getId();
			Pair<Serializable, List<CommentInstance>> pair = cache.getByKey(topicId);
			List<CommentInstance> listToCache = null;
			if (pair == null) {
				listToCache = new LinkedList<>();
			} else {
				listToCache = pair.getSecond();
			}
			int addedCount = 0;
			if (singleComment != null) {
				listToCache.add(singleComment);
				addedCount++;
			}
			// maybe we need to sort the comments before putting them into the cache
			// this should be relatively fast as most of the elements are sorted all of the time
			// TODO: probably we should consider using a tree set
			Collections.sort(listToCache);

			newSize = listToCache.size();

			// check if we have out of date comments list if so we need to add all missing ones
			if (currentCount <= (newSize - addedCount)) {
				int diff = newSize - currentCount;
				// this should add all missing comments at the end of the list
				instance.getComments().addAll(listToCache.subList(newSize - diff, newSize));
			}

			cache.setValue(topicId, listToCache);
		} finally {
			// save the current comment size for the day for later check when merging
			instance.getProperties().put(ForumProperties.TODAY_COMMENT_COUNT, newSize);
			instance.getTopicLock().unlock();
		}
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Comments sync completed in " + tracker.stopInSeconds() + " s");
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public CommentInstance createComment(String content) {
		CommentInstance instance = new CommentInstance();
		instance.setComment(content);
		instance.setFrom(authenticationServiceInst.get().getCurrentUserId());
		instance.setPostedDate(new Date());
		// instance.setIdentifier(generateCommentId());
		instance.setId(SequenceEntityGenerator.generateId());
		return instance;
	}

	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public void postComment(TopicInstance topic, CommentInstance commentInstance) {
		TimeTracker tracker = null;
		if (LOGGER.isDebugEnabled()) {
			tracker = new TimeTracker().begin();
		}
		commentInstance.setTopic(topic);
		commentInstance.setSubSectionId(topic.getTopicAbout().getIdentifier());
		saveCommentInternal(topic, commentInstance);

		syncTodayCache(topic, commentInstance);
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Comment post completed in " + tracker.stopInSeconds() + " s");
		}
	}

	/**
	 * Save comment internal.
	 * 
	 * @param instance
	 *            the instance
	 * @param commentInstance
	 *            the comment instance
	 */
	private void saveCommentInternal(TopicInstance instance, CommentInstance commentInstance) {
		CommentEntity entity = dozerMapper.getMapper().map(commentInstance, CommentEntity.class);
		SequenceEntityGenerator.generateStringId(entity, false);
		CommentEntity saved = dbDao.saveOrUpdate(entity);
		commentInstance.setId(saved.getId());
		// if we have some properties at all
		if (!commentInstance.getProperties().isEmpty()) {
			setCustomPropertiesSaving(true);
			try {
				commentInstance.setTopic(null);
				propertiesService.saveProperties(commentInstance);
				commentInstance.setTopic(instance);
			} finally {
				setCustomPropertiesSaving(false);
			}
		}
	}

	/**
	 * Sets the custom properties saving.
	 * 
	 * @param enable
	 *            the new custom properties saving
	 */
	private void setCustomPropertiesSaving(boolean enable) {
		if (enable) {
			RuntimeConfiguration
					.setConfiguration(
							RuntimeConfigurationProperties.SAVE_PROPERTIES_WITHOUT_DEFINITION,
							Boolean.TRUE);
		} else {
			RuntimeConfiguration
					.clearConfiguration(RuntimeConfigurationProperties.SAVE_PROPERTIES_WITHOUT_DEFINITION);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public TopicInstance loadComments(TopicInstance topic, DateRange range) {
		if (DateRangeUtil.isToday(range)) {
			// if we a refreshing comments for today only no need to execute queries synch with
			// cache only
			syncTodayCache(topic, null);
			return topic;
		}
		TimeTracker tracker = null;
		if (LOGGER.isDebugEnabled()) {
			tracker = new TimeTracker().begin();
		}
		// mark the topic as expanded
		topic.getProperties().put(ForumProperties.IS_TOPIC_EXPANDED, Boolean.TRUE);

		// ensure we have todays comments before we load all of them
		// otherwise if we load for a week then we will have duplicate comments for the day
		if (topic.getComments().isEmpty()) {
			syncTodayCache(topic, null);
		}
		Date endDate = range.getSecond();
		if (!topic.getComments().isEmpty()) {
			CommentInstance instance = topic.getComments().get(0);
			Date postedDate = instance.getPostedDate();
			// if the post date of the top comment is in the range for loading we will load only
			// comments that are before that time, no need to load the comments that are between top
			// comment post date and the end of the given range
			if (range.isInRange(postedDate)) {
				endDate = postedDate;
			}
		}
		range.setSecond(endDate);

		List<CommentInstance> comments = loadCommentsInternal(topic.getId(), range);
		if (topic.getComments().isEmpty()) {
			topic.setComments(comments);
		} else {
			topic.getComments().addAll(0, comments);
			Collections.sort(topic.getComments());
		}
		// we still need to sync comments is someone posted meanwhile
		syncTodayCache(topic, null);
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Comments load completed in " + tracker.stopInSeconds() + " s");
		}
		return topic;
	}

	/**
	 * Load comments internal.
	 * 
	 * @param key
	 *            the key
	 * @param today
	 *            the today
	 * @return the list
	 */
	private List<CommentInstance> loadCommentsInternal(Serializable key, DateRange today) {
		LOGGER.debug("Loading comments for topic=" + key + " in " + today);
		List<Pair<String, Object>> args = new ArrayList<>(3);
		args.add(new Pair<String, Object>("topicId", key));
		args.add(new Triplet<String, Object, TemporalType>("start", today.getFirst(),
				TemporalType.TIMESTAMP));
		args.add(new Triplet<String, Object, TemporalType>("end", today.getSecond(),
				TemporalType.TIMESTAMP));
		List<CommentEntity> list = dbDao.fetchWithNamed(EmfQueries.QUERY_COMMENTS_KEY, args);
		if (list.isEmpty()) {
			return new LinkedList<>();
		}

		Mapper mapper = dozerMapper.getMapper();
		List<CommentInstance> comments = new LinkedList<>();
		for (CommentEntity commentEntity : list) {
			comments.add(mapper.map(commentEntity, ChatInstance.class));
		}
		propertiesService.loadProperties(comments);
		return comments;
	}

	/**
	 * Creates the default value identifier from the topic about object in the format:
	 * referenceTypeName-referenceId.
	 * 
	 * @param ref
	 *            the ref
	 * @return the generated identifier
	 */
	private String createDefaultValueIdentifier(InstanceReference ref) {
		return ref.getIdentifier();
	}

	/**
	 * Convert to link source.
	 * 
	 * @param instance
	 *            the instance
	 * @return the link source id
	 */
	private LinkSourceId convertToLinkSource(Instance instance) {
		return (LinkSourceId) instance.toReference();
	}

	/**
	 * Entity lookup cache callback for fetching and storing a comments for the current day
	 * 
	 * @author BBonev
	 */
	public class TodayCommentsCacheCallback extends
			EntityLookupCallbackDAOAdaptor<String, List<CommentInstance>, Serializable> {

		@Override
		public Pair<String, List<CommentInstance>> findByKey(String key) {
			List<CommentInstance> comments = loadCommentsInternal(key, DateRangeUtil.getToday());
			if (comments.isEmpty()) {
				return null;
			}
			return new Pair<>(key, comments);
		}

		@Override
		public Pair<String, List<CommentInstance>> createValue(List<CommentInstance> value) {
			throw new UnsupportedOperationException("Comments cannot be created via this cache!");
		}
	}

	/**
	 * Entity lookup cache callback for loading and creating topic entities
	 * 
	 * @author BBonev
	 */
	public class TopicEntityCacheCallback extends
			EntityLookupCallbackDAOAdaptor<String, TopicEntity, Pair<String, InstanceReference>> {

		@Override
		public Pair<String, InstanceReference> getValueKey(TopicEntity value) {
			if ((value == null) || (value.getTopicAbout() == null)) {
				return null;
			}
			if (StringUtils.isNullOrEmpty(value.getIdentifier())) {
				value.setIdentifier(createDefaultValueIdentifier(value.getTopicAbout()));
			}
			return new Pair<String, InstanceReference>(value.getIdentifier(), value.getTopicAbout());
		}

		@Override
		public Pair<String, TopicEntity> findByKey(String key) {
			List<Pair<String, Object>> args = new ArrayList<>(1);
			args.add(new Pair<String, Object>("identifier", key));
			List<Object> list = dbDao.fetchWithNamed(EmfQueries.QUERY_TOPIC_BY_ID_KEY, args);
			if (list.isEmpty()) {
				return null;
			}
			if (list.size() > 1) {
				LOGGER.warn("Found more then one topic with ID=" + key);
			}
			return new Pair<>(key, (TopicEntity) list.get(0));
		}

		@Override
		public Pair<String, TopicEntity> findByValue(TopicEntity value) {
			Pair<String, InstanceReference> valueKey = getValueKey(value);
			if (valueKey == null) {
				return null;
			}
			List<Pair<String, Object>> args = new ArrayList<>(3);
			args.add(new Pair<String, Object>("identifier", valueKey.getFirst()));
			args.add(new Pair<String, Object>("aboutId", valueKey.getSecond().getIdentifier()));
			args.add(new Pair<String, Object>("aboutType", valueKey.getSecond().getReferenceType()
					.getId()));
			List<TopicEntity> list = dbDao
					.fetchWithNamed(EmfQueries.QUERY_TOPIC_BY_ABOUT_KEY, args);
			if (list.isEmpty()) {
				return null;
			}
			if (list.size() > 1) {
				LOGGER.warn("Found more then one topic with ID=" + valueKey.getFirst());
			}
			return new Pair<>(valueKey.getFirst(), list.get(0));
		}

		@Override
		public Pair<String, TopicEntity> createValue(TopicEntity value) {
			SequenceEntityGenerator.generateStringId(value, false);
			TopicEntity persisted = dbDao.saveOrUpdate(value);
			return new Pair<>(persisted.getIdentifier(), persisted);
		}
	}

}
