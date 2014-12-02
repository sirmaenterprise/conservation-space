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
import org.opensaml.saml1.core.StatusCode;
import org.opensaml.saml1.core.validator.StatusCodeSchemaValidator;

/**
 * Test case for {@link org.opensaml.saml1.core.validator.StatusCodeSchemaValidator}.
 */
public class StatusCodeSchemaTest extends BaseSAMLObjectValidatorTestCase {

    /** Constructor */
    public StatusCodeSchemaTest() {
        super();
        targetQName = new QName(SAMLConstants.SAML10P_NS, StatusCode.DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML1P_PREFIX);
        validator = new StatusCodeSchemaValidator();
    }

    /** {@inheritDoc} */
    protected void populateRequiredData() {
        super.populateRequiredData();

        StatusCode statusCode = (StatusCode) target;
        statusCode.setValue(StatusCode.SUCCESS);
    }
    
    public void testMissingValue(){
        StatusCode statusCode = (StatusCode) target;
        statusCode.setValue(null);
        assertValidationFail("No Value attribute, should raise a Validation Exception");        
    }

    public void testBadQName1(){
        StatusCode statusCode = (StatusCode) target;
        QName qname = new QName(SAMLConstants.SAML1_NS, "Success", SAMLConstants.SAML1_PREFIX);
        statusCode.setValue(qname);
        assertValidationFail("Value in SAML1 assertion namespace, should raise a Validation Exception");        
    }
    public void testBadQName2(){
        StatusCode statusCode = (StatusCode) target;
        QName qname = new QName(SAMLConstants.SAML10P_NS, "ssSuccess", SAMLConstants.SAML1P_PREFIX);
        statusCode.setValue(qname);
        assertValidationFail("unrecognized LocalName in SAML1 protocol namespace, should raise a Validation Exception");        
    }
}