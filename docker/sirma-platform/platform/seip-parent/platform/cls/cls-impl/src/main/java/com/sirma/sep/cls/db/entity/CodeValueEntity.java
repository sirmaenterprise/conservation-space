package com.sirma.sep.cls.db.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import com.sirma.itt.seip.db.PersistenceUnits;
import com.sirma.itt.seip.db.discovery.PersistenceUnitBinding;

/**
 * Database entity for persisting the properties of {@link com.sirma.sep.cls.model.CodeValue}.
 *
 * @author Mihail Radkov
 */
@Entity
@Table(name = "CLS_CODEVALUE")
@PersistenceUnitBinding(PersistenceUnits.PRIMARY)
@NamedQueries(value = { @NamedQuery(name = CodeValueEntity.QUERY_VALUES_BY_CL_ID_KEY, query = CodeValueEntity.QUERY_VALUES_BY_CL_ID),
		@NamedQuery(name = CodeValueEntity.QUERY_VALUE_BY_VALUE_AND_CL_ID_KEY, query = CodeValueEntity.QUERY_VALUE_BY_VALUE_AND_CL_ID) })
public class CodeValueEntity extends CodeEntity {

	public static final String QUERY_VALUES_BY_CL_ID_KEY = "QUERY_VALUES_BY_CL_ID";
	static final String QUERY_VALUES_BY_CL_ID = "select cv from CodeValueEntity cv where cv.codeListId=:clValue";

	public static final String QUERY_VALUE_BY_VALUE_AND_CL_ID_KEY = "QUERY_VALUE_BY_VALUE_AND_CL_ID";
	static final String QUERY_VALUE_BY_VALUE_AND_CL_ID = "select cv from CodeValueEntity cv where lower(cv.value)=lower(:cvValue) and cv.codeListId=:clValue";

	@Column(name = "codeListId")
	private String codeListId;

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	@JoinColumn(name = "codeId")
	@Fetch(value = FetchMode.JOIN)
	private List<CodeValueDescriptionEntity> descriptions;

	@Column(name = "active")
	private Boolean active = true;

	public String getCodeListId() {
		return codeListId;
	}

	public void setCodeListId(String codeListId) {
		this.codeListId = codeListId;
	}

	public List<CodeValueDescriptionEntity> getDescriptions() {
		if (descriptions == null) {
			descriptions = new ArrayList<>();
		}
		return descriptions;
	}

	public void setDescriptions(List<CodeValueDescriptionEntity> descriptions) {
		this.descriptions = descriptions;
	}

	public Boolean isActive() {
		return active;
	}

	public void setActive(Boolean active) {
		this.active = active;
	}

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), codeListId, descriptions, active);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof CodeValueEntity)) {
			return false;
		}

		CodeValueEntity entity = (CodeValueEntity) obj;
		return super.equals(obj) && Objects.equals(codeListId, entity.codeListId) && Objects.equals(descriptions, entity.descriptions)
				&& Objects.equals(active, entity.active);
	}
}
