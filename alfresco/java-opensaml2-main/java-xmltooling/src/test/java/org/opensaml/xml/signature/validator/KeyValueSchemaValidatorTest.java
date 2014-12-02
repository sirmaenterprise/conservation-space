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
import org.opensaml.xml.mock.SimpleXMLObject;
import org.opensaml.xml.mock.SimpleXMLObjectBuilder;
import org.opensaml.xml.signature.DSAKeyValue;
import org.opensaml.xml.signature.KeyValue;
import org.opensaml.xml.signature.RSAKeyValue;
import org.opensaml.xml.util.XMLConstants;

/**
 *
 */
public class KeyValueSchemaValidatorTest extends BaseXMLObjectValidatorTestCase {
    
    public KeyValueSchemaValidatorTest() {
        targetQName = KeyValue.DEFAULT_ELEMENT_NAME;
        validator = new KeyValueSchemaValidator();
    }

    protected void populateRequiredData() {
        super.populateRequiredData();
        KeyValue keyValue = (KeyValue) target;
        
        keyValue.setRSAKeyValue((RSAKeyValue) buildXMLObject(RSAKeyValue.DEFAULT_ELEMENT_NAME));
    }
    
    public void testEmptyChildren() {
        KeyValue keyValue = (KeyValue) target;
        
        keyValue.setRSAKeyValue(null);
        
        assertValidationFail("KeyValue child list was empty, should have failed validation");
    }
    
    public void testTooManyChildren() {
        KeyValue keyValue = (KeyValue) target;
        
        keyValue.setDSAKeyValue((DSAKeyValue) buildXMLObject(DSAKeyValue.DEFAULT_ELEMENT_NAME));
        
        assertValidationFail("KeyValue had too many children, should have failed validation");
    }

    public void testInvalidNamespaceExtensionChild() {
        KeyValue keyValue = (KeyValue) target;
        
        SimpleXMLObjectBuilder sxoBuilder = new SimpleXMLObjectBuilder();
        SimpleXMLObject sxo = sxoBuilder.buildObject(XMLConstants.XMLSIG_NS, "Foo", XMLConstants.XMLSIG_PREFIX);
        
        keyValue.setRSAKeyValue(null);
        keyValue.setUnknownXMLObject(sxo);
        
        assertValidationFail("KeyInfo contained a child with an invalid namespace, should have failed validation");
    }
}
