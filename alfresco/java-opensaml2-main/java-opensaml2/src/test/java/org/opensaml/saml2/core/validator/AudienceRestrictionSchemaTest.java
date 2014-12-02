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

import org.opensaml.common.BaseSAMLObjectValidatorTestCase;
import org.opensaml.common.xml.SAMLConstants;
import org.opensaml.saml2.core.Audience;
import org.opensaml.saml2.core.AudienceRestriction;
import org.opensaml.xml.validation.ValidationException;

/**
 * Test case for {@link org.opensaml.saml2.core.validator.AudienceRestrictionSchemaValidator}.
 */
public class AudienceRestrictionSchemaTest extends BaseSAMLObjectValidatorTestCase {

    /** Constructor */
    public AudienceRestrictionSchemaTest() {
        targetQName = new QName(SAMLConstants.SAML20_NS, AudienceRestriction.DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML20_PREFIX);
        validator = new AudienceRestrictionSchemaValidator();
    }

    protected void populateRequiredData() {
        super.populateRequiredData();
        AudienceRestriction audienceRestriction = (AudienceRestriction) target;
        Audience audience = (Audience) buildXMLObject(new QName(SAMLConstants.SAML20_NS, Audience.DEFAULT_ELEMENT_LOCAL_NAME,
                SAMLConstants.SAML20_PREFIX));
        audienceRestriction.getAudiences().add(audience);
    }

    /**
     * Tests absent Audience failure.
     * 
     * @throws ValidationException
     */
    public void testAudienceFailure() throws ValidationException {
        AudienceRestriction audienceRestriction = (AudienceRestriction) target;

        audienceRestriction.getAudiences().clear();
        assertValidationFail("Audience list empty, should raise a Validation Exception");
    }
}