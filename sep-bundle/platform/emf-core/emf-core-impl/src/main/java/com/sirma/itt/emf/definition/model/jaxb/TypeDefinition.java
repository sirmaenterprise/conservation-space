//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference
// Implementation, v2.2.6
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a>
// Any modifications to this file will be lost upon recompilation of the source schema.
// Generated on: 2013.11.21 at 04:23:04 PM EET
//

package com.sirma.itt.emf.definition.model.jaxb;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>
 * Java class for typeDefinition complex type.
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="typeDefinition">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="name" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="title" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="description" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="javaClassName" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="uri" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "typeDefinition", propOrder = { "name", "title", "description", "javaClassName",
		"uri" })
public class TypeDefinition {

	/** The name. */
	@XmlElement(required = true)
	protected String name;

	/** The title. */
	protected String title;

	/** The description. */
	protected String description;

	/** The java class name. */
	@XmlElement(required = true)
	protected String javaClassName;

	/** The uri. */
	protected String uri;

	/**
	 * Gets the value of the name property.
	 * 
	 * @return the name possible object is {@link String }
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the value of the name property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 */
	public void setName(String value) {
		this.name = value;
	}

	/**
	 * Gets the value of the title property.
	 * 
	 * @return the title possible object is {@link String }
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * Sets the value of the title property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 */
	public void setTitle(String value) {
		this.title = value;
	}

	/**
	 * Gets the value of the description property.
	 * 
	 * @return the description possible object is {@link String }
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Sets the value of the description property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 */
	public void setDescription(String value) {
		this.description = value;
	}

	/**
	 * Gets the value of the javaClassName property.
	 * 
	 * @return the java class name possible object is {@link String }
	 */
	public String getJavaClassName() {
		return javaClassName;
	}

	/**
	 * Sets the value of the javaClassName property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 */
	public void setJavaClassName(String value) {
		this.javaClassName = value;
	}

	/**
	 * Gets the value of the uri property.
	 * 
	 * @return the uri possible object is {@link String }
	 */
	public String getUri() {
		return uri;
	}

	/**
	 * Sets the value of the uri property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 */
	public void setUri(String value) {
		this.uri = value;
	}

}