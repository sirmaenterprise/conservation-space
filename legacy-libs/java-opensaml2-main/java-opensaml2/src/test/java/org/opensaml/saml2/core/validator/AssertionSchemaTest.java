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

package org.opensaml.saml2.core.validator;

import javax.xml.namespace.QName;

import org.joda.time.DateTime;
import org.joda.time.chrono.ISOChronology;
import org.opensaml.common.BaseSAMLObjectValidatorTestCase;
import org.opensaml.common.xml.SAMLConstants;
import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.Issuer;
import org.opensaml.xml.validation.ValidationException;

/**
 * Test case for {@link org.opensaml.saml2.core.validator.AssertionSchemaValidator}.
 */
public class AssertionSchemaTest extends BaseSAMLObjectValidatorTestCase {

    /** Constructor */
    public AssertionSchemaTest() {
        targetQName = new QName(SAMLConstants.SAML20_NS, Assertion.DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML20_PREFIX);
        validator = new AssertionSchemaValidator();
    }

    /** {@inheritDoc} */
    protected void populateRequiredData() {
        super.populateRequiredData();
        Assertion assertion = (Assertion) target;
        Issuer issuer = (Issuer) buildXMLObject(new QName(SAMLConstants.SAML20_NS, Issuer.DEFAULT_ELEMENT_LOCAL_NAME,
                SAMLConstants.SAML20_PREFIX));

        assertion.setIssuer(issuer);
        assertion.setID("id");
        assertion.setIssueInstant(new DateTime(1984, 8, 26, 10, 01, 30, 43, ISOChronology.getInstanceUTC()));
    }

    /**
     * Tests absent Issuer failure.
     * 
     * @throws ValidationException
     */
    public void testIssuerFailure() throws ValidationException {
        Assertion assertion = (Assertion) target;

        assertion.setIssuer(null);
        assertValidationFail("Issuer was null, should raise a Validation Exception");
    }

    /**
     * Tests absent ID failure.
     * 
     * @throws ValidationException
     */
    public void testIDFailure() throws ValidationException {
        Assertion assertion = (Assertion) target;

        assertion.setID(null);
        assertValidationFail("ID was null, should raise a Validation Exception");

        assertion.setID("");
        assertValidationFail("ID was empty string, should raise a Validation Exception");
        
        assertion.setID("    ");
        assertValidationFail("ID was white space, should raise a Validation Exception");
    }

    /**
     * Tests absent IssueInstant failure.
     * 
     * @throws ValidationException
     */
    public void testIssueInstantFailure() throws ValidationException {
        Assertion assertion = (Assertion) target;

        assertion.setIssueInstant(null);
        assertValidationFail("IssueInstant was null, should raise a Validation Exception");
    }
}