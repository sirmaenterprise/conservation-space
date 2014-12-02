package com.sirma.itt.emf.forum;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.sirma.itt.emf.definition.model.GenericDefinition;
import com.sirma.itt.emf.forum.model.CommentInstance;
import com.sirma.itt.emf.forum.model.TopicInstance;
import com.sirma.itt.emf.instance.dao.InstanceService;
import com.sirma.itt.emf.instance.model.InstanceReference;
import com.sirma.itt.emf.search.model.Sorter;
import com.sirma.itt.emf.security.model.User;
import com.sirma.itt.emf.state.operation.Operation;

/**
 * Service for managing a Topic comments functions. Creating new topics, adding comments, loading
 * comments etc. Currently comments can only be added to a topic and cannot exist on their own.
 *
 * @author BBonev
 */
public interface CommentService extends InstanceService<CommentInstance, GenericDefinition> {


	/**
	 * Gets all topics that belong to/are associated to a instance identified by the given
	 * reference.
	 * 
	 * @param reference
	 *            the target reference to fetch the topics to.
	 * @param lastKnownDate
	 *            the last known date from which the topics to be returned
	 * @param limit
	 *            the limit of the topics to be returned or -1 if all are required
	 * @param includeComments
	 *            if the service should load the comments for the returned topics.
	 * @param sort
	 *            the sort criteria to be applied when searching for topics. Could be
	 *            <code>null</code> for default behavior as sorting ascending by createdOn
	 * @param filterParams
	 *            to apply when searching topics
	 * @return the found topics for the given instance
	 */
	List<TopicInstance> getTopics(InstanceReference reference, Date lastKnownDate, int limit,
			boolean includeComments, Sorter sort, Map<String, Serializable> filterParams);

	/**
	 * Gets the topics by sub section identifier.
	 * 
	 * @param subSectionIdentifier
	 *            the sub section identifier
	 * @param lastKnownDate
	 *            the last known date from which the topics to be returned
	 * @param limit
	 *            the limit of the topics to be returned or -1 if all are required
	 * @param includeComments
	 *            if the service should load the comments for the returned topics.
	 * @param sort
	 *            the sort criteria to be applied when searching for topics. Could be
	 *            <code>null</code> for default behavior as sorting ascending by createdOn
	 * @param filterParams
	 *            to apply when searching topics
	 * @return the topics
	 */
	List<TopicInstance> getTopics(String subSectionIdentifier, Date lastKnownDate, int limit, boolean includeComments, Sorter sort, Map<String, Serializable> filterParams);

	/**
	 * Saves the given topic instance and any comments that have been added and are not saved, yet.
	 *
	 * @param instance
	 *            the instance
	 * @param operation
	 *            the operation
	 * @return the topic instance
	 */
	@Override
	CommentInstance save(CommentInstance instance, Operation operation);

	/**
	 * Post comment to the given topic. The comment is saved to DB and is added to the given topic
	 * instance at the end of the list of loaded comments.
	 *
	 * @param topic
	 *            the topic
	 * @param commentInstance
	 *            the comment instance
	 */
	void postComment(TopicInstance topic, CommentInstance commentInstance);

	/**
	 * Post comment to a topic identified by the given id. The comment is saved to DB and is added
	 * to the given topic instance at the end of the list of loaded comments.
	 *
	 * @param topicId
	 *            the topic id
	 * @param lastCommentDate
	 *            the last comment date
	 * @param commentInstance
	 *            the comment instance
	 * @return the list of current comments list for the given topic including the newly added
	 *         comment
	 */
	List<CommentInstance> postComment(Serializable topicId, Date lastCommentDate,
			CommentInstance commentInstance);

	/**
	 * Deletes comment or topic by id. The topic could be deleted only if there is not comments to
	 * it! This method is not very effective. If you now what type is the id use the
	 * {@link #delete(CommentInstance, Operation, boolean)}method by creating new comment/topic
	 * instance and set the id <code><pre>
	 * TopicInstance topicInstance = new TopicInstance();
	 * topicInstance.setId(id);
	 * commentService.delete(topicInstance, null, false);</pre></code> or <code><pre>
	 * TopicInstance topicInstance = new TopicInstance();
	 * topicInstance.setId(topicId);
	 * CommentInstance commentInstance = new CommentInstance();
	 * commentInstance.setId(commentId);
	 * commentInstance.setTopic(topicInstance);
	 * commentService.delete(commentInstance, null, false);</pre></code>
	 * 
	 * @param id
	 *            the id by witch to detele the comment/topic.
	 */
	void deleteById(Serializable id);

	/**
	 * Retrieve all topics in which the given {@link User} is involved by following criteria
	 * <ul>
	 * <li>all Topics, created by given user
	 * <li>all Topics, on which the current user has commented
	 * <li>all Topics, on instances the given user has created
	 * </ul>
	 * .
	 *
	 * @param user
	 *            the {@link User}
	 * @return all topics created by given {@link User}
	 * @deprecated use {@link #getTopicsByUser(User, Date, int, Sorter, Map)}
	 */
	@Deprecated
	List<TopicInstance> getTopicsByUser(User user);

	/**
	 * Retrieve all topics in which the given {@link User} is involved by following criteria
	 * <ul>
	 * <li>all Topics, created by given user
	 * <li>all Topics, on which the current user has commented
	 * <li>all Topics, on instances the given user has created
	 * </ul>
	 * .
	 * 
	 * @param user
	 *            the {@link User}
	 * @param lastKnownDate
	 *            the last known date
	 * @param limit
	 *            the limit
	 * @param sorter
	 *            the sort criteria to be applied when searching for topics. Could be
	 *            <code>null</code> for default behavior as sorting ascending by createdOn
	 * @param filterParams
	 *            to apply when searching for user topics
	 * @return all topics created by given {@link User}
	 */
	List<TopicInstance> getTopicsByUser(User user, Date lastKnownDate, int limit, Sorter sorter,
			Map<String, Serializable> filterParams);

	/**
	 * Retrieve all {@link TopicInstance}s on given {@link Instance} and and all
	 * {@link TopicInstance}s on its children.
	 * 
	 * @param uri
	 *            of the {@link Instance}
	 * @return all {@link TopicInstance}s made on given {@link Instance} and its children
	 * @deprecated use {@link #getInstanceSuccessorsTopics(String, Date, int, Sorter, Map)}
	 */
	@Deprecated
	List<TopicInstance> getInstanceSuccessorsTopics(String uri);

	/**
	 * Retrieve all {@link TopicInstance}s on given {@link Instance} and and all
	 * {@link TopicInstance}s on its children.
	 * 
	 * @param uri
	 *            of the {@link Instance}
	 * @param lastKnownDate
	 *            the last known date
	 * @param limit
	 *            the limit
	 * @param sorter
	 *            the sort criteria to be applied when searching for topics. Could be
	 *            <code>null</code> for default behavior as sorting ascending by createdOn
	 * @param filterParams
	 *            to apply when searching for successor instance topics
	 * @return all {@link TopicInstance}s made on given {@link Instance} and its children
	 */
	List<TopicInstance> getInstanceSuccessorsTopics(String uri, Date lastKnownDate, int limit,
			Sorter sorter, Map<String, Serializable> filterParams);

	/**
	 * Load comments for the given topic id. The result is limited to the given pageSize and the
	 * result will return only element that is more than the given page number.
	 *
	 * @param topicId
	 *            the topic id
	 * @param lastKnownDate
	 *            the last known date
	 * @param limit
	 *            the limit
	 * @return the list
	 */
	List<CommentInstance> loadComments(Serializable topicId, Date lastKnownDate, int limit);
}
