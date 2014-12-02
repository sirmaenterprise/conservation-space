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

package org.opensaml;

import org.apache.velocity.app.Velocity;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.xml.security.Init;
import org.opensaml.saml1.binding.artifact.SAML1ArtifactBuilderFactory;
import org.opensaml.saml2.binding.artifact.SAML2ArtifactBuilderFactory;
import org.opensaml.xml.ConfigurationException;
import org.opensaml.xml.XMLConfigurator;
import org.opensaml.xml.security.DefaultSecurityConfigurationBootstrap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class can be used to bootstrap the OpenSAML library with the default configurations that ship with the library.
 */
public class DefaultBootstrap {

    /** Class logger. */
    private static Logger log = LoggerFactory.getLogger(DefaultBootstrap.class);

    /** List of default XMLTooling configuration files. */
    private static String[] xmlToolingConfigs = { 
        "/default-config.xml", 
        "/schema-config.xml", 
        "/signature-config.xml",
        "/signature-validation-config.xml", 
        "/encryption-config.xml", 
        "/encryption-validation-config.xml",
        "/soap11-config.xml", 
        "/saml1-assertion-config.xml", 
        "/saml1-protocol-config.xml",
        "/saml1-core-validation-config.xml", 
        "/saml2-assertion-config.xml", 
        "/saml2-protocol-config.xml",
        "/saml2-core-validation-config.xml", 
        "/saml1-metadata-config.xml", 
        "/saml2-metadata-config.xml",
        "/saml2-metadata-validation-config.xml", 
        "/saml2-protocol-thirdparty-config.xml",
        "/saml2-metadata-query-config.xml", };

    /** Constrcutor. */
    protected DefaultBootstrap() {

    }

    /**
     * Initializes the OpenSAML library, loading default configurations.
     * 
     * @throws ConfigurationException thrown if there is a problem initializing the OpenSAML library
     */
    public static synchronized void bootstrap() throws ConfigurationException {

        initializeXMLSecurity();

        initializeVelocity();

        initializeXMLTooling(xmlToolingConfigs);

        initializeArtifactBuilderFactories();

        initializeGlobalSecurityConfiguration();
    }

    /**
     * Initializes the default global security configuration.
     */
    protected static void initializeGlobalSecurityConfiguration() {
        Configuration.setGlobalSecurityConfiguration(DefaultSecurityConfigurationBootstrap.buildDefaultConfig());
    }

    /**
     * Initializes the Apache XMLSecurity libary.
     * 
     * @throws ConfigurationException thrown is there is a problem initializing the library
     */
    protected static void initializeXMLSecurity() throws ConfigurationException {
        if (!Init.isInitialized()) {
            log.debug("Initializing Apache XMLSecurity library");
            Init.init();
        }
    }

    /**
     * Intializes the Apache Velocity template engine.
     * 
     * @throws ConfigurationException thrown if there is a problem initializing Velocity
     */
    protected static void initializeVelocity() throws ConfigurationException {
        try {
            log.debug("Initializing Velocity template engine");
            Velocity.setProperty(RuntimeConstants.RUNTIME_LOG_LOGSYSTEM_CLASS,
                    "org.apache.velocity.runtime.log.NullLogChute");
            Velocity.setProperty(RuntimeConstants.ENCODING_DEFAULT, "UTF-8");
            Velocity.setProperty(RuntimeConstants.OUTPUT_ENCODING, "UTF-8");
            Velocity.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
            Velocity.setProperty("classpath.resource.loader.class",
                    "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
            Velocity.init();
        } catch (Exception e) {
            throw new ConfigurationException("Unable to initialize Velocity template engine", e);
        }
    }

    /**
     * Initializes the XMLTooling library with a default set of object providers.
     * 
     * @param providerConfigs list of provider configuration files located on the classpath
     * 
     * @throws ConfigurationException thrown if there is a problem loading the configuration files
     */
    protected static void initializeXMLTooling(String[] providerConfigs) throws ConfigurationException {
        Class clazz = Configuration.class;
        XMLConfigurator configurator = new XMLConfigurator();

        for (String config : providerConfigs) {
            log.debug("Loading XMLTooling configuration {}", config);
            configurator.load(clazz.getResourceAsStream(config));
        }
    }

    /**
     * Initializes the artifact factories for SAML 1 and SAML 2 artifacts.
     * 
     * @throws ConfigurationException thrown if there is a problem initializing the artifact factory
     */
    protected static void initializeArtifactBuilderFactories() throws ConfigurationException {
        log.debug("Initializing SAML Artifact builder factories");
        Configuration.setSAML1ArtifactBuilderFactory(new SAML1ArtifactBuilderFactory());
        Configuration.setSAML2ArtifactBuilderFactory(new SAML2ArtifactBuilderFactory());
    }
}