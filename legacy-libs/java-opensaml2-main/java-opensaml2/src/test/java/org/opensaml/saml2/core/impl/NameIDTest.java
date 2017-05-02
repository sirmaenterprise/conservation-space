/*
 * Copyright [2005] [University Corporation for Advanced Internet Development, Inc.]
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

package org.opensaml.saml2.core.impl;

import javax.xml.namespace.QName;

import org.opensaml.common.BaseSAMLObjectProviderTestCase;
import org.opensaml.common.xml.SAMLConstants;
import org.opensaml.saml2.core.NameID;

/**
 * Test case for creating, marshalling, and unmarshalling {@link org.opensaml.saml2.core.impl.NameIDImpl}.
 */
public class NameIDTest extends BaseSAMLObjectProviderTestCase {

    /** Expected Name value */
    private String expectedName;
    
    /** Expected NameQualifier value */
    private String expectedNameQualifier;

    /** Expected SPNameQualifier value */
    private String expectedSPNameQualifier;

    /** Expected Format value */
    private String expectedFormat;

    /** Expected SPProvidedID value */
    private String expectedSPID;

    /** Constructor */
    public NameIDTest() {
        singleElementFile = "/data/org/opensaml/saml2/core/impl/NameID.xml";
        singleElementOptionalAttributesFile = "/data/org/opensaml/saml2/core/impl/NameIDOptionalAttributes.xml";
    }

    /** {@inheritDoc} */
    protected void setUp() throws Exception {
        super.setUp();

        expectedName = "id name";
        expectedNameQualifier = "nq";
        expectedSPNameQualifier = "spnq";
        expectedFormat = "format style";
        expectedSPID = "spID";
    }

    /** {@inheritDoc} */
    public void testSingleElementUnmarshall() {
        NameID nameID = (NameID) unmarshallElement(singleElementFile);

        String name = nameID.getValue();
        assertEquals("Name not as expected", name, expectedName);
    }

    /** {@inheritDoc} */
    public void testSingleElementOptionalAttributesUnmarshall() {
        NameID nameID = (NameID) unmarshallElement(singleElementOptionalAttributesFile);

        String name = nameID.getValue();
        assertEquals("Name not as expected", name, expectedName);

        String nameQualifier = nameID.getNameQualifier();
        assertEquals("NameQualifier not as expected", nameQualifier, expectedNameQualifier);

        String spNameQualifier = nameID.getSPNameQualifier();
        assertEquals("SPNameQualifier not as expected", spNameQualifier, expectedSPNameQualifier);

        String format = nameID.getFormat();
        assertEquals("Format not as expected", format, expectedFormat);

        String spProvidedID = nameID.getSPProvidedID();
        assertEquals("SPProviderID not as expected", spProvidedID, expectedSPID);
    }

    /** {@inheritDoc} */
    public void testSingleElementMarshall() {
        QName qname = new QName(SAMLConstants.SAML20_NS, NameID.DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML20_PREFIX);
        NameID nameID = (NameID) buildXMLObject(qname);

        nameID.setValue(expectedName);
        assertEquals(expectedDOM, nameID);
    }

    /** {@inheritDoc} */
    public void testSingleElementOptionalAttributesMarshall() {
        QName qname = new QName(SAMLConstants.SAML20_NS, NameID.DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML20_PREFIX);
        NameID nameID = (NameID) buildXMLObject(qname);

        nameID.setValue(expectedName);
        nameID.setNameQualifier(expectedNameQualifier);
        nameID.setSPNameQualifier(expectedSPNameQualifier);
        nameID.setFormat(expectedFormat);
        nameID.setSPProvidedID(expectedSPID);
        assertEquals(expectedOptionalAttributesDOM, nameID);
    }
}