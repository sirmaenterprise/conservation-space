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
import org.opensaml.saml2.core.EncryptedID;
import org.opensaml.saml2.core.MockBaseID;
import org.opensaml.saml2.core.NameID;
import org.opensaml.saml2.core.NameIDMappingRequest;
import org.opensaml.saml2.core.NameIDMappingResponse;

/**
 *
 */
public class NameIDMappingResponseSchemaTest extends StatusResponseSchemaTestBase {

    /**
     * Constructor
     *
     */
    public NameIDMappingResponseSchemaTest() {
        super();
        targetQName = new QName(SAMLConstants.SAML20P_NS, NameIDMappingResponse.DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML20P_PREFIX);
        validator = new NameIDMappingResponseSchemaValidator();
    }

    /** {@inheritDoc} */
    protected void populateRequiredData() {
        super.populateRequiredData();
        
        NameIDMappingResponse response = (NameIDMappingResponse) target;
        NameID nameid = (NameID) buildXMLObject(new QName(SAMLConstants.SAML20_NS, NameID.DEFAULT_ELEMENT_LOCAL_NAME));
        response.setNameID(nameid);
    }
    
    public void testNoIdentifiersFailure() {
        NameIDMappingResponse response = (NameIDMappingResponse) target;
        
        response.setNameID(null);
        assertValidationFail("No name identifier was present");
    }
    
    public void testTooManyIdentifiersFailure() {
        NameIDMappingResponse response = (NameIDMappingResponse) target;
        
        response.setEncryptedID( (EncryptedID) buildXMLObject(EncryptedID.DEFAULT_ELEMENT_NAME) );
        assertValidationFail("Both NameID and EncryptedID were present");
    }
    
    public void testOtherValidIdentifiers() {
        NameIDMappingResponse response = (NameIDMappingResponse) target;
        
        response.setNameID(null);
        response.setEncryptedID((EncryptedID) buildXMLObject(EncryptedID.DEFAULT_ELEMENT_NAME));
        assertValidationPass("EncryptedID was present");
    }
}
