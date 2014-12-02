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
import org.opensaml.saml1.core.Audience;
import org.opensaml.saml1.core.AudienceRestrictionCondition;

/**
 * Test case for {@link org.opensaml.saml1.core.validator.AudienceRestrictionConditionSchemaValidator}.
 */
public class AudienceRestrictionConditionSchemaTest extends BaseSAMLObjectValidatorTestCase {

    /** Constructor */
    public AudienceRestrictionConditionSchemaTest() {
        super();
        targetQName = new QName(SAMLConstants.SAML1_NS, AudienceRestrictionCondition.DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML1_PREFIX);
        validator = new AudienceRestrictionConditionSchemaValidator();
    }

    /** {@inheritDoc} */
    protected void populateRequiredData() {
        super.populateRequiredData();
        
        AudienceRestrictionCondition audienceRestrictionCondition = (AudienceRestrictionCondition) target;
        QName name = new QName(SAMLConstants.SAML1_NS, Audience.DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML1_PREFIX);
        audienceRestrictionCondition.getAudiences().add((Audience)buildXMLObject(name));
    }
    
    public void testMissingAudience(){
        AudienceRestrictionCondition audienceRestrictionCondition = (AudienceRestrictionCondition) target;
        audienceRestrictionCondition.getAudiences().clear();
        assertValidationFail("Audience was empty, should raise a Validation Exception");
    }
}