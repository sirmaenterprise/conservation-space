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

import org.opensaml.common.BaseSAMLObjectValidatorTestCase;
import org.opensaml.common.xml.SAMLConstants;
import org.opensaml.saml1.core.Assertion;
import org.opensaml.saml1.core.AssertionIDReference;
import org.opensaml.saml1.core.Evidence;

/**
 * Test case for {@link org.opensaml.saml1.core.validator.EvidenceSchemaValidator}.
 */
public class EvidenceSchemaTest extends BaseSAMLObjectValidatorTestCase {

    /** Constructor */
    public EvidenceSchemaTest() {
        super();
        targetQName = new QName(SAMLConstants.SAML1_NS, Evidence.DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML1_PREFIX);
        validator = new EvidenceSchemaValidator();
    }

    /** {@inheritDoc} */
    protected void populateRequiredData() {
        super.populateRequiredData();
        
        Evidence evidence = (Evidence) target;
        QName assertionQname = new QName(SAMLConstants.SAML1_NS, Assertion.DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML1_PREFIX);
        QName assertionIDRefQname = new QName(SAMLConstants.SAML1_NS, AssertionIDReference.DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML1_PREFIX);
        evidence.getAssertions().add((Assertion)buildXMLObject(assertionQname));
        evidence.getAssertionIDReferences().add((AssertionIDReference)buildXMLObject(assertionIDRefQname));
        evidence.getAssertions().add((Assertion)buildXMLObject(assertionQname));
        evidence.getAssertionIDReferences().add((AssertionIDReference)buildXMLObject(assertionIDRefQname));
    }
    
    public void testMissingChildren() {
        Evidence evidence = (Evidence) target;
        
        evidence.getEvidence().clear();
        assertValidationFail("Evidence list was empty");
        
    }
}