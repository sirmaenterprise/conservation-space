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
import org.opensaml.saml2.core.Conditions;
import org.opensaml.saml2.core.OneTimeUse;
import org.opensaml.saml2.core.ProxyRestriction;
import org.opensaml.xml.validation.ValidationException;

/**
 * Test case for {@link org.opensaml.saml2.core.validator.ConditionsSpecValidator}.
 */
public class ConditionsSpecTest extends BaseSAMLObjectValidatorTestCase {

    /** Constructor */
    public ConditionsSpecTest() {
        targetQName = new QName(SAMLConstants.SAML20_NS, Conditions.DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML20_PREFIX);
        validator = new ConditionsSpecValidator();
    }

    protected void populateRequiredData() {
        super.populateRequiredData();
    }

    /**
     * Tests OneTimeUse failure.
     * 
     * @throws ValidationException
     */
    public void testOneTimeUseFailure() throws ValidationException {
        Conditions conditions = (Conditions) target;

        OneTimeUse oneTimeUse1 = (OneTimeUse) buildXMLObject(new QName(SAMLConstants.SAML20_NS, OneTimeUse.DEFAULT_ELEMENT_LOCAL_NAME,
                SAMLConstants.SAML20_PREFIX));

        OneTimeUse oneTimeUse2 = (OneTimeUse) buildXMLObject(new QName(SAMLConstants.SAML20_NS, OneTimeUse.DEFAULT_ELEMENT_LOCAL_NAME,
                SAMLConstants.SAML20_PREFIX));
        
        conditions.getConditions().add(oneTimeUse1);
        conditions.getConditions().add(oneTimeUse2);
        assertValidationFail("Multiple OneTimeUse conditions present, should raise a Validation Exception");
    }
    
    public void testProxyRestrictionFailure() throws ValidationException {
        Conditions conditions = (Conditions) target;
        
        ProxyRestriction proxyRestriction1 = (ProxyRestriction) buildXMLObject(new QName(SAMLConstants.SAML20_NS, ProxyRestriction.DEFAULT_ELEMENT_LOCAL_NAME,
                SAMLConstants.SAML20_PREFIX));

        ProxyRestriction proxyRestriction2 = (ProxyRestriction) buildXMLObject(new QName(SAMLConstants.SAML20_NS, ProxyRestriction.DEFAULT_ELEMENT_LOCAL_NAME,
                SAMLConstants.SAML20_PREFIX));
        
        conditions.getConditions().add(proxyRestriction1);
        conditions.getConditions().add(proxyRestriction2);

        assertValidationFail("Multiple ProxyRestriction conditions present, should raise a Validation Exception");
    }
}