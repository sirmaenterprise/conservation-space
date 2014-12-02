package com.sirma.itt.emf.forum;

import java.io.Serializable;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;

import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.emf.cache.lookup.EntityLookupCache;
import com.sirma.itt.emf.cache.lookup.EntityLookupCache.EntityLookupCallbackDAOAdaptor;
import com.sirma.itt.emf.cache.lookup.EntityLookupCacheContext;
import com.sirma.itt.emf.db.SequenceEntityGenerator;
import com.sirma.itt.emf.definition.model.GenericDefinition;
import com.sirma.itt.emf.domain.Pair;
import com.sirma.itt.emf.domain.model.DefinitionModel;
import com.sirma.itt.emf.domain.model.Entity;
import com.sirma.itt.emf.event.EventService;
import com.sirma.itt.emf.forum.event.CommentUpdatedEvent;
import com.sirma.itt.emf.forum.model.CommentInstance;
import com.sirma.itt.emf.forum.model.TopicInstance;
import com.sirma.itt.emf.instance.PropertiesUtil;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.instance.model.InstanceReference;
import com.sirma.itt.emf.properties.DefaultProperties;
import com.sirma.itt.emf.search.model.Sorter;
import com.sirma.itt.emf.security.AuthenticationService;
import com.sirma.itt.emf.security.SecurityUtil;
import com.sirma.itt.emf.serialization.SerializationUtil;
import com.sirma.itt.emf.state.operation.Operation;
import com.sirma.itt.emf.state.operation.event.OperationExecutedEvent;

/**
 * Base implementation of the topic/comment service. The implementation defines an algorithm for
 * working with topic and comment instances using 3 caches. The implementation classes should
 * provide cache configurations and implement the abstract persistence methods. REVIEW Full of
 * commented code (consider removing it)
 * 
 * @author BBonev
 */
public abstract class AbstractCommentService implements CommentService, Serializable {

	private static final long serialVersionUID = 8659928127808748111L;

	/** The Constant DELETE. */
	private static final Operation DELETE = new Operation("delete");

	/** The Constant CREATE_TOPIC. */
	private static final Operation CREATE_TOPIC = new Operation("createTopic");

	/** The Constant FORBIDDEN_PROPERTIES. */
	private Set<String> FORBIDDEN_PROPERTIES;

	/** The context. */
	@Inject
	private EntityLookupCacheContext context;

	/** The event service. */
	@Inject
	private EventService eventService;

	@Inject
	private javax.enterprise.inject.Instance<AuthenticationService> authenticationService;

	/**
	 * Initialize the cache configurations on bean creation.
	 */
	@PostConstruct
	public void initialize() {
		FORBIDDEN_PROPERTIES = new LinkedHashSet<>(50);
		FORBIDDEN_PROPERTIES.addAll(getForbiddenProperties());

		if (!context.containsCache(getTopicByIdCacheName())) {
			context.createCache(getTopicByIdCacheName(), new TopicByIdLookup());
		}
		if (!context.containsCache(getTopicIdByCommentCacheName())) {
			context.createCache(getTopicIdByCommentCacheName(), new TopicIdByCommentLookup());
		}
		if (!context.containsCache(getTopicIdByTargetCacheName())) {
			context.createCache(getTopicIdByTargetCacheName(), new TopicIdByTargetLookup());
		}
	}

	/**
	 * Gets the forbidden properties for the concrete implementation.
	 * 
	 * @return the forbidden properties
	 */
	protected abstract Set<String> getForbiddenProperties();

	/**
	 * Gets the topic id by topic about cache name. The cache is going to store a set of topic db
	 * ids by instance reference as a key. The key is the reference to an instance on witch is
	 * posted the topic. Multiple topics could be posted on a single instance.
	 * 
	 * @return the topic id by topic about cache name
	 */
	protected abstract String getTopicIdByTargetCacheName();

	/**
	 * Gets the topic id by comment cache name. The cache is going to store a back reference for
	 * each comment what is the topic db id of each comment. The cache will have low memory
	 * footprint but very high number of entries. Optimal number is around >=100K.
	 * 
	 * @return the topic id by comment cache name
	 */
	protected abstract String getTopicIdByCommentCacheName();

