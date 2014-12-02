package com.sirma.itt.emf.forum.model;

import java.io.Serializable;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import com.sirma.itt.emf.converter.TypeConverterUtil;
import com.sirma.itt.emf.domain.model.Node;
import com.sirma.itt.emf.domain.model.PathElement;
import com.sirma.itt.emf.forum.ForumProperties;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.instance.model.InstanceReference;
import com.sirma.itt.emf.util.CollectionUtils;
import com.sirma.itt.emf.util.EqualsHelper;

/**
 * Represents a single comment.
 * 
 * @author BBonev
 */
public class CommentInstance implements Instance, Comparable<CommentInstance> {

	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = 4294445842071060979L;

	/** The id. */
	private Serializable id;

	/** The properties. */
	private Map<String, Serializable> properties;

	// FIXME: reply
	/** The replay of. */
	private CommentInstance replayOf;

	/** The reference. */
	private transient InstanceReference reference;

	private String identifier;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int compareTo(CommentInstance o) {
		return EqualsHelper.nullSafeCompare(getPostedDate(), o.getPostedDate());
	}

	/**
	 * Gets the sub section id.
	 * 
	 * @return the sub section id
	 */
	public String getSubSectionId() {
		return (String) getProperties().get(ForumProperties.TOPIC_ABOUT_SECTION);
	}

	/**
	 * Sets the sub section id.
	 * 
	 * @param subSectionId
	 *            the new sub section id
	 */
	public void setSubSectionId(String subSectionId) {
		getProperties().put(ForumProperties.TOPIC_ABOUT_SECTION, subSectionId);
	}

	/**
	 * Getter method for from.
	 * 
	 * @return the from
	 */
	public String getFrom() {
		return (String) getProperties().get(ForumProperties.CREATED_BY);
	}

	/**
	 * Setter method for from.
	 * 
	 * @param from
	 *            the from to set
	 */
	public void setFrom(String from) {
		getProperties().put(ForumProperties.CREATED_BY, from);
	}

	/**
	 * Getter method for comment.
	 * 
	 * @return the comment
	 */
	public String getComment() {
		return (String) getProperties().get(ForumProperties.CONTENT);
	}

	/**
	 * Setter method for comment.
	 * 
	 * @param comment
	 *            the comment to set
	 */
	public void setComment(String comment) {
		getProperties().put(ForumProperties.CONTENT, comment);
	}

	/**
	 * Getter method for postedDate.
	 * 
	 * @return the postedDate
	 */
	public Date getPostedDate() {
		return (Date) getProperties().get(ForumProperties.CREATED_ON);
	}

	/**
	 * Setter method for postedDate.
	 * 
	 * @param postedDate
	 *            the postedDate to set
	 */
	public void setPostedDate(Date postedDate) {
		getProperties().put(ForumProperties.CREATED_ON, postedDate);
	}

	/**
	 * Getter method for replayOf.
	 * 
	 * @return the replayOf
	 */
	public CommentInstance getReplayOf() {
		return replayOf;
	}

	/**
	 * Setter method for replayOf.
	 * 
	 * @param replayOf
	 *            the replayOf to set
	 */
	public void setReplayOf(CommentInstance replayOf) {
		this.replayOf = replayOf;
	}

	/**
	 * Getter method for topic.
	 * 
	 * @return the topic
	 */
	public TopicInstance getTopic() {
		return (TopicInstance) getProperties().get(ForumProperties.REPLY_TO);
	}

	/**
	 * Setter method for topic.
	 * 
	 * @param topic
	 *            the topic to set
	 */
	public void setTopic(TopicInstance topic) {
		getProperties().put(ForumProperties.REPLY_TO, topic);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Map<String, Serializable> getProperties() {
		if (properties == null) {
			properties = CollectionUtils.createLinkedHashMap(10);
		}
		return properties;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setProperties(Map<String, Serializable> properties) {
		this.properties = properties;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Long getRevision() {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public PathElement getParentElement() {
		return getTopic();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getPath() {
		return getIdentifier();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean hasChildren() {
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Node getChild(String name) {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getIdentifier() {
		return identifier;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Serializable getId() {
		return id;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setId(Serializable id) {
		this.id = id;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = (prime * result) + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof CommentInstance)) {
			return false;
		}
		CommentInstance other = (CommentInstance) obj;
		if (id == null) {
			if (other.id != null) {
				return false;
			}
		} else if (!id.equals(other.id)) {
			return false;
		}
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("CommentInstance [id=");
		builder.append(id);
		builder.append(", from=");
		builder.append(getFrom());
		builder.append(", postedDate=");
		builder.append(getPostedDate());
		builder.append(", comment=");
		builder.append(getComment());
		builder.append(", properties=");
		Map<String, Serializable> map = new LinkedHashMap<>(getProperties());
		map.remove(ForumProperties.REPLY_TO);
		builder.append(map);
		builder.append("]");
		return builder.toString();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setRevision(Long revision) {
		// nothing to do here
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public InstanceReference toReference() {
		if (reference == null) {
			reference = TypeConverterUtil.getConverter().convert(InstanceReference.class, this);
		}
		return reference;
	}

}
