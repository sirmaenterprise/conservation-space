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

package org.opensaml.saml1.core.impl;

import org.opensaml.Configuration;
import org.opensaml.common.impl.AbstractSAMLObjectMarshaller;
import org.opensaml.common.xml.SAMLConstants;
import org.opensaml.saml1.core.Conditions;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.io.MarshallingException;
import org.w3c.dom.Element;

/**
 * A thread safe Marshaller for {@link org.opensaml.saml2.core.Conditions} objects.
 */
public class ConditionsMarshaller extends AbstractSAMLObjectMarshaller {

    /**
     * Constructor.
     */
    public ConditionsMarshaller() {
        super(SAMLConstants.SAML1_NS, Conditions.DEFAULT_ELEMENT_LOCAL_NAME);
    }

    /** {@inheritDoc} */
    protected void marshallAttributes(XMLObject samlElement, Element domElement) throws MarshallingException {

        Conditions conditions = (Conditions) samlElement;

        if (conditions.getNotBefore() != null) {
            String date = Configuration.getSAMLDateFormatter().print(conditions.getNotBefore());
            domElement.setAttributeNS(null, Conditions.NOTBEFORE_ATTRIB_NAME, date);
        }

        if (conditions.getNotOnOrAfter() != null) {
            String date = Configuration.getSAMLDateFormatter().print(conditions.getNotOnOrAfter());
            domElement.setAttributeNS(null, Conditions.NOTONORAFTER_ATTRIB_NAME, date);
        }
    }
}