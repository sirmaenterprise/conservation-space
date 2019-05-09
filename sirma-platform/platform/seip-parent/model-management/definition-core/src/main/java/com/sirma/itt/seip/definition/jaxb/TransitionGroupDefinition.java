package com.sirma.itt.seip.definition.jaxb;

import java.math.BigInteger;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>
 * Java class for transitionGroupDefinition complex type.
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "transitionGroupDefinition")
public class TransitionGroupDefinition {

	@XmlAttribute(name = "id", required = true)
	protected String id;
	@XmlAttribute(name = "label")
	protected String label;
	@XmlAttribute(name = "order")
	protected BigInteger order;
	@XmlAttribute(name = "parent")
	protected String parent;
	@XmlAttribute(name = "type")
	protected String type;

	/**
	 * Gets the value of the id property.
	 *
	 * @return possible object is {@link String }
	 */
	public String getId() {
		return id;
	}

	/**
	 * Sets the value of the id property.
	 *
	 * @param value
	 *            allowed object is {@link String }
	 */
	public void setId(String value) {
		this.id = value;
	}

	/**
	 * Gets the value of the label property.
	 *
	 * @return possible object is {@link String }
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * Sets the value of the label property.
	 *
	 * @param value
	 *            allowed object is {@link String }
	 */
	public void setLabel(String value) {
		this.label = value;
	}

	/**
	 * Gets the value of the order property.
	 *
	 * @return possible object is {@link BigInteger }
	 */
	public BigInteger getOrder() {
		return order;
	}

	/**
	 * Sets the value of the order property.
	 *
	 * @param value
	 *            allowed object is {@link BigInteger }
	 */
	public void setOrder(BigInteger value) {
		this.order = value;
	}

	/**
	 * Gets the value of the parent property.
	 *
	 * @return possible object is {@link String }
	 */
	public String getParent() {
		return parent;
	}

	/**
	 * Sets the value of the parent property.
	 *
	 * @param parent
	 *            the parent
	 */
	public void setParent(String parent) {
		this.parent = parent;
	}

	/**
	 * Gets the value of the type property.
	 *
	 * @return possible object is {@link String }
	 */
	public String getType() {
		return type;
	}

	/**
	 * Sets the value of the type property.
	 *
	 * @param type
	 *            the type
	 */
	public void setType(String type) {
		this.type = type;
	}

}
