package com.sirma.itt.seip.template.db;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import org.hibernate.annotations.Type;

import com.sirma.itt.seip.db.PersistenceUnits;
import com.sirma.itt.seip.db.discovery.PersistenceUnitBinding;
import com.sirma.itt.seip.model.BaseEntity;

/**
 * Entity that represents a single template entity.
 *
 * @author BBonev
 */
@PersistenceUnitBinding(PersistenceUnits.PRIMARY)
@Entity
@Table(name = "sep_template", indexes = { @Index(name = "idx_temp_id", columnList = "templateid"),
		@Index(name = "idx_temp_grid", columnList = "groupid") })
@NamedQueries({
		@NamedQuery(name = TemplateEntity.QUERY_TEMPLATES_BY_TEMPLATE_IDS_KEY, query = TemplateEntity.QUERY_TEMPLATES_BY_TEMPLATE_IDS),
		@NamedQuery(name = TemplateEntity.QUERY_TEMPLATES_NOT_IN_BY_TEMPLATE_IDS_KEY, query = TemplateEntity.QUERY_TEMPLATES_NOT_IN_BY_TEMPLATE_IDS),
		@NamedQuery(name = TemplateEntity.QUERY_TEMPLATES_BY_ID_KEY, query = TemplateEntity.QUERY_TEMPLATES_BY_ID),
		@NamedQuery(name = TemplateEntity.QUERY_TEMPLATES_FOR_GROUP_ID_PURPOSE_KEY, query = TemplateEntity.QUERY_TEMPLATES_FOR_GROUP_ID_PURPOSE),
		@NamedQuery(name = TemplateEntity.QUERY_PRIMARY_TEMPLATE_FOR_GROUP_KEY, query = TemplateEntity.QUERY_PRIMARY_TEMPLATE_FOR_GROUP),
		@NamedQuery(name = TemplateEntity.QUERY_TEMPLATES_FOR_GROUP_ID_KEY, query = TemplateEntity.QUERY_TEMPLATES_FOR_GROUP_ID),
		@NamedQuery(name = TemplateEntity.QUERY_PRIMARY_TEMPLATES_FOR_GROUP_AND_PURPOSE_KEY, query = TemplateEntity.QUERY_PRIMARY_TEMPLATES_FOR_GROUP_AND_PURPOSE),
		@NamedQuery(name = TemplateEntity.QUERY_TEMPLATES_BY_INSTANCE_OR_TEMPLATE_ID_KEY, query = TemplateEntity.QUERY_TEMPLATES_BY_INSTANCE_OR_TEMPLATE_ID),
		@NamedQuery(name = TemplateEntity.QUERY_UPDATE_PUBLISHED_INSTANCE_VERSION_AND_TITLE_KEY, query = TemplateEntity.QUERY_UPDATE_PUBLISHED_INSTANCE_VERSION_AND_TITLE),
		@NamedQuery(name = TemplateEntity.QUERY_ALL_TEMPLATES_KEY, query = TemplateEntity.QUERY_ALL_TEMPLATES) })
public class TemplateEntity extends BaseEntity {

	/** Fetch templates by templates id */
	public static final String QUERY_TEMPLATES_BY_TEMPLATE_IDS_KEY = "QUERY_TEMPLATES_BY_TEMPLATE_IDS";
	static final String QUERY_TEMPLATES_BY_TEMPLATE_IDS = "select t from TemplateEntity t where t.templateId in (:templateId)";

	/** Fetch templies that are not part of the given list of ids */
	public static final String QUERY_TEMPLATES_NOT_IN_BY_TEMPLATE_IDS_KEY = "QUERY_TEMPLATES_NOT_IN_BY_TEMPLATE";
	static final String QUERY_TEMPLATES_NOT_IN_BY_TEMPLATE_IDS = "select t from TemplateEntity t where t.templateId not in (:templateId)";

	/** Fetch templates by primary db ids */
	public static final String QUERY_TEMPLATES_BY_ID_KEY = "QUERY_TEMPLATES_BY_ID";
	static final String QUERY_TEMPLATES_BY_ID = "select t from TemplateEntity t where t.id in (:ids)";

	public static final String QUERY_TEMPLATES_FOR_GROUP_ID_KEY = "QUERY_TEMPLATES_FOR_GROUP_ID";
	static final String QUERY_TEMPLATES_FOR_GROUP_ID = "select t from TemplateEntity t where t.groupId=:groupId";

	/** Fetch template db ids for given group id and purpose. */
	public static final String QUERY_TEMPLATES_FOR_GROUP_ID_PURPOSE_KEY = "QUERY_TEMPLATES_FOR_GROUP_ID_PURPOSE";
	static final String QUERY_TEMPLATES_FOR_GROUP_ID_PURPOSE = "select t from TemplateEntity t where t.groupId=:groupId AND t.purpose=:purpose";

	/** Fetch primary template db ids for given group id. */
	public static final String QUERY_PRIMARY_TEMPLATE_FOR_GROUP_KEY = "QUERY_PRIMARY_TEMPLATE_FOR_GROUP";
	static final String QUERY_PRIMARY_TEMPLATE_FOR_GROUP = "select t.id from TemplateEntity t where t.primary=1 AND t.groupId=:groupId";
	
	public static final String QUERY_PRIMARY_TEMPLATES_FOR_GROUP_AND_PURPOSE_KEY = "QUERY_PRIMARY_TEMPLATE_FOR_GROUP_AND_PURPOSE";
	static final String QUERY_PRIMARY_TEMPLATES_FOR_GROUP_AND_PURPOSE = "select t from TemplateEntity t where t.primary=1 AND t.groupId=:groupId AND t.purpose=:purpose";

