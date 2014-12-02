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

package org.opensaml.common;

import org.opensaml.Configuration;
import org.opensaml.saml2.metadata.LocalizedString;
import org.opensaml.saml2.metadata.Organization;
import org.opensaml.saml2.metadata.OrganizationDisplayName;
import org.opensaml.saml2.metadata.OrganizationName;
import org.opensaml.saml2.metadata.OrganizationURL;
import org.opensaml.saml2.metadata.impl.OrganizationBuilder;
import org.opensaml.saml2.metadata.impl.OrganizationDisplayNameBuilder;
import org.opensaml.saml2.metadata.impl.OrganizationNameBuilder;
import org.opensaml.saml2.metadata.impl.OrganizationURLBuilder;
import org.opensaml.xml.io.Marshaller;
import org.opensaml.xml.io.MarshallerFactory;
import org.opensaml.xml.io.MarshallingException;
import org.opensaml.xml.io.Unmarshaller;
import org.opensaml.xml.io.UnmarshallerFactory;
import org.opensaml.xml.io.UnmarshallingException;
import org.w3c.dom.Element;

/**
 * Round trip messaging test case.
 */
public class RoundTripTest extends BaseTestCase {
    
    /** Organization to marshall */
    private Organization organization;
    
    /** Organization Marshaller */
    private Marshaller orgMarshaller;
    
    /** Organization Unmarshaller */
    private Unmarshaller orgUnmarshaller;
    
    /** {@inheritDoc} */
    protected void setUp(){
        OrganizationBuilder orgBuilder = (OrganizationBuilder) Configuration.getBuilderFactory().getBuilder(Organization.TYPE_NAME);
        organization = orgBuilder.buildObject();            

        OrganizationNameBuilder orgNameBuilder = (OrganizationNameBuilder) Configuration.getBuilderFactory().getBuilder(OrganizationName.DEFAULT_ELEMENT_NAME);     
        OrganizationName newOrgName = orgNameBuilder.buildObject();
        newOrgName.setName(new LocalizedString("OrgFullName", "en"));
        organization.getOrganizationNames().add(newOrgName);

        OrganizationDisplayNameBuilder orgDisplayNameBuilder = (OrganizationDisplayNameBuilder) Configuration.getBuilderFactory().getBuilder(OrganizationDisplayName.DEFAULT_ELEMENT_NAME); 
        OrganizationDisplayName newOrgDisplayName = orgDisplayNameBuilder.buildObject();
        newOrgDisplayName.setName(new LocalizedString("OrgDisplayName", "en"));
        organization.getDisplayNames().add(newOrgDisplayName);

        OrganizationURLBuilder orgURLBuilder = (OrganizationURLBuilder) Configuration.getBuilderFactory().getBuilder(OrganizationURL.DEFAULT_ELEMENT_NAME);     
        OrganizationURL newOrgURL = orgURLBuilder.buildObject();    
        newOrgURL.setURL(new LocalizedString("http://org.url.edu", "en"));
        organization.getURLs().add(newOrgURL);
        
        MarshallerFactory marshallerFactory = Configuration.getMarshallerFactory();
        orgMarshaller = marshallerFactory.getMarshaller(organization);
        
        UnmarshallerFactory unmarshallerFactory = Configuration.getUnmarshallerFactory();
        orgUnmarshaller = unmarshallerFactory.getUnmarshaller(organization.getElementQName());
    }

    /**
     * Tests marshalling and unmarshalling the same object a three times.
     * 
     * @throws MarshallingException thrown if the object can't be marshalled
     * @throws UnmarshallingException thrown if hte object can't be unmarshalled
     */
    public void testRoundTrip() throws MarshallingException, UnmarshallingException{

        //Marshall the element
        Element orgElement1 =  orgMarshaller.marshall(organization);
        
        // Unmarshall it
        Organization org2 = (Organization) orgUnmarshaller.unmarshall(orgElement1);
        
        // Drop DOM and remarshall
        org2.releaseDOM();
        org2.releaseChildrenDOM(true);
        Element orgElement2 = orgMarshaller.marshall(org2);
        assertXMLEqual(orgElement1.getOwnerDocument(), orgElement2.getOwnerDocument());
        
        // Unmarshall again
        Organization org3 = (Organization) orgUnmarshaller.unmarshall(orgElement2);
        
        // Drop DOM and remarshall
        org3.releaseDOM();
        org3.releaseChildrenDOM(true);
        Element orgElement3 = orgMarshaller.marshall(org3);
        assertXMLEqual(orgElement1.getOwnerDocument(), orgElement3.getOwnerDocument());
    }
}