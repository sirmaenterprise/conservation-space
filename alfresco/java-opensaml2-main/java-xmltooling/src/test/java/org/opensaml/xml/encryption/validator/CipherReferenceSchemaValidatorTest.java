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
import org.opensaml.xml.encryption.CipherReference;

/**
 *
 */
public class CipherReferenceSchemaValidatorTest extends BaseXMLObjectValidatorTestCase {
    
    public CipherReferenceSchemaValidatorTest() {
        targetQName = CipherReference.DEFAULT_ELEMENT_NAME;
        validator = new CipherReferenceSchemaValidator();
    }

    protected void populateRequiredData() {
        super.populateRequiredData();
        CipherReference cr = (CipherReference) target;
        
        cr.setURI("urn:string:foo");
    }
    
    public void testMissingURI() {
        CipherReference cr = (CipherReference) target;
        
        cr.setURI(null);
        assertValidationFail("CipherReference URI was null, should have failed validation");
        
        cr.setURI("");
        assertValidationFail("CipherReference URI was empty, should have failed validation");
        
        cr.setURI("       ");
        assertValidationFail("CipherReference URI was all whitespace, should have failed validation");
    }

}
