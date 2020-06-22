package com.sirma.itt.seip.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

import com.sirma.itt.seip.Entity;

/**
 * The Base persistent entity for case, section and document instances.
 *
 * @author BBonev
 */
@MappedSuperclass
public class BaseStringIdEntity implements Entity<String>, Serializable {
	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = 6758524312273659398L;
	/** The id. */
	@Id
	@Column(name = "id", length = 100)
	private String id;

	/**
	 * Getter method for id.
	 *
	 * @return the id
	 */
	@Override
	public String getId() {
		return id;
	}

	/**
	 * Setter method for id.
	 *
	 * @param id
	 *            the id to set
	 */
	@Override
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (id == null ? 0 : id.hashCode());
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof BaseStringIdEntity)) {
			return false;
		}
		BaseStringIdEntity other = (BaseStringIdEntity) obj;
		if (id == null) {
			if (other.id != null) {
				return false;
			}
		} else if (!id.equals(other.id)) {
			return false;
		}
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("BaseEntity [id=");
		builder.append(id);
		builder.append("]");
		return builder.toString();
	}

}
