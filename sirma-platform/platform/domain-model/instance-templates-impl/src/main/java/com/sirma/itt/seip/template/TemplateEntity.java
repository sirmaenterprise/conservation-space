package com.sirma.itt.seip.template;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import org.hibernate.annotations.Index;
import org.hibernate.annotations.Type;

import com.sirma.itt.seip.model.BaseEntity;

/**
 * Entity that represents a single template entity.
 *
 * @author BBonev
 */
@Entity
@Table(name = "sep_template")
@org.hibernate.annotations.Table(appliesTo = "sep_template", indexes = {
		@Index(name = "idx_temp_id", columnNames = "templateid"),
		@Index(name = "idx_temp_grid", columnNames = "groupid") })
@NamedQueries({
		@NamedQuery(name = TemplateEntity.QUERY_TEMPLATES_BY_TEMPLATE_IDS_KEY, query = TemplateEntity.QUERY_TEMPLATES_BY_TEMPLATE_IDS),
		@NamedQuery(name = TemplateEntity.QUERY_TEMPLATES_NOT_IN_BY_TEMPLATE_IDS_KEY, query = TemplateEntity.QUERY_TEMPLATES_NOT_IN_BY_TEMPLATE_IDS),
		@NamedQuery(name = TemplateEntity.QUERY_TEMPLATES_NOT_IN_BY_DMS_IDS_KEY, query = TemplateEntity.QUERY_TEMPLATES_NOT_IN_BY_DMS_IDS),
		@NamedQuery(name = TemplateEntity.QUERY_TEMPLATE_BY_TEMPLATE_OR_DMS_ID_KEY, query = TemplateEntity.QUERY_TEMPLATE_BY_TEMPLATE_OR_DMS_ID),
		@NamedQuery(name = TemplateEntity.QUERY_TEMPLATES_BY_ID_KEY, query = TemplateEntity.QUERY_TEMPLATES_BY_ID),
		@NamedQuery(name = TemplateEntity.QUERY_TEMPLATES_FOR_USER_KEY, query = TemplateEntity.QUERY_TEMPLATES_FOR_USER),
		@NamedQuery(name = TemplateEntity.QUERY_TEMPLATES_FOR_USER_AND_GROUP_ID_KEY, query = TemplateEntity.QUERY_TEMPLATES_FOR_USER_AND_GROUP_ID),
		@NamedQuery(name = TemplateEntity.QUERY_TEMPLATES_FOR_USER_GROUP_ID_PURPOSE_KEY, query = TemplateEntity.QUERY_TEMPLATES_FOR_USER_AND_GROUP_ID_PURPOSE),
		@NamedQuery(name = TemplateEntity.QUERY_PRIMARY_TEMPLATE_FOR_GROUP_KEY, query = TemplateEntity.QUERY_PRIMARY_TEMPLATE_FOR_GROUP),
		@NamedQuery(name = TemplateEntity.QUERY_PRIMARY_TEMPLATE_FOR_GROUP_AND_PURPOSE_KEY, query = TemplateEntity.QUERY_PRIMARY_TEMPLATE_FOR_GROUP_AND_PURPOSE),
		@NamedQuery(name = TemplateEntity.QUERY_TEMPLATES_BY_TEMPLATE_KEY, query = TemplateEntity.QUERY_TEMPLATES_BY_TEMPLATE) })
public class TemplateEntity extends BaseEntity {

	/** Fetch templates by templates id */
	public static final String QUERY_TEMPLATES_BY_TEMPLATE_IDS_KEY = "QUERY_TEMPLATES_BY_TEMPLATE_IDS";
	static final String QUERY_TEMPLATES_BY_TEMPLATE_IDS = "select t from TemplateEntity t where t.templateId in (:templateId)";

	/** Fetch templies that are not part of the given list of ids */
	public static final String QUERY_TEMPLATES_NOT_IN_BY_TEMPLATE_IDS_KEY = "QUERY_TEMPLATES_NOT_IN_BY_TEMPLATE";
	static final String QUERY_TEMPLATES_NOT_IN_BY_TEMPLATE_IDS = "select t from TemplateEntity t where t.templateId not in (:templateId)";

