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

import org.joda.time.DateTime;
import org.opensaml.common.BaseSAMLObjectValidatorTestCase;
import org.opensaml.common.xml.SAMLConstants;
import org.opensaml.saml2.core.Status;
import org.opensaml.saml2.core.StatusResponseType;

/**
 *
 */
public abstract class StatusResponseSchemaTestBase extends BaseSAMLObjectValidatorTestCase {

    /**
     * Constructor
     *
     */
    public StatusResponseSchemaTestBase() {
        super();
    }

    /** {@inheritDoc} */
    protected void setUp() throws Exception {
        super.setUp();
    }
    
    /** {@inheritDoc} */
    protected void populateRequiredData() {
        super.populateRequiredData();
        StatusResponseType sr = (StatusResponseType) target;
        Status status = (Status) buildXMLObject(new QName(SAMLConstants.SAML20P_NS, Status.DEFAULT_ELEMENT_LOCAL_NAME));
        sr.setStatus(status);
        sr.setID("abc123");
        sr.setIssueInstant(new DateTime());
        // note: Version attribute is set automatically by the implementation
    }
    
    /**
     *  Tests invalid Status child element.
     */
    public void testStatusFailure() {
        StatusResponseType sr = (StatusResponseType) target;
        sr.setStatus(null);
        assertValidationFail("Status was null");
    }
    
    /**
     *  Tests invalid ID attribute.
     */
    public void testIDFailure() {
        StatusResponseType sr = (StatusResponseType) target;
        
        sr.setID(null);
        assertValidationFail("ID attribute was null");
        
        sr.setID("");
        assertValidationFail("ID attribute was empty");
        
        sr.setID("               ");
        assertValidationFail("ID attribute was all whitespace");
    }
    
    /**
     *  Tests invalid IssueInstant attribute
     */
    public void testIssueInstantFailure() {
        StatusResponseType sr = (StatusResponseType) target;
        sr.setIssueInstant(null);
        assertValidationFail("IssueInstant attribute was null");
    }
    

}
