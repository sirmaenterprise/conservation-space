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

package org.opensaml.xml.parse;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.lang.ref.SoftReference;
import java.util.Collections;
import java.util.EmptyStackException;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.validation.Schema;

import org.opensaml.xml.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * A pool of JAXP 1.3 {@link DocumentBuilder}s.
 * 
 * Builder retrieved from this pool should be returned to the pool with the method
 * {@link #returnBuilder(DocumentBuilder)}. Builders checked out prior to a change in the pool's properties will not be
 * effected by the change and will be appropriately dealt with when they are returned.
 * 
 * If a the pool reaches its max size and another request for a builder is made behavior is dependent upon
 * {@link #getCreateBuildersAtPoolLimit()}. If this returns the true then a new builder will be created and returned
 * but will be discarded when it is returned. If it returns false a builder will not be created and null will be
 * returned.
 * 
 * References to builders are kept by way of {@link SoftReference} so that the garbage collector may reap the builders
 * if the system is running out of memory.
 */
public class BasicParserPool implements ParserPool {

    /** Class logger. */
    private final Logger log = LoggerFactory.getLogger(BasicParserPool.class);

    /** Current version of the pool. */
    private long poolVersion;

    /** Create a new builder when the pool size is reached. Default value: true */
    private boolean createBuildersAtPoolLimit;

    /** Whether a change has been made to the builder configuration but has not yet been applied. */
    private boolean dirtyBuilderConfiguration;

    /** Factory used to create new builders. */
    private DocumentBuilderFactory builderFactory;

    /** Cache of document builders. */
    private Stack<SoftReference<DocumentBuilder>> builderPool;

    /** Lock used when we're performing operations that remove items from the pool. */
    private Lock builderPoolLock;

    /** Max number of builders allowed in the pool. Default value: 5 */
    private int maxPoolSize;

    /** Builder attributes. */
    private Map<String, Object> builderAttributes;

    /** Whether the builders are coalescing. Default value: true */
    private boolean coalescing;

    /** Whether the builders expand entity references. Default value: true */
    private boolean expandEntityReferences;

    /** Builder features. */
    private Map<String, Boolean> builderFeatures;

    /** Whether the builders ignore comments. Default value: true */
    private boolean ignoreComments;

    /** Whether the builders ignore element content whitespace. Default value: true */
    private boolean ignoreElementContentWhitespace;

    /** Whether the builders are namespace aware. Default value: true */
    private boolean namespaceAware;

    /** Schema used to validate parsed content. */
    private Schema schema;

    /** Whether the builder should validate. Default value: false */
    private boolean dtdValidating;

    /** Whether the builders are XInclude aware. Default value: false */
    private boolean xincludeAware;

    /** Entity resolver used by builders. */
    private EntityResolver entityResolver;

    /** Error handler used by builders. */
    private ErrorHandler errorHandler;

    /** Constructor. */
    public BasicParserPool() {
        Configuration.validateNonSunJAXP();
        maxPoolSize = 5;
        builderPool = new Stack<SoftReference<DocumentBuilder>>();
        builderPoolLock = new ReentrantLock(true);
        builderAttributes = new HashMap<String, Object>();
        coalescing = true;
        expandEntityReferences = true;
        builderFeatures = new HashMap<String, Boolean>();
        ignoreComments = true;
        ignoreElementContentWhitespace = true;
        namespaceAware = true;
        schema = null;
        dtdValidating = false;
        xincludeAware = false;
        errorHandler = new LoggingErrorHandler(log);

        try {
            dirtyBuilderConfiguration = true;
            initializePool();
        } catch (XMLParserException e) {
            // default settings, no parsing exception
        }
    }

    /** {@inheritDoc} */
    public DocumentBuilder getBuilder() throws XMLParserException {
        DocumentBuilder builder = null;

        try {
            if (dirtyBuilderConfiguration) {
                initializePool();
            }
            builder = builderPool.pop().get();
        } catch (EmptyStackException e) {
            // we don't take care of this here because we do the same thing whether
            // we get this exception or if the builder is null because its was
            // garbage collected is the same (we're using soft references, remember?)
        }

        if (builder == null) {
            if (builderPool.size() < maxPoolSize || createBuildersAtPoolLimit) {
                builder = createBuilder();
            }
        }

        if (builder != null) {
            return new DocumentBuilderProxy(builder, this);
        }

        return null;
    }

    /** {@inheritDoc} */
    public void returnBuilder(DocumentBuilder builder) {
        if (!(builder instanceof DocumentBuilderProxy)) {
            return;
        }

        DocumentBuilderProxy proxiedBuilder = (DocumentBuilderProxy) builder;
        if (proxiedBuilder.getOwningPool() != this && proxiedBuilder.getPoolVersion() != poolVersion) {
            return;
        }

        DocumentBuilder unwrappedBuilder = proxiedBuilder.getProxiedBuilder();
        unwrappedBuilder.reset();
        SoftReference<DocumentBuilder> builderReference = new SoftReference<DocumentBuilder>(unwrappedBuilder);

        if (builderPool.size() < maxPoolSize) {
            builderPool.push(builderReference);
        }
    }

    /** {@inheritDoc} */
    public Document newDocument() throws XMLParserException {
        DocumentBuilder builder = getBuilder();
        Document document = builder.newDocument();
        returnBuilder(builder);
        return document;
    }

    /** {@inheritDoc} */
    public Document parse(InputStream input) throws XMLParserException {
        DocumentBuilder builder = getBuilder();
        try {
            Document document = builder.parse(input);
            return document;
        } catch (SAXException e) {
            throw new XMLParserException("Invalid XML", e);
        } catch (IOException e) {
            throw new XMLParserException("Unable to read XML from input stream", e);
        } finally {
            returnBuilder(builder);
        }
    }

    /** {@inheritDoc} */
    public Document parse(Reader input) throws XMLParserException {
        DocumentBuilder builder = getBuilder();
        try {
            Document document = builder.parse(new InputSource(input));
            return document;
        } catch (SAXException e) {
            throw new XMLParserException("Invalid XML", e);
        } catch (IOException e) {
            throw new XMLParserException("Unable to read XML from input stream", e);
        } finally {
            returnBuilder(builder);
        }
    }

    /**
     * Gets the max number of builders the pool will hold.
     * 
     * @return max number of builders the pool will hold
     */
    public int getMaxPoolSize() {
        return maxPoolSize;
    }

    /**
     * Sets the max number of builders the pool will hold.
     * 
     * @param newSize max number of builders the pool will hold
     */
    public void setMaxPoolSize(int newSize) {
        maxPoolSize = newSize;
    }

    /**
     * Gets whether new builders will be created when the max pool size is reached.
     * 
     * @return whether new builders will be created when the max pool size is reached
     */
    public boolean getCreateBuildersAtPoolLimit() {
        return createBuildersAtPoolLimit;
    }

    /**
     * Sets whether new builders will be created when the max pool size is reached.
     * 
     * @param createBuilders whether new builders will be created when the max pool size is reached
     */
    public void setCreateBuildersAtPoolLimit(boolean createBuilders) {
        createBuildersAtPoolLimit = createBuilders;
    }

    /**
     * Gets the builder attributes used when creating builders. This collection is unmodifiable.
     * 
     * @return builder attributes used when creating builders
     */
    public Map<String, Object> getBuilderAttributes() {
        return Collections.unmodifiableMap(builderAttributes);
    }

    /**
     * Sets the builder attributes used when creating builders.
     * 
     * @param newAttributes builder attributes used when creating builders
     */
    public void setBuilderAttributes(Map<String, Object> newAttributes) {
        builderAttributes = newAttributes;
        dirtyBuilderConfiguration = true;
    }

    /**
     * Gets whether the builders are coalescing.
     * 
     * @return whether the builders are coalescing
     */
    public boolean isCoalescing() {
        return coalescing;
    }

    /**
     * Sets whether the builders are coalescing.
     * 
     * @param isCoalescing whether the builders are coalescing
     */
    public void setCoalescing(boolean isCoalescing) {
        coalescing = isCoalescing;
        dirtyBuilderConfiguration = true;
    }

    /**
     * Gets whether builders expand entity references.
     * 
     * @return whether builders expand entity references
     */
    public boolean isExpandEntityReferences() {
        return expandEntityReferences;
    }

    /**
     * Sets whether builders expand entity references.
     * 
     * @param expand whether builders expand entity references
     */
    public void setExpandEntityReferences(boolean expand) {
        expandEntityReferences = expand;
        dirtyBuilderConfiguration = true;
    }

    /**
     * Gets the builders' features. This collection is unmodifiable.
     * 
     * @return the builders' features
     */
    public Map<String, Boolean> getBuilderFeatures() {
        return Collections.unmodifiableMap(builderFeatures);
    }

    /**
     * Sets the the builders' features.
     * 
     * @param newFeatures the builders' features
     */
    public void setBuilderFeatures(Map<String, Boolean> newFeatures) {
        builderFeatures = newFeatures;
        dirtyBuilderConfiguration = true;
    }

    /**
     * Gets whether the builders ignore comments.
     * 
     * @return whether the builders ignore comments
     */
    public boolean getIgnoreComments() {
        return ignoreComments;
    }

    /**
     * Sets whether the builders ignore comments.
     * 
     * @param ignore The ignoreComments to set.
     */
    public void setIgnoreComments(boolean ignore) {
        ignoreComments = ignore;
        dirtyBuilderConfiguration = true;
    }

    /**
     * Get whether the builders ignore element content whitespace.
     * 
     * @return whether the builders ignore element content whitespace
     */
    public boolean isIgnoreElementContentWhitespace() {
        return ignoreElementContentWhitespace;
    }

    /**
     * Sets whether the builders ignore element content whitespace.
     * 
     * @param ignore whether the builders ignore element content whitespace
     */
    public void setIgnoreElementContentWhitespace(boolean ignore) {
        ignoreElementContentWhitespace = ignore;
        dirtyBuilderConfiguration = true;
    }

    /**
     * Gets whether the builders are namespace aware.
     * 
     * @return whether the builders are namespace aware
     */
    public boolean isNamespaceAware() {
        return namespaceAware;
    }

    /**
     * Sets whether the builders are namespace aware.
     * 
     * @param isNamespaceAware whether the builders are namespace aware
     */
    public void setNamespaceAware(boolean isNamespaceAware) {
        namespaceAware = isNamespaceAware;
        dirtyBuilderConfiguration = true;
    }

    /** {@inheritDoc} */
    public Schema getSchema() {
        return schema;
    }

    /** {@inheritDoc} */
    public void setSchema(Schema newSchema) {
        schema = newSchema;
        if (schema != null) {
            setNamespaceAware(true);
            builderAttributes.remove("http://java.sun.com/xml/jaxp/properties/schemaSource");
            builderAttributes.remove("http://java.sun.com/xml/jaxp/properties/schemaLanguage");
        }

        dirtyBuilderConfiguration = true;
    }

    /**
     * Gets whether the builders are validating.
     * 
     * @return whether the builders are validating
     */
    public boolean isDTDValidating() {
        return dtdValidating;
    }

    /**
     * Sets whether the builders are validating.
     * 
     * @param isValidating whether the builders are validating
     */
    public void setDTDValidating(boolean isValidating) {
        dtdValidating = isValidating;
        dirtyBuilderConfiguration = true;
    }

    /**
     * Gets whether the builders are XInclude aware.
     * 
     * @return whether the builders are XInclude aware
     */
    public boolean isXincludeAware() {
        return xincludeAware;
    }

    /**
     * Sets whether the builders are XInclude aware.
     * 
     * @param isXIncludeAware whether the builders are XInclude aware
     */
    public void setXincludeAware(boolean isXIncludeAware) {
        xincludeAware = isXIncludeAware;
        dirtyBuilderConfiguration = true;
    }

    /**
     * Gets the current pool version.
     * 
     * @return current pool version
     */
    protected long getPoolVersion() {
        return poolVersion;
    }

    /**
     * Initializes the pool with a new set of configuration options.
     * 
     * @throws XMLParserException thrown if there is a problem initialzing the pool
     */
    protected synchronized void initializePool() throws XMLParserException {
        if (!dirtyBuilderConfiguration) {
            // in case the pool was initialized by some other thread
            return;
        }

        try {
            DocumentBuilderFactory newFactory = DocumentBuilderFactory.newInstance();

            for (Map.Entry<String, Object> attribute : builderAttributes.entrySet()) {
                newFactory.setAttribute(attribute.getKey(), attribute.getValue());
            }

            for (Map.Entry<String, Boolean> feature : builderFeatures.entrySet()) {
                if (feature.getKey() != null) {
                    newFactory.setFeature(feature.getKey(), feature.getValue().booleanValue());
                }
            }

            newFactory.setCoalescing(coalescing);
            newFactory.setExpandEntityReferences(expandEntityReferences);
            newFactory.setIgnoringComments(ignoreComments);
            newFactory.setIgnoringElementContentWhitespace(ignoreElementContentWhitespace);
            newFactory.setNamespaceAware(namespaceAware);
            newFactory.setSchema(schema);
            newFactory.setValidating(dtdValidating);
            newFactory.setXIncludeAware(xincludeAware);

            synchronized (this) {
                poolVersion++;
                dirtyBuilderConfiguration = false;
                builderFactory = newFactory;
                builderPool.clear();
            }
        } catch (ParserConfigurationException e) {
            throw new XMLParserException("Unable to configure builder factory", e);
        }
    }

    /**
     * Creates a new document builder.
     * 
     * @return newly created document builder
     * 
     * @throws XMLParserException thrown if their is a configuration error with the builder factory
     */
    protected DocumentBuilder createBuilder() throws XMLParserException {
        try {
            DocumentBuilder builder = builderFactory.newDocumentBuilder();

            if (entityResolver != null) {
                builder.setEntityResolver(entityResolver);
            }

            if (errorHandler != null) {
                builder.setErrorHandler(errorHandler);
            }

            return builder;
        } catch (ParserConfigurationException e) {
            log.error("Unable to create new document builder", e);
            throw new XMLParserException("Unable to create new document builder", e);
        }
    }

    /**
     * A proxy that prevents the manages document builders retrieved from the parser pool.
     */
    protected class DocumentBuilderProxy extends DocumentBuilder {

        /** Builder being proxied. */
        private DocumentBuilder builder;

        /** Pool that owns this parser. */
        private ParserPool owningPool;

        /** Version of the pool when this proxy was created. */
        private long owningPoolVersion;

        /**
         * Constructor.
         * 
         * @param target document builder to proxy
         * @param owner the owning pool
         */
        public DocumentBuilderProxy(DocumentBuilder target, BasicParserPool owner) {
            owningPoolVersion = owner.getPoolVersion();
            owningPool = owner;
            builder = target;
        }

        /** {@inheritDoc} */
        public DOMImplementation getDOMImplementation() {
            return builder.getDOMImplementation();
        }

        /** {@inheritDoc} */
        public Schema getSchema() {
            return builder.getSchema();
        }

        /** {@inheritDoc} */
        public boolean isNamespaceAware() {
            return builder.isNamespaceAware();
        }

        /** {@inheritDoc} */
        public boolean isValidating() {
            return builder.isValidating();
        }

        /** {@inheritDoc} */
        public boolean isXIncludeAware() {
            return builder.isXIncludeAware();
        }

        /** {@inheritDoc} */
        public Document newDocument() {
            return builder.newDocument();
        }

        /** {@inheritDoc} */
        public Document parse(File f) throws SAXException, IOException {
            return builder.parse(f);
        }

        /** {@inheritDoc} */
        public Document parse(InputSource is) throws SAXException, IOException {
            return builder.parse(is);
        }

        /** {@inheritDoc} */
        public Document parse(InputStream is) throws SAXException, IOException {
            return builder.parse(is);
        }

        /** {@inheritDoc} */
        public Document parse(InputStream is, String systemId) throws SAXException, IOException {
            return builder.parse(is, systemId);
        }

        /** {@inheritDoc} */
        public Document parse(String uri) throws SAXException, IOException {
            return builder.parse(uri);
        }

        /** {@inheritDoc} */
        public void reset() {
            // ignore, entity resolver and error handler can't be changed
        }

        /** {@inheritDoc} */
        public void setEntityResolver(EntityResolver er) {
            return;
        }

        /** {@inheritDoc} */
        public void setErrorHandler(ErrorHandler eh) {
            return;
        }

        /**
         * Gets the pool that owns this parser.
         * 
         * @return pool that owns this parser
         */
        protected ParserPool getOwningPool() {
            return owningPool;
        }

        /**
         * Gets the version of the pool that owns this parser at the time of the proxy's creation.
         * 
         * @return version of the pool that owns this parser at the time of the proxy's creation
         */
        protected long getPoolVersion() {
            return owningPoolVersion;
        }

        /**
         * Gets the proxied document builder.
         * 
         * @return proxied document builder
         */
        protected DocumentBuilder getProxiedBuilder() {
            return builder;
        }

        /** {@inheritDoc} */
        protected void finalize() throws Throwable {
            super.finalize();
            owningPool.returnBuilder(this);
        }
    }
}