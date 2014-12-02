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

import org.opensaml.common.impl.AbstractSAMLObjectUnmarshaller;
import org.opensaml.common.xml.SAMLConstants;
import org.opensaml.saml2.core.BaseID;
import org.opensaml.saml2.core.EncryptedID;
import org.opensaml.saml2.core.NameID;
import org.opensaml.saml2.core.Subject;
import org.opensaml.saml2.core.SubjectConfirmation;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.io.UnmarshallingException;

/**
 * A thread-safe Unmarshaller for {@link org.opensaml.saml2.core.Subject} objects.
 */
public class SubjectUnmarshaller extends AbstractSAMLObjectUnmarshaller {

    /** Constructor. */
    public SubjectUnmarshaller() {
        super(SAMLConstants.SAML20_NS, Subject.DEFAULT_ELEMENT_LOCAL_NAME);
    }

    /**
     * Constructor.
     * 
     * @param namespaceURI the namespace URI of either the schema type QName or element QName of the elements this
     *            unmarshaller operates on
     * @param elementLocalName the local name of either the schema type QName or element QName of the elements this
     *            unmarshaller operates on
     */
    protected SubjectUnmarshaller(String namespaceURI, String elementLocalName) {
        super(namespaceURI, elementLocalName);
    }

    /** {@inheritDoc} */
    protected void processChildElement(XMLObject parentObject, XMLObject childObject) throws UnmarshallingException {
        Subject subject = (Subject) parentObject;

        if (childObject instanceof BaseID) {
            subject.setBaseID((BaseID) childObject);
        } else if (childObject instanceof NameID) {
            subject.setNameID((NameID) childObject);
        } else if (childObject instanceof EncryptedID) {
            subject.setEncryptedID((EncryptedID) childObject);
        } else if (childObject instanceof SubjectConfirmation) {
            subject.getSubjectConfirmations().add((SubjectConfirmation) childObject);
        } else {
            super.processChildElement(parentObject, childObject);
        }
    }
}