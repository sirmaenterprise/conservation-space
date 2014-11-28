
package com.sirma.codelist.ws.stub;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


// TODO: Auto-generated Javadoc
/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the com.sirma.codelist.ws.stub package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    /** The Constant _DataServiceFault_QNAME. */
    private final static QName _DataServiceFault_QNAME = new QName("http://ws.wso2.org/dataservice", "DataServiceFault");
    
    /** The Constant _Codelists_QNAME. */
    private final static QName _Codelists_QNAME = new QName("http://ws.ais.egov.sirma.com/codelists", "codelists");
    
    /** The Constant _Items_QNAME. */
    private final static QName _Items_QNAME = new QName("http://ws.ais.egov.sirma.com/codelists", "items");
    
    /** The Constant _REQUESTSTATUS_QNAME. */
    private final static QName _REQUESTSTATUS_QNAME = new QName("http://ws.wso2.org/dataservice", "REQUEST_STATUS");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: com.sirma.codelist.ws.stub
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link Codelist }.
     *
     * @return the codelist
     */
    public Codelist createCodelist() {
        return new Codelist();
    }

    /**
     * Create an instance of {@link Items }.
     *
     * @return the items
     */
    public Items createItems() {
        return new Items();
    }

    /**
     * Create an instance of {@link Item }.
     *
     * @return the item
     */
    public Item createItem() {
        return new Item();
    }

    /**
     * Create an instance of {@link Codelists }.
     *
     * @return the codelists
     */
    public Codelists createCodelists() {
        return new Codelists();
    }

    /**
     * Create an instance of {@link GetcodelistCl }.
     *
     * @return the getcodelist cl
     */
    public GetcodelistCl createGetcodelistCl() {
        return new GetcodelistCl();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}.
     *
     * @param value the value
     * @return the JAXB element< string>
     */
    @XmlElementDecl(namespace = "http://ws.wso2.org/dataservice", name = "DataServiceFault")
    public JAXBElement<String> createDataServiceFault(String value) {
        return new JAXBElement<String>(_DataServiceFault_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Codelists }{@code >}}.
     *
     * @param value the value
     * @return the JAXB element< codelists>
     */
    @XmlElementDecl(namespace = "http://ws.ais.egov.sirma.com/codelists", name = "codelists")
    public JAXBElement<Codelists> createCodelists(Codelists value) {
        return new JAXBElement<Codelists>(_Codelists_QNAME, Codelists.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Items }{@code >}}.
     *
     * @param value the value
     * @return the JAXB element< items>
     */
    @XmlElementDecl(namespace = "http://ws.ais.egov.sirma.com/codelists", name = "items")
    public JAXBElement<Items> createItems(Items value) {
        return new JAXBElement<Items>(_Items_QNAME, Items.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}.
     *
     * @param value the value
     * @return the JAXB element< string>
     */
    @XmlElementDecl(namespace = "http://ws.wso2.org/dataservice", name = "REQUEST_STATUS")
    public JAXBElement<String> createREQUESTSTATUS(String value) {
        return new JAXBElement<String>(_REQUESTSTATUS_QNAME, String.class, null, value);
    }

}
