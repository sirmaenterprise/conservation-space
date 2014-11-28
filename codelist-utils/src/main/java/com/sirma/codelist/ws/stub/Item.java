
package com.sirma.codelist.ws.stub;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Date;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;


// TODO: Auto-generated Javadoc
/**
 * <p>Java class for item complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="item">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="id" type="{http://www.w3.org/2001/XMLSchema}integer"/>
 *         &lt;element name="uri" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="value" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="codelist-number" type="{http://www.w3.org/2001/XMLSchema}integer"/>
 *         &lt;element name="master-value" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="description" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="value-order" type="{http://www.w3.org/2001/XMLSchema}integer"/>
 *         &lt;element name="comment" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="extra1" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="extra2" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="extra3" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="valid-from" type="{http://www.w3.org/2001/XMLSchema}date"/>
 *         &lt;element name="valid-to" type="{http://www.w3.org/2001/XMLSchema}date"/>
 *         &lt;element name="status-code" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="data-source" type="{http://www.w3.org/2001/XMLSchema}date"/>
 *         &lt;element name="last-modified" type="{http://www.w3.org/2001/XMLSchema}dateTime"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "item", propOrder = {
    "id",
    "uri",
    "value",
    "codelistNumber",
    "masterValue",
    "description",
    "valueOrder",
    "comment",
    "extra1",
    "extra2",
    "extra3",
    "validFrom",
    "validTo",
    "statusCode",
    "dataSource",
    "lastModified"
})
public class Item implements Serializable {

    /**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = 649565341753996402L;
	
	/** The id. */
	@XmlElement(required = true, nillable = true)
    protected BigInteger id;
    
    /** The uri. */
    @XmlElement(required = true, nillable = true)
    protected String uri;
    
    /** The value. */
    @XmlElement(required = true, nillable = true)
    protected String value;
    
    /** The codelist number. */
    @XmlElement(name = "codelist-number", required = true, nillable = true)
    protected BigInteger codelistNumber;
    
    /** The master value. */
    @XmlElement(name = "master-value", required = true, nillable = true)
    protected String masterValue;
    
    /** The description. */
    @XmlElement(required = true, nillable = true)
    protected String description;
    
    /** The value order. */
    @XmlElement(name = "value-order", required = true, nillable = true)
    protected BigInteger valueOrder;
    
    /** The comment. */
    @XmlElement(required = true, nillable = true)
    protected String comment;
    
    /** The extra1. */
    @XmlElement(required = true, nillable = true)
    protected String extra1;
    
    /** The extra2. */
    @XmlElement(required = true, nillable = true)
    protected String extra2;
    
    /** The extra3. */
    @XmlElement(required = true, nillable = true)
    protected String extra3;
    
    /** The valid from. */
    @XmlElement(name = "valid-from", required = true, nillable = true)
    @XmlSchemaType(name = "date")
    protected Date validFrom;
    
    /** The valid to. */
    @XmlElement(name = "valid-to", required = true, nillable = true)
    @XmlSchemaType(name = "date")
    protected Date validTo;
    
    /** The status code. */
    @XmlElement(name = "status-code", required = true, nillable = true)
    protected String statusCode;
    
    /** The data source. */
    @XmlElement(name = "data-source", required = true, nillable = true)
    @XmlSchemaType(name = "date")
    protected Date dataSource;
    
    /** The last modified. */
    @XmlElement(name = "last-modified", required = true, nillable = true)
    @XmlSchemaType(name = "dateTime")
    protected Date lastModified;

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
     * Gets the value of the value property.
     *
     * @return the value
     * possible object is
     * {@link String }
     */
    public String getValue() {
        return value;
    }

    /**
     * Sets the value of the value property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * Gets the value of the codelistNumber property.
     *
     * @return the codelist number
     * possible object is
     * {@link BigInteger }
     */
    public BigInteger getCodelistNumber() {
        return codelistNumber;
    }

    /**
     * Sets the value of the codelistNumber property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setCodelistNumber(BigInteger value) {
        this.codelistNumber = value;
    }

    /**
     * Gets the value of the masterValue property.
     *
     * @return the master value
     * possible object is
     * {@link String }
     */
    public String getMasterValue() {
        return masterValue;
    }

    /**
     * Sets the value of the masterValue property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMasterValue(String value) {
        this.masterValue = value;
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
     * Gets the value of the valueOrder property.
     *
     * @return the value order
     * possible object is
     * {@link BigInteger }
     */
    public BigInteger getValueOrder() {
        return valueOrder;
    }

    /**
     * Sets the value of the valueOrder property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setValueOrder(BigInteger value) {
        this.valueOrder = value;
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
     * Gets the value of the validFrom property.
     *
     * @return the valid from
     * possible object is
     * {@link Date }
     */
    public Date getValidFrom() {
        return validFrom;
    }

    /**
     * Sets the value of the validFrom property.
     * 
     * @param value
     *     allowed object is
     *     {@link Date }
     *     
     */
    public void setValidFrom(Date value) {
        this.validFrom = value;
    }

    /**
     * Gets the value of the validTo property.
     *
     * @return the valid to
     * possible object is
     * {@link Date }
     */
    public Date getValidTo() {
        return validTo;
    }

    /**
     * Sets the value of the validTo property.
     * 
     * @param value
     *     allowed object is
     *     {@link Date }
     *     
     */
    public void setValidTo(Date value) {
        this.validTo = value;
    }

    /**
     * Gets the value of the statusCode property.
     *
     * @return the status code
     * possible object is
     * {@link String }
     */
    public String getStatusCode() {
        return statusCode;
    }

    /**
     * Sets the value of the statusCode property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setStatusCode(String value) {
        this.statusCode = value;
    }

    /**
     * Gets the value of the dataSource property.
     *
     * @return the data source
     * possible object is
     * {@link Date }
     */
    public Date getDataSource() {
        return dataSource;
    }

    /**
     * Sets the value of the dataSource property.
     * 
     * @param value
     *     allowed object is
     *     {@link Date }
     *     
     */
    public void setDataSource(Date value) {
        this.dataSource = value;
    }

    /**
     * Gets the value of the lastModified property.
     *
     * @return the last modified
     * possible object is
     * {@link Date }
     */
    public Date getLastModified() {
        return lastModified;
    }

    /**
     * Sets the value of the lastModified property.
     * 
     * @param value
     *     allowed object is
     *     {@link Date }
     *     
     */
    public void setLastModified(Date value) {
        this.lastModified = value;
    }

}
