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

package org.opensaml.saml2.metadata;

import java.io.InputStream;

import org.opensaml.common.BaseTestCase;
import org.opensaml.xml.Configuration;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.io.Unmarshaller;
import org.opensaml.xml.io.UnmarshallingException;
import org.opensaml.xml.parse.XMLParserException;
import org.w3c.dom.Document;

/**
 * Test cases that parses real, "in-the-wild", metadata files. Currently uses the InCommon and SWITCH federation
 * metadata files (current as of the time this test was written).
 */
public class MetadataTest extends BaseTestCase {

    /**
     * Constructor
     */
    public MetadataTest() {

    }

    /**
     * {@inheritDoc}
     */
    protected void setUp() throws Exception {
        super.setUp();
    }

    /**
     * Tests unmarshalling an InCommon metadata document.
     * 
     * @throws XMLParserException
     * @throws UnmarshallingException
     */
    public void testInCommonUnmarshall() throws XMLParserException, UnmarshallingException {
        String inCommonMDFile = "/data/org/opensaml/saml2/metadata/InCommon-metadata.xml";

        try {
            InputStream in = MetadataTest.class.getResourceAsStream(inCommonMDFile);
            Document inCommonMDDoc = parser.parse(in);
            Unmarshaller unmarshaller = Configuration.getUnmarshallerFactory().getUnmarshaller(
                    inCommonMDDoc.getDocumentElement());

            XMLObject inCommonMD = unmarshaller.unmarshall(inCommonMDDoc.getDocumentElement());

            assertEquals("First element of InCommon data was not expected EntitiesDescriptor", "EntitiesDescriptor",
                    inCommonMD.getElementQName().getLocalPart());
        } catch (XMLParserException xe) {
            fail("Unable to parse XML file: " + xe);
        } catch (UnmarshallingException ue) {
            fail("Unable to unmarshall XML: " + ue);
        }
    }

    /**
     * Tests unmarshalling an SWITCH metadata document.
     * 
     * @throws XMLParserException
     * @throws UnmarshallingException
     */
    public void testSWITCHUnmarshall() {
        String switchMDFile = "/data/org/opensaml/saml2/metadata/metadata.switchaai_signed.xml";

        try {
            InputStream in = MetadataTest.class.getResourceAsStream(switchMDFile);
            Document switchMDDoc = parser.parse(in);
            Unmarshaller unmarshaller = Configuration.getUnmarshallerFactory().getUnmarshaller(
                    switchMDDoc.getDocumentElement());

            XMLObject switchMD = unmarshaller.unmarshall(switchMDDoc.getDocumentElement());

            assertEquals("First element of SWITCH data was not expected EntitiesDescriptor", "EntitiesDescriptor",
                    switchMD.getElementQName().getLocalPart());
        } catch (XMLParserException xe) {
            fail("Unable to parse XML file: " + xe);
        } catch (UnmarshallingException ue) {
            fail("Unable to unmarshall XML: " + ue);
        }
    }
    
    /**
     * Tests unmarshalling an SWITCH metadata document.
     * 
     * @throws XMLParserException
     * @throws UnmarshallingException
     */
    public void testUKFedUnmarshall() {
        String switchMDFile = "/data/org/opensaml/saml2/metadata/ukfederation-metadata.xml";

        try {
            long parseStart = System.currentTimeMillis();
            InputStream in = MetadataTest.class.getResourceAsStream(switchMDFile);
            Document ukFedDoc = parser.parse(in);
            long parseEnd = System.currentTimeMillis();
            
            long unmarshallStart = System.currentTimeMillis();
            Unmarshaller unmarshaller = Configuration.getUnmarshallerFactory().getUnmarshaller(
                    ukFedDoc.getDocumentElement());
            XMLObject ukFedMD = unmarshaller.unmarshall(ukFedDoc.getDocumentElement());
            long unmarshallEnd = System.currentTimeMillis();
            
            System.out.println("Parse time: " + (parseEnd - parseStart));
            System.out.println("Unmarshall time: " + ( unmarshallEnd - unmarshallStart));

            assertEquals("First element of UK Federation data was not expected EntitiesDescriptor", "EntitiesDescriptor",
                    ukFedMD.getElementQName().getLocalPart());
        } catch (XMLParserException xe) {
            fail("Unable to parse XML file: " + xe);
        } catch (UnmarshallingException ue) {
            fail("Unable to unmarshall XML: " + ue);
        }
    }
}