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

import org.joda.time.DateTime;
import org.joda.time.chrono.ISOChronology;
import org.opensaml.common.impl.AbstractSAMLObjectUnmarshaller;
import org.opensaml.common.xml.SAMLConstants;
import org.opensaml.saml1.core.Condition;
import org.opensaml.saml1.core.Conditions;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.io.UnmarshallingException;
import org.opensaml.xml.util.DatatypeHelper;
import org.w3c.dom.Attr;

/**
 * A thread-safe Unmarshaller for {@link org.opensaml.saml1.core.Conditions} objects.
 */
public class ConditionsUnmarshaller extends AbstractSAMLObjectUnmarshaller {

    /** Constructor. */
    public ConditionsUnmarshaller() {
        super(SAMLConstants.SAML1_NS, Conditions.DEFAULT_ELEMENT_LOCAL_NAME);
    }

    /** {@inheritDoc} */
    protected void processChildElement(XMLObject parentSAMLObject, XMLObject childSAMLObject)
            throws UnmarshallingException {
        Conditions conditions = (Conditions) parentSAMLObject;

        if (childSAMLObject instanceof Condition) {
            conditions.getConditions().add((Condition) childSAMLObject);
        } else {
            super.processChildElement(parentSAMLObject, childSAMLObject);
        }
    }

    /** {@inheritDoc} */
    protected void processAttribute(XMLObject samlObject, Attr attribute) throws UnmarshallingException {

        Conditions conditions = (Conditions) samlObject;

        if (Conditions.NOTBEFORE_ATTRIB_NAME.equals(attribute.getLocalName())
                && !DatatypeHelper.isEmpty(attribute.getValue())) {
            conditions.setNotBefore(new DateTime(attribute.getValue(), ISOChronology.getInstanceUTC()));
        } else if (Conditions.NOTONORAFTER_ATTRIB_NAME.equals(attribute.getLocalName())
                && !DatatypeHelper.isEmpty(attribute.getValue())) {
            conditions.setNotOnOrAfter(new DateTime(attribute.getValue(), ISOChronology.getInstanceUTC()));
        } else {
            processAttribute(samlObject, attribute);
        }
    }
}