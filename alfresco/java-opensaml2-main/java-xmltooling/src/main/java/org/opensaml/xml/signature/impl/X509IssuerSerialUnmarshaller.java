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
import org.opensaml.xml.signature.X509IssuerName;
import org.opensaml.xml.signature.X509IssuerSerial;
import org.opensaml.xml.signature.X509SerialNumber;
import org.opensaml.xml.util.XMLConstants;

/**
 * A thread-safe Unmarshaller for {@link org.opensaml.xml.signature.X509IssuerSerial} objects.
 */
public class X509IssuerSerialUnmarshaller extends AbstractXMLSignatureUnmarshaller {
    
    /**
     * Constructor
     *
     */
    public X509IssuerSerialUnmarshaller(){
        super(XMLConstants.XMLSIG_NS, X509IssuerSerial.DEFAULT_ELEMENT_LOCAL_NAME);
    }

    /**
     * Constructor
     *
     * @param targetNamespaceURI
     * @param targetLocalName
     */
    protected X509IssuerSerialUnmarshaller(String targetNamespaceURI, String targetLocalName){
        super(targetNamespaceURI, targetLocalName);
    }

    /** {@inheritDoc} */
    protected void processChildElement(XMLObject parentXMLObject, XMLObject childXMLObject) throws UnmarshallingException {
        X509IssuerSerial keyValue = (X509IssuerSerial) parentXMLObject;
        
        if (childXMLObject instanceof X509IssuerName) {
            keyValue.setX509IssuerName((X509IssuerName) childXMLObject);
        } else if (childXMLObject instanceof X509SerialNumber) {
            keyValue.setX509SerialNumber((X509SerialNumber) childXMLObject);
        } else {
            super.processChildElement(parentXMLObject, childXMLObject);
        }
    }

}
