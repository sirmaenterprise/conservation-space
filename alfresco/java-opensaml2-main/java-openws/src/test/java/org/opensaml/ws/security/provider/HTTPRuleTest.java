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

package org.opensaml.ws.security.provider;

import org.opensaml.ws.security.BaseSecurityPolicyRuleTestCase;
import org.opensaml.ws.transport.InTransport;
import org.opensaml.ws.transport.http.HTTPInTransport;
import org.opensaml.ws.transport.http.HttpServletRequestAdapter;
import org.springframework.mock.web.MockHttpServletRequest;

/**
 * Test the HTTP security policy rule.
 */
public class HTTPRuleTest extends BaseSecurityPolicyRuleTestCase {
    
    private MockHttpServletRequest httpRequest;
    
    private String contentType = "text/html";
    private String method = "POST";
    private boolean requireSecured = true;

    /** {@inheritDoc} */
    protected void setUp() throws Exception {
        super.setUp();
        
        rule = new HTTPRule(contentType, method, requireSecured);
    }
    
    /** {@inheritDoc} */
    protected MockHttpServletRequest buildServletRequest() {
        MockHttpServletRequest request =  new MockHttpServletRequest();
        request.setContentType(contentType);
        request.setMethod(method);
        request.setSecure(requireSecured);
        return request;
    }

    /** {@inheritDoc} */
    protected InTransport buildInTransport() {
        httpRequest = buildServletRequest();
        HTTPInTransport inTransport = new HttpServletRequestAdapter(httpRequest);
        return inTransport;
    }

    /**
     * Test all parameters valid.
     */
    public void testAllGood() {
        assertRuleSuccess("All request parameters are valid");
    }

    /**
     * Bad request content type.
     */
    public void testContentTypeBad() {
        httpRequest.setContentType("GARBAGE");
        assertRuleFailure("Invalid content type");
    }

    /**
     * Bad request method.
     */
    public void testRequestMethodBad() {
        httpRequest.setMethod("GARBAGE");
        assertRuleFailure("Invalid request method");
    }
    
    /**
     * Bad request secure flag.
     */
    public void testRequireSecureBad() {
        httpRequest.setSecure(!requireSecured);
        assertRuleFailure("Invalid secure flag");
    }
}
