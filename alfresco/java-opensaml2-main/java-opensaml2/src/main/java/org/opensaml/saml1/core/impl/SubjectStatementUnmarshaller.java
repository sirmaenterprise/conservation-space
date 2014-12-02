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

package org.opensaml.saml1.core.impl;

import org.opensaml.common.impl.AbstractSAMLObjectUnmarshaller;
import org.opensaml.saml1.core.Subject;
import org.opensaml.saml1.core.SubjectStatement;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.io.UnmarshallingException;

/**
 * Unamershaller for {@link SubjectStatement}s.
 */
public abstract class SubjectStatementUnmarshaller extends AbstractSAMLObjectUnmarshaller {

    /** Constructor. */
    protected SubjectStatementUnmarshaller() {
        super();
    }

    /**
     * Constructor.
     * 
     * @param targetNamespaceURI
     * @param targetLocalName
     */
    protected SubjectStatementUnmarshaller(String targetNamespaceURI, String targetLocalName) {
        super(targetNamespaceURI, targetLocalName);
    }

    /** {@inheritDoc} */
    protected void processChildElement(XMLObject parentSAMLObject, XMLObject childSAMLObject)
            throws UnmarshallingException {
        SubjectStatement statement = (SubjectStatement) parentSAMLObject;

        if (childSAMLObject instanceof Subject) {
            statement.setSubject((Subject) childSAMLObject);
        } else {
            super.processChildElement(parentSAMLObject, childSAMLObject);
        }
    }

}