	/**
	 * Gets the topic by id cache name. The cache is going to store mapping between topic db id and
	 * the actual topic instance with all of it's comments. From all caches for comments this will
	 * have the bigger memory footprint on a single entry.
	 * 
	 * @return the topic by id cache name
	 */
	protected abstract String getTopicByIdCacheName();

	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public Class<GenericDefinition> getInstanceDefinitionClass() {
		return GenericDefinition.class;
	}

	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public CommentInstance createInstance(GenericDefinition definition, Instance parent) {
		return createInstance(definition, parent, null);
	}

	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public CommentInstance createInstance(GenericDefinition definition, Instance parent,
			Operation operation) {
		CommentInstance commentInstance = new CommentInstance();
		SequenceEntityGenerator.generateStringId(commentInstance, false);
		if (parent instanceof TopicInstance) {
			((TopicInstance) parent).getComments().add(commentInstance);
			commentInstance.setTopic((TopicInstance) parent);
		} else {
			TopicInstance topicInstance = new TopicInstance();
			SequenceEntityGenerator.generateStringId(topicInstance, false);
			topicInstance.getComments().add(commentInstance);
			topicInstance.initBidirection();
			topicInstance.setTopicAbout(parent.toReference());
		}
		return commentInstance;
	}

	@Override
	public CommentInstance cancel(CommentInstance instance) {
		// not going to be implemented
		return null;
	}

	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public void refresh(CommentInstance instance) {
		if ((instance == null) || (instance.getId() == null)) {
			return;
		}
		if (instance instanceof TopicInstance) {
			TopicInstance topic = (TopicInstance) instance;
			topic.getComments().clear();
			EntityLookupCache<Serializable, TopicInstance, Serializable> cache = getTopicByIdCache();
			Pair<Serializable, TopicInstance> pair = cache.getByKey(topic.getId());
			if (pair != null) {
				TopicInstance cached = pair.getSecond();
				for (CommentInstance commentInstance : cached.getComments()) {
					topic.getComments().add(createCopy(commentInstance));
				}
				topic.getProperties().clear();
				topic.getProperties()
						.putAll(PropertiesUtil.cloneProperties(cached.getProperties()));
				topic.setImageAnnotation(cached.getImageAnnotation());
				topic.setPostedDate(cached.getPostedDate());
				topic.setTopicAbout(cached.getTopicAbout());
				topic.setSubSectionId(cached.getSubSectionId());
			}
		}
	}

	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public List<CommentInstance> loadInstances(Instance owner) {
		if (owner == null) {
			return Collections.emptyList();
		}
		return castToCommentList(getTopics(owner.toReference(), null, -1, true, null, null));
	}

	/**
	 * Cast to comment list.
	 * 
	 * @param list
	 *            the list
	 * @return the list
	 */
	@SuppressWarnings("unchecked")
	private List<CommentInstance> castToCommentList(List<?> list) {
		return (List<CommentInstance>) list;
	}

	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public CommentInstance loadByDbId(Serializable id) {
		Pair<Serializable, TopicInstance> pair = getTopicByIdCache().getByKey(id);
		if (pair != null) {
			if (pair.getSecond().getId().equals(id)) {
				return createCopy(pair.getSecond());
			}
			return createCopy(findCommentInTopic(pair.getSecond(), id));
		}
		return null;
	}

	@Override
	public CommentInstance load(Serializable instanceId) {
		return null;
	}

	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public <S extends Serializable> List<CommentInstance> load(List<S> ids) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public <S extends Serializable> List<CommentInstance> loadByDbId(List<S> ids) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public <S extends Serializable> List<CommentInstance> load(List<S> ids, boolean allProperties) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public <S extends Serializable> List<CommentInstance> loadByDbId(List<S> ids,
			boolean allProperties) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public Map<String, List<DefinitionModel>> getAllowedChildren(CommentInstance owner) {
		return Collections.emptyMap();
	}

	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public List<DefinitionModel> getAllowedChildren(CommentInstance owner, String type) {
		return Collections.emptyList();
	}

	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public boolean isChildAllowed(CommentInstance owner, String type) {
		return false;
	}

	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public CommentInstance clone(CommentInstance instance, Operation operation) {
		return null;
	}

