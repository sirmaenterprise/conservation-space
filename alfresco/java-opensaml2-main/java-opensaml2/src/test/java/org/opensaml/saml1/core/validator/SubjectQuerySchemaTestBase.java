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

package org.opensaml.saml1.core.validator;

import javax.xml.namespace.QName;

import org.opensaml.common.BaseSAMLObjectValidatorTestCase;
import org.opensaml.common.xml.SAMLConstants;
import org.opensaml.saml1.core.Subject;
import org.opensaml.saml1.core.SubjectQuery;

/**
 * Test class for {@link org.opensaml.saml1.core.validator.SubjectQuerySchemaValidator}.
 */
public abstract class SubjectQuerySchemaTestBase extends BaseSAMLObjectValidatorTestCase {

    /** Constructor */
    public SubjectQuerySchemaTestBase() {
        super();
    }

    /** {@inheritDoc} */
    protected void populateRequiredData() {
        super.populateRequiredData();
        
        SubjectQuery query = (SubjectQuery) target;
        QName qname = new QName(SAMLConstants.SAML1_NS, Subject.DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML1_PREFIX);
        query.setSubject((Subject) buildXMLObject(qname));
    }
    
    public void testSubject() {
        SubjectQuery query = (SubjectQuery) target;
        query.setSubject(null);
        assertValidationFail("No Subject, should raise a Validation Exception");
    }
}