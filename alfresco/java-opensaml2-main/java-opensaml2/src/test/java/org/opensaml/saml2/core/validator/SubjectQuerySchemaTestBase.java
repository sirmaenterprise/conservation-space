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
package org.opensaml.saml2.core.validator;

import javax.xml.namespace.QName;

import org.opensaml.common.xml.SAMLConstants;
import org.opensaml.saml2.core.Subject;
import org.opensaml.saml2.core.SubjectQuery;



/**
 *
 */
public abstract class SubjectQuerySchemaTestBase extends RequestSchemaTestBase {

    /**
     * Constructor
     *
     */
    public SubjectQuerySchemaTestBase() {
        super();
    }
  
    /** {@inheritDoc} */
    protected void populateRequiredData() {
        super.populateRequiredData();
        SubjectQuery query = (SubjectQuery) target;
        Subject subject = (Subject) buildXMLObject(new QName(SAMLConstants.SAML20_NS, Subject.DEFAULT_ELEMENT_LOCAL_NAME));
        query.setSubject(subject);
    }
    
    /**
     *  Tests invalid Subject child element.
     */
    public void testSubjectFailure() {
        SubjectQuery query = (SubjectQuery) target;
        query.setSubject(null);
        assertValidationFail("Subject was null");
    }

}
