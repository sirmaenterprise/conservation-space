package com.sirma.itt.emf.sequence.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import org.json.JSONObject;

import com.sirma.itt.emf.sequence.Sequence;
import com.sirma.itt.seip.db.PersistenceUnits;
import com.sirma.itt.seip.db.discovery.PersistenceUnitBinding;
import com.sirma.itt.seip.json.JsonRepresentable;
import com.sirma.itt.seip.json.JsonUtil;
import com.sirma.itt.seip.model.BaseEntity;
import com.sirma.itt.seip.util.EqualsHelper;

/**
 * Represents a single sequence entry
 *
 * @author BBonev
 */
@PersistenceUnitBinding(PersistenceUnits.PRIMARY)
@Entity
@Table(name = "emf_sequenceentity", indexes = @Index(name = "idx_seqEnt_id", columnList = "sequenceid"))
@NamedQueries(value = {
		@NamedQuery(name = SequenceEntity.QUERY_SEQUENCES_KEY, query = SequenceEntity.QUERY_SEQUENCES),
		@NamedQuery(name = SequenceEntity.QUERY_SEQUENCE_BY_NAME_KEY, query = SequenceEntity.QUERY_SEQUENCE_BY_NAME),
		@NamedQuery(name = SequenceEntity.UPDATE_SEQUENCES_ENTRY_KEY, query = SequenceEntity.UPDATE_SEQUENCES_ENTRY)})
public class SequenceEntity extends BaseEntity implements Sequence, JsonRepresentable {

	public static final String QUERY_SEQUENCES_KEY = "QUERY_SEQUENCES";
	static final String QUERY_SEQUENCES = "from SequenceEntity";

	public static final String QUERY_SEQUENCE_BY_NAME_KEY = "QUERY_SEQUENCE_BY_NAME";
	static final String QUERY_SEQUENCE_BY_NAME = "from SequenceEntity where sequenceId=:sequenceId";

	public static final String UPDATE_SEQUENCES_ENTRY_KEY = "UPDATE_SEQUENCES_ENTRY";
	static final String UPDATE_SEQUENCES_ENTRY = "update SequenceEntity set sequence=:sequence where sequenceId=:sequenceId";
	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = -7464986391916985694L;

	@Column(name = "sequenceid", nullable = false, length = 100)
	private String sequenceId;

	@Column(name = "sequence", nullable = false)
	private Long sequence;

	/**
	 * Getter method for sequenceId.
	 *
	 * @return the sequenceId
	 */
	public String getSequenceId() {
		return sequenceId;
	}

	/**
	 * Setter method for sequenceId.
	 *
	 * @param sequenceId
	 *            the sequenceId to set
	 */
	public void setSequenceId(String sequenceId) {
		this.sequenceId = sequenceId;
	}

	/**
	 * Getter method for sequence.
	 *
	 * @return the sequence
	 */
	public Long getSequence() {
		return sequence;
	}

	/**
	 * Setter method for sequence.
	 *
	 * @param sequence
	 *            the sequence to set
	 */
	public void setSequence(Long sequence) {
		this.sequence = sequence;
	}

	@Override
	public String getIdentifier() {
		return getSequenceId();
	}

	@Override
	public void setIdentifier(String identifier) {
		// should not allow modification of the sequence object
	}

	@Override
	public Long getValue() {
		return getSequence();
	}

	@Override
	public int hashCode() {
		final int PRIME = 31;
		int result = super.hashCode();
		result = PRIME * result + (sequenceId == null ? 0 : sequenceId.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!super.equals(obj)) {
			return false;
		}
		if (obj instanceof SequenceEntity) {
			return EqualsHelper.nullSafeEquals(sequenceId, ((SequenceEntity) obj).sequenceId);
		}
		return false;
	}

	@Override
	public JSONObject toJSONObject() {
		JSONObject object = new JSONObject();
		JsonUtil.addToJson(object, "name", sequenceId);
		JsonUtil.addToJson(object, "value", sequence);
		return object;
	}

	@Override
	public void fromJSONObject(JSONObject jsonObject) {
		setSequence(JsonUtil.getLongValue(jsonObject, "value"));
		setSequenceId(JsonUtil.getStringValue(jsonObject, "name"));
	}

}
