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

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.opensaml.ws.message.MessageContext;
import org.opensaml.ws.message.decoder.MessageDecodingException;
import org.opensaml.ws.security.SecurityPolicyException;
import org.opensaml.ws.soap.soap11.Envelope;
import org.opensaml.ws.transport.Transport;
import org.opensaml.ws.transport.TransportException;

/**
 * A client for sending and receiving SOAP messages.
 * 
 * When a client sends a message it will create a {@link Transport} instance, based on the endpoint's scheme, marshall
 * and bind the message to the transport, receive, decode, and umarshall the response, evaluate the message security
 * policy, and finally return the response. After this process is complete the response message and transport will be
 * added to the message context.
 */
public class SOAPClient {

    /** Registered transport factories. */
    private HashMap<String, ClientTransportFactory> transportFactories;

    /**
     * Constructor.
     */
    public SOAPClient() {
    }

    /**
     * Gets the transports registered with this client.
     * 
     * @return mutable list of transports registered with this client
     */
    public Map<String, ClientTransportFactory> getRegisteredTransports() {
        return transportFactories;
    }

    /**
     * Sends a SOAP message to the given endpoint.
     * 
     * @param endpointURI endpoint to send the SOAP message to
     * @param messageContext context of the message to send
     * 
     * @throws TransportException thrown if there is a problem creating or using the {@link Transport}
     * @throws MessageDecodingException thrown if there is a problem decoding the response
     * @throws SecurityPolicyException thrown if there is a problem evaluating the decoder's security policy
     */
    public void send(URI endpointURI, MessageContext messageContext) throws TransportException,
            MessageDecodingException, SecurityPolicyException {
        
        if(!(messageContext.getOutboundMessage() instanceof Envelope)){
            throw new TransportException("Outbound message must be a SOAP Envelope");
        }
        
        String transportScheme = endpointURI.getScheme();
        ClientTransportFactory transFactory = transportFactories.get(transportScheme);

        if (transFactory == null) {
            throw new TransportException("No transport registered for URI scheme: " + transportScheme);
        }

        ClientTransport transport = transFactory.createTransport();
        transport.send(endpointURI, messageContext);
    }
}