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

import javax.xml.namespace.QName;

import org.opensaml.common.BaseSAMLObjectProviderTestCase;
import org.opensaml.common.xml.SAMLConstants;
import org.opensaml.saml1.core.ConfirmationMethod;
import org.opensaml.saml1.core.SubjectConfirmation;
import org.opensaml.saml1.core.SubjectConfirmationData;
import org.opensaml.xml.schema.impl.XSAnyBuilder;
import org.w3c.dom.Document;

/**
 * Test for {@link org.opensaml.saml1.core.impl.Subject}
 */
public class SubjectConfirmationTest extends BaseSAMLObjectProviderTestCase {

    /** name used to generate objects */
    private final QName qname;

    private String fullElementsFile;

    private Document expectedFullDOM;

    /**
     * Constructor
     */
    public SubjectConfirmationTest() {
        super();

        singleElementFile = "/data/org/opensaml/saml1/impl/singleSubjectConfirmation.xml";
        singleElementOptionalAttributesFile = "/data/org/opensaml/saml1/impl/singleSubjectConfirmation.xml";
        fullElementsFile = "/data/org/opensaml/saml1/impl/SubjectConfirmationWithChildren.xml";
        qname = new QName(SAMLConstants.SAML1_NS, SubjectConfirmation.DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML1_PREFIX);
    }

    /** {@inheritDoc} */
    protected void setUp() throws Exception {
        super.setUp();

        expectedFullDOM = parser.parse(BaseSAMLObjectProviderTestCase.class
                .getResourceAsStream(fullElementsFile));
    }

    /** {@inheritDoc} */
    public void testSingleElementUnmarshall() {
        SubjectConfirmation subjectConfirmation = (SubjectConfirmation) unmarshallElement(singleElementFile);

        assertEquals("Non zero number of child ConfirmationMethods elements", 0, subjectConfirmation
                .getConfirmationMethods().size());
        assertNull("Non zero number of child SubjectConfirmationData elements", subjectConfirmation
                .getSubjectConfirmationData());
    }

    /** {@inheritDoc} */
    public void testSingleElementOptionalAttributesUnmarshall() {
        // No attributes
    }

    /**
     * Test an XML file with children
     */
    public void testFullElementsUnmarshall() {
        SubjectConfirmation subjectConfirmation = (SubjectConfirmation) unmarshallElement(fullElementsFile);

        assertEquals("Number of ConfirmationMethods", 2, subjectConfirmation.getConfirmationMethods().size());
        assertNotNull("Zero child SubjectConfirmationData elements", subjectConfirmation.getSubjectConfirmationData());
    }

    /** {@inheritDoc} */
    public void testSingleElementMarshall() {
        assertEquals(expectedDOM, buildXMLObject(qname));
    }

    /** {@inheritDoc} */
    public void testSingleElementOptionalAttributesMarshall() {
        // No attributes
    }

    /*
     * Generate an subject with contents
     */

    public void testFullElementsMarshall() {
        SubjectConfirmation subjectConfirmation = (SubjectConfirmationImpl) buildXMLObject(qname);

        QName oqname = new QName(SAMLConstants.SAML1_NS, ConfirmationMethod.DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML1_PREFIX);
        subjectConfirmation.getConfirmationMethods().add((ConfirmationMethod) buildXMLObject(oqname));
        subjectConfirmation.getConfirmationMethods().add((ConfirmationMethod) buildXMLObject(oqname));
        
        XSAnyBuilder proxyBuilder = new XSAnyBuilder();
        oqname = new QName(SAMLConstants.SAML1_NS, SubjectConfirmationData.DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML1_PREFIX);
        subjectConfirmation.setSubjectConfirmationData(proxyBuilder.buildObject(oqname));

        assertEquals(expectedFullDOM, subjectConfirmation);
    }
}
