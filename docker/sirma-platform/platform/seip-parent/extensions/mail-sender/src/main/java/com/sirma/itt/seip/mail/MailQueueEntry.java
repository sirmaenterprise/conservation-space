package com.sirma.itt.seip.mail;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import com.sirma.itt.seip.db.PersistenceUnits;
import com.sirma.itt.seip.db.discovery.PersistenceUnitBinding;
import com.sirma.itt.seip.util.EqualsHelper;

/**
 * Entity for storing mail queue entries.
 *
 * @author Adrian Mitev
 */
@PersistenceUnitBinding(PersistenceUnits.PRIMARY)
@Entity
@Table(name = "emf_mailqueueentry")
@org.hibernate.annotations.Table(appliesTo = "emf_mailqueueentry")
@NamedQueries({
		@NamedQuery(name = MailQueueEntry.COUNT_MAILS_BY_MAIL_GROPI_ID_KEY, query = MailQueueEntry.COUNT_MAILS_BY_MAIL_GROPI_ID) })
public class MailQueueEntry implements com.sirma.itt.seip.Entity<Long>, Serializable {

	private static final long serialVersionUID = -3838088991880393856L;

	/** Count all mails with the same mailGroupId value. */
	public static final String COUNT_MAILS_BY_MAIL_GROPI_ID_KEY = "COUNT_MAILS_BY_MAIL_GROPI_ID";
	static final String COUNT_MAILS_BY_MAIL_GROPI_ID = "select count(*) from MailQueueEntry m where m.mailGroupId = :id";

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(columnDefinition = "TEXT")
	private String content;

	@Column(name = "mailgroupid", length = 128)
	private String mailGroupId;

	@Column(name = "status")
	private int status = 0;

	@Column(name = "lastretry")
	@Temporal(TemporalType.TIMESTAMP)
	private Date lastRetry;

	/**
	 * Getter method for id.
	 *
	 * @return the id
	 */
	@Override
	public Long getId() {
		return id;
	}

	/**
	 * Setter method for id.
	 *
	 * @param id
	 *            the id to set
	 */
	@Override
	public void setId(Long id) {
		this.id = id;
	}

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

	/**
	 * Getter for mail group id
	 *
	 * @return the mail group id
	 */
	public String getMailGroupId() {
		return mailGroupId;
	}

	/**
	 * Setter for mail group id.
	 *
	 * @param mailGroupId
	 *            the mail group id to set
	 */
	public void setMailGroupId(String mailGroupId) {
		this.mailGroupId = mailGroupId;
	}

	/**
	 * Getter method for status.
	 *
	 * @return the status
	 */
	public int getStatus() {
		return status;
	}

	/**
	 * Setter method for status.
	 *
	 * @param status
	 *            the status to set
	 */
	public void setStatus(int status) {
		this.status = status;
	}

	/**
	 * Getter method for lastRetry.
	 *
	 * @return the lastRetry
	 */
	public Date getLastRetry() {
		return lastRetry;
	}

	/**
	 * Setter method for lastRetry.
	 *
	 * @param lastRetry
	 *            the lastRetry to set
	 */
	public void setLastRetry(Date lastRetry) {
		this.lastRetry = lastRetry;
	}

	@Override
	public int hashCode() {
		final int PRIME = 31;
		int result = 1;
		result = PRIME * result + (id == null ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof MailQueueEntry)) {
			return false;
		}
		MailQueueEntry other = (MailQueueEntry) obj;
		return EqualsHelper.nullSafeEquals(id, other.id);
	}

	@Override
	public String toString() {
		return new StringBuilder()
				.append("MailQueueEntry [id=")
					.append(id)
					.append(", mailGroupId=")
					.append(mailGroupId)
					.append("]")
					.toString();
	}

}
