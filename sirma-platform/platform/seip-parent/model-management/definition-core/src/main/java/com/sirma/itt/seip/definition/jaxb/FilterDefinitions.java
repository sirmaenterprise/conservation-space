//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB)
// Reference Implementation, v2.2.6
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a>
// Any modifications to this file will be lost upon recompilation of the source
// schema.
// Generated on: 2013.02.15 at 03:17:22 PM EET
//

package com.sirma.itt.seip.definition.jaxb;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>
 * Java class for anonymous complex type.
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="filter" type="{}filterDefinition" maxOccurs="unbounded"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "filter" })
@XmlRootElement(name = "filterDefinitions")
public class FilterDefinitions {

	/** The filter. */
	@XmlElement(required = true)
	protected List<FilterDefinition> filter;

	/**
	 * Gets the value of the filter property.
	 * <p>
	 * This accessor method returns a reference to the live list, not a snapshot. Therefore any modification you make to
	 * the returned list will be present inside the JAXB object. This is why there is not a <CODE>set</CODE> method for
	 * the filter property.
	 * <p>
	 * For example, to add a new item, do as follows:
	 *
	 * <pre>
	 * getFilter().add(newItem);
	 * </pre>
	 * <p>
	 * Objects of the following type(s) are allowed in the list
	 *
	 * @return the filter {@link FilterDefinition }
	 */
	public List<FilterDefinition> getFilter() {
		if (filter == null) {
			filter = new ArrayList<>();
		}
		return filter;
	}

}