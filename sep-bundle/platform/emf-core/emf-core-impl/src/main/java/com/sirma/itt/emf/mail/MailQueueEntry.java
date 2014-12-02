package com.sirma.itt.emf.mail;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.sirma.itt.emf.entity.BaseEntity;

/**
 * Entity for storing mail queue entries.
 * 
 * @author Adrian Mitev
 */
@Entity
@Table(name = "emf_mailqueueentry")
@org.hibernate.annotations.Table(appliesTo = "emf_mailqueueentry")
public class MailQueueEntry extends BaseEntity {

	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = -189813955623140595L;
	@Column(columnDefinition = "TEXT")
	private String content;

	/**
	 * Getter method for content.
	 * 
	 * @return the content
	 */
	public String getContent() {
		return content;
	}

	/**
	 * Setter method for content.
	 * 
	 * @param content
	 *            the content to set
	 */
	public void setContent(String content) {
		this.content = content;
	}

}