	@Override
	public void delete(CommentInstance instance, Operation operation, boolean permanent) {
		if (instance instanceof TopicInstance) {
			// remove all cache information for the given topic
			getTopicByIdCache().removeByKey(instance.getId());
			// no comments in the cache so nothing to remove

			// List<CommentInstance> comments = ((TopicInstance) instance).getComments();
			// EntityLookupCache<Serializable, Serializable, Serializable> topicIdByCommentCache =
			// getTopicIdByCommentCache();
			// for (CommentInstance commentInstance : comments) {
			// topicIdByCommentCache.removeByKey(commentInstance.getId());
			// }
			EntityLookupCache<Serializable, Set<Serializable>, Serializable> targetCache = getTopicIdByTargetCache();
			if (instance.getSubSectionId() != null) {
				targetCache.removeByKey(instance.getSubSectionId());
			}
			InstanceReference topicAbout = ((TopicInstance) instance).getTopicAbout();
			if (topicAbout != null) {
				targetCache.removeByKey(topicAbout);
			}
		} else {
			// remove the cached entry
			getTopicIdByCommentCache().removeByKey(instance.getId());
			// currently is not need to update any cache because nothing is stored there

			// TopicInstance topic = instance.getTopic();
			// if (topic != null) {
			// Pair<Serializable, TopicInstance> pair = getTopicByIdCache()
			// .getByKey(topic.getId());
			// if (pair != null) {
			// // remove the comment from the cached topic instance and update the cache
			// // NOTE: this section is not thread save and someone else could modify the list
			// // of comments in other thread
			// // TODO: add modification locking to comments modifications
			// TopicInstance topicInstance = pair.getSecond();
			// for (Iterator<CommentInstance> it = topicInstance.getComments().iterator(); it
			// .hasNext();) {
			// CommentInstance comment = it.next();
			// if (comment.getId().equals(instance.getId())) {
			// it.remove();
			// break;
			// }
			// }
			// getTopicByIdCache().setValue(topicInstance.getId(), topicInstance);
			// }
			// }
		}
		deleteEntry(instance);
	}

	/**
	 * Delete entry (topic or comment) from the underlying storage.
	 * 
	 * @param instance
	 *            the instance to delete
	 */
	protected abstract void deleteEntry(CommentInstance instance);

	@Override
	public void attach(CommentInstance targetInstance, Operation operation, Instance... children) {
		if ((children == null) || (children.length == 0)) {
			return;
		}
		if (targetInstance instanceof TopicInstance) {
			List<CommentInstance> comments = ((TopicInstance) targetInstance).getComments();
			for (Instance instance : children) {
				if (instance instanceof CommentInstance) {
					CommentInstance commentInstance = createCopy((CommentInstance) instance);
					comments.add(commentInstance);
					commentInstance.setTopic((TopicInstance) targetInstance);
					save(commentInstance, null);
					// optionally fire an event for comment attachment
				}
			}
		}
	}

