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

/**
 * 
 */
package org.opensaml.saml2.core.validator;

import javax.xml.namespace.QName;

import org.opensaml.common.xml.SAMLConstants;
import org.opensaml.saml2.core.AssertionIDRef;
import org.opensaml.saml2.core.AssertionIDRequest;

/**
 *
 */
public class AssertionIDRequestSchemaTest extends RequestSchemaTestBase {

    /**
     * Constructor
     *
     */
    public AssertionIDRequestSchemaTest() {
        super();
        targetQName = new QName(SAMLConstants.SAML20P_NS, AssertionIDRequest.DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML20P_PREFIX);
        validator = new AssertionIDRequestSchemaValidator();
    }

    /** {@inheritDoc} */
    protected void setUp() throws Exception {
        super.setUp();
    }

    /** {@inheritDoc} */
    protected void populateRequiredData() {
        super.populateRequiredData();
        AssertionIDRequest request = (AssertionIDRequest) target;
        AssertionIDRef ref = (AssertionIDRef) buildXMLObject(new QName(SAMLConstants.SAML20_NS, AssertionIDRef.DEFAULT_ELEMENT_LOCAL_NAME));
        request.getAssertionIDRefs().add(ref);
    }
    
    /**
     * Tests invalid AssertionID Ref children.
     */
    public void testAssertionIDRefFailure() {
        AssertionIDRequest request = (AssertionIDRequest) target;
        request.getAssertionIDRefs().clear();
        assertValidationFail("AssertionIDRef list was empty");
    }

}
