/*
 * Copyright [2007] [University Corporation for Advanced Internet Development, Inc.]
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

package org.opensaml.common.binding.security;

import org.joda.time.DateTime;
import org.opensaml.saml2.core.AttributeQuery;
import org.opensaml.saml2.core.NameID;
import org.opensaml.saml2.core.Response;
import org.opensaml.ws.message.BaseMessageContext;
import org.opensaml.ws.security.SecurityPolicyException;

/**
 * Testing SAML issue instant security policy rule.
 */
public class IssueInstantRuleTest extends BaseSAMLSecurityPolicyRuleTestCase<AttributeQuery, Response, NameID> {
    
    private int clockSkew;
    private int expires;
    
    private DateTime now;

    /** {@inheritDoc} */
    protected void setUp() throws Exception {
        super.setUp();
        
        now = new DateTime();
        clockSkew = 60*5;
        expires = 60*10;
        
        messageContext.setInboundSAMLMessageIssueInstant(now);
        
        rule = new IssueInstantRule(clockSkew, expires);
    }
    
    /**
     *  Test valid issue instant.
     */
    public void testValid() {
        assertRuleSuccess("Message issue instant was valid");
    }
    
    /**
     * Test invalid when issued in future, beyond allowed clock skew.
     */
    public void testInvalidIssuedInFuture() {
        messageContext.setInboundSAMLMessageIssueInstant(now.plusSeconds(clockSkew + 5));
        assertRuleFailure("Message issue instant was in the future");
    }
    
    /**
     *  Test valid when issued in future, but within allowed clock skew.
     */
    public void testValidIssuedInFutureWithinClockSkew() {
        messageContext.setInboundSAMLMessageIssueInstant(now.plusSeconds(clockSkew - 5));
        assertRuleSuccess("Message issue instant was in the future but within clock skew");
    }
    
    /**
     * Test invalid when expired, beyond allowed clock skew.
     */
    public void testInvalidExpired() {
        messageContext.setInboundSAMLMessageIssueInstant(now.minusSeconds(expires + (clockSkew + 5)));
        assertRuleFailure("Message issue instant was expired");
    }
    
    /**
     *  Test valid when expired, but within allowed clock skew.
     */
    public void testValidExpiredWithinClockSkew() {
        messageContext.setInboundSAMLMessageIssueInstant(now.minusSeconds(expires + (clockSkew - 5)));
        assertRuleSuccess("Message issue instant was expired but within clock skew");
    }
 
}