	/** Fetch templates by dms ids. */
	public static final String QUERY_TEMPLATES_NOT_IN_BY_DMS_IDS_KEY = "QUERY_TEMPLATES_NOT_IN_BY_DMS_IDS";
	static final String QUERY_TEMPLATES_NOT_IN_BY_DMS_IDS = "select t from TemplateEntity t where t.dmsId not in (:dmsId)";

	/** Fetch template by template id or dms id */
	public static final String QUERY_TEMPLATE_BY_TEMPLATE_OR_DMS_ID_KEY = "QUERY_TEMPLATE_BY_TEMPLATE_OR_DMS_ID";
	static final String QUERY_TEMPLATE_BY_TEMPLATE_OR_DMS_ID = "select t from TemplateEntity t where t.templateId = :templateId or t.dmsId = :templateId";

	/** Fetch templates by primary db ids */
	public static final String QUERY_TEMPLATES_BY_ID_KEY = "QUERY_TEMPLATES_BY_ID";
	static final String QUERY_TEMPLATES_BY_ID = "select t from TemplateEntity t where t.id in (:ids)";

	/** Fetch template db ids that are visible to all or to the given list of users */
	public static final String QUERY_TEMPLATES_FOR_USER_KEY = "QUERY_TEMPLATES_FOR_USER";
	static final String QUERY_TEMPLATES_FOR_USER = "select t.id from TemplateEntity t where t.visibleTo is null OR t.visibleTo in (:users)";

	/** Fetch template db ids for given group id. */
	public static final String QUERY_TEMPLATES_FOR_USER_AND_GROUP_ID_KEY = "QUERY_TEMPLATES_FOR_USER_AND_GROUP_ID";
	static final String QUERY_TEMPLATES_FOR_USER_AND_GROUP_ID = "select t.id from TemplateEntity t where t.groupId=:groupId";

	/** Fetch template db ids for given group id and purpose. */
	public static final String QUERY_TEMPLATES_FOR_USER_GROUP_ID_PURPOSE_KEY = "QUERY_TEMPLATES_FOR_USER_GROUP_ID_PURPOSE";
	static final String QUERY_TEMPLATES_FOR_USER_AND_GROUP_ID_PURPOSE = "select t.id from TemplateEntity t where t.groupId=:groupId AND t.purpose=:purpose";

	/** Fetch primary template db ids for given group id. */
	public static final String QUERY_PRIMARY_TEMPLATE_FOR_GROUP_KEY = "QUERY_PRIMARY_TEMPLATE_FOR_GROUP";
	static final String QUERY_PRIMARY_TEMPLATE_FOR_GROUP = "select t.id from TemplateEntity t where t.primary=1 AND t.groupId=:groupId";

	/** Fetch primary template db ids for given group id and the given purpose. */
	public static final String QUERY_PRIMARY_TEMPLATE_FOR_GROUP_AND_PURPOSE_KEY = "QUERY_PRIMARY_TEMPLATE_FOR_GROUP_AND_PURPOSE";
	static final String QUERY_PRIMARY_TEMPLATE_FOR_GROUP_AND_PURPOSE = "select t.id from TemplateEntity t where t.primary=1 AND t.groupId=:groupId AND t.purpose=:purpose";

	/** Fetch template db id for given template id. */
	public static final String QUERY_TEMPLATES_BY_TEMPLATE_KEY = "QUERY_TEMPLATES_ID_BY_TEMPLATE_ID";
	static final String QUERY_TEMPLATES_BY_TEMPLATE = "select t.id from TemplateEntity t where t.templateId=:templateId";

	private static final long serialVersionUID = -4329521904963321960L;

	@Column(name = "templateid", length = 256, nullable = false)
	private String templateId;

	@Column(name = "groupid", length = 256, nullable = false)
	private String groupId;

	@Column(name = "visibleto", length = 50, nullable = true)
	private String visibleTo;

	@Type(type = "com.sirma.itt.seip.db.customtype.BooleanCustomType")
	@Column(name = "primarytemplate", nullable = false)
	private Boolean primary;

