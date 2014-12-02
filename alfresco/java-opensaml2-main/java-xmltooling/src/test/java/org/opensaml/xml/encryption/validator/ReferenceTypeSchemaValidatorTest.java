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
import org.opensaml.xml.encryption.DataReference;
import org.opensaml.xml.encryption.ReferenceType;
import org.opensaml.xml.mock.SimpleXMLObject;
import org.opensaml.xml.mock.SimpleXMLObjectBuilder;
import org.opensaml.xml.util.XMLConstants;

/**
 *
 */
public class ReferenceTypeSchemaValidatorTest extends BaseXMLObjectValidatorTestCase {
    
    public ReferenceTypeSchemaValidatorTest() {
        // Don't want to create a builder just for this test,
        // so just use DataReference to test, it doesn't add anything.
        targetQName = DataReference.DEFAULT_ELEMENT_NAME;
        validator = new ReferenceTypeSchemaValidator();
    }

    protected void populateRequiredData() {
        super.populateRequiredData();
        ReferenceType ref = (ReferenceType) target;
        
        ref.setURI("urn:string:foo");
    }
    
    public void testMissingURI() {
        ReferenceType ref = (ReferenceType) target;
        
        ref.setURI(null);
        assertValidationFail("ReferenceType URI was null, should have failed validation");
        
        ref.setURI("");
        assertValidationFail("ReferenceType URI was empty, should have failed validation");
        
        ref.setURI("       ");
        assertValidationFail("ReferenceType URI was all whitespace, should have failed validation");
    }
    
    public void testInvalidNamespaceChildren() {
        ReferenceType rt = (ReferenceType) target;
        
        SimpleXMLObjectBuilder sxoBuilder = new SimpleXMLObjectBuilder();
        SimpleXMLObject sxo = sxoBuilder.buildObject(XMLConstants.XMLENC_NS, "Foo", XMLConstants.XMLENC_PREFIX);
        
        rt.getUnknownXMLObjects().add(sxo);
        
        assertValidationFail("ReferenceType contained a child with an invalid namespace, should have failed validation");
    }

}
