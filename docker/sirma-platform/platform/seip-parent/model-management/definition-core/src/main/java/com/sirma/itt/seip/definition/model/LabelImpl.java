package com.sirma.itt.seip.definition.model;

import static com.sirma.itt.seip.util.EqualsHelper.nullSafeEquals;

import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Type;

import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.db.PersistenceUnits;
import com.sirma.itt.seip.db.customtype.StringSetCustomType;
import com.sirma.itt.seip.db.discovery.PersistenceUnitBinding;
import com.sirma.itt.seip.definition.label.LabelDefinition;
import com.sirma.itt.seip.model.BaseEntity;
import com.sirma.itt.seip.model.SerializableValue;

/**
 * Implementation for label definition.
 *
 * @author BBonev
 */
@PersistenceUnitBinding(PersistenceUnits.PRIMARY)
@Entity
@Table(name = "emf_labels", indexes = @Index(name = "idx_l_labelid", columnList = "labelId"))
@NamedQueries({
		@NamedQuery(name = LabelImpl.QUERY_LABELS_BY_ID_KEY, query = LabelImpl.QUERY_LABELS_BY_ID),
		@NamedQuery(name = LabelImpl.QUERY_LABEL_BY_ID_KEY, query = LabelImpl.QUERY_LABEL_BY_ID),
		@NamedQuery(name = LabelImpl.QUERY_LABELS_BY_DEFINED_IN_KEY, query = LabelImpl.QUERY_LABELS_BY_DEFINED_IN)
})
public class LabelImpl extends BaseEntity implements LabelDefinition, Serializable {

	public static final String QUERY_LABEL_BY_ID_KEY = "QUERY_LABEL_BY_ID";
	public static final String QUERY_LABEL_BY_ID = "select l from LabelImpl l inner join fetch l.value v where l.labelId=:labelId";

	public static final String QUERY_LABELS_BY_ID_KEY = "QUERY_LABELS_BY_ID";
	public static final String QUERY_LABELS_BY_ID = "select l from LabelImpl l inner join fetch l.value v where l.labelId in (:labelId)";

	public static final String QUERY_LABELS_BY_DEFINED_IN_KEY = "QUERY_LABELS_BY_DEFINED_IN";
	public static final String QUERY_LABELS_BY_DEFINED_IN = "select l from LabelImpl l inner join fetch l.value v where l.definedIn like :definedIn";

	private static final long serialVersionUID = 1907538917957126670L;

	@Column(name = "labelId", length = 100, nullable = false)
	private String labelId;

	@Transient
	private Map<String, String> labels;

	@JoinColumn
	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	private SerializableValue value;

	@Column(name = "defined_in", length = 2048)
	@Type(type = StringSetCustomType.TYPE_NAME)
	private Set<String> definedIn;

	@Override
	public String getIdentifier() {
		return labelId;
	}

	@Override
	public void setIdentifier(String identifier) {
		this.labelId = identifier;
	}

	@Override
	public Map<String, String> getLabels() {
		return labels;
	}

	@Override
	public Set<String> getDefinedIn() {
		if (definedIn == null) {
			definedIn = new LinkedHashSet<>();
		}
		return definedIn;
	}

	public void setDefinedIn(Set<String> definedIn) {
		this.definedIn = definedIn;
	}

	public void addDefinedIn(String definitionId) {
		CollectionUtils.addNonNullValue(getDefinedIn(), definitionId);
	}

	public void clearDefinedIn() {
		getDefinedIn().clear();
	}

	public SerializableValue getValue() {
		return value;
	}

	public void setValue(SerializableValue value) {
		this.value = value;
	}

	public void setLabels(Map<String, String> labels) {
		this.labels = labels;
	}

	@Override
	public int hashCode() {
		final int PRIME = 31;
		int result = 1;
		result = PRIME * result + (labelId == null ? 0 : labelId.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof LabelImpl)) {
			return false;
		}
		LabelImpl other = (LabelImpl) obj;
		return nullSafeEquals(labelId, other.labelId);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder(256);
		builder.append("LabelImpl [id=");
		builder.append(getId());
		builder.append(", labelId=");
		builder.append(labelId);
		builder.append(", labels=");
		builder.append(labels);
		builder.append(", value=");
		builder.append(value);
		builder.append(", definedIn=");
		builder.append(definedIn);
		builder.append("]");
		return builder.toString();
	}
}
