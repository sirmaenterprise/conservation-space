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
import org.opensaml.saml2.core.GetComplete;

/**
 *
 */
public class GetCompleteSchemaTest extends BaseSAMLObjectValidatorTestCase {

    /**
     * Constructor
     *
     */
    public GetCompleteSchemaTest() {
        super();
        targetQName = new QName(SAMLConstants.SAML20P_NS, GetComplete.DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML20P_PREFIX);
        validator = new GetCompleteSchemaValidator();
    }

    /** {@inheritDoc} */
    protected void populateRequiredData() {
        super.populateRequiredData();
        GetComplete gc = (GetComplete) target;
        gc.setGetComplete("http://sp.example.org/idplist.xml");
    }
    
    /**
     *  Tests invalid GetComplete element contenet
     */
    public void testGetCompleteFailure() {
        GetComplete gc = (GetComplete) target;
        
        gc.setGetComplete(null);
        assertValidationFail("GetComplete element content was null");
        
        gc.setGetComplete("");
        assertValidationFail("GetComplete element content was empty ");
        
        gc.setGetComplete("              ");
        assertValidationFail("GetComplete element content was all whitespace");
    }

}
