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

package org.opensaml.saml1.core.validator;

import javax.xml.namespace.QName;

import org.opensaml.common.xml.SAMLConstants;
import org.opensaml.saml1.core.AssertionArtifact;
import org.opensaml.saml1.core.AssertionIDReference;
import org.opensaml.saml1.core.AttributeQuery;
import org.opensaml.saml1.core.Query;
import org.opensaml.saml1.core.Request;

/**
 * Test case for {@link org.opensaml.saml1.core.validator.RequestSchemaValidator}.
 */
public class RequestSchemaTest extends RequestAbstractTypeSchemaTestBase  {

    /** Constructor */
    public RequestSchemaTest() {
        super();
        targetQName = new QName(SAMLConstants.SAML10P_NS, Request.DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML1P_PREFIX);
        validator = new RequestSchemaValidator();
    }

    /** {@inheritDoc} */
    protected void populateRequiredData() {
        super.populateRequiredData();
        
        Request request = (Request) target;
        QName qname = new QName(SAMLConstants.SAML10P_NS, AttributeQuery.DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML1P_PREFIX);
        request.setQuery((Query)buildXMLObject(qname));
    }
    
    public void testNothingPresent() {
        Request request = (Request) target;
        request.setQuery(null);
        assertValidationFail("No elements, should raise a Validation Exception");
    }

    public void testQueryAndAssertionIDReference() {
        Request request = (Request) target;
        QName qname = new QName(SAMLConstants.SAML1_NS, AssertionIDReference.DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML1_PREFIX);

        request.getAssertionIDReferences().add((AssertionIDReference)buildXMLObject(qname));        
        
        assertValidationFail("Both Query & AssertionIDRefeference element present, should raise a Validation Exception");
    }

    public void testQueryAndAssertionArtifact() {
        Request request = (Request) target;
        QName qname = new QName(SAMLConstants.SAML10P_NS, AssertionArtifact.DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML1P_PREFIX);

        request.getAssertionArtifacts().add((AssertionArtifact)buildXMLObject(qname));        
        assertValidationFail("Both Query & AssertionArtifact element present, should raise a Validation Exception");
    }

    public void testAssertionIDRefAndAssertionArtifact() {
        Request request = (Request) target;
        QName qname = new QName(SAMLConstants.SAML10P_NS, AssertionArtifact.DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML1P_PREFIX);
        request.getAssertionArtifacts().add((AssertionArtifact)buildXMLObject(qname));        
        qname = new QName(SAMLConstants.SAML1_NS, AssertionIDReference.DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML1_PREFIX);
        request.getAssertionIDReferences().add((AssertionIDReference)buildXMLObject(qname));        
        request.setQuery(null);
        
        assertValidationFail("Both & AssertionIDRefeference AssertionArtifact element present, should raise a Validation Exception");
    }

}