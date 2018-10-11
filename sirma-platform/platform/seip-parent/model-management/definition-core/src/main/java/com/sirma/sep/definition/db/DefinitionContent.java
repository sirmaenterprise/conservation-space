package com.sirma.sep.definition.db;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import com.sirma.itt.seip.db.PersistenceUnits;
import com.sirma.itt.seip.db.discovery.PersistenceUnitBinding;

@Entity
@PersistenceUnitBinding(PersistenceUnits.PRIMARY)

@Table(name = "sep_definition_content", indexes = @Index(name = "idx_definition_id", columnList = "definition_id"))
@NamedQueries({
	@NamedQuery(name = DefinitionContent.DELETE_DEFINITION_CONTENT_BY_DEFINITION_ID_REVISION_KEY, query = DefinitionContent.DELETE_DEFINITION_CONTENT_BY_DEFINITION_ID_REVISION),
	@NamedQuery(name = DefinitionContent.FETCH_CONTENT_OF_ALL_DEFINITIONS_KEY, query = DefinitionContent.FETCH_CONTENT_OF_ALL_DEFINITIONS),
	@NamedQuery(name = DefinitionContent.FETCH_CONTENT_BY_DEFINITION_IDS_KEY, query = DefinitionContent.FETCH_CONTENT_BY_DEFINITION_IDS),
	@NamedQuery(name = DefinitionContent.FETCH_ALL_DEFINTION_IDS_KEY, query = DefinitionContent.FETCH_ALL_DEFINTION_IDS),
})
public class DefinitionContent implements com.sirma.itt.seip.Entity<String> {

	public static final String DELETE_DEFINITION_CONTENT_BY_DEFINITION_ID_REVISION_KEY = "DELETE_DEFINITION_CONTENT_BY_DEFINITION_ID_REVISION";
	static final String DELETE_DEFINITION_CONTENT_BY_DEFINITION_ID_REVISION = "delete from DefinitionContent where definitionId = :definitionId";

	public static final String FETCH_CONTENT_OF_ALL_DEFINITIONS_KEY = "FETCH_CONTENT_OF_ALL_DEFINITIONS";
	static final String FETCH_CONTENT_OF_ALL_DEFINITIONS = "from DefinitionContent";

	public static final String FETCH_CONTENT_BY_DEFINITION_IDS_KEY = "FETCH_CONTENT_BY_DEFINITION_IDS";
	static final String FETCH_CONTENT_BY_DEFINITION_IDS = "select d from DefinitionContent d where d.definitionId in (:ids)";

	public static final String FETCH_ALL_DEFINTION_IDS_KEY = "FETCH_ALL_DEFINTION_IDS";
	static final String FETCH_ALL_DEFINTION_IDS = "select d.definitionId from DefinitionContent d";

	@Id
	@Column(name = "definition_id", length = 256, nullable = false)
	private String definitionId;

	@Column(name = "file_name", unique = true)
	private String fileName;


	@Column(name = "content")
	private String content;

	@Override
	public void setId(String id) {
		this.definitionId = id;
	}

	@Override
	public String getId() {
		return definitionId;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((content == null) ? 0 : content.hashCode());
		result = prime * result + ((definitionId == null) ? 0 : definitionId.hashCode());
		result = prime * result + ((fileName == null) ? 0 : fileName.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		DefinitionContent other = (DefinitionContent) obj;
		if (content == null) {
			if (other.content != null) {
				return false;
			}
		} else if (!content.equals(other.content)) {
			return false;
		}
		if (definitionId == null) {
			if (other.definitionId != null) {
				return false;
			}
		} else if (!definitionId.equals(other.definitionId)) {
			return false;
		}
		if (fileName == null) {
			if (other.fileName != null) {
				return false;
			}
		} else if (!fileName.equals(other.fileName)) {
			return false;
		}

		return true;
	}

}
