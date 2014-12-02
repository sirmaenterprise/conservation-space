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
import org.opensaml.saml1.core.Action;

/**
 * Test for {@link org.opensaml.saml1.core.impl.ActionImpl}
 */
public class ActionTest extends BaseSAMLObjectProviderTestCase {

    private final String expectedContents;
    private final String expectedNamespace;
    private final QName qname;

    /**
     * Constructor
     */
    public ActionTest() {
        super();
        singleElementFile = "/data/org/opensaml/saml1/impl/singleAction.xml";
        singleElementOptionalAttributesFile  = "/data/org/opensaml/saml1/impl/singleActionAttributes.xml";    
        expectedNamespace = "namespace";
        expectedContents = "Action Contents";
        qname = new QName(SAMLConstants.SAML1_NS, Action.DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML1_PREFIX);
    }
    

    /** {@inheritDoc} */

    public void testSingleElementUnmarshall() {
        Action action = (Action) unmarshallElement(singleElementFile);
        assertNull("namespace attribute present", action.getNamespace());
        assertNull("Contents present", action.getContents());
    }

    /** {@inheritDoc} */

    public void testSingleElementOptionalAttributesUnmarshall() {
        Action action = (Action) unmarshallElement(singleElementOptionalAttributesFile);
        assertEquals("namespace attribute ", expectedNamespace, action.getNamespace());
        assertEquals("Contents ", expectedContents, action.getContents());
    }

    /** {@inheritDoc} */

    public void testSingleElementMarshall() {
        assertEquals(expectedDOM, buildXMLObject(qname));
    }

    /** {@inheritDoc} */

    public void testSingleElementOptionalAttributesMarshall() {
        Action action =(Action) buildXMLObject(qname);
        action.setNamespace(expectedNamespace);
        action.setContents(expectedContents);
        assertEquals(expectedOptionalAttributesDOM, action);
    }
}
