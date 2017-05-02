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
import org.opensaml.saml2.core.Action;
import org.opensaml.saml2.core.AuthzDecisionStatement;
import org.opensaml.saml2.core.DecisionTypeEnumeration;
import org.opensaml.xml.validation.ValidationException;

/**
 * Test case for {@link org.opensaml.saml2.core.validator.AuthzDecisionStatementSchemaValidator}.
 */
public class AuthzDecisionStatementSchemaTest extends BaseSAMLObjectValidatorTestCase {

    /** Constructor */
    public AuthzDecisionStatementSchemaTest() {
        targetQName = new QName(SAMLConstants.SAML20_NS, AuthzDecisionStatement.DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML20_PREFIX);
        validator = new AuthzDecisionStatementSchemaValidator();
    }

    protected void populateRequiredData() {
        super.populateRequiredData();
        AuthzDecisionStatement authzDecisionStatement = (AuthzDecisionStatement) target;
        Action action = (Action) buildXMLObject(new QName(SAMLConstants.SAML20_NS, Action.DEFAULT_ELEMENT_LOCAL_NAME,
                SAMLConstants.SAML20_PREFIX));
        authzDecisionStatement.setResource("resource");
        authzDecisionStatement.setDecision(DecisionTypeEnumeration.DENY);
        authzDecisionStatement.getActions().add(action);
    }

    /**
     * Tests absent Resource failure.
     * 
     * @throws ValidationException
     */
    public void testResourceFailure() throws ValidationException {
        AuthzDecisionStatement authzDecisionStatement = (AuthzDecisionStatement) target;

        authzDecisionStatement.setResource(null);
        assertValidationFail("Resource was null, should raise a Validation Exception");

        authzDecisionStatement.setResource("");
        assertValidationFail("Resource was empty string, should raise a Validation Exception");
        
        authzDecisionStatement.setResource("    ");
        assertValidationFail("Resource was white space, should raise a Validation Exception");
    }

    /**
     * Tests absent Decision failure.
     * 
     * @throws ValidationException
     */
    public void testDecisionFailure() throws ValidationException {
        AuthzDecisionStatement authzDecisionStatement = (AuthzDecisionStatement) target;

        authzDecisionStatement.setDecision(null);
        assertValidationFail("Decision was null, should raise a Validation Exception");
    }

    /**
     * Tests absent Action failure.
     * 
     * @throws ValidationException
     */
    public void testActionFailure() throws ValidationException {
        AuthzDecisionStatement authzDecisionStatement = (AuthzDecisionStatement) target;

        authzDecisionStatement.getActions().clear();
        assertValidationFail("Action list was empty, should raise a Validation Exception");
    }
}