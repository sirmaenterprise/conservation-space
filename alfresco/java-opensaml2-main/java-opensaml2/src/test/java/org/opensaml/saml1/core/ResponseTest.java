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

package org.opensaml.saml1.core;

import java.io.InputStream;

import org.opensaml.common.BaseTestCase;
import org.opensaml.xml.Configuration;
import org.opensaml.xml.io.Unmarshaller;
import org.opensaml.xml.io.UnmarshallingException;
import org.opensaml.xml.parse.XMLParserException;
import org.w3c.dom.Document;

/**
 * Tests unmarshalling and marshalling for various response messages.
 */
public class ResponseTest extends BaseTestCase {

    /** Path to file with full response message */
    private String fullResponsePath;
    
    /**
     * Constructor
     */
    public ResponseTest(){
        fullResponsePath = "/data/org/opensaml/saml1/core/FullResponse.xml";
    }
    
    /**
     * Tests unmarshalling a full response message.
     */
    public void testResponseUnmarshall(){

        try {
            InputStream in = ResponseTest.class.getResourceAsStream(fullResponsePath);
            Document responseDoc = parser.parse(in);
            Unmarshaller unmarshaller = Configuration.getUnmarshallerFactory().getUnmarshaller(
                    responseDoc.getDocumentElement());

            Response response = (Response) unmarshaller.unmarshall(responseDoc.getDocumentElement());

            assertEquals("First element of response data was not expected Response", "Response",
                    response.getElementQName().getLocalPart());
        } catch (XMLParserException xe) {
            fail("Unable to parse XML file: " + xe);
        } catch (UnmarshallingException ue) {
            fail("Unable to unmarshall XML: " + ue);
        }
    }
    
    /**
     * Tests marshalling a full response message.
     */
    public void testResponseMarshall(){
        //TODO
    }
}