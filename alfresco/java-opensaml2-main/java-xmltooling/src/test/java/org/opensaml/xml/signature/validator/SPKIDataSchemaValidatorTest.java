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
import org.opensaml.xml.signature.SPKIData;
import org.opensaml.xml.signature.SPKISexp;
import org.opensaml.xml.util.XMLConstants;

/**
 *
 */
public class SPKIDataSchemaValidatorTest extends BaseXMLObjectValidatorTestCase {
    
    public SPKIDataSchemaValidatorTest() {
        targetQName = SPKIData.DEFAULT_ELEMENT_NAME;
        validator = new SPKIDataSchemaValidator();
    }

    protected void populateRequiredData() {
        super.populateRequiredData();
        SPKIData spkiData = (SPKIData) target;
        
        spkiData.getXMLObjects().add(buildXMLObject(SPKISexp.DEFAULT_ELEMENT_NAME));
        spkiData.getXMLObjects().add(buildXMLObject(simpleXMLObjectQName));
    }
    
    public void testEmptyChildren() {
        SPKIData spkiData = (SPKIData) target;
        
        spkiData.getXMLObjects().clear();
        assertValidationFail("SPKIData child list was empty, should have failed validation");
    }

    public void testInvalidNamespaceChildren() {
        SPKIData spkiData = (SPKIData) target;
        
        SimpleXMLObjectBuilder sxoBuilder = new SimpleXMLObjectBuilder();
        SimpleXMLObject sxo = sxoBuilder.buildObject(XMLConstants.XMLSIG_NS, "Foo", XMLConstants.XMLSIG_PREFIX);
        
        spkiData.getXMLObjects().add(buildXMLObject(SPKISexp.DEFAULT_ELEMENT_NAME));
        spkiData.getXMLObjects().add(sxo);
        
        assertValidationFail("SPKIData contained a child with an invalid namespace, should have failed validation");
    }
}
