/*
 * Copyright [2007] [University Corporation for Advanced Internet Development, Inc.]
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

package org.opensaml.xml.signature.validator;

import org.opensaml.xml.BaseXMLObjectValidatorTestCase;
import org.opensaml.xml.signature.Signature;
import org.opensaml.xml.signature.SignatureConstants;

/**
 *
 */
public class SignatureSchemaValidatorTest extends BaseXMLObjectValidatorTestCase {
    
    public SignatureSchemaValidatorTest() {
        targetQName = Signature.DEFAULT_ELEMENT_NAME;
        validator = new SignatureSchemaValidator();
    }

    protected void populateRequiredData() {
        super.populateRequiredData();
        Signature signature = (Signature) target;
        
        signature.setCanonicalizationAlgorithm(SignatureConstants.ALGO_ID_C14N_EXCL_OMIT_COMMENTS);
        signature.setSignatureAlgorithm(SignatureConstants.ALGO_ID_SIGNATURE_RSA_SHA1);
    }
    
    public void testMissingSignatureAlgo() {
        Signature signature = (Signature) target;
        
        signature.setSignatureAlgorithm(null);
        assertValidationFail("Signature algorithm was null, should have failed validation");
        
        signature.setSignatureAlgorithm("");
        assertValidationFail("Signature algorithm was empty, should have failed validation");
        
        signature.setSignatureAlgorithm("       ");
        assertValidationFail("Signature algorithm was all whitespace, should have failed validation");
    }
    
    public void testMissingC14NAlgo() {
        Signature signature = (Signature) target;
        
        signature.setCanonicalizationAlgorithm(null);
        assertValidationFail("C14N algorithm was null, should have failed validation");
        
        signature.setCanonicalizationAlgorithm("");
        assertValidationFail("C14N algorithm was empty, should have failed validation");
        
        signature.setCanonicalizationAlgorithm("       ");
        assertValidationFail("C14N algorithm was all whitespace, should have failed validation");
    }

}
