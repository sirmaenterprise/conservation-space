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

/**
 * 
 */
package org.opensaml.saml1.core.impl;

import javax.xml.namespace.QName;

import org.opensaml.common.BaseSAMLObjectProviderTestCase;
import org.opensaml.common.xml.SAMLConstants;
import org.opensaml.saml1.core.StatusMessage;

/**
 * Test for org.opensaml.saml1.core.StatusMessage 
 */
public class StatusMessageTest extends BaseSAMLObjectProviderTestCase {

    /** name used to generate objects */
    private final QName qname;

    private final String contents;

    /**
     * Constructor
     *
     */
    public StatusMessageTest() {
        super();

        contents = "Nibble a Happy Warthog";
        singleElementFile = "/data/org/opensaml/saml1/impl/singleStatusMessage.xml";
        singleElementOptionalAttributesFile = "/data/org/opensaml/saml1/impl/FullStatusMessage.xml";
        
        qname = new QName(SAMLConstants.SAML10P_NS, StatusMessage.DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML1P_PREFIX);
    }

    /** {@inheritDoc} */

    public void testSingleElementUnmarshall() {
        StatusMessage statusMessage = (StatusMessage) unmarshallElement(singleElementFile);
        assertNull("Contents", statusMessage.getMessage());
    }

    /** {@inheritDoc} */

    public void testSingleElementOptionalAttributesUnmarshall() {
        StatusMessage statusMessage = (StatusMessage) unmarshallElement(singleElementOptionalAttributesFile);
        assertEquals("Contents", contents, statusMessage.getMessage());
    }

    /** {@inheritDoc} */

    public void testSingleElementMarshall() {
        assertEquals(expectedDOM, buildXMLObject(qname));
    }

    /** {@inheritDoc} */

    public void testSingleElementOptionalAttributesMarshall() {
        StatusMessage statusMessage = (StatusMessage) buildXMLObject(qname);

        statusMessage.setMessage(contents);
        assertEquals(expectedOptionalAttributesDOM, statusMessage);
    }
}
