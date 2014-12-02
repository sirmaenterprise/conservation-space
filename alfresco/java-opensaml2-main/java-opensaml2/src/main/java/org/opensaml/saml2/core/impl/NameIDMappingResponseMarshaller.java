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

import org.opensaml.common.xml.SAMLConstants;
import org.opensaml.saml2.core.NameIDMappingResponse;

/**
 * A thread safe Marshaller for {@link org.opensaml.saml2.core.NameIDMappingResponse} objects.
 */
public class NameIDMappingResponseMarshaller extends StatusResponseTypeMarshaller {

    /**
     * Constructor.
     */
    public NameIDMappingResponseMarshaller() {
        super(SAMLConstants.SAML20P_NS, NameIDMappingResponse.DEFAULT_ELEMENT_LOCAL_NAME);
    }

    /**
     * Constructor.
     * 
     * @param namespaceURI the namespace URI of either the schema type QName or element QName of the elements this
     *            marshaller operates on
     * @param elementLocalName the local name of either the schema type QName or element QName of the elements this
     *            marshaller operates on
     */
    protected NameIDMappingResponseMarshaller(String namespaceURI, String elementLocalName) {
        super(namespaceURI, elementLocalName);
    }

    // no attributes to marshall
}