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

package org.opensaml.saml2.core.impl;

import javax.xml.namespace.QName;

import org.joda.time.DateTime;
import org.joda.time.chrono.ISOChronology;
import org.opensaml.common.BaseSAMLObjectProviderTestCase;
import org.opensaml.common.xml.SAMLConstants;
import org.opensaml.saml2.core.AudienceRestriction;
import org.opensaml.saml2.core.Condition;
import org.opensaml.saml2.core.Conditions;
import org.opensaml.saml2.core.OneTimeUse;
import org.opensaml.saml2.core.ProxyRestriction;

/**
 * Test case for creating, marshalling, and unmarshalling {@link org.opensaml.saml2.core.impl.ConditionsImpl}.
 */
public class ConditionsTest extends BaseSAMLObjectProviderTestCase {

    /** Expected NotBefore value */
    private DateTime expectedNotBefore;

    /** Expected NotOnOrAfter value */
    private DateTime expectedNotOnOrAfter;

    /** Count of Condition subelements */
    private int conditionCount = 6;

    /** Count of AudienceRestriction subelements */
    private int audienceRestrictionCount = 3;

    /** Constructor */
    public ConditionsTest() {
        singleElementFile = "/data/org/opensaml/saml2/core/impl/Conditions.xml";
        singleElementOptionalAttributesFile = "/data/org/opensaml/saml2/core/impl/ConditionsOptionalAttributes.xml";
        childElementsFile = "/data/org/opensaml/saml2/core/impl/ConditionsChildElements.xml";
    }

    /** {@inheritDoc} */
    protected void setUp() throws Exception {
        super.setUp();
        expectedNotBefore = new DateTime(1984, 8, 26, 10, 01, 30, 43, ISOChronology.getInstanceUTC());
        expectedNotOnOrAfter = new DateTime(1984, 8, 26, 10, 11, 30, 43, ISOChronology.getInstanceUTC());
    }

    /** {@inheritDoc} */
    public void testSingleElementUnmarshall() {
        Conditions conditions = (Conditions) unmarshallElement(singleElementFile);

        DateTime notBefore = conditions.getNotBefore();
        assertEquals("NotBefore was " + notBefore + ", expected " + expectedNotBefore, expectedNotBefore, notBefore);
    }

    /** {@inheritDoc} */
    public void testSingleElementOptionalAttributesUnmarshall() {
        Conditions conditions = (Conditions) unmarshallElement(singleElementOptionalAttributesFile);

        DateTime notBefore = conditions.getNotBefore();
        assertEquals("NotBefore was " + notBefore + ", expected " + expectedNotBefore, expectedNotBefore, notBefore);

        DateTime notOnOrAfter = conditions.getNotOnOrAfter();
        assertEquals("NotOnOrAfter was " + notOnOrAfter + ", expected " + expectedNotOnOrAfter, expectedNotOnOrAfter,
                notOnOrAfter);
    }

    /** {@inheritDoc} */
    public void testSingleElementMarshall() {
        QName qname = new QName(SAMLConstants.SAML20_NS, Conditions.DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML20_PREFIX);
        Conditions conditions = (Conditions) buildXMLObject(qname);

        conditions.setNotBefore(expectedNotBefore);
        assertEquals(expectedDOM, conditions);
    }

    /** {@inheritDoc} */
    public void testSingleElementOptionalAttributesMarshall() {
        QName qname = new QName(SAMLConstants.SAML20_NS, Conditions.DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML20_PREFIX);
        Conditions conditions = (Conditions) buildXMLObject(qname);

        conditions.setNotBefore(expectedNotBefore);
        conditions.setNotOnOrAfter(expectedNotOnOrAfter);

        assertEquals(expectedOptionalAttributesDOM, conditions);
    }

    /** {@inheritDoc} */
    public void testChildElementsUnmarshall() {
        Conditions conditions = (Conditions) unmarshallElement(childElementsFile);
        assertEquals("Condition count not as expected", conditionCount, conditions.getConditions().size());
        assertNotNull("OneTimeUse absent", conditions.getOneTimeUse());
        assertNotNull("ProxyRestriction absent", conditions.getProxyRestriction());
    }

    /** {@inheritDoc} */
    public void testChildElementsMarshall() {
        QName qname = new QName(SAMLConstants.SAML20_NS, Conditions.DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML20_PREFIX);
        Conditions conditions = (Conditions) buildXMLObject(qname);

        QName oneTimeUserQName = new QName(SAMLConstants.SAML20_NS, OneTimeUse.DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML20_PREFIX);
        conditions.getConditions().add((Condition) buildXMLObject(oneTimeUserQName));
        
        QName audienceRestrictionQName = new QName(SAMLConstants.SAML20_NS, AudienceRestriction.DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML20_PREFIX);
        for (int i = 0; i < audienceRestrictionCount; i++) {
            conditions.getAudienceRestrictions().add((AudienceRestriction) buildXMLObject(audienceRestrictionQName));
        }
        
        conditions.getConditions().add((Condition) buildXMLObject(oneTimeUserQName));
        
        QName proxyRestrictionQName = new QName(SAMLConstants.SAML20_NS, ProxyRestriction.DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML20_PREFIX);
        conditions.getConditions().add((Condition) buildXMLObject(proxyRestrictionQName));
        
        assertEquals(expectedChildElementsDOM, conditions);
    }
}