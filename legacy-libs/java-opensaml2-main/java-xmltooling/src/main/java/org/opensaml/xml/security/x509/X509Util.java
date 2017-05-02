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

package org.opensaml.xml.security.x509;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.cert.CRLException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509CRL;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.security.auth.x500.X500Principal;

import org.apache.commons.ssl.TrustMaterial;
import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.DERObject;
import org.bouncycastle.asn1.DERObjectIdentifier;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.DERSet;
import org.bouncycastle.asn1.DERString;
import org.bouncycastle.asn1.x509.SubjectKeyIdentifier;
import org.bouncycastle.asn1.x509.X509Extensions;
import org.bouncycastle.x509.extension.SubjectKeyIdentifierStructure;
import org.opensaml.xml.util.DatatypeHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for working with X509 objects.
 */
public class X509Util {

    /** Encoding used to store a key or certificate in a file. */
    public static enum ENCODING_FORMAT {
        PEM, DER
    };

    /** Common Name (CN) OID. */
    public static final String CN_OID = "2.5.4.3";

    /** RFC 2459 Other Subject Alt Name type. */
    public static final Integer OTHER_ALT_NAME = new Integer(0);

    /** RFC 2459 RFC 822 (email address) Subject Alt Name type. */
    public static final Integer RFC822_ALT_NAME = new Integer(1);

    /** RFC 2459 DNS Subject Alt Name type. */
    public static final Integer DNS_ALT_NAME = new Integer(2);

    /** RFC 2459 X.400 Address Subject Alt Name type. */
    public static final Integer X400ADDRESS_ALT_NAME = new Integer(3);

    /** RFC 2459 Directory Name Subject Alt Name type. */
    public static final Integer DIRECTORY_ALT_NAME = new Integer(4);

    /** RFC 2459 EDI Party Name Subject Alt Name type. */
    public static final Integer EDI_PARTY_ALT_NAME = new Integer(5);

    /** RFC 2459 URI Subject Alt Name type. */
    public static final Integer URI_ALT_NAME = new Integer(6);

    /** RFC 2459 IP Address Subject Alt Name type. */
    public static final Integer IP_ADDRESS_ALT_NAME = new Integer(7);

    /** RFC 2459 Registered ID Subject Alt Name type. */
    public static final Integer REGISTERED_ID_ALT_NAME = new Integer(8);

    /** Class logger. */
    private static Logger log = LoggerFactory.getLogger(X509Util.class);

    /** Constructed. */
    protected X509Util() {

    }

    /**
     * Gets the commons names that appear within the given distinguished name. The returned list provides the names in
     * the order they appeared in the DN.
     * 
     * @param dn the DN to extract the common names from
     * 
     * @return the common names that appear in the DN in the order they appear or null if the given DN is null
     */
    public static List<String> getCommonNames(X500Principal dn) {
        if (dn == null) {
            return null;
        }

        log.debug("Extracting CNs from the following DN: {}", dn.toString());
        List<String> commonNames = new LinkedList<String>();
        try {
            ASN1InputStream asn1Stream = new ASN1InputStream(dn.getEncoded());
            DERObject parent = asn1Stream.readObject();

            String cn = null;
            DERObject dnComponent;
            DERSequence grandChild;
            DERObjectIdentifier componentId;
            for (int i = 0; i < ((DERSequence) parent).size(); i++) {
                dnComponent = ((DERSequence) parent).getObjectAt(i).getDERObject();
                if (!(dnComponent instanceof DERSet)) {
                    log.debug("No DN components.");
                    continue;
                }

                // Each DN component is a set
                for (int j = 0; j < ((DERSet) dnComponent).size(); j++) {
                    grandChild = (DERSequence) ((DERSet) dnComponent).getObjectAt(j).getDERObject();

                    if (grandChild.getObjectAt(0) != null
                            && grandChild.getObjectAt(0).getDERObject() instanceof DERObjectIdentifier) {
                        componentId = (DERObjectIdentifier) grandChild.getObjectAt(0).getDERObject();

                        if (CN_OID.equals(componentId.getId())) {
                            // OK, this dn component is actually a cn attribute
                            if (grandChild.getObjectAt(1) != null
                                    && grandChild.getObjectAt(1).getDERObject() instanceof DERString) {
                                cn = ((DERString) grandChild.getObjectAt(1).getDERObject()).getString();
                                commonNames.add(cn);
                            }
                        }
                    }
                }
            }

            asn1Stream.close();

            return commonNames;

        } catch (IOException e) {
            log.error("Unable to extract common names from DN: ASN.1 parsing failed: " + e);
            return null;
        }
    }

