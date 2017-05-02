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

package org.opensaml.saml2.core.validator;

import javax.xml.namespace.QName;

import org.opensaml.common.BaseSAMLObjectValidatorTestCase;
import org.opensaml.common.xml.SAMLConstants;
import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.AssertionIDRef;
import org.opensaml.saml2.core.AssertionURIRef;
import org.opensaml.saml2.core.Evidence;
import org.opensaml.xml.validation.ValidationException;

/**
 * Test case for {@link org.opensaml.saml2.core.validator.EvidenceSchemaValidator}.
 */
public class EvidenceSchemaTest extends BaseSAMLObjectValidatorTestCase {

    /** Constructor */
    public EvidenceSchemaTest() {
        targetQName = new QName(SAMLConstants.SAML20_NS, Evidence.DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML20_PREFIX);
        validator = new EvidenceSchemaValidator();
    }

    protected void populateRequiredData() {
        super.populateRequiredData();
        Evidence evidence = (Evidence) target;
        
        Assertion assertion = (Assertion) buildXMLObject(new QName(SAMLConstants.SAML20_NS, Assertion.DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML20_PREFIX));
        evidence.getAssertions().add(assertion);
        
        AssertionIDRef assertionIDRef = (AssertionIDRef) buildXMLObject(new QName(SAMLConstants.SAML20_NS, AssertionIDRef.DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML20_PREFIX));
        evidence.getAssertionIDReferences().add(assertionIDRef);
        
        AssertionURIRef assertionURIRef = (AssertionURIRef) buildXMLObject(new QName(SAMLConstants.SAML20_NS, AssertionURIRef.DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML20_PREFIX));
        evidence.getAssertionURIReferences().add(assertionURIRef);
    }

    /**
     * Tests Assertion failure.
     * 
     * @throws ValidationException
     */
    public void testAssertion() throws ValidationException {
        Evidence evidence = (Evidence) target;
        
        evidence.getEvidence().clear();
        assertValidationFail("No assertions present, should raise a Validation Exception");
    }
}