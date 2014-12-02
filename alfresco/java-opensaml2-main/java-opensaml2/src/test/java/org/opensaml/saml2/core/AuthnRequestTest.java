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

package org.opensaml.saml2.core;

import org.joda.time.DateTime;
import org.joda.time.chrono.ISOChronology;
import org.opensaml.common.BaseComplexSAMLObjectTestCase;
import org.opensaml.common.SAMLVersion;
import org.opensaml.xml.schema.XSBooleanValue;

/**
 * Tests unmarshalling and marshalling for various request messages.
 */
public class AuthnRequestTest extends BaseComplexSAMLObjectTestCase {

    /**
     * Constructor
     */
    public AuthnRequestTest(){
        elementFile = "/data/org/opensaml/saml2/core/AuthnRequest.xml";
    }
    

    /** {@inheritDoc} */
    public void testUnmarshall() {
        AuthnRequest request = (AuthnRequest) unmarshallElement(elementFile);
        
        assertNotNull("AuthnRequest was null", request);
        assertEquals("ForceAuthn", true, request.isForceAuthn().booleanValue());
        assertEquals("AssertionConsumerServiceURL", "http://www.example.com/", request.getAssertionConsumerServiceURL());
        assertEquals("AttributeConsumingServiceIndex", 0, request.getAttributeConsumingServiceIndex().intValue());
        assertEquals("ProviderName", "SomeProvider", request.getProviderName());
        assertEquals("ID", "abe567de6", request.getID());
        assertEquals("Version", SAMLVersion.VERSION_20.toString(), request.getVersion().toString());
        assertEquals("IssueInstant", new DateTime(2005, 1, 31, 12, 0, 0, 0, ISOChronology.getInstanceUTC()), request.getIssueInstant());
        assertEquals("Destination", "http://www.example.com/", request.getDestination());
        assertEquals("Consent", "urn:oasis:names:tc:SAML:2.0:consent:obtained", request.getConsent());
        assertEquals("Subject/NameID/@NameIdFormat", "urn:oasis:names:tc:SAML:1.1:nameid-format:emailAddress", request.getSubject().getNameID().getFormat());
        assertEquals("Subject/NameID contents", "j.doe@company.com", request.getSubject().getNameID().getValue());
        Audience audience = request.getConditions().getAudienceRestrictions().get(0).getAudiences().get(0);
        assertEquals("Conditions/AudienceRestriction[1]/Audience[1] contents", "urn:foo:sp.example.org", audience.getAudienceURI());
        AuthnContextClassRef classRef = (AuthnContextClassRef) request.getRequestedAuthnContext().getAuthnContextClassRefs().get(0);
        assertEquals("RequestedAuthnContext/AuthnContextClassRef[1] contents", "urn:oasis:names:tc:SAML:2.0:ac:classes:PasswordProtectedTransport", classRef.getAuthnContextClassRef());
    }

    /** {@inheritDoc} */
    public void testMarshall() {
        NameID nameid = (NameID) buildXMLObject(NameID.DEFAULT_ELEMENT_NAME);
        nameid.setFormat("urn:oasis:names:tc:SAML:1.1:nameid-format:emailAddress");
        nameid.setValue("j.doe@company.com");
        
        Subject subject = (Subject) buildXMLObject(Subject.DEFAULT_ELEMENT_NAME);
        subject.setNameID(nameid);
        
        Audience audience = (Audience) buildXMLObject(Audience.DEFAULT_ELEMENT_NAME);
        audience.setAudienceURI("urn:foo:sp.example.org");
        
        AudienceRestriction ar = (AudienceRestriction) buildXMLObject(AudienceRestriction.DEFAULT_ELEMENT_NAME);
        ar.getAudiences().add(audience);
        
        Conditions conditions = (Conditions) buildXMLObject(Conditions.DEFAULT_ELEMENT_NAME);
        conditions.getAudienceRestrictions().add(ar);
        
        AuthnContextClassRef classRef = (AuthnContextClassRef) buildXMLObject(AuthnContextClassRef.DEFAULT_ELEMENT_NAME);
        classRef.setAuthnContextClassRef("urn:oasis:names:tc:SAML:2.0:ac:classes:PasswordProtectedTransport");
        
        RequestedAuthnContext rac = (RequestedAuthnContext) buildXMLObject(RequestedAuthnContext.DEFAULT_ELEMENT_NAME);
        rac.getAuthnContextClassRefs().add(classRef);
        
        AuthnRequest request = (AuthnRequest) buildXMLObject(AuthnRequest.DEFAULT_ELEMENT_NAME);
        request.setSubject(subject);
        request.setConditions(conditions);
        request.setRequestedAuthnContext(rac);
        
        request.setForceAuthn(XSBooleanValue.valueOf("true"));
        request.setAssertionConsumerServiceURL("http://www.example.com/");
        request.setAttributeConsumingServiceIndex(0);
        request.setProviderName("SomeProvider");
        request.setID("abe567de6");
        request.setVersion(SAMLVersion.VERSION_20);
        request.setIssueInstant(new DateTime(2005, 1, 31, 12, 0, 0, 0, ISOChronology.getInstanceUTC()));
        request.setDestination("http://www.example.com/");
        request.setConsent("urn:oasis:names:tc:SAML:2.0:consent:obtained");
        
        assertEquals("Marshalled AuthnRequest", expectedDOM, request);
        
        
    }
}