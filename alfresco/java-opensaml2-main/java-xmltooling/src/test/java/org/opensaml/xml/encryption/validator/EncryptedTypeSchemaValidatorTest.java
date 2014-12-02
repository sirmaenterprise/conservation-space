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
import org.opensaml.xml.encryption.CipherData;
import org.opensaml.xml.encryption.EncryptedData;

/**
 * Tests CryptoBinaryValidator.
 */
public class EncryptedTypeSchemaValidatorTest extends BaseXMLObjectValidatorTestCase {
    
    /**
     * Constructor.
     *
     */
    public EncryptedTypeSchemaValidatorTest() {
        // Can't instantiate an EncryptedType b/c it's abstract
        // so just use EncryptedData to test, it doesn't add anything.
        targetQName = EncryptedData.DEFAULT_ELEMENT_NAME;
        validator = new EncryptedTypeSchemaValidator();
    }

    /** {@inheritDoc} */
    protected void populateRequiredData() {
        super.populateRequiredData();
        EncryptedData encData = (EncryptedData) target;
        encData.setCipherData((CipherData) buildXMLObject(CipherData.DEFAULT_ELEMENT_NAME));
    }
    
    /**
     *  Test empty content.
     */
    public void testEmpty() {
        EncryptedData encData = (EncryptedData) target;
        
        encData.setCipherData(null);
        assertValidationFail("CipherData was null, should raise a Validation Exception");
    }

}
