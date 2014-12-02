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

import org.opensaml.xml.encryption.CipherData;
import org.opensaml.xml.util.XMLConstants;

/**
 * A thread-safe Marshaller for {@link org.opensaml.xml.encryption.CipherData} objects.
 */
public class CipherDataMarshaller extends AbstractXMLEncryptionMarshaller {

    /** Constructor. */
    public CipherDataMarshaller() {
        super(XMLConstants.XMLENC_NS, CipherData.DEFAULT_ELEMENT_LOCAL_NAME);
    }

    /**
     * Constructor.
     * 
     * @param targetNamespaceURI namespace URI
     * @param targetLocalName local name
     */
    public CipherDataMarshaller(String targetNamespaceURI, String targetLocalName) {
        super(targetNamespaceURI, targetLocalName);
    }

}
