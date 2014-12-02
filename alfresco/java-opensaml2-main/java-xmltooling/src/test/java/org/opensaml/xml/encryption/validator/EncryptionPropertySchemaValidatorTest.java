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

import javax.xml.namespace.QName;

import org.opensaml.xml.BaseXMLObjectValidatorTestCase;
import org.opensaml.xml.encryption.EncryptionProperty;
import org.opensaml.xml.mock.SimpleXMLObject;
import org.opensaml.xml.mock.SimpleXMLObjectBuilder;
import org.opensaml.xml.util.XMLConstants;

/**
 *
 */
public class EncryptionPropertySchemaValidatorTest extends BaseXMLObjectValidatorTestCase {
    
    public EncryptionPropertySchemaValidatorTest() {
        targetQName = EncryptionProperty.DEFAULT_ELEMENT_NAME;
        validator = new EncryptionPropertySchemaValidator();
    }

    protected void populateRequiredData() {
        super.populateRequiredData();
        EncryptionProperty encProp = (EncryptionProperty) target;
        
        encProp.getUnknownXMLObjects().add(buildXMLObject(simpleXMLObjectQName));
    }
    
    public void testMissingChildren() {
        EncryptionProperty encProp = (EncryptionProperty) target;
        
        encProp.getUnknownXMLObjects().clear();
        assertValidationFail("EncryptionProperty child list was empty, should have failed validation");
    }
    
    public void testInvalidNamespaceChildren() {
        EncryptionProperty encProp = (EncryptionProperty) target;
        
        SimpleXMLObjectBuilder sxoBuilder = new SimpleXMLObjectBuilder();
        SimpleXMLObject sxo = sxoBuilder.buildObject(XMLConstants.XMLENC_NS, "Foo", XMLConstants.XMLENC_PREFIX);
        
        encProp.getUnknownXMLObjects().add(sxo);
        
        assertValidationFail("EncryptionProperty contained a child with an invalid namespace, should have failed validation");
    }
    
    public void testInvalidNamespaceAttributes() {
        EncryptionProperty encProp = (EncryptionProperty) target;
        
        QName attribName = new QName("urn:namespace:foo", "FooAttrib", "foo");
        encProp.getUnknownAttributes().put(attribName, "foobar");
        
        assertValidationFail("EncryptionProperty contained an attribute with an invalid namespace, should have failed validation");
    }
    
    public void testValidNamespaceAttributes() {
        EncryptionProperty encProp = (EncryptionProperty) target;
        
        QName attribName = new QName(XMLConstants.XML_NS, "lang", XMLConstants.XML_PREFIX);
        encProp.getUnknownAttributes().put(attribName, "en-US");
        
        assertValidationPass("EncryptionProperty contained an attribute from the XML namespace, should have passed validation");
    }

}
