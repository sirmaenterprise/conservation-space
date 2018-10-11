//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference
// Implementation, v2.2.6
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a>
// Any modifications to this file will be lost upon recompilation of the source schema.
// Generated on: 2013.06.24 at 07:33:10 PM EEST
//

package com.sirma.itt.seip.definition.jaxb;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>
 * Java class for configsType complex type.
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="configsType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="config" type="{}configType" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="filter" type="{}filterType" maxOccurs="unbounded" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "configsType", propOrder = { "config", "filter" })
public class ConfigsType {

	/** The config. */
	protected List<ConfigType> config;

	/** The filter. */
	protected List<FilterType> filter;

	/**
	 * Gets the value of the config property.
	 * <p>
	 * This accessor method returns a reference to the live list, not a snapshot. Therefore any modification you make to
	 * the returned list will be present inside the JAXB object. This is why there is not a <CODE>set</CODE> method for
	 * the config property.
	 * <p>
	 * For example, to add a new item, do as follows:
	 *
	 * <pre>
	 * getConfig().add(newItem);
	 * </pre>
	 * <p>
	 * Objects of the following type(s) are allowed in the list {@link ConfigType }
	 *
	 * @return the config
	 */
	public List<ConfigType> getConfig() {
		if (config == null) {
			config = new ArrayList<>();
		}
		return config;
	}

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
	 * Objects of the following type(s) are allowed in the list {@link FilterType }
	 *
	 * @return the filter
	 */
	public List<FilterType> getFilter() {
		if (filter == null) {
			filter = new ArrayList<>();
		}
		return filter;
	}

}