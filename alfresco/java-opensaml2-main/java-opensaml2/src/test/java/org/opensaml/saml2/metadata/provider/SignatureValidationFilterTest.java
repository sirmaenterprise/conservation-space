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

package org.opensaml.saml2.metadata.provider;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import org.opensaml.Configuration;
import org.opensaml.common.BaseTestCase;
import org.opensaml.saml2.metadata.EntityDescriptor;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.io.UnmarshallingException;
import org.opensaml.xml.parse.XMLParserException;
import org.opensaml.xml.security.SecurityHelper;
import org.opensaml.xml.security.SecurityTestHelper;
import org.opensaml.xml.security.credential.StaticCredentialResolver;
import org.opensaml.xml.security.x509.X509Credential;
import org.opensaml.xml.signature.SignatureTrustEngine;
import org.opensaml.xml.signature.impl.ExplicitKeySignatureTrustEngine;
import org.w3c.dom.Document;

/**
 * Unit tests for {@link SignatureValidationFilter}.
 */
public class SignatureValidationFilterTest extends BaseTestCase {
    
    private final String switchMDFileValid = "/data/org/opensaml/saml2/metadata/provider/metadata.aaitest_signed.xml";
    private final String switchMDFileInvalid = "/data/org/opensaml/saml2/metadata/provider/metadata.aaitest_signed.invalid.xml";
    
    private Document switchMDDocumentValid;
    private Document switchMDDocumentInvalid;
    
    private SignatureTrustEngine switchSigTrustEngine;
    
    private String switchMDCertBase64 = 
        "MIICrzCCAhgCAQAwDQYJKoZIhvcNAQEEBQAwgZ8xCzAJBgNVBAYTAkNIMUAwPgYDVQQKEzdTV0lU" +
        "Q0ggLSBUZWxlaW5mb3JtYXRpa2RpZW5zdGUgZnVlciBMZWhyZSB1bmQgRm9yc2NodW5nMQwwCgYD" +
        "VQQLEwNBQUkxIjAgBgNVBAMTGVNXSVRDSGFhaSBNZXRhZGF0YSBTaWduZXIxHDAaBgkqhkiG9w0B" +
        "CQEWDWFhaUBzd2l0Y2guY2gwHhcNMDUwODAzMTEyMjUxWhcNMTUwODAxMTEyMjUxWjCBnzELMAkG" +
        "A1UEBhMCQ0gxQDA+BgNVBAoTN1NXSVRDSCAtIFRlbGVpbmZvcm1hdGlrZGllbnN0ZSBmdWVyIExl" +
        "aHJlIHVuZCBGb3JzY2h1bmcxDDAKBgNVBAsTA0FBSTEiMCAGA1UEAxMZU1dJVENIYWFpIE1ldGFk" +
        "YXRhIFNpZ25lcjEcMBoGCSqGSIb3DQEJARYNYWFpQHN3aXRjaC5jaDCBnzANBgkqhkiG9w0BAQEF" +
        "AAOBjQAwgYkCgYEAsmyBYNZ8mKYutdyQShzuOgnVxDP1UBZE+57S2ORZg1qi4JExOJEPnviHuh6H" +
        "EajljhAMGHxr656paDpfXkmGq/Ybk3xmXy2FTnFGpjFpZUV6dY/oJ82rve27C/NVcwZw2nYRl5C5" +
        "aCCgx/QlWsBTw+9972141+wBDH7dXlJ+UGkCAwEAATANBgkqhkiG9w0BAQQFAAOBgQCcLuNwTINk" +
        "fhBlVCIuTixR1R6mYu/+4KUJWtHlRCOUZhSLFept8HxEvfwnuX9xm+Q6Ju/sOgmI1INuSstUGWwV" +
        "y0AbpCphUDDmIh9A85ye8DrVaBHQrj5b/JEjCvkY0zhLJzgDzZ6btT40TuCnk2GpdAClu5SyCTiy" +
        "56+zDYqPqg==";
    
    private final String openIDFileValid = "/data/org/opensaml/saml2/metadata/provider/openid-metadata.xml";
    private final String openIDFileInvalid = "/data/org/opensaml/saml2/metadata/provider/openid-metadata-invalid.xml";
    
