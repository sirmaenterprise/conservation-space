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

package org.opensaml.xml.encryption.validator;

import org.opensaml.xml.BaseXMLObjectValidatorTestCase;
import org.opensaml.xml.encryption.EncryptionConstants;
import org.opensaml.xml.encryption.EncryptionMethod;

/**
 *
 */
public class EncryptionMethodSchemaValidatorTest extends BaseXMLObjectValidatorTestCase {
    
    public EncryptionMethodSchemaValidatorTest() {
        targetQName = EncryptionMethod.DEFAULT_ELEMENT_NAME;
        validator = new EncryptionMethodSchemaValidator();
    }

    protected void populateRequiredData() {
        super.populateRequiredData();
        EncryptionMethod em = (EncryptionMethod) target;
        
        em.setAlgorithm(EncryptionConstants.ALGO_ID_BLOCKCIPHER_AES128);
    }
    
    public void testMissingAlgo() {
        EncryptionMethod em = (EncryptionMethod) target;
        
        em.setAlgorithm(null);
        assertValidationFail("EncryptionMethod algorithm was null, should have failed validation");
        
        em.setAlgorithm("");
        assertValidationFail("EncryptionMethod algorithm was empty, should have failed validation");
        
        em.setAlgorithm("       ");
        assertValidationFail("EncryptionMethod algorithm was all whitespace, should have failed validation");
    }

}
