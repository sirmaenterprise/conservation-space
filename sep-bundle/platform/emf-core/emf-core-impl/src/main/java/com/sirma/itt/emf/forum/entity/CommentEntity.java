package com.sirma.itt.emf.forum.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.Index;

import com.sirma.itt.emf.entity.BaseStringIdEntity;

/**
 * Entity that represents a comment instance into the database.
 *
 * @author BBonev
 */
@Entity
@Table(name = "emf_comment")
@org.hibernate.annotations.Table(appliesTo = "emf_comment", indexes = {
		@Index(name = "idx_cm_fp", columnNames = { "postedFrom", "postedDate" }),
		@Index(name = "idx_cm_fpt", columnNames = { "postedFrom", "postedDate", "topic_id" }),
		@Index(name = "idx_cm_pt", columnNames = { "postedDate", "topic_id" }) })
public class CommentEntity extends BaseStringIdEntity {

	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = 7714861068668974588L;
	@Column(name = "identifier", length = 50, nullable = false)
	private String identifier;
	/** The from. */
	@Column(name = "postedFrom", length = 50, nullable = false)
	private String from;
	/** The comment. */
	@Column(name = "comment", columnDefinition = "TEXT", nullable = false)
	private String comment;
	/** The posted date. */
	@Column(name = "postedDate", nullable = false)
	@Temporal(TemporalType.TIMESTAMP)
	private Date postedDate;
	/** The replay of. */
	@Column(name = "replayOf_id", nullable = true)
	private String replayOfId;
	/** The topic. */
	@Column(name = "topic_id", length = 50, nullable = false)
	private String topicId;

	/**
	 * Getter method for identifier.
	 *
	 * @return the identifier
	 */
	public String getIdentifier() {
		return identifier;
	}

	/**
	 * Setter method for identifier.
	 *
	 * @param identifier
	 *            the identifier to set
	 */
	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	/**
	 * Getter method for from.
	 *
	 * @return the from
	 */
	public String getFrom() {
		return from;
	}

	/**
	 * Setter method for from.
	 *
	 * @param from
	 *            the from to set
	 */
	public void setFrom(String from) {
		this.from = from;
	}

	/**
	 * Getter method for comment.
	 *
	 * @return the comment
	 */
	public String getComment() {
		return comment;
	}

	/**
	 * Setter method for comment.
	 *
	 * @param comment
	 *            the comment to set
	 */
	public void setComment(String comment) {
		this.comment = comment;
	}

	/**
	 * Getter method for postedDate.
	 *
	 * @return the postedDate
	 */
	public Date getPostedDate() {
		return postedDate;
	}

	/**
	 * Setter method for postedDate.
	 *
	 * @param postedDate
	 *            the postedDate to set
	 */
	public void setPostedDate(Date postedDate) {
		this.postedDate = postedDate;
	}

	/**
	 * Getter method for replayOfId.
	 *
	 * @return the replayOfId
	 */
	public String getReplayOfId() {
		return replayOfId;
	}

	/**
	 * Setter method for replayOfId.
	 *
	 * @param replayOfId
	 *            the replayOfId to set
	 */
	public void setReplayOfId(String replayOfId) {
		this.replayOfId = replayOfId;
	}

	/**
	 * Getter method for topicId.
	 *
	 * @return the topicId
	 */
	public String getTopicId() {
		return topicId;
	}

	/**
	 * Setter method for topicId.
	 *
	 * @param topicId
	 *            the topicId to set
	 */
	public void setTopicId(String topicId) {
		this.topicId = topicId;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("CommentEntity [id=");
		builder.append(getId());
		builder.append(", identifier=");
		builder.append(identifier);
		builder.append(", from=");
		builder.append(from);
		builder.append(", comment=");
		builder.append(comment);
		builder.append(", postedDate=");
		builder.append(postedDate);
		builder.append(", replayOfId=");
		builder.append(replayOfId);
		builder.append(", topicId=");
		builder.append(topicId);
		builder.append("]");
		return builder.toString();
	}

}
