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

package org.opensaml.saml2.core.impl;

import org.opensaml.saml2.core.Subject;
import org.opensaml.saml2.core.SubjectQuery;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.io.UnmarshallingException;

/**
 * A thread-safe Unmarshaller for {@link org.opensaml.saml2.core.SubjectQuery}.
 */
public abstract class SubjectQueryUnmarshaller extends RequestAbstractTypeUnmarshaller {

    /** Constructor. */
    protected SubjectQueryUnmarshaller() {
        super();
    }

    /**
     * Constructor.
     * 
     * @param namespaceURI the namespace URI of either the schema type QName or element QName of the elements this
     *            unmarshaller operates on
     * @param elementLocalName the local name of either the schema type QName or element QName of the elements this
     *            unmarshaller operates on
     * @throws IllegalArgumentException
     */
    public SubjectQueryUnmarshaller(String namespaceURI, String elementLocalName) throws IllegalArgumentException {
        super(namespaceURI, elementLocalName);
    }

    /** {@inheritDoc} */
    protected void processChildElement(XMLObject parentSAMLObject, XMLObject childSAMLObject)
            throws UnmarshallingException {
        SubjectQuery sq = (SubjectQuery) parentSAMLObject;

        if (childSAMLObject instanceof Subject) {
            sq.setSubject((Subject) childSAMLObject);
        } else {
            super.processChildElement(parentSAMLObject, childSAMLObject);
        }
    }
}