	@Type(type = "com.sirma.itt.seip.db.customtype.BooleanCustomType")
	@Column(name = "publictemplate", nullable = false)
	private Boolean publicTemplate;

	@Column(name = "dmsid", length = 100, nullable = false)
	private String dmsId;

	@Column(name = "digest", length = 100, nullable = false)
	private String templateDigest;

	@Column(name = "purpose", length = 30, nullable = false)
	private String purpose;

	@Column(name = "correspondinginstance", length = 50, nullable = true)
	private String correspondingInstance;

	/**
	 * Getter method for groupId.
	 *
	 * @return the groupId
	 */
	public String getGroupId() {
		return groupId;
	}

	/**
	 * Setter method for groupId.
	 *
	 * @param groupId
	 *            the groupId to set
	 */
	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}

	/**
	 * Getter method for visibleTo.
	 *
	 * @return the visibleTo
	 */
	public String getVisibleTo() {
		return visibleTo;
	}

	/**
	 * Setter method for visibleTo.
	 *
	 * @param createdBy
	 *            the new visible to
	 */
	public void setVisibleTo(String createdBy) {
		visibleTo = createdBy;
	}

	/**
	 * Getter method for primary.
	 *
	 * @return the primary
	 */
	public Boolean getPrimary() {
		return primary;
	}

	/**
	 * Setter method for primary.
	 *
	 * @param primary
	 *            the primary to set
	 */
	public void setPrimary(Boolean primary) {
		this.primary = primary;
	}

	/**
	 * Getter method for publicTemplate.
	 *
	 * @return the publicTemplate
	 */
	public Boolean getPublicTemplate() {
		return publicTemplate;
	}

	/**
	 * Setter method for publicTemplate.
	 *
	 * @param publicTemplate
	 *            the publicTemplate to set
	 */
	public void setPublicTemplate(Boolean publicTemplate) {
		this.publicTemplate = publicTemplate;
	}

	/**
	 * Getter method for dmsId.
	 *
	 * @return the dmsId
	 */
	public String getDmsId() {
		return dmsId;
	}

	/**
	 * Setter method for dmsId.
	 *
	 * @param dmsId
	 *            the dmsId to set
	 */
	public void setDmsId(String dmsId) {
		this.dmsId = dmsId;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("TemplateEntity [id=");
		builder.append(getId());
		builder.append(", templateId=");
		builder.append(templateId);
		builder.append(", groupId=");
		builder.append(groupId);
		builder.append(", visibleTo=");
		builder.append(visibleTo);
		builder.append(", primary=");
		builder.append(primary);
		builder.append(", publicTemplate=");
		builder.append(publicTemplate);
		builder.append(", dmsId=");
		builder.append(dmsId);
		builder.append(", templateDigest=");
		builder.append(templateDigest);
		builder.append("]");
		return builder.toString();
	}

	/**
	 * Getter method for templateId.
	 *
	 * @return the templateId
	 */
	public String getTemplateId() {
		return templateId;
	}

	/**
	 * Setter method for templateId.
	 *
	 * @param templateId
	 *            the templateId to set
	 */
	public void setTemplateId(String templateId) {
		this.templateId = templateId;
	}

	/**
	 * Getter method for templateDigest.
	 *
	 * @return the templateDigest
	 */
	public String getTemplateDigest() {
		return templateDigest;
	}

	/**
	 * Setter method for templateDigest.
	 *
	 * @param templateDigest
	 *            the templateDigest to set
	 */
	public void setTemplateDigest(String templateDigest) {
		this.templateDigest = templateDigest;
	}

	/**
	 * Getter method for purpose.
	 *
	 * @return the purpose
	 */
	public String getPurpose() {
		return purpose;
	}

	/**
	 * Setter method for purpose.
	 *
	 * @param purpose
	 *            the purpose to set
	 */
	public void setPurpose(String purpose) {
		this.purpose = purpose;
	}

	public String getCorrespondingInstance() {
		return correspondingInstance;
	}

	public void setCorrespondingInstance(String correspondingInstance) {
		this.correspondingInstance = correspondingInstance;
	}

}
