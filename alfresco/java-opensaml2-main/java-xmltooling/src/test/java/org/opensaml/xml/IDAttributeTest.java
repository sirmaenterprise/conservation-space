/*
 * Copyright [2006] [University Corporation for Advanced Internet Development, Inc.]
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.opensaml.xml;

import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;

import org.opensaml.xml.encryption.EncryptedData;
import org.opensaml.xml.encryption.EncryptedKey;
import org.opensaml.xml.io.Unmarshaller;
import org.opensaml.xml.io.UnmarshallingException;
import org.opensaml.xml.mock.SimpleXMLObject;
import org.opensaml.xml.parse.XMLParserException;
import org.opensaml.xml.schema.XSAny;
import org.opensaml.xml.signature.KeyInfo;
import org.opensaml.xml.util.XMLHelper;
import org.w3c.dom.Document;

/**
 * Unit test for unmarshalling functions.
 */
public class IDAttributeTest extends XMLObjectBaseTestCase {
    
    /**
     * Constructor.
     */
    public IDAttributeTest() {
        super();
    }

    /**
     * Simple test of ID attribute on a single object.
     */
    public void testSimpleUnmarshall() {
        SimpleXMLObject sxObject =  (SimpleXMLObject) unmarshallElement("/data/org/opensaml/xml/IDAttribute.xml");

        assertEquals("ID lookup failed", sxObject, sxObject.resolveID("IDLevel1"));
        assertEquals("ID lookup failed", sxObject, sxObject.resolveIDFromRoot("IDLevel1"));
        assertNull("Lookup of non-existent ID didn't return null", sxObject.resolveID("NonExistent"));
        
        sxObject.setId(null);
        assertNull("Lookup of removed ID (formerly extant) didn't return null", sxObject.resolveID("IDLevel1"));
        assertNull("Lookup of removed ID (formerly extant) didn't return null", sxObject.resolveIDFromRoot("IDLevel1"));
    }
    
    /**
     * Test of ID attributes on complex nested unmarshalled elements 
     * where children are singletons.
     */
    public void testComplexUnmarshall() {
        SimpleXMLObject sxObject = 
            (SimpleXMLObject) unmarshallElement("/data/org/opensaml/xml/IDAttributeWithChildren.xml");
        
        assertNull("Lookup of non-existent ID didn't return null", sxObject.resolveID("NonExistent"));
        
        assertEquals("ID lookup failed", sxObject, 
                sxObject.resolveID("SimpleElementID"));
        assertEquals("ID lookup failed", sxObject.getEncryptedData(), 
                sxObject.resolveID("EncryptedDataID"));
        assertEquals("ID lookup failed", sxObject.getEncryptedData().getKeyInfo(), 
                sxObject.resolveID("KeyInfoID"));
        assertEquals("ID lookup failed", sxObject.getEncryptedData().getKeyInfo().getEncryptedKeys().get(0), 
                sxObject.resolveID("EncryptedKeyID"));
        
        EncryptedData encData = sxObject.getEncryptedData();
        EncryptedKey encKey = sxObject.getEncryptedData().getKeyInfo().getEncryptedKeys().get(0);
        
        // testing resolveIDFromRoot
        assertNull("Lookup of ID not in this object's subtree didn't return null", encKey.resolveID("EncryptedDataID"));
        assertEquals("ID lookup failed", encData, encKey.resolveIDFromRoot("EncryptedDataID"));
    }
    
