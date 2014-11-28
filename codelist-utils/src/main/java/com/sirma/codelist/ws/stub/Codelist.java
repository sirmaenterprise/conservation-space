
package com.sirma.codelist.ws.stub;

import java.io.Serializable;
import java.math.BigInteger;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

//TODO: Auto-generated Javadoc
/**
 * <p>Java class for codelist complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="codelist">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="id" type="{http://www.w3.org/2001/XMLSchema}integer"/>
 *         &lt;element name="master-codelist" type="{http://www.w3.org/2001/XMLSchema}integer"/>
 *         &lt;element name="uri" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="description" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="extra1" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="extra2" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="extra3" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="display-type" type="{http://www.w3.org/2001/XMLSchema}integer"/>
 *         &lt;element name="comment" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element ref="{http://ws.ais.egov.sirma.com/codelists}items"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "codelist", propOrder = {
    "id",
    "masterCodelist",
    "uri",
    "description",
    "extra1",
    "extra2",
    "extra3",
    "displayType",
    "comment",
    "items"
})
public class Codelist implements Serializable {

    /**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = -9177942721011696107L;
	
	/** The id. */
	@XmlElement(required = true, nillable = true)
    protected BigInteger id;
    
    /** The master codelist. */
    @XmlElement(name = "master-codelist", required = true, nillable = true)
    protected BigInteger masterCodelist;
    
    /** The uri. */
    @XmlElement(required = true, nillable = true)
    protected String uri;
    
    /** The description. */
    @XmlElement(required = true, nillable = true)
    protected String description;
    
    /** The extra1. */
    @XmlElement(required = true, nillable = true)
    protected String extra1;
    
    /** The extra2. */
    @XmlElement(required = true, nillable = true)
    protected String extra2;
    
    /** The extra3. */
    @XmlElement(required = true, nillable = true)
    protected String extra3;
    
    /** The display type. */
    @XmlElement(name = "display-type", required = true, nillable = true)
    protected BigInteger displayType;
    
    /** The comment. */
    @XmlElement(required = true, nillable = true)
    protected String comment;
    
    /** The items. */
    @XmlElement(required = true)
    protected Items items;

    /**
     * Gets the value of the id property.
     *
     * @return the id
     * possible object is
     * {@link BigInteger }
     */
    public BigInteger getId() {
        return id;
    }

    /**
     * Sets the value of the id property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setId(BigInteger value) {
        this.id = value;
    }

    /**
     * Gets the value of the masterCodelist property.
     *
     * @return the master codelist
     * possible object is
     * {@link BigInteger }
     */
    public BigInteger getMasterCodelist() {
        return masterCodelist;
    }

    /**
     * Sets the value of the masterCodelist property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setMasterCodelist(BigInteger value) {
        this.masterCodelist = value;
    }

    /**
     * Gets the value of the uri property.
     *
     * @return the uri
     * possible object is
     * {@link String }
     */
    public String getUri() {
        return uri;
    }

    /**
     * Sets the value of the uri property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setUri(String value) {
        this.uri = value;
    }

    /**
     * Gets the value of the description property.
     *
     * @return the description
     * possible object is
     * {@link String }
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the value of the description property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDescription(String value) {
        this.description = value;
    }

    /**
     * Gets the value of the extra1 property.
     *
     * @return the extra1
     * possible object is
     * {@link String }
     */
    public String getExtra1() {
        return extra1;
    }

    /**
     * Sets the value of the extra1 property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setExtra1(String value) {
        this.extra1 = value;
    }

    /**
     * Gets the value of the extra2 property.
     *
     * @return the extra2
     * possible object is
     * {@link String }
     */
    public String getExtra2() {
        return extra2;
    }

    /**
     * Sets the value of the extra2 property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setExtra2(String value) {
        this.extra2 = value;
    }

    /**
     * Gets the value of the extra3 property.
     *
     * @return the extra3
     * possible object is
     * {@link String }
     */
    public String getExtra3() {
        return extra3;
    }

    /**
     * Sets the value of the extra3 property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setExtra3(String value) {
        this.extra3 = value;
    }

    /**
     * Gets the value of the displayType property.
     *
     * @return the display type
     * possible object is
     * {@link BigInteger }
     */
    public BigInteger getDisplayType() {
        return displayType;
    }

    /**
     * Sets the value of the displayType property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setDisplayType(BigInteger value) {
        this.displayType = value;
    }

    /**
     * Gets the value of the comment property.
     *
     * @return the comment
     * possible object is
     * {@link String }
     */
    public String getComment() {
        return comment;
    }

    /**
     * Sets the value of the comment property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setComment(String value) {
        this.comment = value;
    }

    /**
     * Gets the value of the items property.
     *
     * @return the items
     * possible object is
     * {@link Items }
     */
    public Items getItems() {
        return items;
    }

    /**
     * Sets the value of the items property.
     * 
     * @param value
     *     allowed object is
     *     {@link Items }
     *     
     */
    public void setItems(Items value) {
        this.items = value;
    }

}
