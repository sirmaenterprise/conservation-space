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
import org.opensaml.saml1.core.Action;
import org.opensaml.saml1.core.AuthorizationDecisionQuery;

/**
 * Test case for {@link org.opensaml.saml1.core.validator.AuthorizationDecisionQuerySchemaValidator}.
 */
public class AuthorizationDecisionQuerySchemaTest extends SubjectQuerySchemaTestBase  {

    /** Constructor */
    public AuthorizationDecisionQuerySchemaTest() {
        super();
        targetQName = new QName(SAMLConstants.SAML10P_NS, AuthorizationDecisionQuery.DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML1P_PREFIX);
        validator = new AuthorizationDecisionQuerySchemaValidator();
    }

    /** {@inheritDoc} */
    protected void populateRequiredData() {
        super.populateRequiredData();
        AuthorizationDecisionQuery query = (AuthorizationDecisionQuery) target;
        
        query.setResource("resource");
        QName qname = new QName(SAMLConstants.SAML1_NS, Action.DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML1_PREFIX);
        query.getActions().add((Action)buildXMLObject(qname));
    }
    
    public void testMissingResource() {
        AuthorizationDecisionQuery query = (AuthorizationDecisionQuery) target;
        
        query.setResource(null);
        assertValidationFail("Resource attribute is null , should raise a Validation Exception");

        query.setResource("");
        assertValidationFail("Resource attribute is empty, should raise a Validation Exception");

        query.setResource("   ");
        assertValidationFail("Resource attribute is white space, should raise a Validation Exception");
    }

    public void testMissingActions() {
        AuthorizationDecisionQuery query = (AuthorizationDecisionQuery) target;
        
        query.getActions().clear();
        assertValidationFail("No Action elements, should raise a Validation Exception");
    }
}