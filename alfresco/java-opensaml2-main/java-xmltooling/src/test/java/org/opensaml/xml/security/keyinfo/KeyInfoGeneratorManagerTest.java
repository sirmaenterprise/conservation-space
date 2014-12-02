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

package org.opensaml.xml.security.keyinfo;

import java.util.Collection;

import org.opensaml.xml.XMLObjectBaseTestCase;
import org.opensaml.xml.security.credential.BasicCredential;
import org.opensaml.xml.security.credential.BasicKeyInfoGeneratorFactory;
import org.opensaml.xml.security.credential.Credential;
import org.opensaml.xml.security.x509.BasicX509Credential;
import org.opensaml.xml.security.x509.X509Credential;
import org.opensaml.xml.security.x509.X509KeyInfoGeneratorFactory;

/**
 * Test the KeyInfoGeneratorFactory manager.
 */
public class KeyInfoGeneratorManagerTest extends XMLObjectBaseTestCase {
    
    private KeyInfoGeneratorManager manager;
    
    private BasicKeyInfoGeneratorFactory basicFactory;
    private BasicKeyInfoGeneratorFactory basicFactory2;
    private X509KeyInfoGeneratorFactory x509Factory;
    private X509KeyInfoGeneratorFactory x509Factory2;
    
    /** {@inheritDoc} */
    protected void setUp() throws Exception {
        super.setUp();
        manager = new KeyInfoGeneratorManager();
        basicFactory = new BasicKeyInfoGeneratorFactory();
        basicFactory2 = new BasicKeyInfoGeneratorFactory();
        x509Factory = new X509KeyInfoGeneratorFactory();
        x509Factory2 = new X509KeyInfoGeneratorFactory();
        
    }

    /** Test factory registration. */
    public void testRegister() {
        assertEquals("Unexpected # of managed factories", 0, manager.getFactories().size());
        
        manager.registerFactory(basicFactory);
        assertEquals("Unexpected # of managed factories", 1, manager.getFactories().size());
        assertTrue("Expected factory not found", manager.getFactories().contains(basicFactory));
        
        manager.registerFactory(x509Factory);
        assertEquals("Unexpected # of managed factories", 2, manager.getFactories().size());
        assertTrue("Expected factory not found", manager.getFactories().contains(x509Factory));
        
        // basicFactory2 should replace basicFactory
        manager.registerFactory(basicFactory2);
        assertEquals("Unexpected # of managed factories", 2, manager.getFactories().size());
        assertTrue("Expected factory not found", manager.getFactories().contains(basicFactory2));
        assertFalse("Unexpected factory found", manager.getFactories().contains(basicFactory));
    }
    
    /** Test factory de-registration. */
    public void testDeregister() {
        assertEquals("Unexpected # of managed factories", 0, manager.getFactories().size());
        
        manager.registerFactory(basicFactory);
        manager.registerFactory(x509Factory);
        assertEquals("Unexpected # of managed factories", 2, manager.getFactories().size());
        assertTrue("Expected factory not found", manager.getFactories().contains(basicFactory));
        assertTrue("Expected factory not found", manager.getFactories().contains(x509Factory));
        
        manager.deregisterFactory(x509Factory);
        assertEquals("Unexpected # of managed factories", 1, manager.getFactories().size());
        assertTrue("Expected factory not found", manager.getFactories().contains(basicFactory));
        assertFalse("Unexpected factory found", manager.getFactories().contains(x509Factory));
        
        manager.deregisterFactory(basicFactory);
        assertEquals("Unexpected # of managed factories", 0, manager.getFactories().size());
        assertFalse("Unexpected factory found", manager.getFactories().contains(basicFactory));
        assertFalse("Unexpected factory found", manager.getFactories().contains(x509Factory));
    }
    
    /** Test that getFactories() works, and is unmodifiable. */
    public void testGetFactories() {
        manager.registerFactory(basicFactory);
        manager.registerFactory(x509Factory);
        assertEquals("Unexpected # of managed factories", 2, manager.getFactories().size());
        
        Collection<KeyInfoGeneratorFactory> factories = manager.getFactories();
        
        try {
            factories.remove(basicFactory);
            fail("Returned factory collection should be unmodifiable");
        } catch (UnsupportedOperationException e) {
            // do nothing, should fail
        }        
    }
    
    /** Test lookup of factory from manager based on a credential instance. */
    public void testLookupFactory() {
        manager.registerFactory(basicFactory);
        manager.registerFactory(x509Factory);
        assertEquals("Unexpected # of managed factories", 2, manager.getFactories().size());
        
        Credential basicCred = new BasicCredential();
        X509Credential x509Cred = new BasicX509Credential();
        
        assertNotNull("Failed to find factory based on credential", manager.getFactory(basicCred));
        assertTrue("Found incorrect factory based on credential", basicFactory == manager.getFactory(basicCred));
        
        assertNotNull("Failed to find factory based on credential", manager.getFactory(x509Cred));
        assertTrue("Found incorrect factory based on credential", x509Factory == manager.getFactory(x509Cred));
        
        manager.registerFactory(x509Factory2);
        assertNotNull("Failed to find factory based on credential", manager.getFactory(x509Cred));
        assertTrue("Found incorrect factory based on credential", x509Factory2 == manager.getFactory(x509Cred));
    }

}
