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

package org.opensaml.saml2.metadata.validator;

import javax.xml.namespace.QName;

import org.opensaml.common.BaseSAMLObjectValidatorTestCase;
import org.opensaml.common.xml.SAMLConstants;
import org.opensaml.saml2.metadata.KeyDescriptor;
import org.opensaml.xml.security.credential.UsageType;
import org.opensaml.xml.signature.KeyInfo;

/**
 * Test case for {@link org.opensaml.saml2.metadata.KeyDescriptor}.
 */
public class KeyDescriptorSchemaTest extends BaseSAMLObjectValidatorTestCase {

    /** Constructor. */
    public KeyDescriptorSchemaTest() {
        targetQName = new QName(SAMLConstants.SAML20MD_NS, KeyDescriptor.DEFAULT_ELEMENT_LOCAL_NAME,
                SAMLConstants.SAML20MD_PREFIX);
        validator = new KeyDescriptorSchemaValidator();
    }

    /** {@inheritDoc} */
    protected void populateRequiredData() {
        super.populateRequiredData();
        KeyDescriptor keyDescriptor = (KeyDescriptor) target;
        keyDescriptor.setKeyInfo((KeyInfo) buildXMLObject(KeyInfo.DEFAULT_ELEMENT_NAME));
    }
    
    /**
     * Tests for valid values of use attribute.
     */
    public void testUseValuesSuccess() {
        KeyDescriptor keyDescriptor = (KeyDescriptor) target;
        
        keyDescriptor.setUse(UsageType.SIGNING);
        assertValidationPass("Use attribute had legal value 'SIGNING'");
        
        keyDescriptor.setUse(UsageType.ENCRYPTION);
        assertValidationPass("Use attribute had legal value 'ENCRYPTION'");
        
        keyDescriptor.setUse(UsageType.UNSPECIFIED);
        assertValidationPass("Use attribute had legal value 'UNSPECIFIED'");
    }
    
    /**
     * Tests missing KeyInfo.
     */
    public void testMissingKeyInfo() {
        KeyDescriptor keyDescriptor = (KeyDescriptor) target;
        
        keyDescriptor.setKeyInfo(null);
        assertValidationFail("KeyInfo was missing");
    }
}