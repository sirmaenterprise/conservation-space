package com.sirma.sep.email.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import com.sirma.itt.seip.db.PersistenceUnits;
import com.sirma.itt.seip.db.discovery.PersistenceUnitBinding;
import com.sirma.itt.seip.model.BaseEntity;

/**
 * Entity that represents a database table with a single char column. The table is used to store semantic classes which
 * can send or receive e-mails (marked as emf:mailboxSupportable). This table is needed for comparison between semantic
 * models before and after reload. If there's a difference and new classes are marked as mailboxSupportable they are
 * stored in DB and mailboxes are created for each instance. If a class is no more mailboxSupportable after reload it's
 * removed from the table.
 *
 * @author S.Djulgerova
 */
@PersistenceUnitBinding(PersistenceUnits.PRIMARY)
@Entity
@Table(name = "emf_mailboxsupportable")
@NamedQueries(value = {
		@NamedQuery(name = MailboxSupportable.QUERY_MAILBOX_SUPPORTABLE_KEY, query = MailboxSupportable.QUERY_MAILBOX_SUPPORTABLE),
		@NamedQuery(name = MailboxSupportable.QUERY_BY_CLASSNAME_KEY, query = MailboxSupportable.QUERY_BY_CLASSNAME),
		@NamedQuery(name = MailboxSupportable.QUERY_DELETE_MAILBOX_SUPPORTABLE_KEY, query = MailboxSupportable.QUERY_DELETE_MAILBOX_SUPPORTABLE) })
public class MailboxSupportable extends BaseEntity {

	private static final long serialVersionUID = 7163930199753747810L;

	public static final String QUERY_MAILBOX_SUPPORTABLE_KEY = "QUERY_MAILBOX_SUPPORTABLE";
	static final String QUERY_MAILBOX_SUPPORTABLE = "select className from MailboxSupportable";

	public static final String QUERY_DELETE_MAILBOX_SUPPORTABLE_KEY = "QUERY_DELETE_MAILBOX_SUPPORTABLE";
	static final String QUERY_DELETE_MAILBOX_SUPPORTABLE = "delete from MailboxSupportable where className=:className";

	public static final String QUERY_BY_CLASSNAME_KEY = "QUERY_BY_CLASSNAME";
	static final String QUERY_BY_CLASSNAME = "select m from MailboxSupportable m where m.className=:className";

	@Column(name = "classname", nullable = false)
	private String className;

	/**
	 * Instantiates a new entity.
	 */
	public MailboxSupportable() {
		// default constructor
	}

	/**
	 * Instantiates a new entity.
	 *
	 * @param className
	 *            the name of the class marked as mailbox supportable
	 */
	public MailboxSupportable(String className) {
		setClassName(className);
	}

	/**
	 * Instantiates a new entity.
	 *
	 * @param id
	 *            the id
	 * @param className
	 *            the name of the class marked as mailbox supportable
	 */
	public MailboxSupportable(Long id, String className) {
		setId(id);
		setClassName(className);
	}

	@Override
	public int hashCode() {
		return super.hashCode() * 31;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof MailboxSupportable) {
			return super.equals(obj);
		}
		return false;
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}
}
