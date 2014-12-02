/*
 * Copyright [2007] [University Corporation for Advanced Internet Development, Inc.]
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

package org.opensaml.xml.security.trust;

import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPublicKey;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.opensaml.xml.security.CriteriaSet;
import org.opensaml.xml.security.SecurityException;
import org.opensaml.xml.security.SecurityHelper;
import org.opensaml.xml.security.SecurityTestHelper;
import org.opensaml.xml.security.credential.BasicCredential;
import org.opensaml.xml.security.credential.Credential;
import org.opensaml.xml.security.credential.CredentialResolver;
import org.opensaml.xml.security.credential.StaticCredentialResolver;
import org.opensaml.xml.security.criteria.EntityIDCriteria;
import org.opensaml.xml.security.x509.BasicX509Credential;

/**
 * Test the explicit key trust engine.
 */
public class ExplicitX509CertificateTrustEngineTest extends TestCase {
    
    private RSAPublicKey entityPubKey;
    private final String rsaBase64 = 
        "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAzVp5BZoctb2GuoDf8QUS" +
        "pGcRct7FKtldC7GG+kN6XvUJW+vgc2jOQ6zfLiKqq6ARN1qdC7a4CrkE6Q6TRQXU" +
        "tqeWn4lLTmC1gQ7Ys0zs7N2d+jBjIyD1GEOLNNyD98j4drnehCqQz4mKszW5EWoi" +
        "MJmEorea/kTGL3en7ir0zp+oez2SOQA+0XWu1VoeTlUqGV5Ucd6sRYaPpmYVtKuH" +
        "1H04uZVsH+BIZHwZc4MP5OYH+HDouq6xqUUtc8Zm7V9UQIPiNtM+ndOINDdlrCub" +
        "LbM4GCqCETiQol8I62mvP0qBXCC6JVkKbbVRwSFGJcg5ZvJiBZXmX+EXhaX5vp1G" +
        "MQIDAQAB";
    
    private X509Certificate entityCert;
    private String entityCertBase64 = 
        "MIIDjDCCAnSgAwIBAgIBKjANBgkqhkiG9w0BAQUFADAtMRIwEAYDVQQKEwlJbnRl" +
        "cm5ldDIxFzAVBgNVBAMTDmNhLmV4YW1wbGUub3JnMB4XDTA3MDQwOTA2MTIwOVoX" +
        "DTE3MDQwNjA2MTIwOVowMTESMBAGA1UEChMJSW50ZXJuZXQyMRswGQYDVQQDExJm" +
        "b29iYXIuZXhhbXBsZS5vcmcwggEiMA0GCSqGSIb3DQEBAQUAA4IBDwAwggEKAoIB" +
        "AQDNWnkFmhy1vYa6gN/xBRKkZxFy3sUq2V0LsYb6Q3pe9Qlb6+BzaM5DrN8uIqqr" +
        "oBE3Wp0LtrgKuQTpDpNFBdS2p5afiUtOYLWBDtizTOzs3Z36MGMjIPUYQ4s03IP3" +
        "yPh2ud6EKpDPiYqzNbkRaiIwmYSit5r+RMYvd6fuKvTOn6h7PZI5AD7Rda7VWh5O" +
        "VSoZXlRx3qxFho+mZhW0q4fUfTi5lWwf4EhkfBlzgw/k5gf4cOi6rrGpRS1zxmbt" +
        "X1RAg+I20z6d04g0N2WsK5stszgYKoIROJCiXwjraa8/SoFcILolWQpttVHBIUYl" +
        "yDlm8mIFleZf4ReFpfm+nUYxAgMBAAGjgbIwga8wCQYDVR0TBAIwADAsBglghkgB" +
        "hvhCAQ0EHxYdT3BlblNTTCBHZW5lcmF0ZWQgQ2VydGlmaWNhdGUwHQYDVR0OBBYE" +
        "FDgRgTkjaKoK6DoZfUZ4g9LDJUWuMFUGA1UdIwROMEyAFNXuZVPeUdqHrULqQW7y" +
        "r9buRpQLoTGkLzAtMRIwEAYDVQQKEwlJbnRlcm5ldDIxFzAVBgNVBAMTDmNhLmV4" +
        "YW1wbGUub3JnggEBMA0GCSqGSIb3DQEBBQUAA4IBAQCPj3Si4Eiw9abNgPBUhBXW" +
        "d6eRYlIHaHcnez6j6g7foAOyuVIUso9Q5c6pvL87lmasK55l09YPXw1qmiH+bHMc" +
        "rwEPODpLx7xd3snlOCi7FyxahxwSs8yfTu8Pq95rWt0LNcfHxQK938Cpnav6jgDo" +
        "2uH/ywAOFFSnoBzGHAfScHMfj8asZ6THosYsklII7FSU8j49GV2utkvGB3mcu4ST" +
        "uLdeRCZmi93vq1D4JVGsXC4UaHjg114+a+9q0XZdz6a1UW4pt1ryXIPotCS62M71" +
        "pkJf5neHUinKAqgoRfPXowudZg1Zl8DjzoOBn+MNHRrR5KYbVGvdHcxoJLCwVB/v";
        
