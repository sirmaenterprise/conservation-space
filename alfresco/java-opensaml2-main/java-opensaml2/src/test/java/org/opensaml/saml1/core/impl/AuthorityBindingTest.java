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
import org.opensaml.saml1.core.AttributeQuery;
import org.opensaml.saml1.core.AuthorityBinding;

/**
 *  Test for {@link org.opensaml.saml1.core.impl.AuthorityBinding}
 */
public class AuthorityBindingTest extends BaseSAMLObjectProviderTestCase {

    /** name used to generate objects */
    private final QName qname;

    /** Value of AuthorityKind in test file */
    private final QName expectedAuthorityKind;

    /** Value of Location in test file */
    private final String expectedLocation;

    /** Value of Binding in test file */
    private final String expectedBinding;

    /**
     * Constructor
     */
    public AuthorityBindingTest() {
        super(); 
        //this attribute is a Schema QName type, e.g. AuthorityKind="samlp:AttributeQuery"
        expectedAuthorityKind = new QName(SAMLConstants.SAML10P_NS, AttributeQuery.DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML1P_PREFIX);
        expectedLocation = "here";
        expectedBinding = "binding";
        singleElementFile = "/data/org/opensaml/saml1/impl/singleAuthorityBinding.xml";
        singleElementOptionalAttributesFile = "/data/org/opensaml/saml1/impl/singleAuthorityBindingAttributes.xml";
        qname = new QName(SAMLConstants.SAML1_NS, AuthorityBinding.DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML1_PREFIX);
    }

    /** {@inheritDoc} */

    public void testSingleElementUnmarshall() {
        AuthorityBinding authorityBinding = (AuthorityBinding) unmarshallElement(singleElementFile);
        assertNull("AuthorityKind attribute present", authorityBinding.getAuthorityKind());
        assertNull("Binding attribute present", authorityBinding.getBinding());
        assertNull("Location attribute present", authorityBinding.getLocation());
    }

    /** {@inheritDoc} */

    public void testSingleElementOptionalAttributesUnmarshall() {
        AuthorityBinding authorityBinding = (AuthorityBinding) unmarshallElement(singleElementOptionalAttributesFile);
        assertEquals("AuthorityKind attribute", expectedAuthorityKind, authorityBinding.getAuthorityKind());
        assertEquals("Binding attribute", expectedBinding, authorityBinding.getBinding());
        assertEquals("Location attribute", expectedLocation, authorityBinding.getLocation());        
    }

    /** {@inheritDoc} */

    public void testSingleElementMarshall() {
        assertEquals(expectedDOM, buildXMLObject(qname));
    }

    /** {@inheritDoc} */

    public void testSingleElementOptionalAttributesMarshall() {
        AuthorityBinding authorityBinding = (AuthorityBinding) buildXMLObject(qname);
        authorityBinding.setAuthorityKind(expectedAuthorityKind);
        authorityBinding.setBinding(expectedBinding);
        authorityBinding.setLocation(expectedLocation);
        assertEquals(expectedOptionalAttributesDOM, authorityBinding);
    }

}