    /**
     *  Test propagation of various changes to ID attribute lookup
     *  where children are singletons.
     */
    public void testChangePropagation() {
        SimpleXMLObject sxObject =  
            (SimpleXMLObject) unmarshallElement("/data/org/opensaml/xml/IDAttributeWithChildren.xml");
        
        EncryptedData encData = sxObject.getEncryptedData();
        KeyInfo keyInfo = sxObject.getEncryptedData().getKeyInfo();
        EncryptedKey encKey = sxObject.getEncryptedData().getKeyInfo().getEncryptedKeys().get(0);
        
        encKey.setID("Foo");
        assertNull("Lookup of non-existent ID didn't return null", sxObject.resolveID("EncryptedKeyID"));
        assertEquals("ID lookup failed", encKey, sxObject.resolveID("Foo"));
        encKey.setID("EncryptedKeyID");
        assertEquals("ID lookup failed", encKey, sxObject.resolveID("EncryptedKeyID"));
        
        encKey.setID(null);
        assertNull("Lookup of non-existent ID didn't return null", sxObject.resolveID("EncryptedKeyID"));
        encKey.setID("EncryptedKeyID");
        assertEquals("ID lookup failed", encKey, sxObject.resolveID("EncryptedKeyID"));
        
        encData.setKeyInfo(null);
        assertEquals("ID lookup failed", sxObject, sxObject.resolveID("SimpleElementID"));
        assertEquals("ID lookup failed", encData, sxObject.resolveID("EncryptedDataID"));
        assertNull("Lookup of non-existent ID didn't return null", sxObject.resolveID("KeyInfoID"));
        assertNull("Lookup of non-existent ID didn't return null", sxObject.resolveID("EncryptedKeyID"));
        
        encData.setKeyInfo(keyInfo);
        assertEquals("ID lookup failed", sxObject, sxObject.resolveID("SimpleElementID"));
        assertEquals("ID lookup failed", encData, sxObject.resolveID("EncryptedDataID"));
        assertEquals("ID lookup failed", keyInfo, sxObject.resolveID("KeyInfoID"));
        assertEquals("ID lookup failed", encKey, sxObject.resolveID("EncryptedKeyID"));
        
        KeyInfo newKeyInfo = (KeyInfo) buildXMLObject(KeyInfo.DEFAULT_ELEMENT_NAME);
        newKeyInfo.setID("NewKeyInfoID");
        sxObject.getEncryptedData().setKeyInfo(newKeyInfo);
        assertEquals("ID lookup failed", sxObject, sxObject.resolveID("SimpleElementID"));
        assertEquals("ID lookup failed", encData, sxObject.resolveID("EncryptedDataID"));
        assertEquals("ID lookup failed", newKeyInfo, sxObject.resolveID("NewKeyInfoID"));
        assertNull("Lookup of non-existent ID didn't return null", sxObject.resolveID("EncryptedKeyID"));
    }
    
