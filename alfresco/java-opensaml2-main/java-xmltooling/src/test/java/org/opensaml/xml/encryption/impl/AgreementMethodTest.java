/*
 * Copyright [2006] [University Corporation for Advanced Internet Development, Inc.]
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

package org.opensaml.xml.encryption.impl;


import org.opensaml.xml.XMLObjectProviderBaseTestCase;
import org.opensaml.xml.encryption.AgreementMethod;
import org.opensaml.xml.encryption.KANonce;
import org.opensaml.xml.encryption.OriginatorKeyInfo;
import org.opensaml.xml.encryption.RecipientKeyInfo;
import org.opensaml.xml.mock.SimpleXMLObject;

/**
 *
 */
public class AgreementMethodTest extends XMLObjectProviderBaseTestCase {
    
    private String expectedAlgorithm;
    private int expectedNumUnknownChildren;
    
    /**
     * Constructor
     *
     */
    public AgreementMethodTest() {
        singleElementFile = "/data/org/opensaml/xml/encryption/impl/AgreementMethod.xml";
        childElementsFile = "/data/org/opensaml/xml/encryption/impl/AgreementMethodChildElements.xml";
    }

    /** {@inheritDoc} */
    protected void setUp() throws Exception {
        super.setUp();
        
        expectedAlgorithm = "urn:string:foo";
        expectedNumUnknownChildren = 2;
    }

    /** {@inheritDoc} */
    public void testSingleElementUnmarshall() {
        AgreementMethod am = (AgreementMethod) unmarshallElement(singleElementFile);
        
        assertNotNull("AgreementMethod", am);
        assertEquals("Algorithm attribute", expectedAlgorithm, am.getAlgorithm());
        assertNull("KA-Nonce child element", am.getKANonce());
        assertEquals("Unknown children", 0, am.getUnknownXMLObjects().size());
        assertNull("OriginatorKeyInfo child element", am.getOriginatorKeyInfo());
        assertNull("RecipientKeyInfo child element", am.getRecipientKeyInfo());
    }

    /** {@inheritDoc} */
    public void testChildElementsUnmarshall() {
        AgreementMethod am = (AgreementMethod) unmarshallElement(childElementsFile);
        
        assertNotNull("AgreementMethod", am);
        assertEquals("Algorithm attribute", expectedAlgorithm, am.getAlgorithm());
        assertNotNull("KA-Nonce child element", am.getKANonce());
        assertEquals("Unknown children", expectedNumUnknownChildren, am.getUnknownXMLObjects().size());
        assertNotNull("OriginatorKeyInfo child element", am.getOriginatorKeyInfo());
        assertNotNull("RecipientKeyInfo child element", am.getRecipientKeyInfo());
    }

    /** {@inheritDoc} */
    public void testSingleElementMarshall() {
        AgreementMethod am = (AgreementMethod) buildXMLObject(AgreementMethod.DEFAULT_ELEMENT_NAME);
        
        am.setAlgorithm(expectedAlgorithm);
        
        assertEquals(expectedDOM, am);
    }

    /** {@inheritDoc} */
    public void testChildElementsMarshall() {
        AgreementMethod am = (AgreementMethod) buildXMLObject(AgreementMethod.DEFAULT_ELEMENT_NAME);
        
        am.setAlgorithm(expectedAlgorithm);
        am.setKANonce((KANonce) buildXMLObject(KANonce.DEFAULT_ELEMENT_NAME));
        am.getUnknownXMLObjects().add((SimpleXMLObject) buildXMLObject(SimpleXMLObject.ELEMENT_NAME));
        am.getUnknownXMLObjects().add((SimpleXMLObject) buildXMLObject(SimpleXMLObject.ELEMENT_NAME));
        am.setOriginatorKeyInfo((OriginatorKeyInfo) buildXMLObject(OriginatorKeyInfo.DEFAULT_ELEMENT_NAME));
        am.setRecipientKeyInfo((RecipientKeyInfo) buildXMLObject(RecipientKeyInfo.DEFAULT_ELEMENT_NAME));
        
        assertEquals(expectedChildElementsDOM, am);
    }

}
