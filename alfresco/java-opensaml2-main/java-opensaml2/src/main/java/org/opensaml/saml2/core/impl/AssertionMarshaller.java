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

package org.opensaml.saml2.core.impl;

import org.opensaml.Configuration;
import org.opensaml.common.impl.AbstractSAMLObjectMarshaller;
import org.opensaml.common.xml.SAMLConstants;
import org.opensaml.saml2.core.Assertion;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.io.MarshallingException;
import org.w3c.dom.Element;

/**
 * A thread-safe Marshaller for {@link org.opensaml.saml2.core.Assertion}.
 */
public class AssertionMarshaller extends AbstractSAMLObjectMarshaller {

    /** Constructor. */
    public AssertionMarshaller() {
        super(SAMLConstants.SAML20_NS, Assertion.DEFAULT_ELEMENT_LOCAL_NAME);
    }

    /**
     * Constructor.
     * 
     * @param targetNamespaceURI the namespace URI of either the schema type QName or element QName of the elements this
     *            unmarshaller operates on
     * @param targetLocalName the local name of either the schema type QName or element QName of the elements this
     *            unmarshaller operates on
     */
    protected AssertionMarshaller(String targetNamespaceURI, String targetLocalName) {
        super(targetNamespaceURI, targetLocalName);
    }

    /** {@inheritDoc} */
    protected void marshallAttributes(XMLObject samlObject, Element domElement) throws MarshallingException {
        Assertion assertion = (Assertion) samlObject;

        if (assertion.getVersion() != null) {
            domElement.setAttributeNS(null, Assertion.VERSION_ATTRIB_NAME, assertion.getVersion().toString());
        }

        if (assertion.getIssueInstant() != null) {
            String issueInstantStr = Configuration.getSAMLDateFormatter().print(assertion.getIssueInstant());
            domElement.setAttributeNS(null, Assertion.ISSUE_INSTANT_ATTRIB_NAME, issueInstantStr);
        }

        if (assertion.getID() != null) {
            domElement.setAttributeNS(null, Assertion.ID_ATTRIB_NAME, assertion.getID());
            domElement.setIdAttributeNS(null, Assertion.ID_ATTRIB_NAME, true);
        }
    }
}