    /**
     * Test of ID attributes on complex nested unmarshalled elements 
     * where children are stored in an XMLObjectChildren list.
     */
    public void testComplexUnmarshallInList() {
        SimpleXMLObject sxObject = 
            (SimpleXMLObject) unmarshallElement("/data/org/opensaml/xml/IDAttributeWithChildrenList.xml");
        
        assertNull("Lookup of non-existent ID didn't return null", sxObject.resolveID("NonExistent"));
        assertNull("Lookup of non-existent ID didn't return null", sxObject.resolveIDFromRoot("NonExistent"));
        
        // Resolving from top-level root
        assertEquals("ID lookup failed", sxObject, 
                sxObject.resolveID("IDLevel1"));
        assertEquals("ID lookup failed", sxObject.getSimpleXMLObjects().get(0), 
                sxObject.resolveID("IDLevel2A"));
        assertEquals("ID lookup failed", sxObject.getSimpleXMLObjects().get(1), 
                sxObject.resolveID("IDLevel2B"));
        assertEquals("ID lookup failed", sxObject.getSimpleXMLObjects().get(3), 
                sxObject.resolveID("IDLevel2C"));
        assertEquals("ID lookup failed", sxObject.getSimpleXMLObjects().get(0).getSimpleXMLObjects().get(0), 
                sxObject.resolveID("IDLevel3A"));
        assertEquals("ID lookup failed", sxObject.getSimpleXMLObjects().get(0).getSimpleXMLObjects().get(2), 
                sxObject.resolveID("IDLevel3B"));
        assertEquals("ID lookup failed", sxObject.getSimpleXMLObjects().get(0).getSimpleXMLObjects().get(3), 
                sxObject.resolveID("IDLevel3C"));
        assertEquals("ID lookup failed", sxObject.getSimpleXMLObjects().get(0).getSimpleXMLObjects().get(0)
                .getSimpleXMLObjects().get(0), 
                sxObject.resolveID("IDLevel4A"));
        
        // Resolving from secondary level root
        assertEquals("ID lookup failed", sxObject.getSimpleXMLObjects().get(0), 
                sxObject.getSimpleXMLObjects().get(0).resolveID("IDLevel2A"));
        assertEquals("ID lookup failed", sxObject.getSimpleXMLObjects().get(0).getSimpleXMLObjects().get(0), 
                sxObject.getSimpleXMLObjects().get(0).resolveID("IDLevel3A"));
        assertEquals("ID lookup failed", sxObject.getSimpleXMLObjects().get(0).getSimpleXMLObjects().get(2), 
                sxObject.getSimpleXMLObjects().get(0).resolveID("IDLevel3B"));
        assertEquals("ID lookup failed", sxObject.getSimpleXMLObjects().get(0).getSimpleXMLObjects().get(3), 
                sxObject.getSimpleXMLObjects().get(0).resolveID("IDLevel3C"));
        assertEquals("ID lookup failed", sxObject.getSimpleXMLObjects().get(0).getSimpleXMLObjects().get(0)
                .getSimpleXMLObjects().get(0), 
                sxObject.getSimpleXMLObjects().get(0).resolveID("IDLevel4A"));
        assertNull("Lookup of non-existent ID didn't return null", 
                sxObject.getSimpleXMLObjects().get(0).resolveID("IDLevel1"));
        
        // Resolving from lower-level child to a non-ancestor object using resolveIDFromRoot.
        SimpleXMLObject sxoIDLevel4A = sxObject.getSimpleXMLObjects().get(0)
            .getSimpleXMLObjects().get(0).getSimpleXMLObjects().get(0); 
        SimpleXMLObject sxoIDLevel2C = sxObject.getSimpleXMLObjects().get(3);
        assertEquals("ID lookup failed", sxoIDLevel2C, sxoIDLevel4A.resolveIDFromRoot("IDLevel2C"));
    }
        
        
    /**
     *  Test propagation of various changes to ID attribute mappings due to attribute value changes
     *  where children are stored in an XMLObjectChildren list. 
     */
    public void testChangePropagationInList() {
        SimpleXMLObject sxObject =  
            (SimpleXMLObject) unmarshallElement("/data/org/opensaml/xml/IDAttributeWithChildrenList.xml");
        
        // Test propagation of attribute value change up the tree 
        sxObject.getSimpleXMLObjects().get(1).setId("NewIDLevel2B");
        assertEquals("ID lookup failed", sxObject.getSimpleXMLObjects().get(1), 
                sxObject.resolveID("NewIDLevel2B"));
        assertNull("Lookup of non-existent ID didn't return null", sxObject.resolveID("IDLevel2B"));
        
        sxObject.getSimpleXMLObjects().get(1).setId(null);
        assertNull("Lookup of non-existent ID didn't return null", sxObject.resolveID("NewIDLevel2B"));
        assertNull("Lookup of non-existent ID didn't return null", sxObject.resolveID("IDLevel2B"));
        
        sxObject.getSimpleXMLObjects().get(1).setId("IDLevel2B");
        assertEquals("ID lookup failed", sxObject.getSimpleXMLObjects().get(1), 
                sxObject.resolveID("IDLevel2B"));
        assertNull("Lookup of non-existent ID didn't return null", sxObject.resolveID("NewIDLevel2B"));
        
        sxObject.getSimpleXMLObjects().get(0).getSimpleXMLObjects().get(3).setId("NewIDLevel3C");
        assertEquals("ID lookup failed", sxObject.getSimpleXMLObjects().get(0).getSimpleXMLObjects().get(3), 
                sxObject.resolveID("NewIDLevel3C"));
        assertNull("Lookup of non-existent ID didn't return null", sxObject.resolveID("IDLevel3C"));
        
        sxObject.getSimpleXMLObjects().get(0).getSimpleXMLObjects().get(0).setId(null);
        assertNull("Lookup of non-existent ID didn't return null", sxObject.resolveID("IDLevel3A"));
        sxObject.getSimpleXMLObjects().get(0).getSimpleXMLObjects().get(0).setId("IDLevel3A");
        assertEquals("ID lookup failed", sxObject.getSimpleXMLObjects().get(0).getSimpleXMLObjects().get(0), 
                sxObject.resolveID("IDLevel3A"));
    }
        
