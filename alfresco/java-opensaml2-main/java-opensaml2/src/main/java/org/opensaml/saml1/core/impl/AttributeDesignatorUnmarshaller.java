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

import org.opensaml.common.impl.AbstractSAMLObjectUnmarshaller;
import org.opensaml.common.xml.SAMLConstants;
import org.opensaml.saml1.core.AttributeDesignator;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.io.UnmarshallingException;
import org.w3c.dom.Attr;

/**
 *
 */
public class AttributeDesignatorUnmarshaller extends AbstractSAMLObjectUnmarshaller {

    /**
     * Constructor
     */
    public AttributeDesignatorUnmarshaller() {
        super(SAMLConstants.SAML1_NS, AttributeDesignator.DEFAULT_ELEMENT_LOCAL_NAME);
    }
    
    /**
     * Constructor
     *
     * @param targetNamespaceURI
     * @param targetLocalName
     * @throws IllegalArgumentException
     */
    protected AttributeDesignatorUnmarshaller(String targetNamespaceURI, String targetLocalName) {
        super(targetNamespaceURI, targetLocalName);
    }
    
    
    /** {@inheritDoc} */
    protected void processAttribute(XMLObject samlObject, Attr attribute) throws UnmarshallingException {

        AttributeDesignator designator = (AttributeDesignator) samlObject;

        if (AttributeDesignator.ATTRIBUTENAME_ATTRIB_NAME.equals(attribute.getLocalName())) {
            designator.setAttributeName(attribute.getValue());
        } else if (AttributeDesignator.ATTRIBUTENAMESPACE_ATTRIB_NAME.equals(attribute.getLocalName())) {
            designator.setAttributeNamespace(attribute.getValue());
        } else {
            super.processAttribute(samlObject, attribute);
        }
    }

}
