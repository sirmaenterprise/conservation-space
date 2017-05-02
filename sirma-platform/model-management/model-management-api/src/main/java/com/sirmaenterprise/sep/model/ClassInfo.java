package com.sirmaenterprise.sep.model;

import java.util.List;

/**
 * Wraps basic information about a single class.
 * 
 * @author Vilizar Tsonev
 */
public class ClassInfo {

	private String id;

	private String label;

	private String ontology;

	/** Contains only the direct parent(s). It is a list, because we imply that multiple-inheritance is possible **/
	private List<String> superClasses;

	/**
	 * Getter method for id.
	 *
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * Setter method for id.
	 *
	 * @param id
	 *            the id to set
	 */
	public ClassInfo setId(String id) {
		this.id = id;
		return this;
	}

	/**
	 * Getter method for label.
	 *
	 * @return the label
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * Setter method for label.
	 *
	 * @param label
	 *            the label to set
	 */
	public ClassInfo setLabel(String label) {
		this.label = label;
		return this;
	}

	/**
	 * Getter method for ontology.
	 *
	 * @return the ontology
	 */
	public String getOntology() {
		return ontology;
	}

	/**
	 * Setter method for ontology.
	 *
	 * @param ontology
	 *            the ontology to set
	 */
	public ClassInfo setOntology(String ontology) {
		this.ontology = ontology;
		return this;
	}

	/**
	 * Getter method for superClasses.
	 *
	 * @return the superClasses
	 */
	public List<String> getSuperClasses() {
		return superClasses;
	}

	/**
	 * Setter method for superClasses.
	 *
	 * @param superClasses
	 *            the superClasses to set
	 */
	public ClassInfo setSuperClasses(List<String> superClasses) {
		this.superClasses = superClasses;
		return this;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((label == null) ? 0 : label.hashCode());
		result = prime * result + ((ontology == null) ? 0 : ontology.hashCode());
		result = prime * result + ((superClasses == null) ? 0 : superClasses.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ClassInfo other = (ClassInfo) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (label == null) {
			if (other.label != null)
				return false;
		} else if (!label.equals(other.label))
			return false;
		if (ontology == null) {
			if (other.ontology != null)
				return false;
		} else if (!ontology.equals(other.ontology))
			return false;
		if (superClasses == null) {
			if (other.superClasses != null)
				return false;
		} else if (!superClasses.equals(other.superClasses))
			return false;
		return true;
	}

}
