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

import org.opensaml.saml2.core.AttributeQuery;
import org.opensaml.saml2.core.NameID;
import org.opensaml.saml2.core.Response;
import org.opensaml.util.storage.MapBasedStorageService;
import org.opensaml.util.storage.ReplayCache;
import org.opensaml.util.storage.ReplayCache.ReplayCacheEntry;

/**
 * Testing SAML message replay security policy rule.
 */
public class MessageReplayRuleTest extends BaseSAMLSecurityPolicyRuleTestCase<AttributeQuery, Response, NameID> {

    private String messageID;

    private MapBasedStorageService<String, ReplayCacheEntry> storageEngine;

    private ReplayCache replayCache;

    /** {@inheritDoc} */
    protected void setUp() throws Exception {
        super.setUp();

        messageID = "abc123";

        messageContext.setInboundMessageIssuer("issuer");
        messageContext.setInboundSAMLMessageId(messageID);

        storageEngine = new MapBasedStorageService<String, ReplayCacheEntry>();
        replayCache = new ReplayCache(storageEngine, 60 * 10 * 1000);
        rule = new MessageReplayRule(replayCache);
    }

    /**
     * Test valid message ID.
     */
    public void testNoReplay() {
        assertRuleSuccess("Message ID was valid");
    }

    /**
     * Test valid message ID, distinct ID.
     */
    public void testNoReplayDistinctIDs() {
        assertRuleSuccess("Message ID was valid");

        messageContext.setInboundSAMLMessageId("someOther" + messageID);
        assertRuleSuccess("Message ID was valid, distinct message ID");

    }

    /**
     * Test invalid replay of message ID.
     */
    public void testReplay() {
        assertRuleSuccess("Message ID was valid");

        assertRuleFailure("Message ID was a replay");
    }

    /**
     * Test valid replay of message ID due to replay cache expiration.
     * 
     * @throws InterruptedException
     */
    public void testReplayValidWithExpiration() throws InterruptedException {
        // Set rule with 3 second expiration, with no clock skew
        ReplayCache replayCache = new ReplayCache(storageEngine, 1000 * 3);
        rule = new MessageReplayRule(replayCache);
        assertRuleSuccess("Message ID was valid");

        // Now sleep for 5 seconds to be sure has expired, and retry same message id
        Thread.sleep(5 * 1000);
        assertRuleSuccess("Message ID was valid, no replay due to expiration");
    }

}