    /**
     * Gets the list of alternative names of a given name type.
     * 
     * @param certificate the certificate to extract the alternative names from
     * @param nameTypes the name types
     * 
     * @return the alt names, of the given type, within the cert
     */
    public static List getAltNames(X509Certificate certificate, Integer[] nameTypes) {
        if (certificate == null) {
            return null;
        }

        List<Object> names = new LinkedList<Object>();
        try {
            Collection<List<?>> altNames = certificate.getSubjectAlternativeNames();
            if (altNames != null) {
                // 0th position represents the alt name type
                // 1st position contains the alt name data
                List altName;
                for (Iterator<List<?>> nameIterator = altNames.iterator(); nameIterator.hasNext();) {
                    altName = nameIterator.next();
                    for (int i = 0; i < nameTypes.length; i++) {
                        if (altName.get(0).equals(nameTypes[i])) {
                            names.add(altName.get(1));
                            break;
                        }
                    }
                }
            }
        } catch (CertificateParsingException e1) {
            log.error("Encountered an problem trying to extract Subject Alternate "
                    + "Name from supplied certificate: " + e1);
        }

        return names;
    }

    /**
     * Gets the common name components of the issuer and all the subject alt names of a given type.
     * 
     * @param certificate certificate to extract names from
     * @param altNameTypes type of alt names to extract
     * 
     * @return list of subject names in the certificate
     */
    @SuppressWarnings("unchecked")
    public static List getSubjectNames(X509Certificate certificate, Integer[] altNameTypes) {
        List issuerNames = new LinkedList();

        List<String> entityCertCNs = X509Util.getCommonNames(certificate.getSubjectX500Principal());
        issuerNames.add(entityCertCNs.get(0));
        issuerNames.addAll(X509Util.getAltNames(certificate, altNameTypes));

        return issuerNames;
    }

    /**
     * Get the plain (non-DER encoded) value of the Subject Key Identifier extension of an X.509 certificate, if
     * present.
     * 
     * @param certificate an X.509 certificate possibly containing a subject key identifier
     * @return the plain (non-DER encoded) value of the Subject Key Identifier extension, or null if the certificate
     *         does not contain the extension
     * @throws IOException
     */
    public static byte[] getSubjectKeyIdentifier(X509Certificate certificate) {
        byte[] derValue = certificate.getExtensionValue(X509Extensions.SubjectKeyIdentifier.getId());
        if (derValue == null || derValue.length == 0) {
            return null;
        }

        SubjectKeyIdentifier ski = null;
        try {
            ski = new SubjectKeyIdentifierStructure(derValue);
        } catch (IOException e) {
            log.error("Unable to extract subject key identifier from certificate: ASN.1 parsing failed: " + e);
            return null;
        }

        if (ski != null) {
            return ski.getKeyIdentifier();
        } else {
            return null;
        }
    }

    /**
     * Decodes X.509 certificates in DER or PEM format.
     * 
     * @param certs encoded certs
     * 
     * @return decoded certs
     * 
     * @throws CertificateException thrown if the certificates can not be decoded
     */
    @SuppressWarnings("unchecked")
    public static Collection<X509Certificate> decodeCertificate(byte[] certs) throws CertificateException {
        try {
            TrustMaterial tm = new TrustMaterial(certs);
            return tm.getCertificates();
        } catch (Exception e) {
            throw new CertificateException("Unable to decode X.509 certificates", e);
        }
    }

    /**
     * Decodes CRLS in DER or PKCS#7 format. If in PKCS#7 format only the CRLs are decode, the rest of the content is
     * ignored.
     * 
     * @param crls encoded CRLs
     * 
     * @return decoded CRLs
     * 
     * @throws CRLException thrown if the CRLs can not be decoded
     */
    @SuppressWarnings("unchecked")
    public static Collection<X509CRL> decodeCRLs(byte[] crls) throws CRLException {
        try {
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            return (Collection<X509CRL>) cf.generateCRLs(new ByteArrayInputStream(crls));
        } catch (GeneralSecurityException e) {
            throw new CRLException("Unable to decode X.509 certificates");
        }
    }

    /**
     * Gets a formatted string representing identifier information from the supplied credential.
     * 
     * <p>This could for example be used in logging messages.</p>
     * 
     * <p>Often it will be the case that a given credential that is being evaluated will NOT have a value for the
     * entity ID property. So extract the certificate subject DN, and if present, the credential's entity ID.</p>
     * 
     * @param credential the credential for which to produce a token.
     * @param handler the X.500 DN handler to use.  If null, a new instance of 
     *          {@link InternalX500DNHandler} will be used.
     * 
     * @return a formatted string containing identifier information present in the credential
     */
    public static String getIdentifiersToken(X509Credential credential, X500DNHandler handler) {
        X500DNHandler x500DNHandler;
        if (handler != null) {
            x500DNHandler = handler;
        } else {
            x500DNHandler = new InternalX500DNHandler();
        }
        X500Principal x500Principal = credential.getEntityCertificate().getSubjectX500Principal();
        StringBuilder builder = new StringBuilder();
        builder.append('[');
        builder.append(String.format("subjectName='%s'", x500DNHandler.getName(x500Principal)));
        if (!DatatypeHelper.isEmpty(credential.getEntityId())) {
            builder.append(String.format(" |credential entityID='%s'", DatatypeHelper.safeTrimOrNullString(credential
                    .getEntityId())));
        }
        builder.append(']');
        return builder.toString();
    }
}