	@Override
	public void detach(CommentInstance sourceInstance, Operation operation, Instance... instances) {
		// nothing to do
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public List<TopicInstance> getTopics(InstanceReference reference, Date lastKnownDate,
			int limit, boolean includeComments, Sorter sort, Map<String, Serializable> filterParams) {
		if ((reference == null) || (reference.getIdentifier() == null)) {
			return Collections.emptyList();
		}
		return getTopicsByTargetKey(reference, lastKnownDate, limit, includeComments, sort,
				filterParams);
	}

	/**
	 * Gets the topics by target key.
	 * 
	 * @param reference
	 *            the reference
	 * @param lastKnownDate
	 *            the last known date
	 * @param limit
	 *            the limit
	 * @param includeComments
	 *            the include comments
	 * @param sorter
	 *            the sorter
	 * @param filterParams
	 *            the filter parameters
	 * @return the topics by target key
	 */
	private List<TopicInstance> getTopicsByTargetKey(Serializable reference, Date lastKnownDate,
			int limit, boolean includeComments, Sorter sorter,
			Map<String, Serializable> filterParams) {
		List<TopicInstance> topics = Collections.emptyList();
		
		if (reference instanceof String) {
			topics = loadTopicBySubSectionId((String) reference, lastKnownDate, limit, sorter,
					filterParams);
		} else if (reference instanceof InstanceReference) {
			topics = loadTopicByTopicAbout((InstanceReference) reference, lastKnownDate, limit,
					sorter, filterParams);
		} else {
			// invalid key nothing to do
			return Collections.emptyList();
		}

		// update the cache with the fetched topics
		EntityLookupCache<Serializable, TopicInstance, Serializable> idCache = getTopicByIdCache();
		for (TopicInstance topicInstance : topics) {
			idCache.setValue(topicInstance.getId(), createCopy(topicInstance));
		}
		return topics;
	}

	/**
	 * Creates the copy. The method is used to create separate instance from that is in the cache
	 * and the one returned to the user.
	 * 
	 * @param <T>
	 *            the generic type
	 * @param instance
	 *            the second
	 * @return the t
	 */
	private <T> T createCopy(T instance) {
		return SerializationUtil.copy(instance);
	}

	@Override
	public List<TopicInstance> getTopics(String subSectionIdentifier, Date lastKnownDate,
			int limit, boolean includeComments, Sorter sort, Map<String, Serializable> filterParams) {
		if (StringUtils.isNullOrEmpty(subSectionIdentifier)) {
			return Collections.emptyList();
		}
		return getTopicsByTargetKey(subSectionIdentifier, lastKnownDate, limit, includeComments,
				sort, filterParams);
	}

	@Override
	public CommentInstance save(CommentInstance instance, Operation operation) {
		if (instance == null) {
			return null;
		}
		boolean isNew = generateId(instance);
		if (instance instanceof TopicInstance) {
			TopicInstance old = null;
			TopicInstance topic = (TopicInstance) instance;
			if (isNew) {
				notifyForOperation(instance, CREATE_TOPIC);
			} else {
				Pair<Serializable, TopicInstance> byKey = getTopicByIdCache().getByKey(
						topic.getId());
				if (byKey != null) {
					old = byKey.getSecond();
				} else {
					old = createCopy(topic);
				}
			}
			notifyForOperation(instance, operation);

			topicUpdated(instance);

			eventService.fire(new CommentUpdatedEvent(instance, old));

			// add passing the old topic instance if edit
			persistTopic(topic, old);
			updateCacheForTopic(topic);
		} else {
			CommentInstance current = null;
			if (!isNew) {
				current = getCommentByDbId(instance.getId());
			}
			// TODO: add passing the old comment instance if edit

			// backup the actual topic - we assume is fully functional
			TopicInstance topic = instance.getTopic();
			CommentInstance commentCopy = createCopy(current);
			if (current != null) {
				// update the properties of the fetched instance with the one that are new and
				// override all
				PropertiesUtil.mergeProperties(instance.getProperties(), current.getProperties(),
						true);
				// update the topic instance - it was a dummy one and set the actual topic
				current.setTopic(topic);
				commentCopy.setTopic(topic);

				eventService.fire(new CommentUpdatedEvent(current, commentCopy));

				// persist changes to db
				persistComment(current, commentCopy);

				// copy the properties that are fetched from DB without overriding the incomming
				// changes
				PropertiesUtil.mergeProperties(current.getProperties(), instance.getProperties(),
						false);
			} else {
				eventService.fire(new CommentUpdatedEvent(instance, null));
				// added new comment and not updated old one
				persistComment(instance, null);
			}

			// update the topic as modified and save it
			TopicInstance copy = createCopy(topic);
			topicUpdated(topic);
			persistTopic(topic, copy);
			updateCacheForTopic(topic);
		}
		return instance;
	}

	/**
	 * Gets the comment by db id.
	 * 
	 * @param id
	 *            the id
	 * @return the comment by db id
	 */
	protected abstract CommentInstance getCommentByDbId(Serializable id);

	/**
	 * Topic updated.
	 * 
	 * @param instance
	 *            the instance
	 */
	protected void topicUpdated(CommentInstance instance) {
		instance.getProperties().put(DefaultProperties.MODIFIED_ON, new Date());
		SecurityUtil.setCurrentUserTo(instance, DefaultProperties.MODIFIED_BY,
				authenticationService);
	}

	/**
	 * Notify for operation.
	 * 
	 * @param instance
	 *            the instance
	 * @param operation
	 *            the operation
	 */
	protected void notifyForOperation(CommentInstance instance, Operation operation) {
		eventService.fire(new OperationExecutedEvent(operation, instance));
	}

	/**
	 * Persist topic.
	 * 
	 * @param instance
	 *            the instance
	 * @param oldInstance
	 *            topic instance if we are updating the current instance. If <code>null</code> then
	 *            we have new topic to add.
	 */
	protected abstract void persistTopic(TopicInstance instance, TopicInstance oldInstance);

	/**
	 * Persist comment.
	 * 
	 * @param instance
	 *            the instance
	 * @param oldInstance
	 *            comment instance if we are updating the current instance. If <code>null</code>
	 *            then we have new comment to add.
	 */
	protected abstract void persistComment(CommentInstance instance, CommentInstance oldInstance);

	@Override
	public void postComment(TopicInstance topic, CommentInstance commentInstance) {
		if (generateId(topic)) {
			notifyForOperation(topic, CREATE_TOPIC);
			topicUpdated(topic);
			persistTopic(topic, null);
		} else {
			// create a separate topic instance that we will use for semantic diff
			TopicInstance copy = createCopy(topic);
			topicUpdated(topic);
			persistTopic(topic, copy);
		}
		if (generateId(commentInstance)) {
			commentInstance.setTopic(topic);
			topic.getComments().add(commentInstance);
			// sort the comments by post date if they are posted
			// asynchronously could be out of order
			Collections.sort(topic.getComments());
			persistComment(commentInstance, null);
			onNewComment(commentInstance);

			initializeCommentsToTopicCache(topic);
		}

		addTopicToTopicAboutCache(topic, getTopicIdByTargetCache());

		updateCacheForTopic(topic);
	}

	/**
	 * Update the given topic instance in the cache. Perform cache merge if needed.
	 * 
	 * @param topic
	 *            the topic
	 */
	private void updateCacheForTopic(TopicInstance topic) {
		// remove non persistable properties
		topic.getProperties().keySet().removeAll(FORBIDDEN_PROPERTIES);
		for (CommentInstance comment : topic.getComments()) {
			comment.getProperties().keySet().removeAll(FORBIDDEN_PROPERTIES);
		}
		// clone the topic before save to cache
		// TODO: we could have a merge of the entry in the cache and the one here
		getTopicByIdCache().setValue(topic.getId(), createCopy(topic));
	}

	/**
	 * Generate id.
	 * 
	 * @param entity
	 *            the entity
	 * @return true, if successful
	 */
	private boolean generateId(Entity<Serializable> entity) {
		if (entity.getId() == null) {
			SequenceEntityGenerator.generateStringId(entity, false);
			return true;
		}
		return false;
	}

	@Override
	public void deleteById(Serializable id) {
		EntityLookupCache<Serializable, TopicInstance, Serializable> topicByIdCache = getTopicByIdCache();

		// until we know the type we first will check if the given id is a comment id if so we will
		// get the topic and delete the comment
		// this is in this order due to the fact that semantic implementation returns for comment id
		// returns topic when searched but not null (not found)
		Pair<Serializable, Serializable> comentToTopic = getTopicIdByCommentCache().getByKey(id);

		Pair<Serializable, TopicInstance> pair;
		if ((comentToTopic != null) && (comentToTopic.getSecond() != null)) {
			pair = topicByIdCache.getByKey(comentToTopic.getSecond());
			if ((pair != null) && (pair.getSecond() != null)) {
				CommentInstance comment = null;
				for (CommentInstance commentInstance : pair.getSecond().getComments()) {
					if (commentInstance.getId().equals(id)) {
						comment = commentInstance;
						break;
					}
				}
				if (comment != null) {
					// FIXME: add firing of proper events
					eventService.fire(new CommentUpdatedEvent(comment, null));
					delete(comment, DELETE, true);
					return;
				}
			}
		}
		pair = topicByIdCache.getByKey(id);
		if (pair != null && pair.getSecond() != null) {
			delete(pair.getSecond(), DELETE, true);
		}
	}

	/**
	 * Load comments after the given date for the given topic.
	 * 
	 * @param topicId
	 *            the topic id
	 * @param lastKnownDate
	 *            the last known date
	 * @param limit
	 *            the limit
	 * @return the list
	 */
	protected abstract List<CommentInstance> loadCommentsAfter(Serializable topicId,
			Date lastKnownDate, int limit);

	/**
	 * Load comments before the given date for the given topic.
	 * 
	 * @param topicId
	 *            the topic id
	 * @param lastKnownDate
	 *            the last known date
	 * @param limit
	 *            the limit
	 * @return the list
	 */
	protected abstract List<CommentInstance> loadCommentsBefore(Serializable topicId,
			Date lastKnownDate, int limit);

	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public List<CommentInstance> loadComments(Serializable topicId, Date lastKnownDate, int limit) {
		Pair<Serializable, TopicInstance> pair = getTopicByIdCache().getByKey(topicId);
		if (pair != null) {
			List<CommentInstance> comments = loadCommentsBefore(topicId, lastKnownDate, limit);
			TopicInstance topicInstance = createCopy(pair.getSecond());
			topicInstance.setComments(comments);
			topicInstance.initBidirection();
			// update the cache mapping for the loaded comments
			initializeCommentsToTopicCache(topicInstance);
			return comments;
		}
		return Collections.emptyList();
	}

	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public List<CommentInstance> postComment(Serializable topicId, Date lastCommentDate,
			CommentInstance commentInstance) {
		EntityLookupCache<Serializable, TopicInstance, Serializable> cache = getTopicByIdCache();
		Pair<Serializable, TopicInstance> pair = cache.getByKey(topicId);
		if (pair != null) {
			// get a copy of the topic in the cache
			TopicInstance instance = createCopy(pair.getSecond());
			List<CommentInstance> loadComments = loadCommentsAfter(topicId, lastCommentDate, -1);
			if (loadComments.isEmpty()) {
				loadComments = new LinkedList<>();
			}
			instance.setComments(loadComments);
			postComment(instance, commentInstance);
			return instance.getComments();
		}
		return Collections.emptyList();
	}

	/**
	 * On new topic.
	 * 
	 * @param topicInstance
	 *            the topic instance
	 */
	protected void onNewTopic(TopicInstance topicInstance) {

	}

	/**
	 * On new comment.
	 * 
	 * @param commentInstance
	 *            the comment instance
	 */
	protected void onNewComment(CommentInstance commentInstance) {

	}

	/**
	 * On comment delete.
	 * 
	 * @param commentInstance
	 *            the comment instance
	 */
	protected void onCommentDelete(CommentInstance commentInstance) {

	}

	/**
	 * On topic delete.
	 * 
	 * @param topicInstance
	 *            the topic instance
	 */
	protected void onTopicDelete(TopicInstance topicInstance) {

	}

	/**
	 * Returns a cache instance. The cache is structured: key=topic db id, value=TopicInstance
	 * 
	 * @return the topic by id cache
	 */
	protected EntityLookupCache<Serializable, TopicInstance, Serializable> getTopicByIdCache() {
		return context.getCache(getTopicByIdCacheName());
	}

	/**
	 * Gets the topic id by comment cache.
	 * 
	 * @return the topic id by comment cache
	 */
	protected EntityLookupCache<Serializable, Serializable, Serializable> getTopicIdByCommentCache() {
		return context.getCache(getTopicIdByCommentCacheName());
	}

	/**
	 * Gets the topic id by topic about cache.
	 * 
	 * @return the topic id by topic about cache
	 */
	protected EntityLookupCache<Serializable, Set<Serializable>, Serializable> getTopicIdByTargetCache() {
		return context.getCache(getTopicIdByTargetCacheName());
	}

	/**
	 * Load topic by topics` URIs.
	 * 
	 * @param key
	 *            the key
	 * @return the topic instance
	 */
	protected abstract List<TopicInstance> loadTopicByDbId(Serializable key);

	/**
	 * Load topic by comment URI.
	 * 
	 * @param key
	 *            the comment URI
	 * @return the list
	 */
	protected abstract TopicInstance loadTopicByCommentDbId(Serializable key);

	/**
	 * Load topic by Instance Reference of the commented object.
	 * 
	 * @param key
	 *            the Instance Reference of the commented object
	 * @param lastKnownDate
	 *            the last known date
	 * @param limit
	 *            the limit
	 * @param sorter
	 *            the sorter
	 * @param filterParams
	 *            the filter params
	 * @return the topic instance
	 */
	protected abstract List<TopicInstance> loadTopicByTopicAbout(InstanceReference key,
			Date lastKnownDate, int limit, Sorter sorter, Map<String, Serializable> filterParams);

	/**
	 * Load topic by URI of sub section of the commented object.
	 * 
	 * @param identifier
	 *            the URI of the sub section
	 * @param lastKnownDate
	 *            the last known date
	 * @param limit
	 *            the limit
	 * @param sorter
	 *            the sorter
	 * @param filterParams
	 *            the filter params
	 * @return the topic instance
	 */
	protected abstract List<TopicInstance> loadTopicBySubSectionId(String identifier,
			Date lastKnownDate, int limit, Sorter sorter, Map<String, Serializable> filterParams);

	/**
	 * Initialize comments to topic cache.
	 * 
	 * @param instance
	 *            the instance
	 */
	protected void initializeCommentsToTopicCache(TopicInstance instance) {
		if ((instance == null) || instance.getComments().isEmpty()) {
			return;
		}
		EntityLookupCache<Serializable, Serializable, Serializable> cache = getTopicIdByCommentCache();
		for (CommentInstance commentInstance : instance.getComments()) {
			cache.setValue(commentInstance.getId(), instance.getId());
		}
	}

	/**
	 * Adds the topic to topic about cache.
	 * 
	 * @param instance
	 *            the instance
	 * @param topicAboutCache
	 *            the topic about cache
	 */
	protected void addTopicToTopicAboutCache(TopicInstance instance,
			EntityLookupCache<Serializable, Set<Serializable>, Serializable> topicAboutCache) {
		// TODO disabled cache
		// addTopicToTargetCache(instance.getTopicAbout(), instance.getId(), topicAboutCache);
		// addTopicToTargetCache(instance.getSubSectionId(), instance.getId(), topicAboutCache);
	}

	/**
	 * Find comment in topic.
	 * 
	 * @param topicInstance
	 *            the topic instance
	 * @param key
	 *            the key
	 * @return the object
	 */
	protected CommentInstance findCommentInTopic(TopicInstance topicInstance, Serializable key) {
		for (CommentInstance instance : topicInstance.getComments()) {
			if (instance.getId().equals(key)) {
				return instance;
			}
		}
		return null;
	}

	/**
	 * Combined cache that stores as a set of topic ids by different identifiers. The supported keys
	 * types are {@link InstanceReference} and String. The {@link InstanceReference} if for topic
	 * about field of the topic. The String key is not sub section identifiers.
	 * 
	 * @author BBonev
	 */
	public class TopicIdByTargetLookup extends
			EntityLookupCallbackDAOAdaptor<Serializable, Set<Serializable>, Serializable> {

		@Override
		public Pair<Serializable, Set<Serializable>> findByKey(Serializable key) {
			return null;
		}

		@Override
		public Pair<Serializable, Set<Serializable>> createValue(Set<Serializable> value) {
			throw new UnsupportedOperationException("Only keys nothing to create here.");
		}
	}

	/**
	 * The Class TopicIdByCommentLookup.
	 * 
	 * @author BBonev
	 */
	public class TopicIdByCommentLookup extends
			EntityLookupCallbackDAOAdaptor<Serializable, Serializable, Serializable> {

		@Override
		public Pair<Serializable, Serializable> findByKey(Serializable key) {
			CommentInstance instance = getCommentByDbId(key);
			if (instance == null) {
				return null;
			}
			if ((instance.getTopic() != null) && (instance.getTopic().getId() != null)) {
				return new Pair<>(key, instance.getTopic().getId());
			}
			// if reached here something is very wrong with the topic search
			return null;
		}

		@Override
		public Pair<Serializable, Serializable> createValue(Serializable value) {
			throw new UnsupportedOperationException("Only keys nothing to create here.");
		}
	}

	/**
	 * The Class TopicByIdLookup.
	 * 
	 * @author BBonev
	 */
	public class TopicByIdLookup extends
			EntityLookupCallbackDAOAdaptor<Serializable, TopicInstance, Serializable> {

		@Override
		public Pair<Serializable, TopicInstance> findByKey(Serializable key) {
			List<TopicInstance> list = loadTopicByDbId(key);
			EntityLookupCache<Serializable, Set<Serializable>, Serializable> topicAboutCache = getTopicIdByTargetCache();
			for (TopicInstance instance : list) {
				addTopicToTopicAboutCache(instance, topicAboutCache);
				initializeCommentsToTopicCache(instance);
				if (instance.getId().equals(key)) {
					return new Pair<>(key, instance);
				} else if (findCommentInTopic(instance, key) != null) {
					// the provided id was to an comment so return the actual id
					return new Pair<>(instance.getId(), instance);
				}
			}
			// not found
			return null;
		}

		@Override
		public Pair<Serializable, TopicInstance> createValue(TopicInstance value) {
			throw new UnsupportedOperationException("Topic creation is external operation");
		}
	}

}
