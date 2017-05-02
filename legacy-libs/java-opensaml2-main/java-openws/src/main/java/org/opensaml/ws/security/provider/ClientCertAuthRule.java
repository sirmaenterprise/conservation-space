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

package org.opensaml.ws.security.provider;

import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import org.opensaml.ws.message.MessageContext;
import org.opensaml.ws.security.SecurityPolicyException;
import org.opensaml.ws.transport.Transport;
import org.opensaml.xml.security.CriteriaSet;
import org.opensaml.xml.security.credential.Credential;
import org.opensaml.xml.security.credential.UsageType;
import org.opensaml.xml.security.criteria.EntityIDCriteria;
import org.opensaml.xml.security.criteria.UsageCriteria;
import org.opensaml.xml.security.trust.TrustEngine;
import org.opensaml.xml.security.x509.X509Credential;
import org.opensaml.xml.security.x509.X509Util;
import org.opensaml.xml.util.DatatypeHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Policy rule that checks if the client cert used to authenticate the request is valid and trusted.
 * 
 * <p>
 * This rule is only evaluated if the message context contains a peer {@link X509Credential} as returned from the
 * inbound message context's inbound message transport {@link Transport#getPeerCredential()}.
 * </p>
 * 
 * <p>
 * If the inbound message issuer has been previously set in the message context by another rule, then that issuer is
 * used to evaluate the request's X509Credential. If this trust evaluation is successful, the message context's inbound
 * transport authentication state will be set to <code>true</code> and processing is terminated. If unsuccessful, a
 * {@link SecurityPolicyException} is thrown.
 * </p>
 * 
 * <p>
 * If no context issuer was previously set, then rule evaluation will be attempted as described in
 * {@link #evaluateCertificateNameDerivedIssuers(X509Credential, MessageContext)}, based on the currently configured
 * certificate name evaluation options. If this method returns a non-null issuer entity ID, it will be set as the
 * inbound message issuer in the message context, the message context's inbound transport issuer authentication state
 * will be set to <code>true</code> and rule processing is terminated. If the method returns null, the message context
 * issuer and transport authentication state will remain unmodified and rule processing continues.
 * </p>
 * 
 * <p>
 * Finally rule evaluation will proceed as described in {@link #evaluateDerivedIssuers(X509Credential, MessageContext)}.
 * This is primarily an extension point by which subclasses may implement specific custom logic. If this method returns
 * a non-null issuer entity ID, it will be set as the inbound message issuer in the message context, the message
 * context's inbound transport authentication state will be set to <code>true</code> and rule processing is
 * terminated. If the method returns null, the message context issuer and transport authentication state will remain
 * unmodified.
 * </p>
 */
public class ClientCertAuthRule extends BaseTrustEngineRule<X509Credential> {

    /** Logger. */
    private final Logger log = LoggerFactory.getLogger(ClientCertAuthRule.class);

    /** Options for derving issuer names from an X.509 certificate. */
    private CertificateNameOptions certNameOptions;

    /**
     * Constructor.
     * 
     * @param engine Trust engine used to verify the request X509Credential
     * @param nameOptions options for deriving issuer names from an X.509 certificate
     * 
     */
    public ClientCertAuthRule(TrustEngine<X509Credential> engine, CertificateNameOptions nameOptions) {
        super(engine);
        certNameOptions = nameOptions;
    }

    /** {@inheritDoc} */
    public void evaluate(MessageContext messageContext) throws SecurityPolicyException {

        Credential peerCredential = messageContext.getInboundMessageTransport().getPeerCredential();

        if (peerCredential == null) {
            log.info("Inbound message transport did not contain a peer credential, "
                    + "skipping client certificate authentication");
            return;
        }
        if (!(peerCredential instanceof X509Credential)) {
            log.info("Inbound message transport did not contain an X509Credential, "
                    + "skipping client certificate authentication");
            return;
        }

        X509Credential requestCredential = (X509Credential) peerCredential;

        doEvaluate(requestCredential, messageContext);
    }

    /**
     * Get the currently configured certificate name options.
     * 
     * @return the certificate name options
     */
    protected CertificateNameOptions getCertificateNameOptions() {
        return certNameOptions;
    }

    /**
     * Evaluate the request credential.
     * 
     * @param requestCredential the X509Credential derived from the request
     * @param messageContext the message context being evaluated
     * @throws SecurityPolicyException thrown if a message context issuer is present and the client certificate token
     *             can not be trusted on that basis, or if there is error during evaluation processing
     */
    protected void doEvaluate(X509Credential requestCredential, MessageContext messageContext)
            throws SecurityPolicyException {

        String contextIssuer = messageContext.getInboundMessageIssuer();

        if (contextIssuer != null) {
            log.debug("Attempting client certificate authentication using context issuer: {}", contextIssuer);
            if (evaluate(requestCredential, contextIssuer, messageContext)) {
                log.info("Authentication via client certificate succeeded for context issuer entity ID: {}",
                        contextIssuer);
                messageContext.getInboundMessageTransport().setAuthenticated(true);
            } else {
                log.error("Authentication via client certificate failed for context issuer entity ID {}",
                        contextIssuer);
                throw new SecurityPolicyException(
                        "Client certificate authentication failed for context issuer entity ID");
            }
            return;
        }

        String derivedIssuer = evaluateCertificateNameDerivedIssuers(requestCredential, messageContext);
        if (derivedIssuer != null) {
            log.info("Authentication via client certificate succeeded for certificate-derived issuer entity ID {}",
                    derivedIssuer);
            messageContext.setInboundMessageIssuer(derivedIssuer);
            messageContext.getInboundMessageTransport().setAuthenticated(true);
            return;
        }

        derivedIssuer = evaluateDerivedIssuers(requestCredential, messageContext);
        if (derivedIssuer != null) {
            log.info("Authentication via client certificate succeeded for derived issuer entity ID {}", derivedIssuer);
            messageContext.setInboundMessageIssuer(derivedIssuer);
            messageContext.getInboundMessageTransport().setAuthenticated(true);
            return;
        }
    }

    /** {@inheritDoc} */
    protected CriteriaSet buildCriteriaSet(String entityID, MessageContext messageContext)
            throws SecurityPolicyException {

        CriteriaSet criteriaSet = new CriteriaSet();
        if (!DatatypeHelper.isEmpty(entityID)) {
            criteriaSet.add(new EntityIDCriteria(entityID));
        }

        criteriaSet.add(new UsageCriteria(UsageType.SIGNING));

        return criteriaSet;
    }

    /**
     * Evaluate any candidate issuer entity ID's which may be derived from the credential or other message context
     * information.
     * 
     * <p>
     * This serves primarily as an extension point for subclasses to implement application-specific logic.
     * </p>
     * 
     * <p>
     * If multiple derived candidate entity ID's would satisfy the trust engine criteria, the choice of which one to
     * return as the canonical issuer value is implementation-specific.
     * </p>
     * 
     * @param requestCredential the X509Credential derived from the request
     * @param messageContext the message context being evaluated
     * @return an issuer entity ID which was successfully evaluated by the trust engine
     * @throws SecurityPolicyException thrown if there is error during processing
     */
    protected String evaluateDerivedIssuers(X509Credential requestCredential, MessageContext messageContext)
            throws SecurityPolicyException {

        return null;
    }

    /**
     * Evaluate candidate issuer entity ID's which may be derived from the request credential's entity certificate
     * according to the options supplied via {@link CertificateNameOptions}.
     * 
     * <p>
     * Configured certificate name types are derived as candidate issuers and processed in the following order:
     * <ol>
     * <li>The certificate subject DN string as serialized by the X500DNHandler obtained via
     * {@link CertificateNameOptions#getX500DNHandler()} and using the output format indicated by
     * {@link CertificateNameOptions#getX500SubjectDNFormat()}.</li>
     * <li>Subject alternative names of the types configured via {@link CertificateNameOptions#getSubjectAltNames()}.
     * Note that this is a LinkedHashSet, so the order of evaluation is the order of insertion.</li>
     * <li>The first common name (CN) value appearing in the certificate subject DN.</li>
     * </ol>
     * </p>
     * 
     * <p>
     * The first one of the above which is successfully evaluated by the trust engine using criteria built from
     * {@link BaseTrustEngineRule#buildCriteriaSet(String, MessageContext)} will be returned.
     * </p>
     * 
     * @param requestCredential the X509Credential derived from the request
     * @param messageContext the message context being evaluated
     * @return an issuer entity ID which was successfully evaluated by the trust engine
     * @throws SecurityPolicyException thrown if there is error during processing
     */
    protected String evaluateCertificateNameDerivedIssuers(X509Credential requestCredential,
            MessageContext messageContext) throws SecurityPolicyException {

        String candidateIssuer = null;

        if (certNameOptions.evaluateSubjectDN()) {
            candidateIssuer = evaluateSubjectDN(requestCredential, messageContext);
            if (candidateIssuer != null) {
                return candidateIssuer;
            }
        }

        if (!certNameOptions.getSubjectAltNames().isEmpty()) {
            candidateIssuer = evaluateSubjectAltNames(requestCredential, messageContext);
            if (candidateIssuer != null) {
                return candidateIssuer;
            }
        }

        if (certNameOptions.evaluateSubjectCommonName()) {
            candidateIssuer = evaluateSubjectCommonName(requestCredential, messageContext);
            if (candidateIssuer != null) {
                return candidateIssuer;
            }
        }

        return null;
    }

    /**
     * Evaluate the issuer entity ID as derived from the cert subject common name (CN).
     * 
     * Only the first CN value from the subject DN is evaluated.
     * 
     * @param requestCredential the X509Credential derived from the request
     * @param messageContext the message context being evaluated
     * @return an issuer entity ID which was successfully evaluated by the trust engine
     * @throws SecurityPolicyException thrown if there is error during processing
     */
    protected String evaluateSubjectCommonName(X509Credential requestCredential, MessageContext messageContext)
            throws SecurityPolicyException {

        log.debug("Evaluating client cert by deriving issuer as cert CN");
        X509Certificate certificate = requestCredential.getEntityCertificate();
        String candidateIssuer = getCommonName(certificate);
        if (candidateIssuer != null) {
            if (evaluate(requestCredential, candidateIssuer, messageContext)) {
                log.info("Authentication succeeded for issuer derived from CN {}", candidateIssuer);
                return candidateIssuer;
            }
        }
        return null;
    }

    /**
     * Evaluate the issuer entity ID as derived from the cert subject DN.
     * 
     * @param requestCredential the X509Credential derived from the request
     * @param messageContext the message context being evaluated
     * @return an issuer entity ID which was successfully evaluated by the trust engine
     * @throws SecurityPolicyException thrown if there is error during processing
     */
    protected String evaluateSubjectDN(X509Credential requestCredential, MessageContext messageContext)
            throws SecurityPolicyException {

        log.debug("Evaluating client cert by deriving issuer as cert subject DN");
        X509Certificate certificate = requestCredential.getEntityCertificate();
        String candidateIssuer = getSubjectName(certificate);
        if (candidateIssuer != null) {
            if (evaluate(requestCredential, candidateIssuer, messageContext)) {
                log.info("Authentication succeeded for issuer derived from subject DN {}", candidateIssuer);
                return candidateIssuer;
            }
        }
        return null;
    }

    /**
     * Evaluate the issuer entity ID as derived from the cert subject alternative names specified by types enumerated in
     * {@link CertificateNameOptions#getSubjectAltNames()}.
     * 
     * @param requestCredential the X509Credential derived from the request
     * @param messageContext the message context being evaluated
     * @return an issuer entity ID which was successfully evaluated by the trust engine
     * @throws SecurityPolicyException thrown if there is error during processing
     */
    protected String evaluateSubjectAltNames(X509Credential requestCredential, MessageContext messageContext)
            throws SecurityPolicyException {

        log.debug("Evaluating client cert by deriving issuer from subject alt names");
        X509Certificate certificate = requestCredential.getEntityCertificate();
        for (Integer altNameType : certNameOptions.getSubjectAltNames()) {
            log.debug("Evaluating alt names of type: {}", altNameType.toString());
            List<String> altNames = getAltNames(certificate, altNameType);
            for (String altName : altNames) {
                if (evaluate(requestCredential, altName, messageContext)) {
                    log.info("Authentication succeeded for issuer derived from subject alt name {}", altName);
                    return altName;
                }
            }
        }
        return null;
    }

    /**
     * Get the first common name (CN) value from the subject DN of the specified certificate.
     * 
     * @param cert the certificate being processed
     * @return the first CN value, or null if there are none
     */
    protected String getCommonName(X509Certificate cert) {
        List<String> names = X509Util.getCommonNames(cert.getSubjectX500Principal());
        if (names != null && !names.isEmpty()) {
            String name = names.get(0);
            log.debug("Extracted common name from certificate: {}", name);
            return name;
        }
        return null;
    }

    /**
     * Get subject name from a certificate, using the currently configured X500DNHandler and subject DN output format.
     * 
     * @param cert the certificate being processed
     * @return the subject name
     */
    protected String getSubjectName(X509Certificate cert) {
        if (cert == null) {
            return null;
        }
        String name = null;
        if (!DatatypeHelper.isEmpty(certNameOptions.getX500SubjectDNFormat())) {
            name = certNameOptions.getX500DNHandler().getName(cert.getSubjectX500Principal(),
                    certNameOptions.getX500SubjectDNFormat());
        } else {
            name = certNameOptions.getX500DNHandler().getName(cert.getSubjectX500Principal());
        }
        log.debug("Extracted subject name from certificate: {}", name);
        return name;
    }

    /**
     * Get the list of subject alt name values from the certificate which are of the specified alt name type.
     * 
     * @param cert the certificate from which to extract alt names
     * @param altNameType the type of alt name to extract
     * 
     * @return the list of certificate subject alt names
     */
    protected List<String> getAltNames(X509Certificate cert, Integer altNameType) {
        log.debug("Extracting alt names from certificate of type: {}", altNameType.toString());
        Integer[] nameTypes = new Integer[] { altNameType };
        List altNames = X509Util.getAltNames(cert, nameTypes);
        List<String> names = new ArrayList<String>();
        for (Object altNameValue : altNames) {
            if (!(altNameValue instanceof String)) {
                log.debug("Skipping non-String certificate alt name value");
            } else {
                names.add((String) altNameValue);
            }
        }
        log.debug("Extracted alt names from certificate: {}", names.toString());
        return names;
    }

}