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
import org.opensaml.saml1.core.ConfirmationMethod;
import org.opensaml.saml1.core.SubjectConfirmation;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.io.UnmarshallingException;

/**
 * A thread-safe Unmarshaller for {@link org.opensaml.saml1.core.SubjectConfirmation} objects.
 */
public class SubjectConfirmationUnmarshaller extends AbstractSAMLObjectUnmarshaller {

    /**
     * Constructor
     */
    public SubjectConfirmationUnmarshaller() {
        super(SAMLConstants.SAML1_NS, SubjectConfirmation.DEFAULT_ELEMENT_LOCAL_NAME);
    }

    /** {@inheritDoc} */
    protected void processChildElement(XMLObject parentSAMLObject, XMLObject childSAMLObject)
            throws UnmarshallingException {

        SubjectConfirmation subjectConfirmation = (SubjectConfirmation) parentSAMLObject;

        if (childSAMLObject instanceof ConfirmationMethod) {
            subjectConfirmation.getConfirmationMethods().add((ConfirmationMethod) childSAMLObject);
        } else {
            subjectConfirmation.setSubjectConfirmationData(childSAMLObject);
        }
    }
}