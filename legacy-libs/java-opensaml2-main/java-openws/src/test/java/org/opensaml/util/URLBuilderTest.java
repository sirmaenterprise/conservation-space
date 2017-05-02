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

package org.opensaml.util;

import junit.framework.TestCase;

/**
 * Tests building and parsing URLs with the builder.
 */
public class URLBuilderTest extends TestCase {
    
    /**
     * Test with scheme and host.
     */
    public void testURLBuilder1(){
        String url = "http://www.example.com";
        URLBuilder builder1 = new URLBuilder(url);
        assertEquals("http", builder1.getScheme());
        assertEquals(null, builder1.getUsername());
        assertEquals(null, builder1.getPassword());
        assertEquals("www.example.com", builder1.getHost());
        assertEquals(-1, builder1.getPort());
        assertEquals(null, builder1.getPath());
        assertEquals(0, builder1.getQueryParams().size());
        assertEquals(null, builder1.getFragment());
        
        assertEquals(url, builder1.buildURL());
    }
    
    /**
     * Test with scheme, host, and path.
     */
    public void testURLBuilder2(){
        String url = "https://www.example.com/foo/index.html";
        URLBuilder builder1 = new URLBuilder(url);
        assertEquals("https", builder1.getScheme());
        assertEquals(null, builder1.getUsername());
        assertEquals(null, builder1.getPassword());
        assertEquals("www.example.com", builder1.getHost());
        assertEquals(-1, builder1.getPort());
        assertEquals("/foo/index.html", builder1.getPath());
        assertEquals(0, builder1.getQueryParams().size());
        assertEquals(null, builder1.getFragment());
        
        assertEquals(url, builder1.buildURL());
    }
    
    /**
     * Test with scheme, host, port, path, and query params.
     */
    public void testURLBuilder3(){
        String url = "http://www.example.com:8080/index.html?attrib1=value1&attrib2=value&attrib3";
        URLBuilder builder1 = new URLBuilder(url);
        assertEquals("http", builder1.getScheme());
        assertEquals(null, builder1.getUsername());
        assertEquals(null, builder1.getPassword());
        assertEquals("www.example.com", builder1.getHost());
        assertEquals(8080, builder1.getPort());
        assertEquals("/index.html", builder1.getPath());
        assertEquals(3, builder1.getQueryParams().size());
        assertEquals(null, builder1.getFragment());
        
        assertEquals(url, builder1.buildURL());
    }
    
    /**
     * Test with scheme, host, and fragment.
     */
    public void testURLBuilder4(){
        String url = "https://www.example.com#anchor";
        URLBuilder builder1 = new URLBuilder(url);
        assertEquals("https", builder1.getScheme());
        assertEquals(null, builder1.getUsername());
        assertEquals(null, builder1.getPassword());
        assertEquals("www.example.com", builder1.getHost());
        assertEquals(-1, builder1.getPort());
        assertEquals(null, builder1.getPath());
        assertEquals(0, builder1.getQueryParams().size());
        assertEquals("anchor", builder1.getFragment());
        
        assertEquals(url, builder1.buildURL());
    }

    /**
     * Test with scheme, host, port, path, query params, and anchor.
     */
    public void testURLBuilder5(){
        String url = "http://www.example.com/index.html?attrib1=value1&attrib2=value&attrib3#anchor";
        URLBuilder builder1 = new URLBuilder(url);
        assertEquals("http", builder1.getScheme());
        assertEquals(null, builder1.getUsername());
        assertEquals(null, builder1.getPassword());
        assertEquals("www.example.com", builder1.getHost());
        assertEquals(-1, builder1.getPort());
        assertEquals("/index.html", builder1.getPath());
        assertEquals(3, builder1.getQueryParams().size());
        assertEquals("anchor", builder1.getFragment());
        
        assertEquals(url, builder1.buildURL());
    }
    
    /**
     * Test with scheme, username, password, and host.
     */
    public void testURLBuilder6(){
        String url = "http://user:pass@www.example.com";
        URLBuilder builder1 = new URLBuilder(url);
        assertEquals("http", builder1.getScheme());
        assertEquals("user", builder1.getUsername());
        assertEquals("pass", builder1.getPassword());
        assertEquals("www.example.com", builder1.getHost());
        assertEquals(-1, builder1.getPort());
        assertEquals(null, builder1.getPath());
        assertEquals(0, builder1.getQueryParams().size());
        assertEquals(null, builder1.getFragment());
        
        assertEquals(url, builder1.buildURL());
    }
    
    /**
     * Test with scheme, username, and host.
     */
    public void testURLBuilder7(){
        String url = "http://user@www.example.com";
        URLBuilder builder1 = new URLBuilder(url);
        assertEquals("http", builder1.getScheme());
        assertEquals("user", builder1.getUsername());
        assertEquals(null, builder1.getPassword());
        assertEquals("www.example.com", builder1.getHost());
        assertEquals(-1, builder1.getPort());
        assertEquals(null, builder1.getPath());
        assertEquals(0, builder1.getQueryParams().size());
        assertEquals(null, builder1.getFragment());
        
        assertEquals(url, builder1.buildURL());
    }
}