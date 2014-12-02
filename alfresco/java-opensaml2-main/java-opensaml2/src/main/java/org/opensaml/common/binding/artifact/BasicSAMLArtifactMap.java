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

package org.opensaml.common.binding.artifact;

import java.io.StringReader;
import java.io.StringWriter;

import org.joda.time.DateTime;
import org.opensaml.Configuration;
import org.opensaml.common.SAMLObject;
import org.opensaml.util.storage.StorageService;
import org.opensaml.xml.io.Marshaller;
import org.opensaml.xml.io.MarshallingException;
import org.opensaml.xml.io.Unmarshaller;
import org.opensaml.xml.parse.ParserPool;
import org.opensaml.xml.util.DatatypeHelper;
import org.opensaml.xml.util.XMLHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

/**
 * Basic artifact map implementation that uses a {@link StorageService} to store and retrieve artifacts.
 */
public class BasicSAMLArtifactMap implements SAMLArtifactMap {

    /** Class Logger. */
    private final Logger log = LoggerFactory.getLogger(BasicSAMLArtifactMap.class);

    /** Pool used to parse serialized messages. */
    private ParserPool parserPool;

    /** Artifact mapping storage. */
    private StorageService<String, SAMLArtifactMapEntry> artifactStore;

    /** Storage service partition used by this cache. default: artifact */
    private String partition;

    /** Lifetime of an artifact in milliseconds. */
    private long artifactLifetime;

    /**
     * Constructor.
     * 
     * @param parser parser pool used to parse serialized messages
     * @param storage artifact mapping storage
     * @param lifetime lifetime of an artifact in milliseconds
     */
    public BasicSAMLArtifactMap(ParserPool parser, StorageService<String, SAMLArtifactMapEntry> storage, long lifetime) {
        parserPool = parser;
        artifactStore = storage;
        partition = "artifact";
        artifactLifetime = lifetime;
    }

    /**
     * Constructor.
     * 
     * @param storage artifact mapping storage
     * @param storageParition name of storage service partition to use
     * @param lifetime lifetime of an artifact in milliseconds
     */
    public BasicSAMLArtifactMap(StorageService<String, SAMLArtifactMapEntry> storage, String storageParition,
            long lifetime) {
        artifactStore = storage;
        if (!DatatypeHelper.isEmpty(storageParition)) {
            partition = DatatypeHelper.safeTrim(storageParition);
        } else {
            partition = "artifact";
        }
        artifactLifetime = lifetime;
    }

    /** {@inheritDoc} */
    public boolean contains(String artifact) {
        return artifactStore.contains(partition, artifact);
    }

    /** {@inheritDoc} */
    public SAMLArtifactMapEntry get(String artifact) {
        SAMLArtifactMapEntry entry = artifactStore.get(partition, artifact);

        if (entry.isExpired()) {
            remove(artifact);
            return null;
        }

        return entry;
    }

    /** {@inheritDoc} */
    public void put(String artifact, String relyingPartyId, String issuerId, SAMLObject samlMessage)
            throws MarshallingException {
        BasicSAMLArtifactMapEntry artifactEntry = new BasicSAMLArtifactMapEntry(artifact, issuerId, relyingPartyId,
                samlMessage, artifactLifetime);
        artifactStore.put(partition, artifact, artifactEntry);
    }

    /** {@inheritDoc} */
    public void remove(String artifact) {
        artifactStore.remove(partition, artifact);
    }

    /** Basic implementation of {@link SAMLArtifactMapEntry}. */
    public class BasicSAMLArtifactMapEntry implements SAMLArtifactMapEntry {

        /** SAML artifact being mapped. */
        private String artifact;

        /** Entity ID of the issuer of the artifact. */
        private String issuer;

        /** Entity ID of the receiver of the artifact. */
        private String relyingParty;

        /** Serialized SAML object mapped to the artifact. */
        private String serializedMessage;

        /** Time this artifact entry expires. */
        private DateTime expirationTime;

        /**
         * Constructor.
         * 
         * @param artifact artifact associated with the message
         * @param issuer issuer of the artifact
         * @param relyingParty receiver of the artifact
         * @param saml SAML message mapped to the artifact
         * @param lifetime lifetime of the artifact
         * 
         * @throws MarshallingException thrown if the SAML message can not be serialzed to a string
         */
        public BasicSAMLArtifactMapEntry(String artifact, String issuer, String relyingParty, SAMLObject saml,
                long lifetime) throws MarshallingException {
            this.artifact = artifact;
            this.issuer = issuer;
            this.relyingParty = relyingParty;
            expirationTime = new DateTime().plus(lifetime);

            StringWriter writer = new StringWriter();
            Marshaller marshaller = Configuration.getMarshallerFactory().getMarshaller(saml);
            XMLHelper.writeNode(marshaller.marshall(saml), writer);
            serializedMessage = writer.toString();
        }

        /** {@inheritDoc} */
        public String getArtifact() {
            return artifact;
        }

        /** {@inheritDoc} */
        public String getIssuerId() {
            return issuer;
        }

        /** {@inheritDoc} */
        public String getRelyingPartyId() {
            return relyingParty;
        }

        /** {@inheritDoc} */
        public SAMLObject getSamlMessage() {
            try {
                Element messageElem = parserPool.parse(new StringReader(serializedMessage)).getDocumentElement();
                Unmarshaller unmarshaller = Configuration.getUnmarshallerFactory().getUnmarshaller(messageElem);
                return (SAMLObject) unmarshaller.unmarshall(messageElem);
            } catch (Exception e) {
                log.error("Unable to deserialize and unmarshall SAML message associated with artifact " + artifact, e);
                return null;
            }
        }

        /** {@inheritDoc} */
        public DateTime getExpirationTime() {
            return expirationTime;
        }

        /** {@inheritDoc} */
        public boolean isExpired() {
            return expirationTime.isBeforeNow();
        }

        /** {@inheritDoc} */
        public void onExpire() {

        }
    }
}