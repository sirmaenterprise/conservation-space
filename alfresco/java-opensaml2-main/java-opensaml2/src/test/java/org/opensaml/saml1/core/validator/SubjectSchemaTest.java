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
import org.opensaml.saml1.core.NameIdentifier;
import org.opensaml.saml1.core.Subject;
import org.opensaml.saml1.core.SubjectConfirmation;

/**
 * Test case for {@link org.opensaml.saml1.core.validator.SubjectSchemaValidator}.
 */
public class SubjectSchemaTest extends BaseSAMLObjectValidatorTestCase {

    /** Constructor */
    public SubjectSchemaTest() {
        super();
        targetQName = new QName(SAMLConstants.SAML1_NS, Subject.DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML1_PREFIX);
        validator = new SubjectSchemaValidator();
    }

    /** {@inheritDoc} */
    protected void populateRequiredData() {
        super.populateRequiredData();

        Subject subject = (Subject) target;
        
        QName qname = new QName(SAMLConstants.SAML1_NS, NameIdentifier.DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML1_PREFIX);
        subject.setNameIdentifier((NameIdentifier)buildXMLObject(qname));
        qname = new QName(SAMLConstants.SAML1_NS, SubjectConfirmation.DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML1_PREFIX);
        subject.setSubjectConfirmation((SubjectConfirmation)buildXMLObject(qname));
    }
    
    public void testMissingNameId(){
        Subject subject = (Subject) target;

        subject.setNameIdentifier(null);
        assertValidationPass("No NameIdenitifer, but SubjectConfirmation still present, should be valid");
        subject.setSubjectConfirmation(null);
        assertValidationFail("No NameIdenitifer and no SubjectConfirmation");
    }

    public void testMissingSubjectConf(){
        Subject subject = (Subject) target;

        subject.setSubjectConfirmation(null);
        assertValidationPass("No SubjectConfirmation, but NameIdenitifer still present, should be valid");
        subject.setNameIdentifier(null);
        assertValidationFail("No NameIdenitifer and no SubjectConfirmation");
    }
}