    /**
     *  Test propagation of various changes to ID attribute mappings due to list operations
     *  where children are stored in an XMLObjectChildren list. 
     */
    public void testListOpChangePropagation() {
        
        SimpleXMLObject sxObject =  
            (SimpleXMLObject) unmarshallElement("/data/org/opensaml/xml/IDAttributeWithChildrenList.xml");
        
        SimpleXMLObject targetIDLevel3B = sxObject.getSimpleXMLObjects().get(0).getSimpleXMLObjects().get(2);
        assertEquals("ID lookup failed", targetIDLevel3B, sxObject.resolveID("IDLevel3B"));
        
        // remove(int)
        sxObject.getSimpleXMLObjects().get(0).getSimpleXMLObjects().remove(2);
        assertNull("Lookup of non-existent ID didn't return null", sxObject.resolveID("IDLevel3B"));
        // add(XMLObject)
        sxObject.getSimpleXMLObjects().get(0).getSimpleXMLObjects().add(targetIDLevel3B);
        assertEquals("ID lookup failed", targetIDLevel3B, sxObject.resolveID("IDLevel3B"));
        // remove(XMLObject)
        sxObject.getSimpleXMLObjects().get(0).getSimpleXMLObjects().remove(targetIDLevel3B);
        assertNull("Lookup of non-existent ID didn't return null", sxObject.resolveID("IDLevel3B"));
        // set(int, XMLObject)
        sxObject.getSimpleXMLObjects().get(0).getSimpleXMLObjects().set(1, targetIDLevel3B);
        assertEquals("ID lookup failed", targetIDLevel3B, sxObject.resolveID("IDLevel3B"));
        sxObject.getSimpleXMLObjects().get(0).getSimpleXMLObjects().remove(targetIDLevel3B);
        assertNull("Lookup of non-existent ID didn't return null", sxObject.resolveID("IDLevel3B"));
        
        // Ops using new object
        SimpleXMLObject newSimpleObject = (SimpleXMLObject) buildXMLObject(SimpleXMLObject.ELEMENT_NAME);
        newSimpleObject.setId("NewSimpleElement");
        
        sxObject.getSimpleXMLObjects().get(3).getSimpleXMLObjects().add(newSimpleObject);
        assertEquals("ID lookup failed", newSimpleObject, sxObject.resolveID("NewSimpleElement"));
        sxObject.getSimpleXMLObjects().get(3).getSimpleXMLObjects().remove(newSimpleObject);
        assertNull("Lookup of non-existent ID didn't return null", sxObject.resolveID("NewSimpleElement"));
        
        // clear
        sxObject.getSimpleXMLObjects().get(0).getSimpleXMLObjects().clear();
        assertNull("Lookup of non-existent ID didn't return null", sxObject.resolveID("IDLevel3A"));
        assertNull("Lookup of non-existent ID didn't return null", sxObject.resolveID("IDLevel3B"));
        assertNull("Lookup of non-existent ID didn't return null", sxObject.resolveID("IDLevel3C"));
        assertNull("Lookup of non-existent ID didn't return null", sxObject.resolveID("IDLevel4A"));
    }
    
