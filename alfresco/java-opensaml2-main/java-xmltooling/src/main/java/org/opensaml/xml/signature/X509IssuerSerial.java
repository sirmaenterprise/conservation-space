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

package org.opensaml.xml.signature;

import javax.xml.namespace.QName;

import org.opensaml.xml.util.XMLConstants;
import org.opensaml.xml.validation.ValidatingXMLObject;

/**
 * XMLObject representing XML Digital Signature, version 20020212, X509IssuerSerial element.
 */
public interface X509IssuerSerial extends ValidatingXMLObject {
    
    /** Element local name */
    public final static String DEFAULT_ELEMENT_LOCAL_NAME = "X509IssuerSerial";
    
    /** Default element name */
    public final static QName DEFAULT_ELEMENT_NAME = new QName(XMLConstants.XMLSIG_NS, DEFAULT_ELEMENT_LOCAL_NAME, XMLConstants.XMLSIG_PREFIX);
    
    /** Local name of the XSI type */
    public final static String TYPE_LOCAL_NAME = "X509IssuerSerialType"; 
        
    /** QName of the XSI type */
    public final static QName TYPE_NAME = new QName(XMLConstants.XMLSIG_NS, TYPE_LOCAL_NAME, XMLConstants.XMLSIG_PREFIX);
    
    /**
     * Get the X509IssuerName child element
     * 
     * @return the X509Issuername child element
     */
    public X509IssuerName getX509IssuerName();
    
    /**
     * Set the X509IssuerName child element
     * 
     * @param newX509IssuerName the new X509IssuerName child element
     */
    public void setX509IssuerName(X509IssuerName newX509IssuerName);
    
    /**
     * Get the X509SerialNumber child element
     * 
     * @return the X509SerialNumber child element
     */
    public X509SerialNumber getX509SerialNumber();
    
    /**
     * Set the X509SerialNumber child element
     * 
     * @param newX509SerialNumber the new X509SerialNumber child element
     */
    public void setX509SerialNumber(X509SerialNumber newX509SerialNumber);
    
}
