package com.sirma.itt.emf.forum.model;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.Lock;

import com.sirma.itt.emf.domain.model.BidirectionalMapping;
import com.sirma.itt.emf.domain.model.Node;
import com.sirma.itt.emf.forum.ForumProperties;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.instance.model.InstanceReference;
import com.sirma.itt.emf.util.PathHelper;

/**
 * Represents a topic over something with list of top level comments. The replays of particular
 * comments are part of the tree of each comment
 *
 * @author BBonev
 */
public class TopicInstance extends CommentInstance implements Serializable, Instance,
		BidirectionalMapping {
	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = 2830393307903671067L;

	/** The comments. */
	private List<CommentInstance> comments;

	/** The topic lock used then updating comments on a particular topic. */
	private Lock topicLock;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void initBidirection() {
		if (comments == null) {
			return;
		}
		for (CommentInstance commentInstance : getComments()) {
			commentInstance.setTopic(this);
		}
		ImageAnnotation annotation = getImageAnnotation();
		if (annotation != null) {
			annotation.setTopic(this);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean hasChildren() {
		return (comments != null) && !comments.isEmpty();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Node getChild(String name) {
		return PathHelper.find(getComments(), name);
	}

	/**
	 * Returns a reference to the primary object for which the topic was created for.
	 * 
	 * @return a primary object reference, never <code>null</code>.
	 */
	public InstanceReference getTopicAbout() {
		return (InstanceReference) getProperties().get(ForumProperties.TOPIC_ABOUT);
	}

	/**
	 * Sets the target instance reference for the current topic.
	 * 
	 * @param topicAbout
	 *            the topicAbout to set
	 */
	public void setTopicAbout(InstanceReference topicAbout) {
		getProperties().put(ForumProperties.TOPIC_ABOUT, topicAbout);
	}

	/**
	 * Returns the list of comments of the given topic.
	 * 
	 * @return the comments list, never <code>null</code>.
	 */
	public List<CommentInstance> getComments() {
		if (comments == null) {
			comments = new LinkedList<CommentInstance>();
		}
		return comments;
	}

	/**
	 * Setter method for comments.
	 *
	 * @param comments
	 *            the comments to set
	 */
	public void setComments(List<CommentInstance> comments) {
		this.comments = comments;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("TopicInstance [id=");
		builder.append(getId());
		builder.append(", properties=");
		builder.append(getProperties());
		builder.append(", topicAbout=");
		builder.append(getTopicAbout());
		builder.append(", comments=");
		builder.append(comments);
		builder.append("]");
		return builder.toString();
	}

	/**
	 * Returns a per topic lock that could be used for synchronization when modifying topics and his
	 * comments.
	 * <p>
	 * NOTE: if the lock is required call BBonev :)
	 * 
	 * @return the topicLock or <code>null</code> if currently not supported.
	 */
	public Lock getTopicLock() {
		return topicLock;
	}

	/**
	 * Setter method for topicLock.
	 *
	 * @param topicLock the topicLock to set
	 */
	public void setTopicLock(Lock topicLock) {
		this.topicLock = topicLock;
	}

	/**
	 * Returns the image annotation associated with the current topic.
	 * 
	 * @return the imageAnnotation
	 */
	public ImageAnnotation getImageAnnotation() {
		return (ImageAnnotation) getProperties().get(ForumProperties.IMAGE_ANNOTATION);
	}

	/**
	 * Setter method for imageAnnotation.
	 * 
	 * @param imageAnnotation
	 *            the imageAnnotation to set
	 */
	public void setImageAnnotation(ImageAnnotation imageAnnotation) {
		getProperties().put(ForumProperties.IMAGE_ANNOTATION, imageAnnotation);
	}

	/**
	 * Gets the tags field.
	 * 
	 * @return the tags
	 */
	public String getTags() {
		return (String) getProperties().get(ForumProperties.TAGS);
	}

	/**
	 * Sets the tags.
	 * 
	 * @param tags
	 *            the new tags
	 */
	public void setTags(String tags) {
		getProperties().put(ForumProperties.TAGS, tags);
	}

	/**
	 * Gets the title.
	 * 
	 * @return the title
	 */
	public String getTitle() {
		return (String) getProperties().get(ForumProperties.TITLE);
	}

	/**
	 * Sets the title.
	 * 
	 * @param title
	 *            the new title
	 */
	public void setTitle(String title) {
		getProperties().put(ForumProperties.TITLE, title);
	}

}
