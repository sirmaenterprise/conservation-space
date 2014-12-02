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

import org.opensaml.common.impl.AbstractSAMLObjectMarshaller;
import org.opensaml.common.xml.SAMLConstants;
import org.opensaml.saml1.core.AttributeDesignator;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.io.MarshallingException;
import org.w3c.dom.Element;

/**
 * 
 */
public class AttributeDesignatorMarshaller extends AbstractSAMLObjectMarshaller {

    /**
     * Constructor
     */
    public AttributeDesignatorMarshaller() {
        super(SAMLConstants.SAML1_NS, AttributeDesignator.DEFAULT_ELEMENT_LOCAL_NAME);
    }

    /**
     * Constructor
     * 
     * @param targetNamespaceURI
     * @param targetLocalName
     * @throws NullPointerException
     */
    protected AttributeDesignatorMarshaller(String targetNamespaceURI, String targetLocalName)
            throws NullPointerException {
        super(targetNamespaceURI, targetLocalName);
    }

    /** {@inheritDoc} */
    protected void marshallAttributes(XMLObject samlElement, Element domElement) throws MarshallingException {
        AttributeDesignator designator = (AttributeDesignator) samlElement;

        if (designator.getAttributeName() != null) {
            domElement.setAttributeNS(null, AttributeDesignator.ATTRIBUTENAME_ATTRIB_NAME, designator
                    .getAttributeName());
        }

        if (designator.getAttributeNamespace() != null) {
            domElement.setAttributeNS(null, AttributeDesignator.ATTRIBUTENAMESPACE_ATTRIB_NAME, designator
                    .getAttributeNamespace());
        }
    }

}