    /**
     * Tests registering ID-to-XMLObject mapping when unmarshalling unknown content,
     * using the AttributeMap supplied by way of the AttributeExtensibleXMLObject interface.
     * This tests general AttributeMap functionality on unmarshalling.
     * 
     * For purposes of this test, the attribute in the control XML file with
     * local name "id" on element "product" will be treated as an ID type.
     * 
     * @throws XMLParserException when parser encounters an error
     * @throws UnmarshallingException when unmarshaller encounters an error
     */
    public void testAttributeMap() throws XMLParserException, UnmarshallingException{
        String documentLocation = "/data/org/opensaml/xml/IDAttributeWithAttributeMap.xml";
        Document document = parserPool.parse(IDAttributeTest.class.getResourceAsStream(documentLocation));

        Unmarshaller unmarshaller = unmarshallerFactory.getUnmarshaller(Configuration.getDefaultProviderQName());
        XMLObject xmlobject = unmarshaller.unmarshall(document.getDocumentElement());
        
        XSAny epParent = (XSAny) xmlobject;
        assertNotNull("Cast of parent to XSAny failed", epParent);
        
        XSAny epChild0 = (XSAny) epParent.getUnknownXMLObjects().get(0);
        assertNotNull("Cast of child 0 to XSAny failed", epChild0);
        
        XSAny epChild1 = (XSAny) epParent.getUnknownXMLObjects().get(1);
        assertNotNull("Cast of child 1 to XSAny failed", epChild1);
        
        // Since not doing schema validation, etc, the parser won't register the ID type in the DOM
        // (i.e. DOM Attribute.isId() will fail) and so the unmarshaller won't be able to register 
        // the "id" attribute as an ID type. This is expected.
        assertNull("Lookup of non-existent ID mapping didn't return null", epParent.resolveID("1144"));
        assertNull("Lookup of non-existent ID mapping didn't return null", epParent.resolveID("1166"));
        
        // Now manually register the "id" attribute in the AttributeMap of child 0 as being an ID type.
        // This should cause the expected ID-to-XMLObject mapping behaviour to take place.
        QName idName = XMLHelper.constructQName(null, "id", null);
        epChild0.getUnknownAttributes().registerID(idName);
        assertEquals("Lookup of ID mapping failed", epChild0, epParent.resolveID("1144"));
        
        // Resolving from child1 to child0, which is not an ancestor of child1, using resolveIDFromRoot
        assertNull("Lookup of non-existent ID mapping didn't return null", epChild1.resolveID("1144"));
        assertEquals("Lookup of ID mapping failed", epChild0, epChild1.resolveIDFromRoot("1144"));
    }
    