	/** Fetch template db id for given template id of for given corresponding instance id. */
	public static final String QUERY_TEMPLATES_BY_INSTANCE_OR_TEMPLATE_ID_KEY = "QUERY_TEMPLATES_BY_INSTANCE_OR_TEMPLATE_ID";
	static final String QUERY_TEMPLATES_BY_INSTANCE_OR_TEMPLATE_ID = "select t from TemplateEntity t where t.templateId=:id OR t.correspondingInstance=:id";

	public static final String QUERY_UPDATE_PUBLISHED_INSTANCE_VERSION_AND_TITLE_KEY = "QUERY_UPDATE_PUBLISHED_INSTANCE_VERSION_AND_TITLE";
	static final String QUERY_UPDATE_PUBLISHED_INSTANCE_VERSION_AND_TITLE = "update TemplateEntity set publishedInstanceVersion = :instanceVersion, title = :title where correspondingInstance=:templateInstanceId";

	public static final String QUERY_ALL_TEMPLATES_KEY = "QUERY_ALL_TEMPLATES";
	static final String QUERY_ALL_TEMPLATES = "select t from TemplateEntity t";

	private static final long serialVersionUID = -4329521904963321960L;

	@Column(name = "templateid", length = 256, nullable = false)
	private String templateId;

	@Column(name = "groupid", length = 256, nullable = false)
	private String groupId;

	@Type(type = "com.sirma.itt.seip.db.customtype.BooleanCustomType")
	@Column(name = "primarytemplate", nullable = false)
	private Boolean primary;

	@Column(name = "digest", length = 100, nullable = false)
	private String contentDigest;

	@Column(name = "purpose", length = 30, nullable = false)
	private String purpose;

	@Column(name = "correspondinginstance", length = 50, nullable = true)
	private String correspondingInstance;

	@Column(name = "title", length = 300, nullable = false)
	private String title;

	/**
	 * Version of the corresponding instance when publishing the template.
	 */
	@Column(name = "published_instace_version", length = 50, nullable = true)
	private String publishedInstanceVersion;

	@Column(name = "rule", length = 1024, nullable = true)
	private String rule;

	@Column(name = "modified_on", nullable = true)
	private Date modifiedOn;

	@Column(name = "modified_by", nullable = true)
	private String modifiedBy;

	public String getGroupId() {
		return groupId;
	}

	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}

	public Boolean getPrimary() {
		return primary;
	}

	public void setPrimary(Boolean primary) {
		this.primary = primary;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("TemplateEntity [id=");
		builder.append(getId());
		builder.append(", templateId=");
		builder.append(templateId);
		builder.append(", groupId=");
		builder.append(groupId);
		builder.append(", primary=");
		builder.append(primary);
		builder.append(", contentDigest=");
		builder.append(contentDigest);
		builder.append(", contentDigest=");
		builder.append(contentDigest);
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

	public String getContentDigest() {
		return contentDigest;
	}

	public void setContentDigest(String contenteDigest) {
		this.contentDigest = contenteDigest;
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

	public String getRule() {
		return rule;
	}

	public void setRule(String rule) {
		this.rule = rule;
	}

	public String getPublishedInstanceVersion() {
		return publishedInstanceVersion;
	}

	public void setPublishedInstanceVersion(String publishedInstanceVersion) {
		this.publishedInstanceVersion = publishedInstanceVersion;
	}

	public Date getModifiedOn() {
		return modifiedOn;
	}

	public void setModifiedOn(Date modifiedOn) {
		this.modifiedOn = modifiedOn;
	}

	public String getModifiedBy() {
		return modifiedBy;
	}

	public void setModifiedBy(String modifiedBy) {
		this.modifiedBy = modifiedBy;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((contentDigest == null) ? 0 : contentDigest.hashCode());
		result = prime * result + ((correspondingInstance == null) ? 0 : correspondingInstance.hashCode());
		result = prime * result + ((groupId == null) ? 0 : groupId.hashCode());
		result = prime * result + ((primary == null) ? 0 : primary.hashCode());
		result = prime * result + ((publishedInstanceVersion == null) ? 0 : publishedInstanceVersion.hashCode());
		result = prime * result + ((purpose == null) ? 0 : purpose.hashCode());
		result = prime * result + ((rule == null) ? 0 : rule.hashCode());
		result = prime * result + ((templateId == null) ? 0 : templateId.hashCode());
		result = prime * result + ((title == null) ? 0 : title.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) { // NOSONAR
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		TemplateEntity other = (TemplateEntity) obj;
		if (contentDigest == null) {
			if (other.contentDigest != null)
				return false;
		} else if (!contentDigest.equals(other.contentDigest))
			return false;
		if (correspondingInstance == null) {
			if (other.correspondingInstance != null)
				return false;
		} else if (!correspondingInstance.equals(other.correspondingInstance))
			return false;
		if (groupId == null) {
			if (other.groupId != null)
				return false;
		} else if (!groupId.equals(other.groupId))
			return false;
		if (primary == null) {
			if (other.primary != null)
				return false;
		} else if (!primary.equals(other.primary))
			return false;
		if (publishedInstanceVersion == null) {
			if (other.publishedInstanceVersion != null)
				return false;
		} else if (!publishedInstanceVersion.equals(other.publishedInstanceVersion))
			return false;
		if (purpose == null) {
			if (other.purpose != null)
				return false;
		} else if (!purpose.equals(other.purpose))
			return false;
		if (rule == null) {
			if (other.rule != null)
				return false;
		} else if (!rule.equals(other.rule))
			return false;
		if (templateId == null) {
			if (other.templateId != null)
				return false;
		} else if (!templateId.equals(other.templateId))
			return false;
		if (title == null) {
			if (other.title != null)
				return false;
		} else if (!title.equals(other.title))
			return false;
		return true;
	}
}
