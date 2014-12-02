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

import org.opensaml.common.BaseSAMLObjectProviderTestCase;
import org.opensaml.common.xml.SAMLConstants;
import org.opensaml.saml1.core.Audience;
import org.opensaml.saml1.core.AudienceRestrictionCondition;

/**
 * Test class for data.org.opensaml.saml1.AudienceRestrictionCondition
 */
public class AudienceRestrictionConditionTest extends BaseSAMLObjectProviderTestCase {

    /** name used to generate objects */
    private final QName qname;

    /**
     * Constructor
     */
    public AudienceRestrictionConditionTest() {
        singleElementFile = "/data/org/opensaml/saml1/impl/singleAudienceRestrictionCondition.xml";
        childElementsFile = "/data/org/opensaml/saml1/impl/AudienceRestrictionConditionWithChildren.xml";
        qname = new QName(SAMLConstants.SAML1_NS, AudienceRestrictionCondition.DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML1_PREFIX);
    }

    /** {@inheritDoc} */

    public void testSingleElementUnmarshall() {
        AudienceRestrictionCondition audienceRestrictionCondition;

        audienceRestrictionCondition = (AudienceRestrictionCondition) unmarshallElement(singleElementFile);

        assertEquals("Count of child Audience elements !=0", 0, audienceRestrictionCondition.getAudiences().size());
    }

    /** {@inheritDoc} */

    public void testChildElementsUnmarshall() {

        AudienceRestrictionCondition audienceRestrictionCondition;

        audienceRestrictionCondition = (AudienceRestrictionCondition) unmarshallElement(childElementsFile);

        assertEquals("Count of child Audience elements", 2, audienceRestrictionCondition.getAudiences().size());

    }

    /** {@inheritDoc} */

    public void testSingleElementMarshall() {
        assertEquals(expectedDOM, buildXMLObject(qname));
    }

    /** {@inheritDoc} */

    public void testChildElementsMarshall() {
        AudienceRestrictionCondition audienceRestrictionCondition;

        audienceRestrictionCondition = (AudienceRestrictionCondition) buildXMLObject(qname);

        QName audienceName = new QName(SAMLConstants.SAML1_NS, Audience.DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML1_PREFIX);
        audienceRestrictionCondition.getAudiences().add((Audience) buildXMLObject(audienceName));
        audienceRestrictionCondition.getAudiences().add((Audience) buildXMLObject(audienceName));

        assertEquals(expectedChildElementsDOM, audienceRestrictionCondition);
    }

}
