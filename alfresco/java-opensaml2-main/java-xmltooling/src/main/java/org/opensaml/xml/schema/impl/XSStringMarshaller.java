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

package org.opensaml.xml.schema.impl;

import org.opensaml.xml.XMLObject;
import org.opensaml.xml.io.AbstractXMLObjectMarshaller;
import org.opensaml.xml.io.MarshallingException;
import org.opensaml.xml.schema.XSString;
import org.opensaml.xml.util.XMLHelper;
import org.w3c.dom.Element;

/**
 * Thread-safe marshaller of {@link org.opensaml.xml.schema.XSString} objects.
 */
public class XSStringMarshaller extends AbstractXMLObjectMarshaller {
    
    /** Constructor. */
    public XSStringMarshaller() {
        super();
    }

    /**
     * This constructor supports checking an XMLObject to be marshalled, either element name or schema type, against a
     * given namespace/local name pair.
     * 
     * @param targetNamespaceURI the namespace URI of either the schema type QName or element QName of the elements this
     *            marshaller operates on
     * @param targetLocalName the local name of either the schema type QName or element QName of the elements this
     *            marshaller operates on
     */
    protected XSStringMarshaller(String targetNamespaceURI, String targetLocalName) {
        super(targetNamespaceURI, targetLocalName);
    }

    /** {@inheritDoc} */
    protected void marshallAttributes(XMLObject xmlObject, Element domElement) throws MarshallingException {
        // no attributes
    }

    /** {@inheritDoc} */
    protected void marshallElementContent(XMLObject xmlObject, Element domElement) throws MarshallingException {
        XSString xsiString = (XSString) xmlObject;
        
        XMLHelper.appendTextContent(domElement, xsiString.getValue());
    }
}