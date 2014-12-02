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

import org.opensaml.ws.message.MessageContext;
import org.opensaml.ws.transport.TransportException;

/**
 * Transport used by the {@link SOAPClient} to connect to a peer and send data.
 */
public interface ClientTransport {

    /**
     * Sends the given SOAP message to the provided peer endpoint.
     * 
     * @param endpointURI peer endpoint
     * @param messageContext message context
     * 
     * @throws TransportException thrown if there is a problem sending the message
     */
    public void send(URI endpointURI, MessageContext messageContext) throws TransportException;
}