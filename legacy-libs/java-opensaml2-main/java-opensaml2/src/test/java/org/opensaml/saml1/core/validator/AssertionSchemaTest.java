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

import org.joda.time.DateTime;
import org.opensaml.common.BaseSAMLObjectValidatorTestCase;
import org.opensaml.common.SAMLVersion;
import org.opensaml.common.xml.SAMLConstants;
import org.opensaml.saml1.core.Assertion;
import org.opensaml.saml1.core.AttributeStatement;

/**
 * Test case for {@link org.opensaml.saml1.core.validator.AssertionSchemaValidator}.
 */
public class AssertionSchemaTest extends BaseSAMLObjectValidatorTestCase {

    /** Constructor */
    public AssertionSchemaTest() {
        super();
        targetQName = new QName(SAMLConstants.SAML1_NS, Assertion.DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML1_PREFIX);
        validator = new AssertionSchemaValidator();
    }

    /**
     * Common setup method (populateRequiredData & the first test
     */
    private void setupRequiredData() {
        
        Assertion assertion = (Assertion) target;
        assertion.setIssuer("Issuer");
        assertion.setID("ident");
        assertion.setIssueInstant(new DateTime());
        QName name = new QName(SAMLConstants.SAML1_NS, AttributeStatement.DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML1_PREFIX);
        assertion.getStatements().add((AttributeStatement)buildXMLObject(name));
        
    }
    /** {@inheritDoc} */
    protected void populateRequiredData() {
        super.populateRequiredData();
        setupRequiredData();
    }
    
    public void testWrongVersion() {
        target = buildXMLObject(targetQName);
        setupRequiredData();
        assertValidationPass("SAML1.0 is OK");
        Assertion assertion = (Assertion) target;
        assertion.setVersion(SAMLVersion.VERSION_20);
        assertValidationFail("SAML2.0 is not OK");
    }
    
    public void testMissingID(){
        Assertion assertion = (Assertion) target;
        assertion.setID("");
        assertValidationFail("ID was empty, should raise a Validation Exception");
        assertion.setID(null);
        assertValidationFail("ID was null, should raise a Validation Exception");
        assertion.setID("  ");
        assertValidationFail("ID was whitespace, should raise a Validation Exception");

    }

    public void testMissingIssuer(){
        Assertion assertion = (Assertion) target;
        assertion.setIssuer("");
        assertValidationFail("Issuer was empty, should raise a Validation Exception");
        assertion.setIssuer(null);
        assertValidationFail("Issuer was null, should raise a Validation Exception");
        assertion.setIssuer("   ");
        assertValidationFail("Issuer was whitespace, should raise a Validation Exception");
    }

    public void testMissingIssueInstant(){
        Assertion assertion = (Assertion) target;
        assertion.setIssueInstant(null);
        assertValidationFail("IssueInstant was empty, should raise a Validation Exception");
    }

    public void testMissingStatement(){
        Assertion assertion = (Assertion) target;
        assertion.getStatements().clear();
        assertValidationFail("No statements, should raise a Validation Exception");
    }
}