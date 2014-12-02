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
import org.opensaml.saml1.core.ConfirmationMethod;
import org.opensaml.saml1.core.SubjectConfirmation;

/**
 * Test case for {@link org.opensaml.saml1.core.validator.SubjectConfirmationSchemaValidator}.
 */
public class SubjectConfirmationSchemaTest extends BaseSAMLObjectValidatorTestCase {

    /** Constructor */
    public SubjectConfirmationSchemaTest() {
        super();
        targetQName = new QName(SAMLConstants.SAML1_NS, SubjectConfirmation.DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML1_PREFIX);
        validator = new SubjectConfirmationSchemaValidator();
    }

    /** {@inheritDoc} */
    protected void populateRequiredData() {
        super.populateRequiredData();

        SubjectConfirmation subjectConfirmation = (SubjectConfirmation) target;
        
        QName qname = new QName(SAMLConstants.SAML1_NS, ConfirmationMethod.DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML1_PREFIX);
        subjectConfirmation.getConfirmationMethods().add((ConfirmationMethod)buildXMLObject(qname));
    }
    
    public void testMissingConfirmationMethod(){
        SubjectConfirmation subjectConfirmation = (SubjectConfirmation) target;

        subjectConfirmation.getConfirmationMethods().clear();
        assertValidationFail("No Confirmation methods - should fail");
    }
}