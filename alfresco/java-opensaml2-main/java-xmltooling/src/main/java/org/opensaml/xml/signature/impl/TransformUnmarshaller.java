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

package org.opensaml.xml.signature.impl;

import org.opensaml.xml.XMLObject;
import org.opensaml.xml.io.UnmarshallingException;
import org.opensaml.xml.signature.Transform;
import org.opensaml.xml.util.XMLConstants;
import org.w3c.dom.Attr;

/**
 * A thread-safe Unmarshaller for {@link org.opensaml.xml.signature.Transform} objects.
 */
public class TransformUnmarshaller extends AbstractXMLSignatureUnmarshaller {
    
    /**
     * Constructor
     *
     */
    public TransformUnmarshaller(){
        super(XMLConstants.XMLSIG_NS, Transform.DEFAULT_ELEMENT_LOCAL_NAME);
    }

    /**
     * Constructor
     *
     * @param targetNamespaceURI
     * @param targetLocalName
     */
    public TransformUnmarshaller(String targetNamespaceURI, String targetLocalName){
        super(targetNamespaceURI, targetLocalName);
    }

    /** {@inheritDoc} */
    protected void processAttribute(XMLObject xmlObject, Attr attribute) throws UnmarshallingException {
        Transform transform = (Transform) xmlObject;
        
        if (attribute.getLocalName().equals(Transform.ALGORITHM_ATTRIB_NAME)) {
            transform.setAlgorithm(attribute.getValue());
        } else {
            super.processAttribute(xmlObject, attribute);
        }
    }

    /** {@inheritDoc} */
    protected void processChildElement(XMLObject parentXMLObject, XMLObject childXMLObject) throws UnmarshallingException {
        Transform transform = (Transform) parentXMLObject;
        
        // Has <any> open content model + XPath children
        transform.getAllChildren().add(childXMLObject);
    }

}
