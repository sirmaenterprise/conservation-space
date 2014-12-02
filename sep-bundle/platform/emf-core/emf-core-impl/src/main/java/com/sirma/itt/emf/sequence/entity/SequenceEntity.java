package com.sirma.itt.emf.sequence.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.hibernate.annotations.Index;

import com.sirma.itt.emf.entity.BaseEntity;

/**
 * Represents a single sequence entry
 * 
 * @author BBonev
 */
@Entity
@Table(name = "emf_sequenceentity")
@org.hibernate.annotations.Table(appliesTo = "emf_sequenceentity", indexes = { @Index(name = "idx_seqEnt_id", columnNames = "sequenceid") })
public class SequenceEntity extends BaseEntity {

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
	 * @param sequenceId the sequenceId to set
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
	 * @param sequence the sequence to set
	 */
	public void setSequence(Long sequence) {
		this.sequence = sequence;
	}

}
