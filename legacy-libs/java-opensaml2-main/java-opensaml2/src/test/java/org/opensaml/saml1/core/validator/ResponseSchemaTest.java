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
import org.opensaml.saml1.core.Response;
import org.opensaml.saml1.core.Status;

/**
 * Test case for {@link org.opensaml.saml1.core.validator.ResponseSchemaValidator}.
 */
public class ResponseSchemaTest extends ResponseAbstractTypeSchemaTestBase  {

    /** Constructor */
    public ResponseSchemaTest() {
        super();
        targetQName = new QName(SAMLConstants.SAML10P_NS, Response.DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML1P_PREFIX);
        validator = new ResponseSchemaValidator();
    }

    /** {@inheritDoc} */
    protected void populateRequiredData() {
        super.populateRequiredData();
        
        Response response = (Response) target;
        QName qname = new QName(SAMLConstants.SAML10P_NS, Status.DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML1P_PREFIX);
        response.setStatus((Status)buildXMLObject(qname));
    }
    
    public void testMissingStatus() {
        Response response = (Response) target;
        response.setStatus(null);
        assertValidationFail("No Status, should raise a Validation Exception");
    }
}