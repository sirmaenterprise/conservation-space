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
import org.opensaml.xml.signature.X509IssuerName;
import org.opensaml.xml.signature.X509IssuerSerial;
import org.opensaml.xml.signature.X509SerialNumber;

/**
 *
 */
public class X509IssuerSerialSchemaValidatorTest extends BaseXMLObjectValidatorTestCase {
    
    public X509IssuerSerialSchemaValidatorTest() {
        targetQName = X509IssuerSerial.DEFAULT_ELEMENT_NAME;
        validator = new X509IssuerSerialSchemaValidator();
    }

    protected void populateRequiredData() {
        super.populateRequiredData();
        X509IssuerSerial is = (X509IssuerSerial) target;
        
        is.setX509IssuerName((X509IssuerName) buildXMLObject(X509IssuerName.DEFAULT_ELEMENT_NAME));
        is.setX509SerialNumber((X509SerialNumber) buildXMLObject(X509SerialNumber.DEFAULT_ELEMENT_NAME));
    }
    
    public void testMissingIssuerName() {
        X509IssuerSerial is = (X509IssuerSerial) target;
        
        is.setX509IssuerName(null);
        
        assertValidationFail("X509IssuerSerial number did not contain an X509IssuerName, should have failed validation");
    }
 
    public void testMissingSerialNumber() {
        X509IssuerSerial is = (X509IssuerSerial) target;
        
        is.setX509SerialNumber(null);
        
        assertValidationFail("X509IssuerSerial number did not contain an X509SerialNumber, should have failed validation");
    }
}
