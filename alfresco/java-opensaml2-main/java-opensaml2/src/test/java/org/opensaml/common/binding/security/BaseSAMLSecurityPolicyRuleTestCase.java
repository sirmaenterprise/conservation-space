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

import org.opensaml.common.BaseTestCase;
import org.opensaml.common.SAMLObject;
import org.opensaml.common.binding.BasicSAMLMessageContext;
import org.opensaml.common.binding.SAMLMessageContext;
import org.opensaml.ws.security.SecurityPolicyException;
import org.opensaml.ws.security.SecurityPolicyRule;
import org.opensaml.ws.transport.InTransport;
import org.opensaml.xml.XMLObject;

/**
 * Base class for security policy rule tests.
 *
 * @param <InboundMessageType> type of inbound SAML message
 * @param <OutboundMessageType> type of outbound SAML message (here unused)
 * @param <NameIDType> type of SAML context name identifier (here unused)
 */
public abstract class BaseSAMLSecurityPolicyRuleTestCase<InboundMessageType extends SAMLObject, 
    OutboundMessageType extends SAMLObject, NameIDType extends SAMLObject>  extends BaseTestCase {
    
    /** The security policy rule to evaluate. */
    protected SecurityPolicyRule rule;
    
    /** The message context to evaluate. */
    protected SAMLMessageContext<InboundMessageType, OutboundMessageType, NameIDType> messageContext;

    /** {@inheritDoc} */
    protected void setUp() throws Exception {
        super.setUp();
        messageContext = buildMessageContext();
        messageContext.setInboundMessageTransport( buildInTransport() );
        messageContext.setInboundMessage( buildInboundMessage() );
        messageContext.setInboundSAMLMessage( buildInboundSAMLMessage() );
    }
    
    /**
     * Build the message context that will be evaluated.
     * 
     * @return a new instance of SAMLMessageContext
     */
    protected SAMLMessageContext<InboundMessageType, OutboundMessageType, NameIDType> buildMessageContext() {
        return new BasicSAMLMessageContext<InboundMessageType, OutboundMessageType, NameIDType>();
    }
    
    /**
     * Build the inbound message InTransport that will be set in the message context.
     * 
     * @return InTransport
     */
    protected InTransport buildInTransport() {
        return null;
    }
    
    /**
     * Build the message to be evaled.  Subclasses should override
     * if they will be testing/manipulating message content.
     * 
     * @return a newly constructed message object
     */
    protected XMLObject buildInboundMessage() {
       return null; 
    }
    
    /**
     * Build the inbound SAML message.
     * 
     * @return the inbound SAML message (instance of SAMLObject)
     */
    protected InboundMessageType buildInboundSAMLMessage() {
       return null; 
    }
    
    /**
     * Evaluate the current policy rule using the current message context.
     * Successful evaluation of the rule is expected.
     * 
     * @param msg message to include in fail() messages
     */
    protected void assertRuleSuccess(String msg) {
        try {
            rule.evaluate(messageContext);
        } catch (SecurityPolicyException e) {
            fail("Security policy rule failed, expected success: " + msg + ": " + e);
        }
    }
    
    /**
     * 
     * Evaluate the current policy rule using the current message context.
     * Failed evaluation of the rule is expected.
     * 
     * @param msg message to include in fail() messages
     */
    protected void assertRuleFailure(String msg) {
        try {
            rule.evaluate(messageContext);
            fail("Security policy rule succeeded, expected failure: " + msg);
        } catch (SecurityPolicyException e) {
            //do nothing, failure expected
            return;
        }
    }

}
