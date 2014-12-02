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
import org.opensaml.xml.signature.PGPData;
import org.opensaml.xml.signature.PGPKeyID;
import org.opensaml.xml.signature.PGPKeyPacket;
import org.opensaml.xml.util.XMLConstants;

/**
 * A thread-safe Unmarshaller for {@link org.opensaml.xml.signature.PGPData} objects.
 */
public class PGPDataUnmarshaller extends AbstractXMLSignatureUnmarshaller {
    
    /**
     * Constructor
     *
     */
    public PGPDataUnmarshaller(){
        super(XMLConstants.XMLSIG_NS, PGPData.DEFAULT_ELEMENT_LOCAL_NAME);
    }

    /**
     * Constructor
     *
     * @param targetNamespaceURI
     * @param targetLocalName
     */
    protected PGPDataUnmarshaller(String targetNamespaceURI, String targetLocalName){
        super(targetNamespaceURI, targetLocalName);
    }

    /** {@inheritDoc} */
    protected void processChildElement(XMLObject parentXMLObject, XMLObject childXMLObject) throws UnmarshallingException {
        PGPData pgpData = (PGPData) parentXMLObject;
        
        if (childXMLObject instanceof PGPKeyID) {
            pgpData.setPGPKeyID((PGPKeyID) childXMLObject);
        } else if (childXMLObject instanceof PGPKeyPacket) {
            pgpData.setPGPKeyPacket((PGPKeyPacket) childXMLObject);
        } else {
            // Unbounded choice of <any> wildcard elements
            pgpData.getUnknownXMLObjects().add(childXMLObject);
        }
    }

}
