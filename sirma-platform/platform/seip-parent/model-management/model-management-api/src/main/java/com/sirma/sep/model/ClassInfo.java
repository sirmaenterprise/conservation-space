package com.sirma.sep.model;

import java.util.List;
import java.util.Objects;

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
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		ClassInfo classInfo = (ClassInfo) o;
		return Objects.equals(id, classInfo.id) &&
				Objects.equals(label, classInfo.label) &&
				Objects.equals(ontology, classInfo.ontology) &&
				Objects.equals(superClasses, classInfo.superClasses);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, label, ontology, superClasses);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ClassInfo [id=").append(id).append(", ontology=").append(ontology).append("]");
		return builder.toString();
	}

}
