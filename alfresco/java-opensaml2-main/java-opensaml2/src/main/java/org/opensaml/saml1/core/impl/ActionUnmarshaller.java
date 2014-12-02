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

import org.opensaml.common.impl.AbstractSAMLObjectUnmarshaller;
import org.opensaml.common.xml.SAMLConstants;
import org.opensaml.saml1.core.Action;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.io.UnmarshallingException;
import org.w3c.dom.Attr;

/**
 * A thread-safe Unmarshaller for {@link org.opensaml.saml1.core.Action} objects.
 */
public class ActionUnmarshaller extends AbstractSAMLObjectUnmarshaller {

    /** Constructor. */
    public ActionUnmarshaller() {
        super(SAMLConstants.SAML1_NS, Action.DEFAULT_ELEMENT_LOCAL_NAME);
    }

    /** {@inheritDoc} */
    protected void processAttribute(XMLObject samlObject, Attr attribute) throws UnmarshallingException {

        if (Action.NAMESPACEATTRIB_NAME.equals(attribute.getLocalName())) {
            Action action = (Action) samlObject;
            action.setNamespace(attribute.getValue());
        } else {
            super.processAttribute(samlObject, attribute);
        }
    }

    /** {@inheritDoc} */
    protected void processElementContent(XMLObject samlObject, String elementContent) {
        Action action = (Action) samlObject;
        action.setContents(elementContent);
    }
}