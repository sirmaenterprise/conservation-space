/*
 * Copyright 2008 University Corporation for Advanced Internet Development, Inc.
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

package org.opensaml.saml2.core;

import java.util.List;

import org.opensaml.common.impl.AbstractSAMLObject;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.schema.XSString;

/**
 * Mock BaseID class for testing purposes.
 */
public class MockBaseID extends AbstractSAMLObject implements BaseID, XSString {
    
    /** String content. */
    private String content;
    
    /** Name qualifier. */
    private String nameQualifier;
    
    /** SP name qualifier. */
    private String spNameQualifier;
    
    /** Constructor. */
    public MockBaseID() {
        this(
                BaseID.DEFAULT_ELEMENT_NAME.getNamespaceURI(), 
                BaseID.DEFAULT_ELEMENT_LOCAL_NAME,
                BaseID.DEFAULT_ELEMENT_NAME.getPrefix());
    }

    /**
     * Constructor.
     *
     * @param namespaceURI
     * @param elementLocalName
     * @param namespacePrefix
     */
    protected MockBaseID(String namespaceURI, String elementLocalName, String namespacePrefix) {
        super(namespaceURI, elementLocalName, namespacePrefix);
    }

    /** {@inheritDoc} */
    public String getNameQualifier() {
        return nameQualifier;
    }

    /** {@inheritDoc} */
    public String getSPNameQualifier() {
        return spNameQualifier;
    }

    /** {@inheritDoc} */
    public void setNameQualifier(String newNameQualifier) {
        nameQualifier = prepareForAssignment(nameQualifier, newNameQualifier);
    }

    /** {@inheritDoc} */
    public void setSPNameQualifier(String newSPNameQualifier) {
        spNameQualifier = prepareForAssignment(spNameQualifier, newSPNameQualifier);
    }

    /** {@inheritDoc} */
    public List<XMLObject> getOrderedChildren() {
        return null;
    }

    /** {@inheritDoc} */
    public String getValue() {
        return content;
    }

    /** {@inheritDoc} */
    public void setValue(String newValue) {
        content = prepareForAssignment(content, newValue);
    }
}
