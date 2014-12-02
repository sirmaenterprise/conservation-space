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
import org.opensaml.saml2.core.Action;

/**
 * Test case for creating, marshalling, and unmarshalling {@link org.opensaml.saml2.core.impl.ActionImpl}.
 */
public class ActionTest extends BaseSAMLObjectProviderTestCase {

    /** Expected value of action */
    protected String expectedAction;

    /** Expected value of Namespace */
    protected String expectedNamespace;

    /** Constructor */
    public ActionTest() {
        singleElementFile = "/data/org/opensaml/saml2/core/impl/Action.xml";
        singleElementOptionalAttributesFile = "/data/org/opensaml/saml2/core/impl/ActionOptionalAttributes.xml";
    }

    /** {@inheritDoc} */
    protected void setUp() throws Exception {
        super.setUp();

        expectedAction = "action name";
        expectedNamespace = "ns";
    }

    /** {@inheritDoc} */
    public void testSingleElementUnmarshall() {
        Action action = (Action) unmarshallElement(singleElementFile);

        String actionname = action.getAction();
        assertEquals("Action was " + actionname + ", expected " + expectedAction, expectedAction, actionname);
    }

    /** {@inheritDoc} */
    public void testSingleElementOptionalAttributesUnmarshall() {
        Action action = (Action) unmarshallElement(singleElementOptionalAttributesFile);

        String actionname = action.getAction();
        assertEquals("Action was " + actionname + ", expected " + expectedAction, expectedAction, actionname);

        String namespace = action.getNamespace();
        assertEquals("Namespace was " + namespace + ", expected " + expectedNamespace, expectedNamespace, namespace);
    }

    /** {@inheritDoc} */
    public void testSingleElementMarshall() {
        QName qname = new QName(SAMLConstants.SAML20_NS, Action.DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML20_PREFIX);
        Action action = (Action) buildXMLObject(qname);

        action.setAction(expectedAction);
        assertEquals(expectedDOM, action);
    }

    /** {@inheritDoc} */
    public void testSingleElementOptionalAttributesMarshall() {
        QName qname = new QName(SAMLConstants.SAML20_NS, Action.DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML20_PREFIX);
        Action action = (Action) buildXMLObject(qname);

        action.setAction(expectedAction);
        action.setNamespace(expectedNamespace);
        assertEquals(expectedOptionalAttributesDOM, action);
    }
}