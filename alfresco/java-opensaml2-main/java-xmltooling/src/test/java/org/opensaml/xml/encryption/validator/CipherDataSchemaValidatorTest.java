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
import org.opensaml.xml.encryption.CipherReference;
import org.opensaml.xml.encryption.CipherValue;

/**
 *
 */
public class CipherDataSchemaValidatorTest extends BaseXMLObjectValidatorTestCase {
    
    public CipherDataSchemaValidatorTest() {
        targetQName = CipherData.DEFAULT_ELEMENT_NAME;
        validator = new CipherDataSchemaValidator();
    }

    protected void populateRequiredData() {
        super.populateRequiredData();
        CipherData cd = (CipherData) target;
        
        cd.setCipherValue((CipherValue) buildXMLObject(CipherValue.DEFAULT_ELEMENT_NAME));
    }
    
    public void testWithCipherReference() {
        CipherData cd = (CipherData) target;
        
        cd.setCipherValue(null);
        cd.setCipherReference((CipherReference) buildXMLObject(CipherReference.DEFAULT_ELEMENT_NAME));
        
        assertValidationPass("CipherData contained only a CipherReference, should have passed validation");
    }
    
    public void testMissingChildren() {
        CipherData cd = (CipherData) target;
        
        cd.setCipherValue(null);
        
        assertValidationFail("CipherData did not contain a CipherValue or CipherReference, should have failed validation");
    }
    
    public void testTooManyChildren() {
        CipherData cd = (CipherData) target;
        
        cd.setCipherReference((CipherReference) buildXMLObject(CipherReference.DEFAULT_ELEMENT_NAME));
        
        assertValidationFail("CipherData contained both CipherValue and CipherReference, should have failed validation");
    }
}
