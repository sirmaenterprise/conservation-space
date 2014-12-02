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

package org.opensaml.saml1.core.impl;

import org.opensaml.common.impl.AbstractSAMLObjectMarshaller;
import org.opensaml.common.xml.SAMLConstants;
import org.opensaml.saml1.core.NameIdentifier;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.io.MarshallingException;
import org.opensaml.xml.util.XMLHelper;
import org.w3c.dom.Element;

/**
 * A thread safe Marshaller for {@link org.opensaml.saml1.core.NameIdentifier} objects.
 */
public class NameIdentifierMarshaller extends AbstractSAMLObjectMarshaller {

    /**
     * Constructor
     * 
     * @throws IllegalArgumentException
     */
    public NameIdentifierMarshaller() throws IllegalArgumentException {
        super(SAMLConstants.SAML1_NS, NameIdentifier.DEFAULT_ELEMENT_LOCAL_NAME);
    }

    /** {@inheritDoc} */
    protected void marshallAttributes(XMLObject samlElement, Element domElement) throws MarshallingException {
        NameIdentifier nameIdentifier = (NameIdentifier) samlElement;

        if (nameIdentifier.getNameQualifier() != null) {
            domElement.setAttributeNS(null, NameIdentifier.NAMEQUALIFIER_ATTRIB_NAME, nameIdentifier.getNameQualifier());
        }

        if (nameIdentifier.getFormat() != null) {
            domElement.setAttributeNS(null, NameIdentifier.FORMAT_ATTRIB_NAME, nameIdentifier.getFormat());
        }
    }

    /** {@inheritDoc} */
    protected void marshallElementContent(XMLObject samlObject, Element domElement) throws MarshallingException {
        NameIdentifier nameIdentifier = (NameIdentifier) samlObject;

        if (nameIdentifier.getNameIdentifier() != null) {
            XMLHelper.appendTextContent(domElement,nameIdentifier.getNameIdentifier());
        }
    }
}