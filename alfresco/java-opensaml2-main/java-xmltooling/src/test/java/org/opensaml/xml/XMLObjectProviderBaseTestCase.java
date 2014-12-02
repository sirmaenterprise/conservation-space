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

package org.opensaml.xml;

import org.w3c.dom.Document;

/**
 * Base test case for {@link org.opensaml.xml.XMLObject}s in XMLTooling for which we need a full set
 * of object provider tests, i.e marshalling and unmarshalling of single elements; with optional
 * attributes; and with child elements.
 */
public abstract class XMLObjectProviderBaseTestCase extends XMLObjectBaseTestCase {

    /** Location of file containing a single element with NO optional attributes */
    protected String singleElementFile;

    /** Location of file containing a single element with all optional attributes */
    protected String singleElementOptionalAttributesFile;

    /** Location of file containing a single element with child elements */
    protected String childElementsFile;

    /** The expected result of a marshalled single element with no optional attributes */
    protected Document expectedDOM;

    /** The expected result of a marshalled single element with all optional attributes */
    protected Document expectedOptionalAttributesDOM;

    /** The expected result of a marshalled single element with child elements */
    protected Document expectedChildElementsDOM;

    /** {@inheritDoc} */
    protected void setUp() throws Exception {
        super.setUp();

        if (singleElementFile != null) {
            expectedDOM = parserPool.parse(XMLObjectProviderBaseTestCase.class
                    .getResourceAsStream(singleElementFile));
        }

        if (singleElementOptionalAttributesFile != null) {
            expectedOptionalAttributesDOM = parserPool.parse(XMLObjectProviderBaseTestCase.class
                    .getResourceAsStream(singleElementOptionalAttributesFile));
        }

        if (childElementsFile != null) {
            expectedChildElementsDOM = parserPool.parse(XMLObjectProviderBaseTestCase.class
                    .getResourceAsStream(childElementsFile));
        }
    }

    /** {@inheritDoc} */
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Tests unmarshalling a document that contains a single element (no children) with no optional attributes.
     */
    public abstract void testSingleElementUnmarshall();

    /**
     * Tests unmarshalling a document that contains a single element (no children) with all that element's optional
     * attributes.
     */
    public void testSingleElementOptionalAttributesUnmarshall() {
        assertNull("No testSingleElementOptionalAttributesUnmarshall present", singleElementOptionalAttributesFile);
    }

    /**
     * Tests unmarshalling a document that contains a single element with children.
     */
    public void testChildElementsUnmarshall() {
        assertNull("No testSingleElementChildElementsUnmarshall present", childElementsFile);
    }

    /**
     * Tests marshalling the contents of a single element, with no optional attributes, to a DOM document.
     */
    public abstract void testSingleElementMarshall();

    /**
     * Tests marshalling the contents of a single element, with all optional attributes, to a DOM document.
     */
    public void testSingleElementOptionalAttributesMarshall() {
        assertNull("No testSingleElementOptionalAttributesMarshall", expectedOptionalAttributesDOM);
    }

    /**
     * Tests marshalling the contents of a single element with child elements to a DOM document.
     */
    public void testChildElementsMarshall() {
        assertNull("No testSingleElementChildElementsMarshall", expectedChildElementsDOM);
    }

}