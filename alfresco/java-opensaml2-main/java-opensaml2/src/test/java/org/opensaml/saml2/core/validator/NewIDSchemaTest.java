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

import org.opensaml.common.BaseSAMLObjectValidatorTestCase;
import org.opensaml.common.xml.SAMLConstants;
import org.opensaml.saml2.core.NewID;

/**
 *
 */
public class NewIDSchemaTest extends BaseSAMLObjectValidatorTestCase {

    /**
     * Constructor
     *
     */
    public NewIDSchemaTest() {
        super();
        targetQName = new QName(SAMLConstants.SAML20P_NS, NewID.DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML20P_PREFIX);
        validator = new NewIDSchemaValidator();
    }

    /** {@inheritDoc} */
    protected void populateRequiredData() {
        super.populateRequiredData();
        NewID newid = (NewID) target;
        newid.setNewID("some cool new NameID");
    }
    
    /**
     *  Test invalid element content.
     */
    public void testNewIDFailure() {
        NewID newid = (NewID) target;
        
        newid.setNewID(null);
        assertValidationFail("NewID was null");
        
        newid.setNewID("");
        assertValidationFail("NewID was empty");
        
        newid.setNewID("                      ");
        assertValidationFail("NewID was all whitespace");
    }

}
