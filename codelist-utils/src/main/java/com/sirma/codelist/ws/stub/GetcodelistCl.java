
package com.sirma.codelist.ws.stub;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


// TODO: Auto-generated Javadoc
/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="cl" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "cl"
})
@XmlRootElement(name = "_getcodelist_cl")
public class GetcodelistCl {

    /** The cl. */
    @XmlElement(required = true, nillable = true)
    protected String cl;

    /**
     * Gets the value of the cl property.
     *
     * @return the cl
     * possible object is
     * {@link String }
     */
    public String getCl() {
        return cl;
    }

    /**
     * Sets the value of the cl property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCl(String value) {
        this.cl = value;
    }

}
