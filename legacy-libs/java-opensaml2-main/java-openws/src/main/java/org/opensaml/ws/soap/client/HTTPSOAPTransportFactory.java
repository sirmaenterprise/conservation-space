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

package org.opensaml.ws.soap.client;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpVersion;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.params.HttpClientParams;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.opensaml.ws.message.decoder.MessageDecoder;
import org.opensaml.ws.message.encoder.MessageEncoder;
import org.opensaml.ws.transport.http.HTTPTransport.HTTP_VERSION;

/**
 * HTTP-based SOAP transport factory.
 */
public class HTTPSOAPTransportFactory implements ClientTransportFactory {

    /** Socket timeout in milliseconds, defaults to 30,000. */
    private int socketTimeout = 30000;

    /** Connection timeout in milliseconds, defaults to 60,000. */
    private int connectionTimeout = 60000;

    /** HTTP version used when connecting, defaults to HTTP 1.1. */
    private HTTP_VERSION httpVersion = HTTP_VERSION.HTTP1_1;

    /** Encoder used to encode messages onto the outgoing transport. */
    private MessageEncoder messageEncoder;

    /** Decoder used to decode message from inbound transport. */
    private MessageDecoder messageDecoder;

    /** Client used by transports. */
    private HttpClient httpClient;

    /**
     * Constructor.
     * 
     * @param encoder encoder used to encode messages onto the outgoing transport
     * @param decoder decoder used to decode messages from inbound transport
     */
    public HTTPSOAPTransportFactory(MessageEncoder encoder, MessageDecoder decoder) {
        messageEncoder = encoder;
        messageDecoder = decoder;
        initializeHttpClient();
    }

    /**
     * Gets the connection timeout in milliseconds.
     * 
     * @return connection timeout in milliseconds
     */
    public int getConnectionTimeout() {
        return connectionTimeout;
    }

    /**
     * Sets the connection timeout in milliseconds.
     * 
     * @param timeout connection timeout in milliseconds
     */
    public void setConnectionTimeout(int timeout) {
        connectionTimeout = timeout;
    }

    /**
     * Gets the HTTP version used when connecting to peers.
     * 
     * @return HTTP version used when connecting to peers
     */
    public HTTP_VERSION getHttpVersion() {
        return httpVersion;
    }

    /**
     * Sets the HTTP version used when connecting to peers.
     * 
     * @param version HTTP version used when connecting to peers
     */
    public void setHttpVersion(HTTP_VERSION version) {
        this.httpVersion = version;
    }

    /**
     * Gets the socket connection timeout in milliseconds.
     * 
     * @return socket connection timeout in milliseconds
     */
    public int getSocketTimeout() {
        return socketTimeout;
    }

    /**
     * Sets the socket connection timeout in milliseconds.
     * 
     * @param timeout socket connection timeout in milliseconds
     */
    public void setSocketTimeout(int timeout) {
        this.socketTimeout = timeout;
    }

    /**
     * Gets the decoder used to decode messages from the inbound transport.
     * 
     * @return decoder used to decode messages from the inbound transport
     */
    public MessageDecoder getMessageDecoder() {
        return messageDecoder;
    }

    /**
     * Gets the encoder used to encode messages to the outbound transport.
     * 
     * @return encoder used to encode messages to the outbound transport
     */
    public MessageEncoder getMessageEncoder() {
        return messageEncoder;
    }

    /** {@inheritDoc} */
    public ClientTransport createTransport() {
        return new HTTPSOAPTransport(httpClient, messageEncoder, messageDecoder);
    }

    /**
     * Initializes the {@link HttpClient} that will be used by the created {@link HTTPSOAPTransport} built by this
     * factory.
     */
    protected void initializeHttpClient() {
        HttpConnectionManagerParams connectionParams = new HttpConnectionManagerParams();
        connectionParams.setConnectionTimeout(connectionTimeout);

        MultiThreadedHttpConnectionManager connectionManager = new MultiThreadedHttpConnectionManager();
        connectionManager.setParams(connectionParams);

        HttpClientParams clientParams = new HttpClientParams();
        clientParams.setSoTimeout(socketTimeout);
        if (httpVersion == HTTP_VERSION.HTTP1_0) {
            clientParams.setVersion(HttpVersion.HTTP_1_0);
        } else {
            clientParams.setVersion(HttpVersion.HTTP_1_1);
        }

        httpClient = new HttpClient(clientParams, connectionManager);
    }
}