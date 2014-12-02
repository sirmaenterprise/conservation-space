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

import org.opensaml.saml2.core.EncryptedID;
import org.opensaml.saml2.core.ManageNameIDRequest;
import org.opensaml.saml2.core.NameID;
import org.opensaml.saml2.core.NewEncryptedID;
import org.opensaml.saml2.core.NewID;
import org.opensaml.saml2.core.Terminate;

/**
 *
 */
public class ManageNameIDRequestSchemaTest extends RequestSchemaTestBase {
    
    private NameID nameID;
    private EncryptedID encryptedID;
    
    private NewID newID;
    private NewEncryptedID newEncryptedID;
    private Terminate terminate;

    /**
     * Constructor
     *
     */
    public ManageNameIDRequestSchemaTest() {
        super();
        targetQName = ManageNameIDRequest.DEFAULT_ELEMENT_NAME;
        validator = new ManageNameIDRequestSchemaValidator();
    }

    /** {@inheritDoc} */
    protected void setUp() throws Exception {
        super.setUp();
        
        terminate = (Terminate) buildXMLObject(Terminate.DEFAULT_ELEMENT_NAME);
        encryptedID = (EncryptedID) buildXMLObject(EncryptedID.DEFAULT_ELEMENT_NAME);
        newEncryptedID = (NewEncryptedID) buildXMLObject(NewEncryptedID.DEFAULT_ELEMENT_NAME);
    }

    /** {@inheritDoc} */
    protected void populateRequiredData() {
        super.populateRequiredData();
        ManageNameIDRequest request = (ManageNameIDRequest) target;
        
        nameID  = (NameID) buildXMLObject(NameID.DEFAULT_ELEMENT_NAME);
        newID = (NewID) buildXMLObject(NewID.DEFAULT_ELEMENT_NAME);
        
        request.setNameID(nameID);
        request.setNewID(newID);
    }
    
    public void testNoIdentifiersFailure() {
        ManageNameIDRequest request = (ManageNameIDRequest) target;
        
        request.setNameID(null);
        assertValidationFail("No name identifier was present");
    }
    
    public void testOtherValidIdentifiers() {
        ManageNameIDRequest request = (ManageNameIDRequest) target;
        
        request.setNameID(null);
        request.setEncryptedID(encryptedID);
        assertValidationPass("EncryptedID was present");
    }
    
    public void testTooManyIdentifiersFailure() {
        ManageNameIDRequest request = (ManageNameIDRequest) target;
        
        request.setEncryptedID(encryptedID);
        assertValidationFail("Both NameID and EncryptedID were present");
    }
   
    public void testNewIDandTerminateFailure() {
        ManageNameIDRequest request = (ManageNameIDRequest) target;
        
        request.setTerminate(terminate);
        assertValidationFail("Both NewID and Terminate were present");
        
        request.setNewID(null);
        request.setTerminate(null);
        assertValidationFail("Both NewID and Terminate were null");
        
        request.setNewID(newID);
        request.setNewEncryptedID(newEncryptedID);
        assertValidationFail("Both NewID and NewEncryptedID were present");
    }

}
