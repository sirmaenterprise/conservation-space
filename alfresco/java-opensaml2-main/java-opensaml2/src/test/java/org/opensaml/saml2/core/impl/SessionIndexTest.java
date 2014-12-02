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

/**
 * 
 */
package org.opensaml.saml2.core.impl;

import javax.xml.namespace.QName;

import org.opensaml.common.BaseSAMLObjectProviderTestCase;
import org.opensaml.common.xml.SAMLConstants;
import org.opensaml.saml2.core.SessionIndex;

/**
 *
 */
public class SessionIndexTest extends BaseSAMLObjectProviderTestCase {
    
    /** Expected element content */
    private String expectedSessionIndex;

    /**
     * Constructor
     *
     */
    public SessionIndexTest() {
        singleElementFile = "/data/org/opensaml/saml2/core/impl/SessionIndex.xml";
    }
    
    

    /** {@inheritDoc} */
    protected void setUp() throws Exception {
        super.setUp();
        expectedSessionIndex = "Session1234";
    }


    /** {@inheritDoc} */
    public void testSingleElementUnmarshall() {
        SessionIndex si = (SessionIndex) unmarshallElement(singleElementFile);
        
        assertEquals("The unmarshalled session index as not the expected value", expectedSessionIndex, si.getSessionIndex());
    }

    /** {@inheritDoc} */
    public void testSingleElementMarshall() {
        QName qname = new QName(SAMLConstants.SAML20P_NS, SessionIndex.DEFAULT_ELEMENT_LOCAL_NAME);
        SessionIndex si = (SessionIndex) buildXMLObject(qname);
        
        si.setSessionIndex(expectedSessionIndex);
        
        assertEquals(expectedDOM, si);
    }
}