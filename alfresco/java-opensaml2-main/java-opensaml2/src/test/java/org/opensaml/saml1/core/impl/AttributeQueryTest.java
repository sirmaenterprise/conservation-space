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

package org.opensaml.saml1.core.impl;

import java.util.List;

import javax.xml.namespace.QName;

import org.opensaml.common.BaseSAMLObjectProviderTestCase;
import org.opensaml.common.xml.SAMLConstants;
import org.opensaml.saml1.core.AttributeDesignator;
import org.opensaml.saml1.core.AttributeQuery;
import org.opensaml.saml1.core.Subject;

/**
 * Test class for org.opensaml.saml1.core.AttributeQuery
 */
public class AttributeQueryTest extends BaseSAMLObjectProviderTestCase {

    /** name used to generate objects */
    private final QName qname;

   /** The expected value of the Resource Identifier */
    private final String expectedResource;

    /**
     * Constructor
     */
    public AttributeQueryTest() {
        singleElementFile = "/data/org/opensaml/saml1/impl/singleAttributeQuery.xml";
        singleElementOptionalAttributesFile = "/data/org/opensaml/saml1/impl/singleAttributeQueryAttributes.xml";
        childElementsFile = "/data/org/opensaml/saml1/impl/AttributeQueryWithChildren.xml";
        expectedResource = "resource";
        qname = new QName(SAMLConstants.SAML10P_NS, AttributeQuery.DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML1P_PREFIX);
    }

    /** {@inheritDoc} */
    public void testSingleElementUnmarshall() {

        AttributeQuery attributeQuery;
        attributeQuery = (AttributeQuery) unmarshallElement(singleElementFile);

        assertNull("Resource attribute present", attributeQuery.getResource());
        assertNull("Subject element present", attributeQuery.getSubject());
        assertEquals("Count of AttributeDesignator elements", 0, attributeQuery.getAttributeDesignators().size());
    }

    /** {@inheritDoc} */
    public void testSingleElementOptionalAttributesUnmarshall() {
        AttributeQuery attributeQuery;
        attributeQuery = (AttributeQuery) unmarshallElement(singleElementOptionalAttributesFile);

        assertEquals("Resource attribute", expectedResource, attributeQuery.getResource());
        assertNull("Subject element present", attributeQuery.getSubject());
        assertEquals("Count of AttributeDesignator elements", 0, attributeQuery.getAttributeDesignators().size());
    }

    /** {@inheritDoc} */
    public void testChildElementsUnmarshall() {
        AttributeQuery attributeQuery;
        attributeQuery = (AttributeQuery) unmarshallElement(childElementsFile);

        assertNotNull("Subject element present", attributeQuery.getSubject());
        assertEquals("Count of AttributeDesignator elements", 4, attributeQuery.getAttributeDesignators().size());
    }

    /** {@inheritDoc} */
    public void testSingleElementMarshall() {
        assertEquals(expectedDOM, buildXMLObject(qname));
    }

    /** {@inheritDoc} */
    public void testSingleElementOptionalAttributesMarshall() {
        AttributeQuery attributeQuery;
        attributeQuery = (AttributeQuery) buildXMLObject(qname);

        attributeQuery.setResource(expectedResource);
        assertEquals(expectedOptionalAttributesDOM, attributeQuery);
    }

    /** {@inheritDoc} */
    public void testChildElementsMarshall() {
        AttributeQuery attributeQuery = (AttributeQuery) buildXMLObject(qname);

        attributeQuery.setSubject((Subject) buildXMLObject(new QName(SAMLConstants.SAML1_NS, Subject.DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML1_PREFIX)));
        List <AttributeDesignator> list = attributeQuery.getAttributeDesignators();
        QName attqname = new QName(SAMLConstants.SAML1_NS, AttributeDesignator.DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML1_PREFIX);  
        list.add((AttributeDesignator) buildXMLObject(attqname));
        list.add((AttributeDesignator) buildXMLObject(attqname));
        list.add((AttributeDesignator) buildXMLObject(attqname)); 
        list.add((AttributeDesignator) buildXMLObject(attqname)); 
        assertEquals(expectedChildElementsDOM, attributeQuery);

    }

}
