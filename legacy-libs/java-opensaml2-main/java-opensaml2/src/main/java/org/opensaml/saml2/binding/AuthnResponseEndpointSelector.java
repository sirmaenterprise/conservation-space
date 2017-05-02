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

package org.opensaml.saml2.binding;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.opensaml.common.binding.BasicEndpointSelector;
import org.opensaml.saml2.core.AuthnRequest;
import org.opensaml.saml2.metadata.Endpoint;
import org.opensaml.saml2.metadata.IndexedEndpoint;
import org.opensaml.xml.util.DatatypeHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An endpoint selector that implements the additional selection constraints described within the SAML 2.0 AuthnRequest
 * specification. If an endpoint can not be resolved using either the information within the assertion consumer service
 * index or the assertion consumer service URL given in the authentication request, or if this information isn't
 * present, than the rules for the {@link BasicEndpointSelector} are used.
 */
public class AuthnResponseEndpointSelector extends BasicEndpointSelector {

    /** Class logger. */
    private final Logger log = LoggerFactory.getLogger(AuthnResponseEndpointSelector.class);

    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    public Endpoint selectEndpoint() {
        if (getEntityRoleMetadata() == null) {
            log.debug("Unable to select endpoint, no entity role metadata available.");
            return null;
        }

        List<? extends Endpoint> endpoints = getEntityRoleMetadata().getEndpoints(getEndpointType());
        if (endpoints == null || endpoints.size() == 0) {
            return null;
        }

        Endpoint endpoint = null;
        if (getSamlRequest() != null) {
            AuthnRequest request = (AuthnRequest) getSamlRequest();

            endpoints = filterEndpointsByProtocolBinding(endpoints);
            if (endpoints == null || endpoints.isEmpty()) {
                return null;
            }

            if (request.getAssertionConsumerServiceIndex() != null) {
                log.debug("Selecting endpoint by ACS index for request {} from entity {}", request.getID(),
                        getEntityMetadata().getEntityID());
                endpoint = selectEndpointByACSIndex(request, (List<IndexedEndpoint>) endpoints);
            } else if (request.getAssertionConsumerServiceURL() != null) {
                log.debug("Selecting endpoint by ACS URL for request {} from entity {}", request.getID(),
                        getEntityMetadata().getEntityID());
                endpoint = selectEndpointByACSURL(request, (List<IndexedEndpoint>) endpoints);
            }
        }

        if (endpoint == null) {
            if (endpoints.get(0) instanceof IndexedEndpoint) {
                endpoint = selectIndexedEndpoint((List<IndexedEndpoint>) endpoints);
            } else {
                endpoint = selectNonIndexedEndpoint((List<Endpoint>) endpoints);
            }
        }

        return endpoint;
    }

    /**
     * Filters the list of possible endpoints by supported outbound bindings and, if the authentication request contains
     * a requested binding and not an ACS index, that too is used to filter the list.
     * 
     * @param endpoints raw list of endpoints
     * 
     * @return filtered endpoints
     */
    protected List<? extends Endpoint> filterEndpointsByProtocolBinding(List<? extends Endpoint> endpoints) {
        log.debug("Filtering peer endpoints.  Supported peer endpoint bindings: {}", getSupportedIssuerBindings());
        AuthnRequest request = (AuthnRequest) getSamlRequest();

        boolean filterByRequestBinding = false;
        String acsBinding = DatatypeHelper.safeTrimOrNullString(request.getProtocolBinding());
        if (acsBinding != null && request.getAssertionConsumerServiceIndex() != null) {
            filterByRequestBinding = true;
        }

        List<Endpoint> filteredEndpoints = new ArrayList<Endpoint>(endpoints);
        Iterator<Endpoint> endpointItr = filteredEndpoints.iterator();
        Endpoint endpoint;
        while (endpointItr.hasNext()) {
            endpoint = endpointItr.next();
            if (!getSupportedIssuerBindings().contains(endpoint.getBinding())) {
                log.debug("Removing endpoint {} because its binding {} is not supported", endpoint.getLocation(),
                        endpoint.getBinding());
                endpointItr.remove();
                continue;
            }

            if (filterByRequestBinding && !endpoint.getBinding().equals(acsBinding)) {
                log.debug("Removing endpoint {} because its binding {} does not match request's requested binding",
                        endpoint.getLocation(), endpoint.getBinding());
                endpointItr.remove();
            }
        }

        return filteredEndpoints;
    }

    /**
     * Selects the endpoint by way of the assertion consumer service index given in the AuthnRequest.
     * 
     * @param request the AuthnRequest
     * @param endpoints list of endpoints to select from
     * 
     * @return the selected endpoint
     */
    protected Endpoint selectEndpointByACSIndex(AuthnRequest request, List<IndexedEndpoint> endpoints) {
        Integer acsIndex = request.getAssertionConsumerServiceIndex();
        for (IndexedEndpoint endpoint : endpoints) {
            if (endpoint == null || !getSupportedIssuerBindings().contains(endpoint.getBinding())) {
                continue;
            }

            if (endpoint.getIndex() != null && endpoint.getIndex().equals(acsIndex)) {
                return endpoint;
            }
        }

        return null;
    }

    /**
     * Selects the endpoint by way of the assertion consumer service URL given in the AuthnRequest.
     * 
     * @param request the AuthnRequest
     * @param endpoints list of endpoints to select from
     * 
     * @return the selected endpoint
     */
    protected Endpoint selectEndpointByACSURL(AuthnRequest request, List<IndexedEndpoint> endpoints) {
        String acsBinding = DatatypeHelper.safeTrimOrNullString(request.getProtocolBinding());
        if (acsBinding == null) {
            return null;
        }

        for (IndexedEndpoint endpoint : endpoints) {
            if (!getSupportedIssuerBindings().contains(endpoint.getBinding())) {
                continue;
            }

            if (endpoint.getBinding().equals(acsBinding)) {
                if ((endpoint.getLocation() != null && endpoint.getLocation().equals(request.getAssertionConsumerServiceURL()))
                        || (endpoint.getResponseLocation() != null && endpoint.getResponseLocation().equals(request.getAssertionConsumerServiceURL()))) {
                    return endpoint;
                }
            }
        }

        return null;
    }
}