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
package org.opensaml.saml2.core.impl;

import javax.xml.namespace.QName;

import org.opensaml.common.xml.SAMLConstants;
import org.opensaml.saml2.core.Action;
import org.opensaml.saml2.core.AuthzDecisionQuery;
import org.opensaml.saml2.core.Evidence;

/**
 *
 */
public class AuthzDecisionQueryTest extends SubjectQueryTestBase {
    
    /** Expected Resource attribute value */
    private String expectedResource;
    
    /** Expected number of Action child elements */
    private int expectedNumActions;
    

    /**
     * Constructor
     *
     */
    public AuthzDecisionQueryTest() {
        super();
        singleElementFile = "/data/org/opensaml/saml2/core/impl/AuthzDecisionQuery.xml";
        singleElementOptionalAttributesFile = "/data/org/opensaml/saml2/core/impl/AuthzDecisionQueryOptionalAttributes.xml";
        childElementsFile = "/data/org/opensaml/saml2/core/impl/AuthzDecisionQueryChildElements.xml";
    }

    /** {@inheritDoc} */
    protected void setUp() throws Exception {
        super.setUp();
        expectedResource = "urn:string:resource";
        expectedNumActions = 2;
    }

    /** {@inheritDoc} */
    public void testSingleElementMarshall() {
        QName qname = new QName(SAMLConstants.SAML20P_NS, AuthzDecisionQuery.DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML20P_PREFIX);
        AuthzDecisionQuery query = (AuthzDecisionQuery) buildXMLObject(qname);
        
        super.populateRequiredAttributes(query);
        query.setResource(expectedResource);
        
        assertEquals(expectedDOM, query);
    }
    
    
    
    /** {@inheritDoc} */
    public void testSingleElementOptionalAttributesMarshall() {
        QName qname = new QName(SAMLConstants.SAML20P_NS, AuthzDecisionQuery.DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML20P_PREFIX);
        AuthzDecisionQuery query = (AuthzDecisionQuery) buildXMLObject(qname);
        
        super.populateRequiredAttributes(query);
        super.populateOptionalAttributes(query);
        query.setResource(expectedResource);
        
        assertEquals(expectedOptionalAttributesDOM, query);
    }
    
    

    /** {@inheritDoc} */
    public void testChildElementsMarshall() {
        QName qname = new QName(SAMLConstants.SAML20P_NS, AuthzDecisionQuery.DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML20P_PREFIX);
        AuthzDecisionQuery query = (AuthzDecisionQuery) buildXMLObject(qname);
        
        super.populateChildElements(query);
        
        QName actionQName = new QName(SAMLConstants.SAML20_NS, Action.DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML20_PREFIX);
        for (int i=0; i<expectedNumActions; i++){
            query.getActions().add((Action) buildXMLObject(actionQName));
        }
        
        QName evidenceQName = new QName(SAMLConstants.SAML20_NS, Evidence.DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML20_PREFIX);
        query.setEvidence((Evidence) buildXMLObject(evidenceQName));
        
        assertEquals(expectedChildElementsDOM, query);
    }

    /** {@inheritDoc} */
    public void testSingleElementUnmarshall() {
        AuthzDecisionQuery query = (AuthzDecisionQuery) unmarshallElement(singleElementFile);
        
        assertNotNull("AuthzDecisionQuery was null", query);
        assertEquals("Unmarshalled Resource attribute was not the expected value", expectedResource, query.getResource());
        super.helperTestSingleElementUnmarshall(query);

    }
    
    /** {@inheritDoc} */
    public void testSingleElementOptionalAttributesUnmarshall() {
        AuthzDecisionQuery query = (AuthzDecisionQuery) unmarshallElement(singleElementOptionalAttributesFile);
        
        super.helperTestSingleElementOptionalAttributesUnmarshall(query);
    }

    /** {@inheritDoc} */
    public void testChildElementsUnmarshall() {
        AuthzDecisionQuery query = (AuthzDecisionQuery) unmarshallElement(childElementsFile);
        
        assertEquals("Action count", expectedNumActions, query.getActions().size());
        assertNotNull("Evidence was null", query.getEvidence());
        super.helperTestChildElementsUnmarshall(query);
    }
}