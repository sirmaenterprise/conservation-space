package com.sirma.itt.seip.eai.model.request.composed;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.sirma.itt.seip.eai.model.ResultPaging;
import com.sirma.itt.seip.eai.model.request.ResultOrdering;

/**
 * The query configuration json<->java binding.
 * 
 * @author bbanchev
 */
@JsonTypeName(value = "configuration")
public class Configuration implements Serializable {
	private static final long serialVersionUID = 3468222292237195362L;
	@JsonProperty(value = "uid", required = true)
	private String uid;
	@JsonProperty(value = "entities")
	private List<Entity> entities;
	@JsonProperty(value = "ordering")
	private List<ResultOrdering> ordering;
	@JsonProperty(value = "paging")
	private ResultPaging paging;

	/**
	 * Getter method for uid.
	 *
	 * @return the uid
	 */
	public String getUid() {
		return uid;
	}

	/**
	 * Setter method for uid.
	 *
	 * @param uid
	 *            the uid to set
	 */
	public void setUid(String uid) {
		this.uid = uid;
	}

	/**
	 * Getter method for entities.
	 *
	 * @return the entities
	 */
	public List<Entity> getEntities() {
		return entities;
	}

	/**
	 * Setter method for entities.
	 *
	 * @param entities
	 *            the entities to set
	 */
	public void setEntities(List<Entity> entities) {
		this.entities = entities;
	}

	/**
	 * Getter method for ordering.
	 *
	 * @return the ordering
	 */
	public List<ResultOrdering> getOrdering() {
		return ordering;
	}

	/**
	 * Setter method for ordering.
	 *
	 * @param ordering
	 *            the ordering to set
	 */
	public void setOrdering(List<ResultOrdering> ordering) {
		this.ordering = ordering;
	}

	/**
	 * Getter method for paging.
	 *
	 * @return the paging
	 */
	public ResultPaging getPaging() {
		return paging;
	}

	/**
	 * Setter method for paging.
	 *
	 * @param paging
	 *            the paging to set
	 */
	public void setPaging(ResultPaging paging) {
		this.paging = paging;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((entities == null) ? 0 : entities.hashCode());
		result = prime * result + ((ordering == null) ? 0 : ordering.hashCode());
		result = prime * result + ((paging == null) ? 0 : paging.hashCode());
		result = prime * result + ((uid == null) ? 0 : uid.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof Configuration))
			return false;
		Configuration other = (Configuration) obj;
		if (entities == null) {
			if (other.entities != null)
				return false;
		} else if (!entities.equals(other.entities))
			return false;
		if (ordering == null) {
			if (other.ordering != null)
				return false;
		} else if (!ordering.equals(other.ordering))
			return false;
		if (paging == null) {
			if (other.paging != null)
				return false;
		} else if (!paging.equals(other.paging))
			return false;
		if (uid == null) {
			if (other.uid != null)
				return false;
		} else if (!uid.equals(other.uid))
			return false;
		return true;
	}

}
