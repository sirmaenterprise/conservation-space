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
import org.opensaml.xml.signature.PGPData;
import org.opensaml.xml.signature.PGPKeyID;
import org.opensaml.xml.signature.PGPKeyPacket;
import org.opensaml.xml.util.XMLConstants;

/**
 *
 */
public class PGPDataSchemaValidatorTest extends BaseXMLObjectValidatorTestCase {
    
    public PGPDataSchemaValidatorTest() {
        targetQName = PGPData.DEFAULT_ELEMENT_NAME;
        validator = new PGPDataSchemaValidator();
    }

    protected void populateRequiredData() {
        super.populateRequiredData();
        PGPData pgpData = (PGPData) target;
        
        pgpData.setPGPKeyID((PGPKeyID) buildXMLObject(PGPKeyID.DEFAULT_ELEMENT_NAME));
        pgpData.setPGPKeyPacket(((PGPKeyPacket) buildXMLObject(PGPKeyPacket.DEFAULT_ELEMENT_NAME)));
        pgpData.getUnknownXMLObjects().add(buildXMLObject(simpleXMLObjectQName));
    }
    
    public void testNoKeyID() {
        PGPData pgpData = (PGPData) target;
        
        pgpData.setPGPKeyID(null);
        assertValidationPass("PGPData had no PDPKeyID, but should have been valid");
    }
    
    public void testNoKeyPacket() {
        PGPData pgpData = (PGPData) target;
        
        pgpData.setPGPKeyPacket(null);
        assertValidationPass("PGPData had no PDPKeyPacket, but should have been valid");
    }
    
    public void testEmptyChildren() {
        PGPData pgpData = (PGPData) target;
        
        pgpData.setPGPKeyID(null);
        pgpData.setPGPKeyPacket(null);
        pgpData.getUnknownXMLObjects().clear();
        assertValidationFail("PGPData child list was empty, should have failed validation");
    }

    public void testInvalidNamespaceChildren() {
        PGPData pgpData = (PGPData) target;
        
        SimpleXMLObjectBuilder sxoBuilder = new SimpleXMLObjectBuilder();
        SimpleXMLObject sxo = sxoBuilder.buildObject(XMLConstants.XMLSIG_NS, "Foo", XMLConstants.XMLSIG_PREFIX);
        
        pgpData.getUnknownXMLObjects().add(sxo);
        
        assertValidationFail("PGPData contained a child with an invalid namespace, should have failed validation");
    }
}
