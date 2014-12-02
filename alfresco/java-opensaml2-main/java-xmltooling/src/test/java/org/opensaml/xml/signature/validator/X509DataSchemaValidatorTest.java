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
import org.opensaml.xml.signature.KeyInfo;
import org.opensaml.xml.signature.X509Certificate;
import org.opensaml.xml.signature.X509Data;
import org.opensaml.xml.util.XMLConstants;

/**
 *
 */
public class X509DataSchemaValidatorTest extends BaseXMLObjectValidatorTestCase {
    
    public X509DataSchemaValidatorTest() {
        targetQName = X509Data.DEFAULT_ELEMENT_NAME;
        validator = new X509DataSchemaValidator();
    }

    protected void populateRequiredData() {
        super.populateRequiredData();
        X509Data x509Data = (X509Data) target;
        
        x509Data.getX509Certificates().add((X509Certificate) buildXMLObject(X509Certificate.DEFAULT_ELEMENT_NAME));
    }
    
    public void testEmptyChildren() {
        X509Data x509Data = (X509Data) target;
        
        x509Data.getXMLObjects().clear();
        assertValidationFail("X509Data child list was empty, should have failed validation");
    }

    public void testInvalidNamespaceChildren() {
        X509Data x509Data = (X509Data) target;
        
        SimpleXMLObjectBuilder sxoBuilder = new SimpleXMLObjectBuilder();
        SimpleXMLObject sxo = sxoBuilder.buildObject(XMLConstants.XMLSIG_NS, "Foo", XMLConstants.XMLSIG_PREFIX);
        
        x509Data.getXMLObjects().add(sxo);
        
        assertValidationFail("X509Data contained a child with an invalid namespace, should have failed validation");
    }
}