    private X509Certificate otherCert1;
    private String otherCert1Base64 = 
        "MIIECTCCAvGgAwIBAgIBMzANBgkqhkiG9w0BAQUFADAtMRIwEAYDVQQKEwlJbnRl" +
        "cm5ldDIxFzAVBgNVBAMTDmNhLmV4YW1wbGUub3JnMB4XDTA3MDUyNTIwMTYxMVoX" +
        "DTE3MDUyMjIwMTYxMVowGjEYMBYGA1UEAxMPaWRwLmV4YW1wbGUub3JnMIIBtjCC" +
        "ASsGByqGSM44BAEwggEeAoGBAI+ktw7R9m7TxjaCrT2MHwWNQUAyXPrqbFCcu+DC" +
        "irr861U6R6W/GyqWdcy8/D1Hh/I1U94POQn5yfqVPpVH2ZRS4OMFndHWaoo9V5LJ" +
        "oXTXHiDYB3W4t9tn0fm7It0n7VoUI5C4y9LG32Hq+UIGF/ktNTmo//mEqLS6aJNd" +
        "bMFpAhUArmKGh0hcpmjukYArWcMRvipB4CMCgYBuCiCrUaHBRRtqrk0P/Luq0l2M" +
        "2718GwSGeLPZip06gACDG7IctMrgH1J+ZIjsx6vffi977wnMDiktqacmaobV+SCR" +
        "W9ijJRdkYpUHmlLvuJGnDPjkvewpbGWJsCabpWEvWdYw3ma8RuHOPj4Jkrdd4VcR" +
        "aFwox/fPJ7cG6kBydgOBhAACgYBxQIPv9DCsmiMHG1FAxSARX0GcRiELJPJ+MtaS" +
        "tdTrVobNa2jebwc3npLiTvUR4U/CDo1mSZb+Sp/wian8kNZHmGcR6KbtJs9UDsa3" +
        "V0pbbgpUar4HcxV+NQJBbhn9RGu85g3PDILUrINiUAf26mhPN5Y0paM+HbM68nUf" +
        "1OLv16OBsjCBrzAJBgNVHRMEAjAAMCwGCWCGSAGG+EIBDQQfFh1PcGVuU1NMIEdl" +
        "bmVyYXRlZCBDZXJ0aWZpY2F0ZTAdBgNVHQ4EFgQUIHFAEB/3jIIZzJEJ/qdsuI8v" +
        "N3kwVQYDVR0jBE4wTIAU1e5lU95R2oetQupBbvKv1u5GlAuhMaQvMC0xEjAQBgNV" +
        "BAoTCUludGVybmV0MjEXMBUGA1UEAxMOY2EuZXhhbXBsZS5vcmeCAQEwDQYJKoZI" +
        "hvcNAQEFBQADggEBAJt4Q34+pqjW5tHHhkdzTITSBjOOf8EvYMgxTMRzhagLSHTt" +
        "9RgO5i/G7ELvnwe1j6187m1XD9iEAWKeKbB//ljeOpgnwzkLR9Er5tr1RI3cbil0" +
        "AX+oX0c1jfRaQnR50Rfb5YoNX6G963iphlxp9C8VLB6eOk/S270XoWoQIkO1ioQ8" +
        "JY4HE6AyDsOpJaOmHpBaxjgsiko52ZWZeZyaCyL98BXwVxeml7pYnHlXWWidB0N/" +
        "Zy+LbvWg3urUkiDjMcB6nGImmEfDSxRdybitcMwbwL26z2WOpwL3llm3mcCydKXg" +
        "Xt8IQhfDhOZOHWckeD2tStnJRP/cqBgO62/qirw=";

            
    private X509Certificate otherCert2;
    private String otherCert2Base64 = 
        "MIIC8DCCAdigAwIBAgIBNDANBgkqhkiG9w0BAQUFADAtMRIwEAYDVQQKEwlJbnRl" +
        "cm5ldDIxFzAVBgNVBAMTDmNhLmV4YW1wbGUub3JnMB4XDTA3MDUyNTIwMzEwOFoX" +
        "DTE3MDUyMjIwMzEwOFowGTEXMBUGA1UEAxMOc3AuZXhhbXBsZS5vcmcwgZ8wDQYJ" +
        "KoZIhvcNAQEBBQADgY0AMIGJAoGBAKk7xVQ23rc51qieDAcj3CCsumTTpTiFCxGi" +
        "qDA8lStdcBmoNX3v+lBFKkXtQKwebcwOF3YyHFyZ2kXFREfZrG76h/5QBFNQZa6y" +
        "/rJskCl9bnz70HVDzBGizRc3qWVDKP1EQ6rRY2qkeGd33NKHkVUepGMPhHsv3kK+" +
        "zVUR6/v/AgMBAAGjgbIwga8wCQYDVR0TBAIwADAsBglghkgBhvhCAQ0EHxYdT3Bl" +
        "blNTTCBHZW5lcmF0ZWQgQ2VydGlmaWNhdGUwHQYDVR0OBBYEFOhBatBFJQgNeKiG" +
        "1/Ro9W9zmO92MFUGA1UdIwROMEyAFNXuZVPeUdqHrULqQW7yr9buRpQLoTGkLzAt" +
        "MRIwEAYDVQQKEwlJbnRlcm5ldDIxFzAVBgNVBAMTDmNhLmV4YW1wbGUub3JnggEB" +
        "MA0GCSqGSIb3DQEBBQUAA4IBAQAhpFQ4+OOrtETiuSagUe9dSqm1hahcQuv3iWRf" +
        "L7Pp6i2x1rR6hlk9zXUC58Oou/UOUOpPWZG/8cr3B50ViuT3zVisIYdaCGeUZ3ef" +
        "EeeeaI77m73xpn/qNI4aIlr6Wmj9C0biJsF3hJvtK2VJu74GCUtxsXkEKyQ0fNx6" +
        "bTd50RxUhM4uto7YqvMfKH6GDJPRcFmsR5YEajxOVihfRtOjvd1rTnOQLWYQRy1O" +
        "ZQek1Z6M7aJmYanYEAjQhyzbKM+R2sqzMlgHbGnbeAADEBnQi8GSkTpgyieT6NC8" +
        "6SvWhq47gNpBYySYhb9zKx/OcZMK4UBD48HhAatEgovrKCHH";
    
