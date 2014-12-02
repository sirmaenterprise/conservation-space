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

package org.opensaml.saml1.core.impl;

import javax.xml.namespace.QName;

import org.opensaml.common.BaseSAMLObjectProviderTestCase;
import org.opensaml.common.xml.SAMLConstants;
import org.opensaml.saml1.core.StatusCode;

/**
 * Test class for org.opensaml.saml1.core.StatusCode.
 */
public class StatusCodeTest extends BaseSAMLObjectProviderTestCase {

    /** name used to generate objects. */
    private final QName qname;

    /**Constructor. */
    public StatusCodeTest() {
        childElementsFile = "/data/org/opensaml/saml1/impl/FullStatusCode.xml";
        singleElementFile = "/data/org/opensaml/saml1/impl/singleStatusCode.xml";
        
        qname = new QName(SAMLConstants.SAML10P_NS, StatusCode.DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML1P_PREFIX);
    }

    /** {@inheritDoc} */

    public void testSingleElementUnmarshall() {

        StatusCode code = (StatusCode) unmarshallElement(singleElementFile);

        assertEquals("Single Element Value wrong", StatusCode.SUCCESS, code.getValue());
    }

    /** {@inheritDoc} */

    public void testChildElementsUnmarshall() {

        StatusCode code = (StatusCode) unmarshallElement(childElementsFile);

        assertNotNull("Child StatusCode", code.getStatusCode());
    }

    /** {@inheritDoc} */

    public void testSingleElementMarshall() {
        StatusCode code = (StatusCode) buildXMLObject(qname);

        code.setValue(StatusCode.SUCCESS);

        assertEquals(expectedDOM, code);
    }

    /** {@inheritDoc} */

    public void testChildElementsMarshall() {

        StatusCode code = (StatusCode) buildXMLObject(qname);

        code.setValue(StatusCode.REQUESTER);

        code.setStatusCode((StatusCode) buildXMLObject(qname));

        code.getStatusCode().setValue(StatusCode.VERSION_MISMATCH);

        assertEquals(expectedChildElementsDOM, code);
    }
}
