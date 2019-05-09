package com.sirma.itt.seip.definition.model;

import static com.sirma.itt.seip.util.EqualsHelper.nullSafeEquals;

import java.io.Serializable;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Type;

import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.db.PersistenceUnits;
import com.sirma.itt.seip.db.customtype.StringSetCustomType;
import com.sirma.itt.seip.db.discovery.PersistenceUnitBinding;
import com.sirma.itt.seip.domain.filter.Filter;

/**
 * Implementation class for filter definitions.
 *
 * @author BBonev
 */
@PersistenceUnitBinding(PersistenceUnits.PRIMARY)
@Entity
@Table(name = "emf_filterDefinition", indexes = @Index(name = "idx_fild_id", columnList = "filterId"))
@NamedQueries({
		@NamedQuery(name = FilterDefinitionImpl.QUERY_FILTER_BY_ID_KEY, query = FilterDefinitionImpl.QUERY_FILTER_BY_ID),
		@NamedQuery(name = FilterDefinitionImpl.QUERY_FILTERS_BY_ID_KEY, query = FilterDefinitionImpl.QUERY_FILTERS_BY_ID) })
public class FilterDefinitionImpl implements Filter, Serializable {

	private static final long serialVersionUID = -5645641080640106915L;

	/** Get {@link FilterDefinitionImpl} by filterId */
	public static final String QUERY_FILTER_BY_ID_KEY = "QUERY_FILTER_BY_ID";
	static final String QUERY_FILTER_BY_ID = "select l from FilterDefinitionImpl l where l.filterId=:filterId";

	/** Get {@link FilterDefinitionImpl}s by filterIds */
	public static final String QUERY_FILTERS_BY_ID_KEY = "QUERY_FILTERS_BY_ID";
	static final String QUERY_FILTERS_BY_ID = "select l from FilterDefinitionImpl l where l.filterId in (:filterId)";

	/** The id. */
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	/** The identifier. */
	@Column(name = "filterId", length = 100, nullable = false)
	private String filterId;

	/** The mode. */
	@Column(name = "filterMode", length = 20, nullable = true)
	private String mode;

	/** The filter values. */
	@Column(name = "filterValues", length = 2048, nullable = false)
	@Type(type = "com.sirma.itt.seip.db.customtype.StringSetCustomType")
	private Set<String> filterValues;

	@Column(name = "defined_in", length = 2048)
	@Type(type = StringSetCustomType.TYPE_NAME)
	private Set<String> definedIn;

	/** The xml value. */
	@Transient
	private String xmlValue;

	@Override
	public Long getId() {
		return id;
	}

	@Override
	public void setId(Long id) {
		this.id = id;
	}

	@Override
	public String getIdentifier() {
		return filterId;
	}

	@Override
	public void setIdentifier(String identifier) {
		this.filterId = identifier;
	}

	@Override
	public String getMode() {
		return mode;
	}

	public void setMode(String mode) {
		this.mode = mode;
	}

	@Override
	public Set<String> getFilterValues() {
		if (filterValues == null) {
			filterValues = new LinkedHashSet<>();
		}
		return filterValues;
	}

	public void setFilterValues(Set<String> filterValues) {
		this.filterValues = filterValues;
	}

	public String getXmlValue() {
		return xmlValue;
	}

	@Override
	public Set<String> getDefinedIn() {
		return definedIn;
	}

	public void setDefinedIn(Set<String> definedIn) {
		this.definedIn = definedIn;
	}

	public void addDefinedIn(String definitionId) {
		if (definedIn == null) {
			definedIn = new LinkedHashSet<>();
		}
		CollectionUtils.addNonNullValue(definedIn, definitionId);
	}

	public void setXmlValue(String xmlValue) {
		this.xmlValue = xmlValue;
		if (xmlValue != null) {
			String[] split = xmlValue.split("\\s*,\\s*");
			filterValues = new LinkedHashSet<>(Arrays.asList(split));
		}
	}

	@Override
	public int hashCode() {
		final int PRIME = 31;
		int result = 1;
		result = PRIME * result + (filterId == null ? 0 : filterId.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof FilterDefinitionImpl)) {
			return false;
		}
		FilterDefinitionImpl other = (FilterDefinitionImpl) obj;
		return nullSafeEquals(filterId, other.filterId);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder(256);
		builder.append("FilterDefinitionImpl [id=");
		builder.append(id);
		builder.append(", filterId=");
		builder.append(filterId);
		builder.append(", mode=");
		builder.append(mode);
		builder.append(", filterValues=");
		builder.append(filterValues);
		builder.append(", definedIn=");
		builder.append(definedIn);
		builder.append("]");
		return builder.toString();
	}

}
