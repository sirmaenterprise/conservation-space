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
import org.opensaml.saml2.core.AttributeStatement;
import org.opensaml.saml2.core.AuthnStatement;
import org.opensaml.saml2.core.AuthzDecisionStatement;
import org.opensaml.saml2.core.Subject;
import org.opensaml.xml.validation.ValidationException;

/**
 * Test case for {@link org.opensaml.saml2.core.validator.AssertionSpecValidator}.
 */
public class AssertionSpecTest extends BaseSAMLObjectValidatorTestCase {

    /** Constructor */
    public AssertionSpecTest() {
        targetQName = new QName(SAMLConstants.SAML20_NS, Assertion.DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML20_PREFIX);
        validator = new AssertionSpecValidator();
    }

    /** {@inheritDoc} */
    protected void populateRequiredData() {
        super.populateRequiredData();
        Assertion assertion = (Assertion) target;
        Subject subject = (Subject) buildXMLObject(new QName(SAMLConstants.SAML20_NS, Subject.DEFAULT_ELEMENT_LOCAL_NAME,
                SAMLConstants.SAML20_PREFIX));

        assertion.setSubject(subject);
    }

    /**
     * Tests absent Subject failure.
     * 
     * @throws ValidationException
     */
    public void testSubjectFailure() throws ValidationException {
        Assertion assertion = (Assertion) target;
        AuthnStatement authnStatement = (AuthnStatement) buildXMLObject(new QName(SAMLConstants.SAML20_NS, AuthnStatement.DEFAULT_ELEMENT_LOCAL_NAME,
                SAMLConstants.SAML20_PREFIX));
        AuthzDecisionStatement authzDecisionStatement = (AuthzDecisionStatement) buildXMLObject(new QName(SAMLConstants.SAML20_NS, AuthzDecisionStatement.DEFAULT_ELEMENT_LOCAL_NAME,
                SAMLConstants.SAML20_PREFIX));
        AttributeStatement attributeStatement = (AttributeStatement) buildXMLObject(new QName(SAMLConstants.SAML20_NS, AttributeStatement.DEFAULT_ELEMENT_LOCAL_NAME,
                SAMLConstants.SAML20_PREFIX));
        
        assertion.setSubject(null);
        assertValidationFail("Subject was null in the absence of statements, should raise a Validation Exception");

        assertion.getAuthnStatements().add(authnStatement);
        assertValidationFail("Subject was null in the presence of AuthnStatement, should raise a Validation Exception.");
        
        assertion.getAuthnStatements().clear();
        assertion.getAuthzDecisionStatements().add(authzDecisionStatement);
        assertValidationFail("Subject was null in the presence of AuthzDecisionStatement, should raise a Validation Exception.");
        
        assertion.getAuthzDecisionStatements().clear();
        assertion.getAttributeStatements().add(attributeStatement);
        assertValidationFail("Subject was null in the presence of AttributeStatement, should raise a Validation Exception.");
    }
}