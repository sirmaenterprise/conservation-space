package com.sirma.itt.seip.instance.draft;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Index;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.FetchMode;

import com.sirma.itt.seip.Entity;
import com.sirma.itt.seip.db.PersistenceUnits;
import com.sirma.itt.seip.db.discovery.PersistenceUnitBinding;

/**
 * Represent an entry in the draft table for given instance and user.
 */
@PersistenceUnitBinding(PersistenceUnits.PRIMARY)
@javax.persistence.Entity
@Table(name = "cmf_draftinstances", indexes = { @Index(name = "idx_draft_user_id", columnList = "userId"),
		@Index(name = "idx_draft_instance_id", columnList = "instanceId") })
@org.hibernate.annotations.Table(appliesTo = "cmf_draftinstances", fetch = FetchMode.SELECT)
@NamedQueries(value = {
		@NamedQuery(name = DraftEntity.QUERY_DRAFT_INSTANCES_BY_ENTITY_ID_KEY, query = DraftEntity.QUERY_DRAFT_INSTANCES_BY_ENTITY_ID),
		@NamedQuery(name = DraftEntity.QUERY_DRAFT_INSTANCES_BY_ENTITY_ID_AND_USER_KEY, query = DraftEntity.QUERY_DRAFT_INSTANCES_BY_ENTITY_ID_AND_USER) })
public class DraftEntity implements Entity<DraftEntityId>, Serializable {

	private static final long serialVersionUID = -3417902761945706828L;

	/** Finds drafts for given instance. */
	public static final String QUERY_DRAFT_INSTANCES_BY_ENTITY_ID_KEY = "QUERY_DRAFT_INSTANCES_BY_ENTITY_ID";
	static final String QUERY_DRAFT_INSTANCES_BY_ENTITY_ID = "select d from DraftEntity d where d.id.instanceId=:uri";

	/** Finds drafts for given instance and user. */
	public static final String QUERY_DRAFT_INSTANCES_BY_ENTITY_ID_AND_USER_KEY = "QUERY_DRAFT_INSTANCES_BY_ENTITY_ID_AND_USER";
	static final String QUERY_DRAFT_INSTANCES_BY_ENTITY_ID_AND_USER = "select d from DraftEntity d where d.id.instanceId=:uri AND d.id.userId=:userId";

	@EmbeddedId
	private DraftEntityId id;

	@Column(name = "content", nullable = false)
	private String content;

	@Column(name = "created", nullable = false)
	@Temporal(TemporalType.TIMESTAMP)
	private Date created;

	@Override
	public DraftEntityId getId() {
		return id;
	}

	@Override
	public void setId(DraftEntityId id) {
		this.id = id;
	}

	/**
	 * Gets the content id.
	 *
	 * @return the content id
	 */
	public String getContentId() {
		return content;
	}

	/**
	 * Sets the content.
	 *
	 * @param contentId
	 *            the new content id
	 */
	public void setContentId(String contentId) {
		content = contentId;
	}

	/**
	 * @return the created
	 */
	public Date getCreated() {
		return created;
	}

	/**
	 * @param created
	 *            the created to set
	 */
	public void setCreated(Date created) {
		this.created = created;
	}
}
