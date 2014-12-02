package com.sirma.itt.emf.forum.model;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

import com.sirma.itt.emf.forum.ForumProperties;

/**
 * Comment instance that stores the comment entity in a class variable.
 * 
 * @author BBonev
 */
public class ChatInstance extends CommentInstance {

	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = 4109081632269076021L;
	/** The comment. */
	private String comment;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setComment(String comment) {
		this.comment = comment;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getComment() {
		return comment;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ChatInstance [id=");
		builder.append(getId());
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
}
