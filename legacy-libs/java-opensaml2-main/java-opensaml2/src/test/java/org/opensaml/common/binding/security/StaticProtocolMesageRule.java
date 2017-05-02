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
import org.opensaml.common.binding.SAMLMessageContext;
import org.opensaml.ws.message.MessageContext;
import org.opensaml.ws.security.SecurityPolicyException;

/**
 * Static rule for testing, to set issuer, message ID and issue instant in SAML security policy context.
 */
public class StaticProtocolMesageRule extends StaticIssuerRule {

    /** Static issue instant to set. */
    private DateTime issueInstant;

    /** Static message ID to set. */
    private String messageID;

    /**
     * Constructor.
     * 
     * @param newIssuer the new issuer
     * @param newIssuerAuthenticated the new issuer authenticated state to set
     * @param newMessageID the new message ID
     * @param newIssueInstant the new issue instant
     */
    protected StaticProtocolMesageRule(String newIssuer, Boolean newIssuerAuthenticated, String newMessageID,
            DateTime newIssueInstant) {
        super(newIssuer, newIssuerAuthenticated);
        messageID = newMessageID;
        issueInstant = newIssueInstant;
    }

    /** {@inheritDoc} */
    public void evaluate(MessageContext messageContext) throws SecurityPolicyException {
        SAMLMessageContext samlMsgCtx = (SAMLMessageContext) messageContext;

        super.evaluate(messageContext);
        samlMsgCtx.setInboundSAMLMessageId(messageID);
        samlMsgCtx.setInboundSAMLMessageIssueInstant(issueInstant);

    }

}