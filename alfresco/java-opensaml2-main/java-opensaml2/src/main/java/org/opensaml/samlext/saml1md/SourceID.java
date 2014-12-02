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

package org.opensaml.samlext.saml1md;

import javax.xml.namespace.QName;

import org.opensaml.common.SAMLObject;
import org.opensaml.common.xml.SAMLConstants;
import org.opensaml.xml.schema.XSString;

/**
 * SAML 1 Metadata extension SourceID
 */
public interface SourceID extends SAMLObject, XSString {
    
    /** Element name, no namespace */
    public final static String DEFAULT_ELEMENT_LOCAL_NAME = "SourceID";
    
    /** Default element name */
    public final static QName DEFAULT_ELEMENT_NAME = new QName(SAMLConstants.SAML1MD_NS, DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML1MD_PREFIX);
}