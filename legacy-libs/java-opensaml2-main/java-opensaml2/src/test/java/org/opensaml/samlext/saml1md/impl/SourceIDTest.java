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

package org.opensaml.samlext.saml1md.impl;

import org.opensaml.common.BaseSAMLObjectProviderTestCase;
import org.opensaml.samlext.saml1md.SourceID;

/**
 * Tests {@link SourceIDImpl}
 */
public class SourceIDTest extends BaseSAMLObjectProviderTestCase {

    /** Expected source ID value */
    private String expectedValue;

    /** Constructor */
    public SourceIDTest() {
        super();
        singleElementFile = "/data/org/opensaml/samlext/saml1md/impl/SourceID.xml";
    }

    /** {@inheritDoc} */
    protected void setUp() throws Exception {
        super.setUp();

        expectedValue = "9392kjc98";
    }

    /** {@inheritDoc} */
    public void testSingleElementMarshall() {
        SourceIDBuilder builder = (SourceIDBuilder) builderFactory.getBuilder(SourceID.DEFAULT_ELEMENT_NAME);

        SourceID sourceID = builder.buildObject();
        sourceID.setValue(expectedValue);

        assertEquals(expectedDOM, sourceID);
    }

    /** {@inheritDoc} */
    public void testSingleElementUnmarshall() {
        SourceID sourceID = (SourceID) unmarshallElement(singleElementFile);

        assertNotNull(sourceID);
        assertEquals(expectedValue, sourceID.getValue());
    }
}