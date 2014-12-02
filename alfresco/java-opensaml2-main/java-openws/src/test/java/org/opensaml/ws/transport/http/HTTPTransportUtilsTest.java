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

package org.opensaml.ws.transport.http;

import junit.framework.TestCase;

/**
 *
 */
public class HTTPTransportUtilsTest extends TestCase {
    
    public void testGetRawQueryParameter() {
        String queryString;
        String expected = "Foo=Bar";
        
        queryString = "ABC=123&Foo=Bar&XYZ=456";
        assertEquals(HTTPTransportUtils.getRawQueryStringParameter(queryString, "Foo"), expected);
        
        queryString = "Foo=Bar&XYZ=456";
        assertEquals(HTTPTransportUtils.getRawQueryStringParameter(queryString, "Foo"), expected);
        
        queryString = "ABC=123&Foo=Bar";
        assertEquals(HTTPTransportUtils.getRawQueryStringParameter(queryString, "Foo"), expected);
        
        queryString = "Foo=Bar";
        assertEquals(HTTPTransportUtils.getRawQueryStringParameter(queryString, "Foo"), expected);
        
        queryString = "ABC=123&Foo=Bar&XYZ456";
        assertNull(HTTPTransportUtils.getRawQueryStringParameter(queryString, "NotThere"));
        
        queryString = "ABC=123&XYZ456";
        assertNull(HTTPTransportUtils.getRawQueryStringParameter(queryString, "Foo"));
        
        queryString = null;
        assertNull(HTTPTransportUtils.getRawQueryStringParameter(queryString, "Foo"));
        
    }

}
