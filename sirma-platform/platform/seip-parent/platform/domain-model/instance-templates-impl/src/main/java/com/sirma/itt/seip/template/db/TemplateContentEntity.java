package com.sirma.itt.seip.template.db;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import com.sirma.itt.seip.db.PersistenceUnits;
import com.sirma.itt.seip.db.discovery.PersistenceUnitBinding;

/**
 * Represents a single template content record.
 * 
 * @author Vilizar Tsonev
 */
@Entity
@Table(name = "sep_template_content", indexes = @Index(name = "idx_tmpl_id", columnList = "template_id"))
@PersistenceUnitBinding(PersistenceUnits.PRIMARY)
@NamedQueries({
		@NamedQuery(name = TemplateContentEntity.QUERY_BY_TEMPLATE_ID_KEY, query = TemplateContentEntity.QUERY_BY_TEMPLATE_ID) })
public class TemplateContentEntity implements com.sirma.itt.seip.Entity<String> {

	public static final String QUERY_BY_TEMPLATE_ID_KEY = "QUERY_BY_TEMPLATE_ID";
	static final String QUERY_BY_TEMPLATE_ID = "select t from TemplateContentEntity t where t.templateId = :templateId";

	@Id
	@Column(name = "template_id", length = 256, nullable = false)
	private String templateId;

	@Column(name = "content")
	private String content;
	
	@Column(name = "file_name", length = 256)
	private String fileName;

	@Override
	public String getId() {
		return templateId;
	}

	@Override
	public void setId(String id) {
		this.templateId = id;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((content == null) ? 0 : content.hashCode());
		result = prime * result + ((fileName == null) ? 0 : fileName.hashCode());
		result = prime * result + ((templateId == null) ? 0 : templateId.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TemplateContentEntity other = (TemplateContentEntity) obj;
		if (content == null) {
			if (other.content != null)
				return false;
		} else if (!content.equals(other.content))
			return false;
		if (fileName == null) {
			if (other.fileName != null)
				return false;
		} else if (!fileName.equals(other.fileName))
			return false;
		if (templateId == null) {
			if (other.templateId != null)
				return false;
		} else if (!templateId.equals(other.templateId))
			return false;
		return true;
	}

}
