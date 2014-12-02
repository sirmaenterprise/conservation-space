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

import org.opensaml.xml.XMLObject;
import org.opensaml.xml.encryption.DataReference;
import org.opensaml.xml.encryption.KeyReference;
import org.opensaml.xml.encryption.ReferenceList;
import org.opensaml.xml.io.UnmarshallingException;
import org.opensaml.xml.util.XMLConstants;


/**
 * A thread-safe Unmarshaller for {@link org.opensaml.xml.encryption.ReferenceList} objects.
 */
public class ReferenceListUnmarshaller extends AbstractXMLEncryptionUnmarshaller {
    
    /**
     * Constructor
     *
     */
    public ReferenceListUnmarshaller(){
       super(XMLConstants.XMLENC_NS, ReferenceList.DEFAULT_ELEMENT_LOCAL_NAME);
    }

    /**
     * Constructor
     *
     * @param targetNamespaceURI
     * @param targetLocalName
     */
    public ReferenceListUnmarshaller(String targetNamespaceURI, String targetLocalName){
        super(targetNamespaceURI, targetLocalName);
    }

    /** {@inheritDoc} */
    protected void processChildElement(XMLObject parentXMLObject, XMLObject childXMLObject) throws UnmarshallingException {
        ReferenceList rl = (ReferenceList) parentXMLObject;
        
        if (childXMLObject instanceof DataReference) {
            rl.getReferences().add((DataReference) childXMLObject);
        } else if (childXMLObject instanceof KeyReference) {
            rl.getReferences().add((KeyReference) childXMLObject);
        } else {
            super.processChildElement(parentXMLObject, childXMLObject);
        }
    }

}
