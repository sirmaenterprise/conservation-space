package com.sirma.itt.emf.forum;

import com.sirma.itt.emf.forum.model.CommentInstance;
import com.sirma.itt.emf.forum.model.TopicInstance;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.time.DateRange;

/**
 * Service for managing a forum functions. Creating new topics, adding comments, loading comments
 * etc. Currently comments can only be added to a topic and cannot exist on their own.
 *
 * @author BBonev
 */
public interface ForumService {

	/**
	 * Gets the or create topic about the given instance. The method creates a default topic
	 * associated with the given instance object. If the default topic exists for the given instance
	 * then it will be returned or new will be created but will not be saved until called the
	 * {@link #save(TopicInstance)} method.
	 * <p>
	 * <b>NOTE 1: </b> An argument to the method could be any instance that is valid when passed to
	 * {@link com.sirma.itt.emf.definition.DictionaryService#getDataTypeDefinition(String)} as
	 * <code><pre>
	 * dictionaryService.getDataTypeDefinition(instance.getClass().getSimpleName().toLowerCase());
	 * or
	 * dictionaryService.getDataTypeDefinition(instance.getClass().getName());
	 * </pre></code> and the result is not <code>null</code>. <br>
	 * <b>NOTE 2: </b> The method will not load any comments if the topic exists. To load comments
	 * call {@link #loadComments(TopicInstance, DateRange)}
	 * 
	 * @param instance
	 *            the instance
	 * @return the or create topic about
	 */
	TopicInstance getOrCreateTopicAbout(Instance instance);

	/**
	 * Saves the given topic instance and any comments that have been added and are not saved, yet.
	 *
	 * @param instance
	 *            the instance
	 * @return the topic instance
	 */
	TopicInstance save(TopicInstance instance);

	/**
	 * Creates a comment for the given message and also fills the current user as creator and post
	 * date and generates an id for the comment.
	 * <p>
	 * <b>NOTE: </b>The comment is not attached to any topic and is not saved. To save it call
	 * {@link #postComment(TopicInstance, CommentInstance)}
	 *
	 * @param content
	 *            the content
	 * @return the comment instance
	 */
	CommentInstance createComment(String content);

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
	 * Loads the comments of the given topic for the given data range. The comments in the current
	 * topic instance are going to be extended to the given range.
	 *
	 * @param topic
	 *            the topic
	 * @param range
	 *            the range
	 * @return the topic instance
	 */
	TopicInstance loadComments(TopicInstance topic, DateRange range);
}
