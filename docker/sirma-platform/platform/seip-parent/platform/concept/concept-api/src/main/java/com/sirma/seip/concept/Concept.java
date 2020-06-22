package com.sirma.seip.concept;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Carries the basic data of a skos concept.
 * 
 * @author Vilizar Tsonev
 */
public class Concept {

	private String id;

	private String title;

	private List<Concept> ancestors;

	private String parent;

	/**
	 * Constructs the concept.
	 * 
	 * @param id
	 *            is the concept id
	 * @param title
	 *            is the concept title
	 * @param ancestors
	 *            are the concept's ancestors
	 * @param parent
	 *            is the parent node
	 */
	public Concept(String id, String title, List<Concept> ancestors, String parent) {
		super();
		this.id = id;
		this.title = title;
		this.ancestors = ancestors;
		this.parent = parent;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public List<Concept> getAncestors() {
		return ancestors;
	}

	public void setAncestors(List<Concept> ancestors) {
		this.ancestors = ancestors;
	}

	@JsonIgnore
	public String getParent() {
		return parent;
	}

	public void setParent(String parent) {
		this.parent = parent;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int res = 1;
		res = prime * res + ((ancestors == null) ? 0 : ancestors.hashCode());
		res = prime * res + ((id == null) ? 0 : id.hashCode());
		res = prime * res + ((parent == null) ? 0 : parent.hashCode());
		res = prime * res + ((title == null) ? 0 : title.hashCode());
		return res;
	}

	@Override
	public boolean equals(Object object) { // NOSONAR
		if (this == object)
			return true;
		if (object == null)
			return false;
		if (getClass() != object.getClass())
			return false;
		Concept other = (Concept) object;
		if (ancestors == null) {
			if (other.ancestors != null)
				return false;
		} else if (!ancestors.equals(other.ancestors))
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (parent == null) {
			if (other.parent != null)
				return false;
		} else if (!parent.equals(other.parent))
			return false;
		if (title == null) {
			if (other.title != null)
				return false;
		} else if (!title.equals(other.title))
			return false;
		return true;
	}
}
