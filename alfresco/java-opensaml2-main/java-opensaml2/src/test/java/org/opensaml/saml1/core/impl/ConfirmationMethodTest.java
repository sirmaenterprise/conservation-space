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
import org.opensaml.saml1.core.ConfirmationMethod;

/**
 * test for {@link org.opensaml.saml1.core.impl.ConfirmationMethod}
 */
public class ConfirmationMethodTest extends BaseSAMLObjectProviderTestCase {

    /** name used to generate objects */
    private final QName qname;

    /** Pattern in XML file */
    private String expectedConfirmationMethod;
    
    /**
     * Constructor
     */
    public ConfirmationMethodTest() {
        super();
        singleElementFile = "/data/org/opensaml/saml1/impl/singleConfirmationMethod.xml";
        singleElementOptionalAttributesFile = "/data/org/opensaml/saml1/impl/singleConfirmationMethodAttributes.xml";
        expectedConfirmationMethod = "confirmation";
        
        qname = new QName(SAMLConstants.SAML1_NS, ConfirmationMethod.DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML1_PREFIX);
    }

    /** {@inheritDoc} */

    public void testSingleElementUnmarshall() {
        ConfirmationMethod confirmationMethod = (ConfirmationMethod) unmarshallElement(singleElementFile);
        
        assertNull("Contents of Confirmation Method", confirmationMethod.getConfirmationMethod());

    }

    /** {@inheritDoc} */

    public void testSingleElementOptionalAttributesUnmarshall() {
        ConfirmationMethod confirmationMethod = (ConfirmationMethod) unmarshallElement(singleElementOptionalAttributesFile);
        
        assertEquals("Contents of Confirmation Method", expectedConfirmationMethod, confirmationMethod.getConfirmationMethod());
    }

    /** {@inheritDoc} */

    public void testSingleElementMarshall() {
        assertEquals(expectedDOM, buildXMLObject(qname));
    }

    /** {@inheritDoc} */

    public void testSingleElementOptionalAttributesMarshall() {
        ConfirmationMethod confirmationMethod = (ConfirmationMethod) buildXMLObject(qname);
        confirmationMethod.setConfirmationMethod(expectedConfirmationMethod);
        
        assertEquals(expectedOptionalAttributesDOM, confirmationMethod);
    }

}
