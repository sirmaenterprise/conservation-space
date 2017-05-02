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

import org.opensaml.common.binding.SAMLMessageContext;
import org.opensaml.ws.message.MessageContext;
import org.opensaml.ws.security.SecurityPolicyException;
import org.opensaml.ws.security.SecurityPolicyRule;

/**
 * A factory for rules which set the policy context issuer to fixed, specified value.
 */
class StaticIssuerRule implements SecurityPolicyRule {

    /** The issuer to set in the security policy context. */
    private String issuer;

    /** State of issuer authentication. */
    private Boolean issuerAuthenticated;

    /**
     * Constructor.
     * 
     * @param newIssuer the issuer to set in the context
     * @param newIssuerAuthenticated the issuer authenticated state to set
     */
    protected StaticIssuerRule(String newIssuer, Boolean newIssuerAuthenticated) {
        issuer = newIssuer;
        issuerAuthenticated = newIssuerAuthenticated;
    }

    /** {@inheritDoc} */
    public void evaluate(MessageContext messageContext) throws SecurityPolicyException {
        SAMLMessageContext samlMsgCtx = (SAMLMessageContext) messageContext;

        samlMsgCtx.setInboundMessageIssuer(issuer);

        if (issuerAuthenticated == Boolean.TRUE) {
            samlMsgCtx.setInboundSAMLMessageAuthenticated(true);
        } else if (issuerAuthenticated == Boolean.FALSE) {
            samlMsgCtx.setInboundSAMLMessageAuthenticated(false);
        }

    }

}