    private BasicCredential entityRSACred;
    private BasicX509Credential entityX509Cred;
    
    private List<Credential> credentials;
    
    private CriteriaSet criteriaSet;

    /** {@inheritDoc} */
    protected void setUp() throws Exception {
        entityPubKey = SecurityTestHelper.buildJavaRSAPublicKey(rsaBase64);
        entityCert = SecurityTestHelper.buildJavaX509Cert(entityCertBase64);
        otherCert1 = SecurityTestHelper.buildJavaX509Cert(otherCert1Base64);
        otherCert2 = SecurityTestHelper.buildJavaX509Cert(otherCert2Base64);
        
        entityRSACred = SecurityHelper.getSimpleCredential(entityPubKey, null);
        entityRSACred.setEntityId("entity-RSA");
        
        entityX509Cred = new BasicX509Credential();
        entityX509Cred.setEntityCertificate(entityCert);
        entityX509Cred.setEntityId("entity-X509");
        
        BasicX509Credential otherCred1 = new BasicX509Credential();
        otherCred1.setEntityCertificate(otherCert1);
        otherCred1.setEntityId("other-1");
        
        BasicX509Credential otherCred2 = new BasicX509Credential();
        otherCred2.setEntityCertificate(otherCert2);
        otherCred2.setEntityId("other-2");
        
        credentials = new ArrayList<Credential>();
        credentials.add(otherCred1);
        credentials.add(otherCred2);
        
        // not used, but have to pass in a non-null, non-empty criteria set
        criteriaSet = new CriteriaSet();
        criteriaSet.add( new EntityIDCriteria("dummyEntityID") );
    }

    public void testCertTrusted() throws SecurityException {
        credentials.add(entityX509Cred);
        CredentialResolver resolver = new StaticCredentialResolver(credentials);
        ExplicitX509CertificateTrustEngine engine = new ExplicitX509CertificateTrustEngine(resolver);
        
        assertTrue("Entity X509 credential was not trusted", engine.validate(entityX509Cred, criteriaSet));
    }
    
    public void testUntrusted() throws SecurityException {
        CredentialResolver resolver = new StaticCredentialResolver(credentials);
        ExplicitX509CertificateTrustEngine engine = new ExplicitX509CertificateTrustEngine(resolver);
        
        assertFalse("Entity X509 credential was trusted", engine.validate(entityX509Cred, criteriaSet));
    }

    public void testTrustedKeyNoTrustedCert() throws SecurityException {
        // This has the same key as entityCert, but X509Certificate engine should skip b/c not X509Credential
        credentials.add(entityRSACred);
        CredentialResolver resolver = new StaticCredentialResolver(credentials);
        ExplicitX509CertificateTrustEngine engine = new ExplicitX509CertificateTrustEngine(resolver);
        
        assertFalse("Entity X509 credential was trusted", engine.validate(entityX509Cred, criteriaSet));
    }
}
