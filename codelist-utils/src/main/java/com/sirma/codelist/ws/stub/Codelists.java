
package com.sirma.codelist.ws.stub;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


// TODO: Auto-generated Javadoc
/**
 * <p>Java class for codelists complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="codelists">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="codelist" type="{http://ws.ais.egov.sirma.com/codelists}codelist" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "codelists", propOrder = {
    "codelist"
})
public class Codelists {

    /** The codelist. */
    protected List<Codelist> codelist;

    /**
     * Gets the value of the codelist property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the codelist property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     * getCodelist().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     *
     * @return the codelist
     * {@link Codelist }
     */
    public List<Codelist> getCodelist() {
        if (codelist == null) {
            codelist = new ArrayList<Codelist>();
        }
        return this.codelist;
    }

}
