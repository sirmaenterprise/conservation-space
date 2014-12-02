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

import org.opensaml.common.BaseTestCase;
import org.opensaml.saml2.metadata.EntitiesDescriptor;

/**
 * Unit tests for {@link SchemaValidationFilter}.
 */
public class SchemaValidationFilterTest extends BaseTestCase {

    /** URL to InCommon metadata. */
    private String inCommonMDURL;

    /** {@inheritDoc} */
    protected void setUp() throws Exception {
        super.setUp();

        inCommonMDURL = "http://wayf.incommonfederation.org/InCommon/InCommon-metadata.xml";
    }

    public void test() throws Exception {
        HTTPMetadataProvider metadataProvider = new HTTPMetadataProvider(inCommonMDURL, 1000 * 5);
        metadataProvider.setParserPool(parser);
        metadataProvider.setMetadataFilter(new SchemaValidationFilter(null));
        metadataProvider.initialize();

        EntitiesDescriptor descriptor = (EntitiesDescriptor) metadataProvider.getMetadata();
    }
}