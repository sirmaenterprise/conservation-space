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

/**
 * 
 */

package org.opensaml.saml2.metadata.impl;

import org.opensaml.common.impl.AbstractSAMLObjectMarshaller;
import org.opensaml.common.xml.SAMLConstants;
import org.opensaml.saml2.metadata.AttributeConsumingService;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.io.MarshallingException;
import org.w3c.dom.Element;

/**
 * A thread safe Marshaller for {@link org.opensaml.saml2.metadata.AttributeConsumingService} objects.
 */
public class AttributeConsumingServiceMarshaller extends AbstractSAMLObjectMarshaller {

    /**
     * Constructor
     */
    public AttributeConsumingServiceMarshaller() {
        super(SAMLConstants.SAML20MD_NS, AttributeConsumingService.DEFAULT_ELEMENT_LOCAL_NAME);
    }

    /**
     * Constructor
     * 
     * @param namespaceURI
     * @param elementLocalName
     */
    protected AttributeConsumingServiceMarshaller(String namespaceURI, String elementLocalName) {
        super(namespaceURI, elementLocalName);
    }

    /** {@inheritDoc} */
    protected void marshallAttributes(XMLObject samlObject, Element domElement) throws MarshallingException {
        AttributeConsumingService service = (AttributeConsumingService) samlObject;

        domElement.setAttributeNS(null, AttributeConsumingService.INDEX_ATTRIB_NAME, Integer.toString(service
                .getIndex()));

        if (service.isDefaultXSBoolean() != null) {
            domElement.setAttributeNS(null, AttributeConsumingService.IS_DEFAULT_ATTRIB_NAME,
                    service.isDefaultXSBoolean().toString());
        }
    }
}