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
import org.opensaml.saml1.core.Action;
import org.opensaml.saml1.core.AuthorizationDecisionQuery;
import org.opensaml.saml1.core.Evidence;
import org.opensaml.saml1.core.Subject;
import org.w3c.dom.Document;

/**
 * Test class for org.opensaml.saml1.core.AttributeQuery
 */
public class AuthorizationDecisionQueryTest extends BaseSAMLObjectProviderTestCase {

    /** name used to generate objects */
    private final QName qname;

    /** A file with a AuthenticationQuery with kids */

    private final String fullElementsFile;

    /** The expected result of a marshalled multiple element */

    private Document expectedFullDOM;

    private final String expectedResource;

    /**
     * Constructor
     */
    public AuthorizationDecisionQueryTest() {
        singleElementFile = "/data/org/opensaml/saml1/impl/singleAuthorizationDecisionQuery.xml";
        singleElementOptionalAttributesFile = "/data/org/opensaml/saml1/impl/singleAuthorizationDecisionQueryAttributes.xml";
        fullElementsFile = "/data/org/opensaml/saml1/impl/AuthorizationDecisionQueryWithChildren.xml";

        expectedResource = "resource";
        
        qname =new QName(SAMLConstants.SAML10P_NS, AuthorizationDecisionQuery.DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML1P_PREFIX);
    }

    /** {@inheritDoc} */
    protected void setUp() throws Exception {
        super.setUp();

        expectedFullDOM = parser.parse(BaseSAMLObjectProviderTestCase.class
                .getResourceAsStream(fullElementsFile));
    }

    /** {@inheritDoc} */
    public void testSingleElementUnmarshall() {

        AuthorizationDecisionQuery authorizationDecisionQuery;
        authorizationDecisionQuery = (AuthorizationDecisionQuery) unmarshallElement(singleElementFile);

        assertNull("Resource attribute present", authorizationDecisionQuery.getResource());
        assertNull("Subject element present", authorizationDecisionQuery.getSubject());
        assertEquals("Count of AttributeDesignator elements", 0, authorizationDecisionQuery.getActions().size());
        assertNull("Evidence element present", authorizationDecisionQuery.getEvidence());
    }

    /** {@inheritDoc} */
    public void testSingleElementOptionalAttributesUnmarshall() {
        AuthorizationDecisionQuery authorizationDecisionQuery;
        authorizationDecisionQuery = (AuthorizationDecisionQuery) unmarshallElement(singleElementOptionalAttributesFile);

        assertEquals("Resource attribute", expectedResource, authorizationDecisionQuery.getResource());
        assertNull("Subject element present", authorizationDecisionQuery.getSubject());
        assertEquals("Count of AttributeDesignator elements", 0, authorizationDecisionQuery.getActions().size());
        assertNull("Evidence element present", authorizationDecisionQuery.getEvidence());
    }

    /**
     * Test an Response file with children
     */
    public void testFullElementsUnmarshall() {
        AuthorizationDecisionQuery authorizationDecisionQuery;
        authorizationDecisionQuery = (AuthorizationDecisionQuery) unmarshallElement(fullElementsFile);

        assertNotNull("Subject element present", authorizationDecisionQuery.getSubject());
        assertEquals("Count of Action elements", 3, authorizationDecisionQuery.getActions().size());
        assertNotNull("Evidence element present", authorizationDecisionQuery.getEvidence());
    }

    /** {@inheritDoc} */
    public void testSingleElementMarshall() {
        assertEquals(expectedDOM, buildXMLObject(qname));
    } 

    /** {@inheritDoc} */
    public void testSingleElementOptionalAttributesMarshall() {
        AuthorizationDecisionQuery authorizationDecisionQuery;
        authorizationDecisionQuery = (AuthorizationDecisionQuery) buildXMLObject(qname);

        authorizationDecisionQuery.setResource(expectedResource);
        assertEquals(expectedOptionalAttributesDOM, authorizationDecisionQuery);
    }

    /**
     * Test Marshalling up a file with children
     * 
     */
    public void testFullElementsMarshall() {
        AuthorizationDecisionQuery authorizationDecisionQuery;
        authorizationDecisionQuery = (AuthorizationDecisionQuery) buildXMLObject(qname);
        authorizationDecisionQuery.setSubject((Subject) buildXMLObject(new QName(SAMLConstants.SAML1_NS, Subject.DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML1_PREFIX)));

        QName actionQname = new QName(SAMLConstants.SAML1_NS, Action.DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML1_PREFIX);
        List <Action> list = authorizationDecisionQuery.getActions();
        list.add((Action) buildXMLObject(actionQname));
        list.add((Action) buildXMLObject(actionQname));
        list.add((Action) buildXMLObject(actionQname));
        
        authorizationDecisionQuery.setEvidence((Evidence) buildXMLObject(new QName(SAMLConstants.SAML1_NS, Evidence.DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML1_PREFIX)));
        assertEquals(expectedFullDOM, authorizationDecisionQuery);

    }

}
