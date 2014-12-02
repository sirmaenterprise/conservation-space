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

package org.opensaml.common.impl;

import org.opensaml.xml.XMLObject;
import org.opensaml.xml.io.AbstractXMLObjectMarshaller;
import org.opensaml.xml.io.MarshallingException;
import org.w3c.dom.Element;

/**
 * A thread safe, abstract implementation of the {@link org.opensaml.xml.io.Marshaller} interface that handles most of
 * the boilerplate code for Marshallers.
 */
public abstract class AbstractSAMLObjectMarshaller extends AbstractXMLObjectMarshaller {
    
    /** Constructor. */
    protected AbstractSAMLObjectMarshaller(){
        super();
    }

    /**
     * Constructor.
     * 
     * @param targetNamespaceURI the namespace URI of either the schema type QName or element QName of the elements this
     *            unmarshaller operates on
     * @param targetLocalName the local name of either the schema type QName or element QName of the elements this
     *            unmarshaller operates on
     */
    protected AbstractSAMLObjectMarshaller(String targetNamespaceURI, String targetLocalName) {
        super(targetNamespaceURI, targetLocalName);
    }

    /**
     * No-op method. Extending implementations should override this method if they have attributes to marshall into the
     * Element.
     * 
     * {@inheritDoc}
     */
    protected void marshallAttributes(XMLObject xmlObject, Element domElement) throws MarshallingException {

    }

    /**
     * No-op method. Extending implementations should override this method if they have text content to marshall into
     * the Element.
     * 
     * {@inheritDoc}
     */
    protected void marshallElementContent(XMLObject xmlObject, Element domElement) throws MarshallingException {

    }
}