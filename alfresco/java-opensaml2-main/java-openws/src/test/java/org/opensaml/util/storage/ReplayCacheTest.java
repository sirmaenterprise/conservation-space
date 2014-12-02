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

package org.opensaml.util.storage;

import junit.framework.TestCase;

import org.opensaml.util.storage.ReplayCache.ReplayCacheEntry;

/**
 * Testing SAML message replay security policy rule.
 */
public class ReplayCacheTest extends TestCase {

    private String messageID;

    private MapBasedStorageService<String, ReplayCacheEntry> storageEngine;

    /** {@inheritDoc} */
    protected void setUp() throws Exception {
        super.setUp();

        messageID = "abc123";

        storageEngine = new MapBasedStorageService<String, ReplayCacheEntry>();
    }

    /**
     * Test valid non-replayed message ID.
     */
    public void testNonReplayEmptyCache() {
        ReplayCache replayCache = new ReplayCache(storageEngine, 10000);
        assertFalse("Message was not replay, insert into empty cache", replayCache.isReplay("test", messageID));
    }

    /**
     * Test valid non-replayed message ID.
     */
    public void testNonReplayDistinctIDs() {
        ReplayCache replayCache = new ReplayCache(storageEngine, 10000);
        assertFalse("Message was not replay, insert into empty cache", replayCache.isReplay("test", messageID));
        assertFalse("Message was not replay, insert into empty cache", replayCache.isReplay("test", "IDWhichIsNot"
                + messageID));
    }

    /**
     * Test invalid replayed message ID, using replay cache default expiration duration.
     */
    public void testReplay() {
        ReplayCache replayCache = new ReplayCache(storageEngine, 10000);
        assertFalse("Message was not replay, insert into empty cache", replayCache.isReplay("test", messageID));
        assertTrue("Message was replay", replayCache.isReplay("test", messageID));
    }

    /**
     * Test valid replayed message ID, setting expriation by millisecond duration.
     * 
     * @throws InterruptedException
     */
    public void testNonReplayValidByMillisecondExpiriation() throws InterruptedException {
        ReplayCache replayCache = new ReplayCache(storageEngine, 5);

        // Expiration set to 5 milliseconds in the future
        assertFalse("Message was not replay, insert into empty cache", replayCache.isReplay("test", messageID));
        // Sleep for 500 milliseconds to make sure replay cache entry has expired
        Thread.sleep(500);
        assertFalse("Message was not replay, previous cache entry should have expired", replayCache.isReplay("test",
                messageID));
    }
}