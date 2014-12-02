/*
 * Copyright [2005] [University Corporation for Advanced Internet Development, Inc.]
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

/**
 * 
 */
package org.opensaml.saml2.core.validator;

import javax.xml.namespace.QName;

import org.opensaml.common.xml.SAMLConstants;
import org.opensaml.saml2.core.Attribute;
import org.opensaml.saml2.core.AttributeQuery;

/**
 *
 */
public class AttributeQuerySchemaTest extends SubjectQuerySchemaTestBase {

    /**
     * Constructor
     *
     */
    public AttributeQuerySchemaTest() {
        super();
        targetQName = new QName(SAMLConstants.SAML20P_NS, AttributeQuery.DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML20P_PREFIX);
        validator = new AttributeQuerySchemaValidator();
    }
    
    /**
     *  Tests valid Attribute child elements.
     */
    public void testAttributesSuccess() {
        AttributeQuery query = (AttributeQuery) target;
        
        Attribute attrib;
        
        attrib = (Attribute) buildXMLObject(Attribute.DEFAULT_ELEMENT_NAME);
        attrib.setName("Foo");
        attrib.setNameFormat(Attribute.UNSPECIFIED);
        query.getAttributes().add(attrib);
        
        assertValidationPass("Attributes were valid");
        
        attrib = (Attribute) buildXMLObject(Attribute.DEFAULT_ELEMENT_NAME);
        attrib.setName("Bar");
        attrib.setNameFormat(Attribute.UNSPECIFIED);
        query.getAttributes().add(attrib);
        
        assertValidationPass("Attributes were valid");
        
        attrib = (Attribute) buildXMLObject(Attribute.DEFAULT_ELEMENT_NAME);
        attrib.setName("urn:test:attr:Baz");
        attrib.setNameFormat(Attribute.URI_REFERENCE);
        query.getAttributes().add(attrib);
        
        assertValidationPass("Attributes were valid");
        
        attrib = (Attribute) buildXMLObject(Attribute.DEFAULT_ELEMENT_NAME);
        // same name as before, different format
        attrib.setName("urn:test:attr:Baz");
        attrib.setNameFormat(Attribute.UNSPECIFIED);
        query.getAttributes().add(attrib);
        
        assertValidationPass("Attributes were valid");
        
        attrib = (Attribute) buildXMLObject(Attribute.DEFAULT_ELEMENT_NAME);
        attrib.setName("urn:test:attr:BlahBlah");
        attrib.setNameFormat(null);
        query.getAttributes().add(attrib);
        
        assertValidationPass("Attributes were valid");
    }

    /**
     *  Tests duplicate Attribute child elements.
     */
    public void testAttributesFailDuplicatesWithFormat() {
        AttributeQuery query = (AttributeQuery) target;
        
        Attribute attrib;
        
        attrib = (Attribute) buildXMLObject(Attribute.DEFAULT_ELEMENT_NAME);
        attrib.setName("Foo");
        attrib.setNameFormat(Attribute.UNSPECIFIED);
        query.getAttributes().add(attrib);

        attrib = (Attribute) buildXMLObject(Attribute.DEFAULT_ELEMENT_NAME);
        attrib.setName("Foo");
        attrib.setNameFormat(Attribute.UNSPECIFIED);
        query.getAttributes().add(attrib);
        
        assertValidationFail("Attributes were invalid, duplicates with explicit format");
    }
    
    /**
     *  Tests valid Attribute child elements, with null format.
     */
    public void testAttributesPassNoFormat() {
        AttributeQuery query = (AttributeQuery) target;
        
        Attribute attrib;
        
        attrib = (Attribute) buildXMLObject(Attribute.DEFAULT_ELEMENT_NAME);
        attrib.setName("Foo");
        attrib.setNameFormat(null);
        query.getAttributes().add(attrib);

        attrib = (Attribute) buildXMLObject(Attribute.DEFAULT_ELEMENT_NAME);
        attrib.setName("Bar");
        attrib.setNameFormat(null);
        query.getAttributes().add(attrib);
        
        assertValidationPass("Attributes were valid, no format specified");
    }
    
    /**
     *  Tests duplicate Attribute child elements, with null format.
     */
    public void testAttributesFailDuplicatesNoFormat() {
        AttributeQuery query = (AttributeQuery) target;
        
        Attribute attrib;
        
        attrib = (Attribute) buildXMLObject(Attribute.DEFAULT_ELEMENT_NAME);
        attrib.setName("Foo");
        attrib.setNameFormat(null);
        query.getAttributes().add(attrib);

        attrib = (Attribute) buildXMLObject(Attribute.DEFAULT_ELEMENT_NAME);
        attrib.setName("Foo");
        attrib.setNameFormat(null);
        query.getAttributes().add(attrib);
        
        assertValidationFail("Attributes were invalid, duplicates with no format specified");
    }
    
    /**
     *  Tests duplicate Attribute child elements, even out of order (not consecutive in document)
     *  with an intervening attribute with same Name for different NameFormat.
     *  This was to confirm bug where we were incorrectly tracking the seen attributes with a map structure.
     *  
     */
    public void testFailOutOfOrderDuplicates() {
        AttributeQuery query = (AttributeQuery) target;
        
        Attribute attrib;
        
        attrib = (Attribute) buildXMLObject(Attribute.DEFAULT_ELEMENT_NAME);
        attrib.setName("urn:test:foo");
        attrib.setNameFormat(Attribute.UNSPECIFIED);
        query.getAttributes().add(attrib);
        
        assertValidationPass("Attributes were valid");

        attrib = (Attribute) buildXMLObject(Attribute.DEFAULT_ELEMENT_NAME);
        attrib.setName("urn:test:foo");
        attrib.setNameFormat(Attribute.URI_REFERENCE);
        query.getAttributes().add(attrib);
        
        assertValidationPass("Attributes were valid");
        
        attrib = (Attribute) buildXMLObject(Attribute.DEFAULT_ELEMENT_NAME);
        attrib.setName("urn:test:foo");
        attrib.setNameFormat(Attribute.UNSPECIFIED);
        query.getAttributes().add(attrib);
        
        assertValidationFail("Attributes were invalid, non-consecutive duplicate was present");
    }
}
