/*
 * Copyright [2006] [University Corporation for Advanced Internet Development, Inc.]
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

package org.opensaml.saml2.metadata.provider;

import java.io.File;

import org.opensaml.common.BaseTestCase;
import org.opensaml.saml2.metadata.EntitiesDescriptor;

/**
 * Test case for {@link FileBackedHTTPMetadataProvider}.
 */
public class FileBackedURLMetadataProviderTest extends BaseTestCase {

    private String inCommonMDURL;

    private String badMDURL;

    private String backupFilePath;

    private FileBackedHTTPMetadataProvider metadataProvider;

    /** {@inheritDoc} */
    protected void setUp() throws Exception {
        super.setUp();

        inCommonMDURL = "http://wayf.incommonfederation.org/InCommon/InCommon-metadata.xml";
        badMDURL = "http://www.google.com/";
        backupFilePath = "metadata.xml";
        metadataProvider = new FileBackedHTTPMetadataProvider(inCommonMDURL, 1000 * 5, backupFilePath);
        metadataProvider.setParserPool(parser);
        metadataProvider.initialize();
    }

    /** {@inheritDoc} */
    protected void tearDown() {
        File backupFile = new File(backupFilePath);
        backupFile.delete();
    }

    /**
     * Tests the {@link HTTPMetadataProvider#getMetadata()} method.
     */
    public void testGetMetadata() throws MetadataProviderException {
        EntitiesDescriptor descriptor = (EntitiesDescriptor) metadataProvider.getMetadata();
        assertNotNull("Retrieved metadata was null", descriptor);

        File backupFile = new File(backupFilePath);
        assertTrue("Backup file was not created", backupFile.exists());
        assertTrue("Backup file contains no data", backupFile.length() > 0);

        // Test pulling it from the backup file
        FileBackedHTTPMetadataProvider badProvider = new FileBackedHTTPMetadataProvider(badMDURL, 1000 * 5,
                backupFilePath);
        badProvider.setParserPool(parser);
        badProvider.initialize();
        assertNotNull(badProvider.getMetadata());
    }
}