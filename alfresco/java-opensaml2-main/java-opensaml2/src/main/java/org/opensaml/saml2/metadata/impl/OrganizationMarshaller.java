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

package org.opensaml.saml2.metadata.impl;

import java.util.Map.Entry;

import javax.xml.namespace.QName;

import org.opensaml.common.impl.AbstractSAMLObjectMarshaller;
import org.opensaml.common.xml.SAMLConstants;
import org.opensaml.saml2.metadata.Organization;
import org.opensaml.xml.Configuration;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.io.MarshallingException;
import org.opensaml.xml.util.XMLHelper;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;

/**
 * A thread safe Marshaller for {@link org.opensaml.saml2.metadata.Organization} objects.
 */
public class OrganizationMarshaller extends AbstractSAMLObjectMarshaller {

    /**
     * Constructor
     */
    public OrganizationMarshaller() {
        super(SAMLConstants.SAML20MD_NS, Organization.DEFAULT_ELEMENT_LOCAL_NAME);
    }

    /**
     * Constructor
     * 
     * @param namespaceURI
     * @param elementLocalName
     */
    protected OrganizationMarshaller(String namespaceURI, String elementLocalName) {
        super(namespaceURI, elementLocalName);
    }
    
    /**
     * {@inheritDoc}
     */
    protected void marshallAttributes(XMLObject xmlObject, Element domElement) throws MarshallingException {
        Organization org = (Organization) xmlObject;
        
        Attr attribute;
        for(Entry<QName, String> entry: org.getUnknownAttributes().entrySet()){
            attribute = XMLHelper.constructAttribute(domElement.getOwnerDocument(), entry.getKey());
            attribute.setValue(entry.getValue());
            domElement.setAttributeNodeNS(attribute);
            if (Configuration.isIDAttribute(entry.getKey()) 
                    || org.getUnknownAttributes().isIDAttribute(entry.getKey())) {
                attribute.getOwnerElement().setIdAttributeNode(attribute, true);
            }
        }
    }
}