package com.sirma.itt.emf.forum.entity;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.persistence.AssociationOverride;
import javax.persistence.AssociationOverrides;
import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.Index;

import com.sirma.itt.emf.entity.BaseStringIdEntity;
import com.sirma.itt.emf.entity.LinkSourceId;

/**
 * Entity class for topic instance
 *
 * @author BBonev
 */
@Entity
@Table(name = "emf_topic")
@org.hibernate.annotations.Table(appliesTo = "emf_topic", indexes = {
		@Index(name = "idx_top_id", columnNames = "identifier"),
		@Index(name = "idx_top_iabout", columnNames = {"identifier", "topicAboutId", "topicAboutType" }) })
@AssociationOverrides(value = { @AssociationOverride(name = "topicAbout.sourceType", joinColumns = @JoinColumn(name = "topicAboutType", nullable = false)) })
public class TopicEntity extends BaseStringIdEntity {

	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = -9208451339541895432L;

	/** The identifier. */
	@Column(name = "identifier", length = 50, nullable = false)
	private String identifier;

	/** The topic about. */
	@AttributeOverrides(value = { @AttributeOverride(name = "sourceId", column = @Column(name = "topicAboutId", length = 50, nullable = false)) })
	private LinkSourceId topicAbout;

	/** The image annotation. */
	@OneToOne(fetch = FetchType.EAGER, cascade = { CascadeType.ALL }, orphanRemoval = true, optional = true)
	@Fetch(FetchMode.JOIN)
	private ImageAnnotationEntity imageAnnotation;

	/** The topic lock. */
	@Transient
	private transient Lock topicLock;

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
	 * Getter method for topicAbout.
	 *
	 * @return the topicAbout
	 */
	public LinkSourceId getTopicAbout() {
		return topicAbout;
	}

	/**
	 * Setter method for topicAbout.
	 *
	 * @param topicAbout
	 *            the topicAbout to set
	 */
	public void setTopicAbout(LinkSourceId topicAbout) {
		this.topicAbout = topicAbout;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("TopicEntity [id=");
		builder.append(getId());
		builder.append(", identifier=");
		builder.append(identifier);
		builder.append(", topicAbout=");
		builder.append(topicAbout);
		builder.append(", imageAnnotation=");
		builder.append(imageAnnotation);
		builder.append("]");
		return builder.toString();
	}

	/**
	 * Getter method for topicLock.
	 *
	 * @return the topicLock
	 */
	public Lock getTopicLock() {
		if (topicLock == null) {
			topicLock = new ReentrantLock(true);
		}
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
	 * Getter method for imageAnnotation.
	 * 
	 * @return the imageAnnotation
	 */
	public ImageAnnotationEntity getImageAnnotation() {
		return imageAnnotation;
	}

	/**
	 * Setter method for imageAnnotation.
	 * 
	 * @param imageAnnotation
	 *            the imageAnnotation to set
	 */
	public void setImageAnnotation(ImageAnnotationEntity imageAnnotation) {
		this.imageAnnotation = imageAnnotation;
	}

}
