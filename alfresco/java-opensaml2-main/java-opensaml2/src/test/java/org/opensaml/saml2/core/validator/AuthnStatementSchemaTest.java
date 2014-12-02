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
import org.opensaml.saml2.core.AuthnContext;
import org.opensaml.saml2.core.AuthnStatement;
import org.opensaml.xml.validation.ValidationException;

/**
 * Test case for {@link org.opensaml.saml2.core.validator.AuthnStatementSchemaValidator}.
 */
public class AuthnStatementSchemaTest extends BaseSAMLObjectValidatorTestCase {

    /** Constructor */
    public AuthnStatementSchemaTest() {
        targetQName = new QName(SAMLConstants.SAML20_NS, AuthnStatement.DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML20_PREFIX);
        validator = new AuthnStatementSchemaValidator();
    }

    /** {@inheritDoc} */
    protected void populateRequiredData() {
        super.populateRequiredData();
        AuthnStatement authnStatement = (AuthnStatement) target;
        AuthnContext authnContext = (AuthnContext) buildXMLObject(new QName(SAMLConstants.SAML20_NS,
                AuthnContext.DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML20_PREFIX));
        authnStatement.setAuthnInstant(new DateTime(1984, 8, 26, 10, 01, 30, 43, ISOChronology.getInstanceUTC()));
        authnStatement.setAuthnContext(authnContext);
    }

    /**
     * Tests absent AuthnInstant failure.
     * 
     * @throws ValidationException
     */
    public void testIssuerFailure() throws ValidationException {
        AuthnStatement authnStatement = (AuthnStatement) target;

        authnStatement.setAuthnInstant(null);
        try {
            validator.validate(authnStatement);
            fail("AuthnInstant was null, should raise a Validation Exception");
        } catch (ValidationException e) {
        }
    }

    /**
     * Tests absent AuthnContext failure.
     * 
     * @throws ValidationException
     */
    public void testIDFailure() throws ValidationException {
        AuthnStatement authnStatement = (AuthnStatement) target;

        authnStatement.setAuthnContext(null);
        try {
            validator.validate(authnStatement);
            fail("AuthnContext was null, should raise a Validation Exception");
        } catch (ValidationException e) {
        }
    }
}