    /**
     * Tests registering ID-to-XMLObject mapping when unmarshalling unknown content,
     * using the AttributeMap supplied by way of the AttributeExtensibleXMLObject interface.
     * This test tests propagation of changes on the various AttributeMap operations.
     * 
     * For purposes of this test, the attribute in the control XML file with
     * local name "id" on element "product" will be treated as an ID type.
     * 
     * @throws XMLParserException when parser encounters an error
     * @throws UnmarshallingException when unmarshaller encounters an error
     */
    public void testAttributeMapOps() throws XMLParserException, UnmarshallingException{
        String documentLocation = "/data/org/opensaml/xml/IDAttributeWithAttributeMap.xml";
        Document document = parserPool.parse(IDAttributeTest.class.getResourceAsStream(documentLocation));

        Unmarshaller unmarshaller = unmarshallerFactory.getUnmarshaller(Configuration.getDefaultProviderQName());
        XMLObject xmlobject = unmarshaller.unmarshall(document.getDocumentElement());
        
        XSAny epParent = (XSAny) xmlobject;
        assertNotNull("Cast of parent to XSAny failed", epParent);
        
        XSAny epChild0 = (XSAny) epParent.getUnknownXMLObjects().get(0);
        assertNotNull("Cast of child 0 to XSAny failed", epChild0);
        
        // Now manually register the "id" attribute in the AttributeMap of child 0 as being an ID type.
        // This should cause the expected ID-to-XMLObject mapping behaviour to take place.
        QName idName = XMLHelper.constructQName(null, "id", null);
        epChild0.getUnknownAttributes().registerID(idName);
        assertEquals("Lookup of ID mapping failed", epChild0, epParent.resolveID("1144"));
        
        // AttributeMap op tests
        // put
        epChild0.getUnknownAttributes().put(idName, "9999");
        assertNull("Lookup of non-existent ID mapping didn't return null", epParent.resolveID("1144"));
        assertEquals("Lookup of ID mapping failed", epChild0, epParent.resolveID("9999"));
        // remove
        epChild0.getUnknownAttributes().remove(idName);
        assertNull("Lookup of non-existent ID mapping didn't return null", epParent.resolveID("9999"));
        // putAll
        Map<QName, String> attribs = new HashMap<QName, String>();
        attribs.put(idName, "1967");
        epChild0.getUnknownAttributes().putAll(attribs);
        assertEquals("Lookup of ID mapping failed", epChild0, epParent.resolveID("1967"));
        // clear
        epChild0.getUnknownAttributes().clear();
        assertNull("Lookup of non-existent ID mapping didn't return null", epParent.resolveID("1967"));
        // deregisterID
        epChild0.getUnknownAttributes().put(idName, "abc123");
        assertEquals("Lookup of ID mapping failed", epChild0, epParent.resolveID("abc123"));
        epChild0.getUnknownAttributes().deregisterID(idName);
        assertNull("Lookup of non-existent ID mapping didn't return null", epParent.resolveID("abc123"));
    }
    
    /**
     * Tests that attributes registered globally on {@link org.opensaml.xml.Configuration} are being
     * handled properly in the AttributeMap.
     * @throws XMLParserException 
     * @throws UnmarshallingException 
     */
    public void testGlobalIDRegistration() throws XMLParserException, UnmarshallingException {
        XMLObject xmlObject;
        QName attribQName = new QName("http://www.example.org", "id", "test");
        
        String documentLocation = "/data/org/opensaml/xml/IDAttributeGlobal.xml";
        Document document = parserPool.parse(IDAttributeTest.class.getResourceAsStream(documentLocation));
        Unmarshaller unmarshaller = unmarshallerFactory.getUnmarshaller(Configuration.getDefaultProviderQName());
        
        // With no registration
        xmlObject = unmarshaller.unmarshall(document.getDocumentElement());
        assertNull("Lookup of non-existent ID mapping didn't return null", xmlObject.resolveID("GlobalID1"));
        assertNull("Lookup of non-existent ID mapping didn't return null", xmlObject.resolveID("GlobalID2"));
        
        // Now register the attribute QName in the global config
        Configuration.registerIDAttribute(attribQName);
        document = parserPool.parse(IDAttributeTest.class.getResourceAsStream(documentLocation));
        xmlObject = unmarshaller.unmarshall(document.getDocumentElement());
        assertEquals("Lookup of ID mapping failed", xmlObject, xmlObject.resolveID("GlobalID1"));
        assertEquals("Lookup of ID mapping failed", ((XSAny) xmlObject).getUnknownXMLObjects().get(0),
                xmlObject.resolveID("GlobalID2"));
        
        // After deregistration
        Configuration.deregisterIDAttribute(attribQName);
        document = parserPool.parse(IDAttributeTest.class.getResourceAsStream(documentLocation));
        xmlObject = unmarshaller.unmarshall(document.getDocumentElement());
        assertNull("Lookup of non-existent ID mapping didn't return null", xmlObject.resolveID("GlobalID1"));
        assertNull("Lookup of non-existent ID mapping didn't return null", xmlObject.resolveID("GlobalID2"));
    }
        
}