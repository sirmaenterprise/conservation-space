package com.sirma.sep.cls.db.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import com.sirma.itt.seip.db.PersistenceUnits;
import com.sirma.itt.seip.db.discovery.PersistenceUnitBinding;

/**
 * Database entity for persisting the properties of {@link com.sirma.sep.cls.model.CodeList}.
 *
 * @author Mihail Radkov
 */
@Entity
@Table(name = "CLS_CODELIST")
@PersistenceUnitBinding(PersistenceUnits.PRIMARY)
@NamedQueries(value = { @NamedQuery(name = CodeListEntity.QUERY_ALL_CODELISTS_KEY, query = CodeListEntity.QUERY_ALL_CODELISTS),
		@NamedQuery(name = CodeListEntity.QUERY_CODELIST_BY_VALUE_KEY, query = CodeListEntity.QUERY_CODELIST_BY_VALUE) })
public class CodeListEntity extends CodeEntity {

	public static final String QUERY_ALL_CODELISTS_KEY = "QUERY_ALL_CODELISTS";
	static final String QUERY_ALL_CODELISTS = "select cl from CodeListEntity cl";

	public static final String QUERY_CODELIST_BY_VALUE_KEY = "QUERY_CODELIST_BY_VALUE";
	static final String QUERY_CODELIST_BY_VALUE = "select cl from CodeListEntity cl where lower(cl.value)=lower(:clValue)";

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	@JoinColumn(name = "codeId")
	@Fetch(value = FetchMode.JOIN)
	private List<CodeListDescriptionEntity> descriptions;

	/**
	 * Values are lazily loaded for performance.
	 */
	@Transient
	private transient List<CodeValueEntity> values;

	public List<CodeListDescriptionEntity> getDescriptions() {
		if (descriptions == null) {
			descriptions = new ArrayList<>();
		}
		return descriptions;
	}

	public void setDescriptions(List<CodeListDescriptionEntity> descriptions) {
		this.descriptions = descriptions;
	}

	public List<CodeValueEntity> getValues() {
		if (values == null) {
			values = new ArrayList<>();
		}
		return values;
	}

	public void setValues(List<CodeValueEntity> values) {
		this.values = values;
	}

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), descriptions);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof CodeListEntity)) {
			return false;
		}

		CodeListEntity entity = (CodeListEntity) obj;
		return super.equals(obj) && Objects.equals(descriptions, entity.descriptions);
	}
}
