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

import org.opensaml.xml.AbstractXMLObjectBuilder;
import org.opensaml.xml.encryption.Transforms;
import org.opensaml.xml.encryption.XMLEncryptionBuilder;
import org.opensaml.xml.util.XMLConstants;

/**
 * Builder of {@link org.opensaml.xml.encryption.Transforms}
 */
public class TransformsBuilder extends AbstractXMLObjectBuilder<Transforms> implements XMLEncryptionBuilder<Transforms> {

    /**
     * Constructor
     *
     */
    public TransformsBuilder() {
    }

    /** {@inheritDoc} */
    public Transforms buildObject(String namespaceURI, String localName, String namespacePrefix) {
        return new TransformsImpl(namespaceURI, localName, namespacePrefix);
    }

    /** {@inheritDoc} */
    public Transforms buildObject() {
        return buildObject(XMLConstants.XMLENC_NS, Transforms.DEFAULT_ELEMENT_LOCAL_NAME, XMLConstants.XMLENC_PREFIX);
    }

}
