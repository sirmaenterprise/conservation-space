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
package org.opensaml.saml2.core.validator;

import javax.xml.namespace.QName;

import org.opensaml.common.xml.SAMLConstants;
import org.opensaml.saml2.core.Action;
import org.opensaml.saml2.core.AuthzDecisionQuery;

/**
 *
 */
public class AuthzDecisionQuerySchemaTest extends SubjectQuerySchemaTestBase {

    /**
     * Constructor
     *
     */
    public AuthzDecisionQuerySchemaTest() {
        super();
        targetQName = new QName(SAMLConstants.SAML20P_NS, AuthzDecisionQuery.DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML20P_PREFIX);
        validator = new AuthzDecisionQuerySchemaValidator();
    }

    /** {@inheritDoc} */
    protected void populateRequiredData() {
        super.populateRequiredData();
        AuthzDecisionQuery query = (AuthzDecisionQuery) target;
        query.setResource("urn:string:resource");
        Action action  = (Action) buildXMLObject(new QName(SAMLConstants.SAML20_NS, Action.DEFAULT_ELEMENT_LOCAL_NAME));
        query.getActions().add(action);
    }
    
    
    /**
     *  Tests invalid Resource attribute
     */
    public void testResourceFailure() {
        AuthzDecisionQuery query = (AuthzDecisionQuery) target;
        
        query.setResource(null);
        assertValidationFail("Resource attribute was null");
        
        query.setResource("");
        assertValidationFail("Resource attribute was empty");
        
        query.setResource("                ");
        assertValidationFail("Resource attribute was all whitespace");
    }
    
    /**
     *  Tests invalid Action child elements
     */
    public void testActionFailure() {
        AuthzDecisionQuery query = (AuthzDecisionQuery) target;
        
        query.getActions().clear();
        assertValidationFail("Action child element list was empty");
        
    }
}

