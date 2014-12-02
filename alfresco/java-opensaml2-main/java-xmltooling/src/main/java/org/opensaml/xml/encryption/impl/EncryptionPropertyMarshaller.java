/*
 * Copyright [2006] [University Corporation for Advanced Internet Development, Inc.]
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

package org.opensaml.xml.encryption.impl;

import java.util.Map.Entry;

import javax.xml.namespace.QName;

import org.opensaml.xml.Configuration;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.encryption.EncryptionProperty;
import org.opensaml.xml.io.MarshallingException;
import org.opensaml.xml.util.XMLConstants;
import org.opensaml.xml.util.XMLHelper;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;

/**
 * A thread-safe Marshaller for {@link org.opensaml.xml.encryption.EncryptionProperty} objects.
 */
public class EncryptionPropertyMarshaller extends AbstractXMLEncryptionMarshaller {
    
    /**
     * Constructor
     *
     */
    public EncryptionPropertyMarshaller(){
        super(XMLConstants.XMLENC_NS, EncryptionProperty.DEFAULT_ELEMENT_LOCAL_NAME);
    }

    /**
     * Constructor
     *
     * @param targetNamespaceURI
     * @param targetLocalName
     * @throws NullPointerException
     */
    protected EncryptionPropertyMarshaller(String targetNamespaceURI, String targetLocalName){
        super(targetNamespaceURI, targetLocalName);
    }

    /** {@inheritDoc} */
    protected void marshallAttributes(XMLObject xmlObject, Element domElement) throws MarshallingException {
        EncryptionProperty ep = (EncryptionProperty) xmlObject;
        
        if (ep.getID() != null) {
            domElement.setAttributeNS(null, EncryptionProperty.ID_ATTRIB_NAME, ep.getID());
            domElement.setIdAttributeNS(null, EncryptionProperty.ID_ATTRIB_NAME, true);
        }
        if (ep.getTarget() != null) {
            domElement.setAttributeNS(null, EncryptionProperty.TARGET_ATTRIB_NAME, ep.getTarget());
        }
        
        Attr attribute;
        for(Entry<QName, String> entry: ep.getUnknownAttributes().entrySet()){
            attribute = XMLHelper.constructAttribute(domElement.getOwnerDocument(), entry.getKey());
            attribute.setValue(entry.getValue());
            domElement.setAttributeNodeNS(attribute);
            if (Configuration.isIDAttribute(entry.getKey()) 
                    || ep.getUnknownAttributes().isIDAttribute(entry.getKey())) {
                attribute.getOwnerElement().setIdAttributeNode(attribute, true);
            }
        }
    }
    
    

}