    private String openIDCertBase64 = 
        "MIICfTCCAeagAwIBAgIGAReueFpXMA0GCSqGSIb3DQEBBQUAMIGBMQswCQYDVQQGEwJVUzELMAkG" +
        "A1UECBMCQ0ExFDASBgNVBAcTC1NpbWkgVmFsbGV5MR4wHAYDVQQKExVSYXBhdHRvbmkgQ29ycG9y" +
        "YXRpb24xFDASBgNVBAsTC1NTTyBTdXBwb3J0MRkwFwYDVQQDExBtbHNzdGdzd21pY2hpZ2FuMB4X" +
        "DTA4MDEyNTAxMDMxOFoXDTA5MDEyNDAxMDMxOFowgYExCzAJBgNVBAYTAlVTMQswCQYDVQQIEwJD" +
        "QTEUMBIGA1UEBxMLU2ltaSBWYWxsZXkxHjAcBgNVBAoTFVJhcGF0dG9uaSBDb3Jwb3JhdGlvbjEU" +
        "MBIGA1UECxMLU1NPIFN1cHBvcnQxGTAXBgNVBAMTEG1sc3N0Z3N3bWljaGlnYW4wgZ8wDQYJKoZI" +
        "hvcNAQEBBQADgY0AMIGJAoGBAIOnt2MOfIYvvyhiKBS2yb5IXFx+SFEa/TLSUPkE9gZJCIe22GGf" +
        "iwzsC8ubpifebZUru1fespnaCE8rc7MtWXERW7x6Dp8wg/91NOgUB00eEUlA72DhDjelsYTJa+Az" +
        "ztBsWh6J3HFKNdNaSVTS+CqbmgdTlDW+BExbtHUfSP0RAgMBAAEwDQYJKoZIhvcNAQEFBQADgYEA" +
        "YT8js8O7gbLq4X/yuGCiuKHofQHFAE6pAWaxdTD+Bd2pu48GKICYAhFwHTqrG3bOqObfsILz4Pca" +
        "vCfzIS7/dk9oPnjeH7GqbxUZMsms4qDZzdNkNDUDWj82lJzIMfZyUKbn2waTsgg3mKja0dGw2UBy" +
        "urPV4NvVcNaIQZJunHI=";

    /** {@inheritDoc} */
    protected void setUp() throws Exception {
        super.setUp();
        
        switchMDDocumentValid = parser.parse(SignatureValidationFilterTest.class.getResourceAsStream(switchMDFileValid));
        switchMDDocumentInvalid = parser.parse(SignatureValidationFilterTest.class.getResourceAsStream(switchMDFileInvalid));
        
        X509Certificate switchCert = SecurityTestHelper.buildJavaX509Cert(switchMDCertBase64);
        X509Credential switchCred = SecurityHelper.getSimpleCredential(switchCert, null);
        StaticCredentialResolver switchCredResolver = new StaticCredentialResolver(switchCred);
        switchSigTrustEngine = new ExplicitKeySignatureTrustEngine(switchCredResolver, 
                Configuration.getGlobalSecurityConfiguration().getDefaultKeyInfoCredentialResolver());
    }

    public void testValidSWITCHStandalone() throws UnmarshallingException {
        XMLObject xmlObject = unmarshallerFactory.getUnmarshaller(switchMDDocumentValid
                .getDocumentElement()).unmarshall(switchMDDocumentValid.getDocumentElement());
        
        SignatureValidationFilter filter = new SignatureValidationFilter(switchSigTrustEngine);
        try {
            filter.doFilter(xmlObject);
        } catch (FilterException e) {
            fail("Filter failed validation, should have succeeded: " + e.getMessage());
        }
    }
    
    public void testInvalidSWITCHStandalone() throws UnmarshallingException {
        XMLObject xmlObject = unmarshallerFactory.getUnmarshaller(switchMDDocumentInvalid
                .getDocumentElement()).unmarshall(switchMDDocumentInvalid.getDocumentElement());
        
        SignatureValidationFilter filter = new SignatureValidationFilter(switchSigTrustEngine);
        try {
            filter.doFilter(xmlObject);
            fail("Filter passed validation, should have failed");
        } catch (FilterException e) {
            // do nothing, should fail
        }
    }
    
    public void testEntityDescriptor() throws UnmarshallingException, CertificateException, XMLParserException {
        X509Certificate cert = SecurityTestHelper.buildJavaX509Cert(openIDCertBase64);
        X509Credential cred = SecurityHelper.getSimpleCredential(cert, null);
        StaticCredentialResolver credResolver = new StaticCredentialResolver(cred);
        SignatureTrustEngine trustEngine = new ExplicitKeySignatureTrustEngine(credResolver, 
                Configuration.getGlobalSecurityConfiguration().getDefaultKeyInfoCredentialResolver());
        
        Document mdDoc = parser.parse(SignatureValidationFilterTest.class.getResourceAsStream(openIDFileValid));
        XMLObject xmlObject = 
            unmarshallerFactory.getUnmarshaller(mdDoc.getDocumentElement()).unmarshall(mdDoc.getDocumentElement());
        assertTrue(xmlObject instanceof EntityDescriptor);
        EntityDescriptor ed = (EntityDescriptor) xmlObject;
        assertTrue(ed.isSigned());
        assertNotNull("Signature was null", ed.getSignature());
        
        SignatureValidationFilter filter = new SignatureValidationFilter(trustEngine);
        try {
            filter.doFilter(ed);
        } catch (FilterException e) {
            fail("Filter failed validation, should have succeeded: " + e.getMessage());
        }
    }
    
