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

package org.opensaml.samlext.saml1md.impl;

import org.opensaml.common.impl.AbstractSAMLObjectMarshaller;
import org.opensaml.common.xml.SAMLConstants;
import org.opensaml.samlext.saml1md.SourceID;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.io.MarshallingException;
import org.opensaml.xml.util.DatatypeHelper;
import org.opensaml.xml.util.XMLHelper;
import org.w3c.dom.Element;

/**
 * Marshaller of {@link SourceID} objects.
 */
public class SourceIDMarshaller extends AbstractSAMLObjectMarshaller {

    /** Constructor */
    public SourceIDMarshaller() {
        super(SAMLConstants.SAML1MD_NS, SourceID.DEFAULT_ELEMENT_LOCAL_NAME);
    }

    /** {@inheritDoc} */
    protected void marshallElementContent(XMLObject xmlObject, Element domElement) throws MarshallingException {
        SourceID sourceID = (SourceID) xmlObject;

        if (!DatatypeHelper.isEmpty(sourceID.getValue())) {
            XMLHelper.appendTextContent(domElement, sourceID.getValue());
        }
    }
}