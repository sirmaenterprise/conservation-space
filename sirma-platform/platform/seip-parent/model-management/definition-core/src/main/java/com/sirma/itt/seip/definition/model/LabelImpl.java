package com.sirma.itt.seip.definition.model;

import static com.sirma.itt.seip.util.EqualsHelper.nullSafeEquals;

import java.io.Serializable;
import java.util.Map;

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

import com.sirma.itt.seip.db.PersistenceUnits;
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
@NamedQueries({@NamedQuery(name = LabelImpl.QUERY_LABELS_BY_ID_KEY, query = LabelImpl.QUERY_LABELS_BY_ID),
		@NamedQuery(name = LabelImpl.QUERY_LABEL_BY_ID_KEY, query = LabelImpl.QUERY_LABEL_BY_ID)})
public class LabelImpl extends BaseEntity implements LabelDefinition, Serializable {

	/** The Constant QUERY_LABEL_BY_ID_KEY. */
	public static final String QUERY_LABEL_BY_ID_KEY = "QUERY_LABEL_BY_ID";
	/** The Constant QUERY_LABEL_BY_ID. */
	public static final String QUERY_LABEL_BY_ID = "select l from LabelImpl l inner join fetch l.value v where l.labelId=:labelId";
	/** The Constant QUERY_LABELS_BY_ID_KEY. */
	public static final String QUERY_LABELS_BY_ID_KEY = "QUERY_LABELS_BY_ID";
	/** The Constant QUERY_LABELS_BY_ID. */
	public static final String QUERY_LABELS_BY_ID = "select l from LabelImpl l inner join fetch l.value v where l.labelId in (:labelId)";
	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = 1907538917957126670L;

	/** The label id. */
	@Column(name = "labelId", length = 100, nullable = false)
	private String labelId;

	/** The labels. */
	@Transient
	private Map<String, String> labels;

	/** The value. */
	@JoinColumn
	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	private SerializableValue value;

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

	/**
	 * Getter method for value.
	 *
	 * @return the value
	 */
	public SerializableValue getValue() {
		return value;
	}

	/**
	 * Setter method for value.
	 *
	 * @param value
	 *            the value to set
	 */
	public void setValue(SerializableValue value) {
		this.value = value;
	}

	/**
	 * Setter method for labels.
	 *
	 * @param labels
	 *            the labels to set
	 */
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

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("LabelImpl [id=");
		builder.append(getId());
		builder.append(", labelId=");
		builder.append(labelId);
		builder.append(", labels=");
		builder.append(labels);
		builder.append(", value=");
		builder.append(value);
		builder.append("]");
		return builder.toString();
	}

}
