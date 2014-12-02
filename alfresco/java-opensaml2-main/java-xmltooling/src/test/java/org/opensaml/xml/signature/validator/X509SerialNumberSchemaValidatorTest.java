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

import java.math.BigInteger;

import org.opensaml.xml.BaseXMLObjectValidatorTestCase;
import org.opensaml.xml.signature.X509SerialNumber;

/**
 *
 */
public class X509SerialNumberSchemaValidatorTest extends BaseXMLObjectValidatorTestCase {
    
    public X509SerialNumberSchemaValidatorTest() {
        targetQName = X509SerialNumber.DEFAULT_ELEMENT_NAME;
        validator = new X509SerialNumberSchemaValidator();
    }

    protected void populateRequiredData() {
        super.populateRequiredData();
        X509SerialNumber sn = (X509SerialNumber) target;
        
        sn.setValue(new BigInteger("42"));
    }
    
    public void testMissingValue() {
        X509SerialNumber sn = (X509SerialNumber) target;
        
        sn.setValue(null);
        
        assertValidationFail("X509SerialNumber contained no value, should have failed validation");
        
    }

}
