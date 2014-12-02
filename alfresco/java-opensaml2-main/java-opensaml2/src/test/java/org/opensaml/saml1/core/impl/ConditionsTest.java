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

package org.opensaml.saml1.core.impl;

import javax.xml.namespace.QName;

import org.joda.time.DateTime;
import org.joda.time.chrono.ISOChronology;
import org.opensaml.common.BaseSAMLObjectProviderTestCase;
import org.opensaml.common.xml.SAMLConstants;
import org.opensaml.saml1.core.AudienceRestrictionCondition;
import org.opensaml.saml1.core.Conditions;
import org.opensaml.saml1.core.DoNotCacheCondition;

/**
 * Test class for org.opensaml.saml1.core.Conditions
 */
public class ConditionsTest extends BaseSAMLObjectProviderTestCase {

    /** name used to generate objects */
    private final QName qname;

    /**
     * Representation of NotBefore in test file.
     */
    private final DateTime expectedNotBeforeDate;

    /**
     * Representation of NotOnOrAfter in test file.
     */
    private final DateTime expectedNotOnOfAfter;

    /**
     * Constructor
     * 
     */
    public ConditionsTest() {
        singleElementFile = "/data/org/opensaml/saml1/impl/singleConditions.xml";
        singleElementOptionalAttributesFile = "/data/org/opensaml/saml1/impl/singleConditionsAttributes.xml";
        childElementsFile = "/data/org/opensaml/saml1/impl/ConditionsWithChildren.xml";
        //
        // NotBefore="1970-01-01T01:00:00.123Z"
        //
        expectedNotBeforeDate = new DateTime(1970, 1, 01, 01, 00, 00, 123, ISOChronology.getInstanceUTC());
        //
        // NotOnOrAfter="1970-01-01T00:00:01.000Z"
        //
        expectedNotOnOfAfter = new DateTime(1970, 1, 01, 00, 00, 01, 0, ISOChronology.getInstanceUTC());
        
        qname = new QName(SAMLConstants.SAML1_NS, Conditions.DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML1_PREFIX);
    }

    /** {@inheritDoc} */
    public void testSingleElementUnmarshall() {
        Conditions conditions;

        conditions = (Conditions) unmarshallElement(singleElementFile);

        DateTime date = conditions.getNotBefore();
        assertNull("NotBefore attribute has a value of " + date + ", expected no value", date);

        date = conditions.getNotOnOrAfter();
        assertNull("NotOnOrAfter attribute has a value of " + date + ", expected no value", date);

    }

    /** {@inheritDoc} */
    public void testSingleElementOptionalAttributesUnmarshall() {
        Conditions conditions;

        conditions = (Conditions) unmarshallElement(singleElementOptionalAttributesFile);

        assertEquals("NotBefore attribute ", expectedNotBeforeDate, conditions.getNotBefore());
        assertEquals("NotOnOrAfter attribute ", expectedNotOnOfAfter, conditions.getNotOnOrAfter());
    }

    /*
     * Test an XML file with children
     */
    public void testChildElementsUnmarshall() {
        Conditions conditions;

        conditions = (Conditions) unmarshallElement(childElementsFile);

        assertEquals("Number of AudienceRestrictionCondition elements", 3, conditions
                .getAudienceRestrictionConditions().size());
        assertEquals("Number of DoNotCacheCondition children", 1, conditions.getDoNotCacheConditions().size());
        assertEquals("Wrong number of Condition children", 4, conditions.getConditions().size());
    }

    /** {@inheritDoc} */
    public void testSingleElementMarshall() {
        Conditions conditions = (Conditions) buildXMLObject(qname);

        assertEquals(expectedDOM, conditions);

    }

    /** {@inheritDoc} */
    public void testSingleElementOptionalAttributesMarshall() {
        Conditions conditions = (Conditions) buildXMLObject(qname);

        conditions.setNotBefore(expectedNotBeforeDate);
        conditions.setNotOnOrAfter(expectedNotOnOfAfter);

        assertEquals(expectedOptionalAttributesDOM, conditions);
    }

    /** {@inheritDoc} */
    public void testChildElementsMarshall() {

        Conditions conditions = (Conditions) buildXMLObject(qname);

        QName arcQname = new QName(SAMLConstants.SAML1_NS, AudienceRestrictionCondition.DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML1_PREFIX);
        
        conditions.getConditions().add((AudienceRestrictionCondition) buildXMLObject(arcQname));
        conditions.getConditions().add((DoNotCacheCondition) buildXMLObject(new QName(SAMLConstants.SAML1_NS, DoNotCacheCondition.DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML1_PREFIX)));
        // conditions.addCondition(condition);

        conditions.getConditions().add((AudienceRestrictionCondition) buildXMLObject(arcQname));
        // conditions.addCondition(condition);
        //           
        conditions.getConditions().add((AudienceRestrictionCondition) buildXMLObject(arcQname));

        assertEquals(expectedChildElementsDOM, conditions);

    }

}
