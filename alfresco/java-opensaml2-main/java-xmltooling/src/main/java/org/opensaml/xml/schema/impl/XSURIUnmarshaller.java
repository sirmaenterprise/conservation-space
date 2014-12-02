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
import org.opensaml.xml.io.AbstractXMLObjectUnmarshaller;
import org.opensaml.xml.io.UnmarshallingException;
import org.opensaml.xml.schema.XSURI;
import org.w3c.dom.Attr;

/**
 * A thread-safe unmarshaller for {@link org.opensaml.xml.schema.XSURI}s.
 */
public class XSURIUnmarshaller extends AbstractXMLObjectUnmarshaller {

    /** Constructor. */
    public XSURIUnmarshaller() {
        super();
    }

    /**
     * This constructor supports checking an XMLObject to be marshalled, either element name or schema type, against a
     * given namespace/local name pair.
     * 
     * @param targetNamespaceURI the namespace URI of either the schema type QName or element QName of the elements this
     *            unmarshaller operates on
     * @param targetLocalName the local name of either the schema type QName or element QName of the elements this
     *            unmarshaller operates on
     */
    protected XSURIUnmarshaller(String targetNamespaceURI, String targetLocalName) {
        super(targetNamespaceURI, targetLocalName);
    }

    /** {@inheritDoc} */
    protected void processChildElement(XMLObject parentXMLObject, XMLObject childXMLObject)
            throws UnmarshallingException {
        // no child elements
    }

    /** {@inheritDoc} */
    protected void processAttribute(XMLObject xmlObject, Attr attribute) throws UnmarshallingException {
        // no attributes
    }

    /** {@inheritDoc} */
    protected void processElementContent(XMLObject xmlObject, String elementContent) {
        XSURI uri = (XSURI) xmlObject;
        uri.setValue(elementContent);
    }
}