    public void testEntityDescriptorInvalid() throws UnmarshallingException, CertificateException, XMLParserException {
        X509Certificate cert = SecurityTestHelper.buildJavaX509Cert(openIDCertBase64);
        X509Credential cred = SecurityHelper.getSimpleCredential(cert, null);
        StaticCredentialResolver credResolver = new StaticCredentialResolver(cred);
        SignatureTrustEngine trustEngine = new ExplicitKeySignatureTrustEngine(credResolver, 
                Configuration.getGlobalSecurityConfiguration().getDefaultKeyInfoCredentialResolver());
        
        Document mdDoc = parser.parse(SignatureValidationFilterTest.class.getResourceAsStream(openIDFileInvalid));
        XMLObject xmlObject = 
            unmarshallerFactory.getUnmarshaller(mdDoc.getDocumentElement()).unmarshall(mdDoc.getDocumentElement());
        assertTrue(xmlObject instanceof EntityDescriptor);
        EntityDescriptor ed = (EntityDescriptor) xmlObject;
        assertTrue(ed.isSigned());
        assertNotNull("Signature was null", ed.getSignature());
        
        SignatureValidationFilter filter = new SignatureValidationFilter(trustEngine);
        try {
            filter.doFilter(xmlObject);
            fail("Filter passed validation, should have failed");
        } catch (FilterException e) {
            // do nothing, should fail
        }
    }
    
    public void testEntityDescriptorWithProvider() throws CertificateException, XMLParserException, UnmarshallingException {
        X509Certificate cert = SecurityTestHelper.buildJavaX509Cert(openIDCertBase64);
        X509Credential cred = SecurityHelper.getSimpleCredential(cert, null);
        StaticCredentialResolver credResolver = new StaticCredentialResolver(cred);
        SignatureTrustEngine trustEngine = new ExplicitKeySignatureTrustEngine(credResolver, 
                Configuration.getGlobalSecurityConfiguration().getDefaultKeyInfoCredentialResolver());
        
        Document mdDoc = parser.parse(SignatureValidationFilterTest.class.getResourceAsStream(openIDFileValid));
        
        DOMMetadataProvider mdProvider = new DOMMetadataProvider(mdDoc.getDocumentElement());
        mdProvider.setParserPool(parser);
        mdProvider.setRequireValidMetadata(false);
        
        SignatureValidationFilter filter = new SignatureValidationFilter(trustEngine);
        try {
            mdProvider.setMetadataFilter(filter);
        } catch (MetadataProviderException e) {
            fail("Could not set metadata filter on provider");
        }
        try {
            mdProvider.initialize();
        } catch (MetadataProviderException e) {
            fail("Failed when initializing metadata provider");
        }
    }
    
    public void testInvalidEntityDescriptorWithProvider() throws CertificateException, XMLParserException, UnmarshallingException {
        X509Certificate cert = SecurityTestHelper.buildJavaX509Cert(openIDCertBase64);
        X509Credential cred = SecurityHelper.getSimpleCredential(cert, null);
        StaticCredentialResolver credResolver = new StaticCredentialResolver(cred);
        SignatureTrustEngine trustEngine = new ExplicitKeySignatureTrustEngine(credResolver, 
                Configuration.getGlobalSecurityConfiguration().getDefaultKeyInfoCredentialResolver());
        
        Document mdDoc = parser.parse(SignatureValidationFilterTest.class.getResourceAsStream(openIDFileInvalid));
        
        DOMMetadataProvider mdProvider = new DOMMetadataProvider(mdDoc.getDocumentElement());
        mdProvider.setParserPool(parser);
        mdProvider.setRequireValidMetadata(false);
        
        SignatureValidationFilter filter = new SignatureValidationFilter(trustEngine);
        try {
            mdProvider.setMetadataFilter(filter);
        } catch (MetadataProviderException e) {
            fail("Could not set metadata filter on provider");
        }
        try {
            mdProvider.initialize();
            fail("Metadata signature was invalid, provider initialization should have failed");
        } catch (MetadataProviderException e) {
            // do nothing, failure expected
        }
    }

}