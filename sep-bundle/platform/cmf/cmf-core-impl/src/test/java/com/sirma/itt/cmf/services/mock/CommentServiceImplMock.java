package com.sirma.itt.cmf.services.mock;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;

import org.apache.log4j.Logger;

import com.sirma.itt.emf.cache.CacheConfiguration;
import com.sirma.itt.emf.cache.Eviction;
import com.sirma.itt.emf.cache.Expiration;
import com.sirma.itt.emf.db.SequenceEntityGenerator;
import com.sirma.itt.emf.forum.AbstractCommentService;
import com.sirma.itt.emf.forum.CommentService;
import com.sirma.itt.emf.forum.model.CommentInstance;
import com.sirma.itt.emf.forum.model.TopicInstance;
import com.sirma.itt.emf.instance.model.InstanceReference;
import com.sirma.itt.emf.search.model.Sorter;
import com.sirma.itt.emf.security.model.User;
import com.sirma.itt.emf.time.TimeTracker;
import com.sirma.itt.emf.util.Documentation;

/**
 * CommentService simple mock. TODO implement what is still needed - now basic save is only
 * implemented
 *
 * @author bbanchev
 */
@ApplicationScoped
public class CommentServiceImplMock extends AbstractCommentService {

	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = 6502878793761243439L;

	/** The Constant LOG. */
	private static final Logger LOG = Logger.getLogger(CommentService.class);

	/** The instances. */
	private static Map<Serializable, CommentInstance> instances = new HashMap<>();

	/** The to topic. */
	private static Map<TopicInstance, List<CommentInstance>> toTopic = new HashMap<>();

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

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected String getTopicByIdCacheName() {
		return SEMANTIC_TOPIC_ENTITY_BY_ID_CACHE;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void persistTopic(TopicInstance topic, TopicInstance oldInstance) {
		if (debug) {
			LOG.debug("CommentService.persistTopic topic start");
		}
		TopicInstance generateStringId = SequenceEntityGenerator.generateStringId(topic, false);
		instances.put(generateStringId.getId(), topic);
		if (topic.getImageAnnotation() != null) {
			SequenceEntityGenerator.generateStringId(topic.getImageAnnotation(), false);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void persistComment(CommentInstance comment, CommentInstance oldInstance) {
		TimeTracker tracker = null;
		if (debug) {
			tracker = new TimeTracker().begin();
			LOG.debug("CommentService.persistComment comment start");
		}
		TopicInstance topic = comment.getTopic();

		// generate id
		CommentInstance generateStringId = SequenceEntityGenerator.generateStringId(comment, false);
		instances.put(generateStringId.getId(), generateStringId);
		if (topic != null) {
			if (!toTopic.containsKey(topic)) {
				toTopic.put(topic, new LinkedList<CommentInstance>());
			}
			toTopic.get(topic).add(generateStringId);
		}
		if (debug) {
			LOG.debug("CommentService.persistComment comment in " + tracker.stopInSeconds() + " s");
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected List<TopicInstance> loadTopicByDbId(Serializable key) {
		return new ArrayList<>();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected TopicInstance loadTopicByCommentDbId(Serializable key) {
		return (TopicInstance) instances.get(key);

	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<TopicInstance> getTopicsByUser(User user) {
		return Collections.emptyList();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<TopicInstance> getInstanceSuccessorsTopics(String uri) {
		return Collections.emptyList();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void deleteEntry(CommentInstance instance) {
		instances.remove(instance.getId());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Set<String> getForbiddenProperties() {
		return Collections.emptySet();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<TopicInstance> getTopicsByUser(User user, Date lastKnownDate, int limit,
			Sorter sorter, Map<String, Serializable> filterParams) {
		return Collections.emptyList();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<TopicInstance> getInstanceSuccessorsTopics(String uri, Date lastKnownDate,
			int limit, Sorter sorter, Map<String, Serializable> filterParams) {
		return Collections.emptyList();
	}

	@Override
	protected List<CommentInstance> loadCommentsAfter(Serializable topicId, Date lastKnownDate,
			int limit) {
		return Collections.emptyList();
	}

	@Override
	protected List<CommentInstance> loadCommentsBefore(Serializable topicId, Date lastKnownDate,
			int limit) {
		return Collections.emptyList();
	}

	@Override
	protected List<TopicInstance> loadTopicByTopicAbout(InstanceReference key, Date lastKnownDate,
			int limit, Sorter sorter, Map<String, Serializable> filterParams) {
		return Collections.emptyList();
	}

	@Override
	protected List<TopicInstance> loadTopicBySubSectionId(String identifier, Date lastKnownDate,
			int limit, Sorter sorter, Map<String, Serializable> filterParams) {
		return Collections.emptyList();
	}

	@Override
	protected CommentInstance getCommentByDbId(Serializable id) {
		return null;
	}

}
