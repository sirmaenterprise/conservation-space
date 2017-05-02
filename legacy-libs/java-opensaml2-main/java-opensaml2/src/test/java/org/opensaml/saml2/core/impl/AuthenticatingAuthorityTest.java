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
import org.opensaml.saml2.core.AuthenticatingAuthority;

/**
 * Test case for creating, marshalling, and unmarshalling
 * {@link org.opensaml.saml2.core.impl.AuthenticatingAuthorityImpl}.
 */
public class AuthenticatingAuthorityTest extends BaseSAMLObjectProviderTestCase {

    /** Expected URI value */
    protected String expectedURI;

    /** Constructor */
    public AuthenticatingAuthorityTest() {
        singleElementFile = "/data/org/opensaml/saml2/core/impl/AuthenticatingAuthority.xml";
    }

    /** {@inheritDoc} */
    protected void setUp() throws Exception {
        super.setUp();

        expectedURI = "authenticating URI";
    }

    /** {@inheritDoc} */
    public void testSingleElementUnmarshall() {
        AuthenticatingAuthority authenticatingAuthority = (AuthenticatingAuthority) unmarshallElement(singleElementFile);

        String assertionURI = authenticatingAuthority.getURI();
        assertEquals("URI was " + assertionURI + ", expected " + expectedURI, expectedURI, assertionURI);
    }

    /** {@inheritDoc} */
    public void testSingleElementOptionalAttributesUnmarshall() {
        // do nothing
    }

    /** {@inheritDoc} */
    public void testSingleElementMarshall() {
        QName qname = new QName(SAMLConstants.SAML20_NS, AuthenticatingAuthority.DEFAULT_ELEMENT_LOCAL_NAME,
                SAMLConstants.SAML20_PREFIX);
        AuthenticatingAuthority authenticatingAuthority = (AuthenticatingAuthority) buildXMLObject(qname);

        authenticatingAuthority.setURI(expectedURI);
        assertEquals(expectedDOM, authenticatingAuthority);

    }

    /** {@inheritDoc} */
    public void testSingleElementOptionalAttributesMarshall() {
        // do nothing
    }
}