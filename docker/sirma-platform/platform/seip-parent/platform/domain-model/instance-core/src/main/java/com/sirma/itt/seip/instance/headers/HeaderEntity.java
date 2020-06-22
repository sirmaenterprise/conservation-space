package com.sirma.itt.seip.instance.headers;

import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import com.sirma.itt.seip.db.PersistenceUnits;
import com.sirma.itt.seip.db.discovery.PersistenceUnitBinding;
import com.sirma.itt.seip.model.BaseEntity;

/**
 * Entity that represents a single registered header for a definition.
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 24/11/2017
 */
@Entity
@Table(name = "sep_headers", indexes = @Index(name = "idx_h_defid", columnList = "definition_id"))
@PersistenceUnitBinding(PersistenceUnits.PRIMARY)
@NamedQueries(@NamedQuery(name = HeaderEntity.QUERY_BY_DEFINITION_ID_KEY, query = HeaderEntity.QUERY_BY_DEFINITION_ID))
public class HeaderEntity extends BaseEntity {

	public static final String QUERY_BY_DEFINITION_ID_KEY = "QUERY_HEADER_BY_DEFINITION_ID";
	static final String QUERY_BY_DEFINITION_ID = "from HeaderEntity where definitionId = :definitionId";

	@Column(name = "definition_id", length = 128, nullable = false)
	private String definitionId;
	@Column(name = "header", length = 4096)
	private String header;

	public String getDefinitionId() {
		return definitionId;
	}

	public void setDefinitionId(String definitionId) {
		this.definitionId = definitionId;
	}

	public String getHeader() {
		return header;
	}

	public void setHeader(String header) {
		this.header = header;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof HeaderEntity)) {
			return false;
		}
		if (!super.equals(o)) {
			return false;
		}
		HeaderEntity that = (HeaderEntity) o;
		return Objects.equals(definitionId, that.definitionId) &&
				Objects.equals(header, that.header);
	}

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), definitionId, header);
	}
}
