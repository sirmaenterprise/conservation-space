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
 * Test the NamedKeyInfoGeneratorFactory manager.
 */
public class NamedKeyInfoGeneratorManagerTest extends XMLObjectBaseTestCase {
    private NamedKeyInfoGeneratorManager manager;
    
    private BasicKeyInfoGeneratorFactory basicFactoryFoo;
    private BasicKeyInfoGeneratorFactory basicFactoryFoo2;
    private BasicKeyInfoGeneratorFactory basicFactoryBar;
    private X509KeyInfoGeneratorFactory x509FactoryFoo;
    private X509KeyInfoGeneratorFactory x509FactoryBar;
    
    private String nameFoo = "FOO";
    private String nameBar = "BAR";
    
    /** {@inheritDoc} */
    protected void setUp() throws Exception {
        super.setUp();
        manager = new NamedKeyInfoGeneratorManager();
        basicFactoryFoo = new BasicKeyInfoGeneratorFactory();
        basicFactoryFoo2 = new BasicKeyInfoGeneratorFactory();
        basicFactoryBar = new BasicKeyInfoGeneratorFactory();
        x509FactoryFoo = new X509KeyInfoGeneratorFactory();
        x509FactoryBar = new X509KeyInfoGeneratorFactory();
        
    }
    
    /** Test factory registration. */
    public void testRegister() {
        manager.registerFactory(nameFoo, basicFactoryFoo);
        manager.registerFactory(nameFoo, x509FactoryFoo);
        
        KeyInfoGeneratorManager fooManager = manager.getManager(nameFoo);
        assertNotNull("Expected named manager not present/created", fooManager);
        assertEquals("Unexpected # of managed factories", 2, fooManager.getFactories().size());
        
        assertTrue("Expected factory not found", fooManager.getFactories().contains(basicFactoryFoo));
        assertTrue("Expected factory not found", fooManager.getFactories().contains(x509FactoryFoo));
        
        // basicFactoryFoo2 should replace basicFactoryFoo
        manager.registerFactory(nameFoo, basicFactoryFoo2);
        assertFalse("Unexpected factory found", fooManager.getFactories().contains(basicFactoryFoo));
        assertTrue("Expected factory not found", fooManager.getFactories().contains(basicFactoryFoo2));
    }
    
    /** Test factory de-registration. */
    public void testDeregister() {
        manager.registerFactory(nameFoo, basicFactoryFoo);
        manager.registerFactory(nameFoo, x509FactoryFoo);
        
        KeyInfoGeneratorManager fooManager = manager.getManager(nameFoo);
        assertNotNull("Expected named manager not present/created", fooManager);
        assertEquals("Unexpected # of managed factories", 2, fooManager.getFactories().size());
        
        manager.deregisterFactory(nameFoo, x509FactoryFoo);
        assertTrue("Expected factory not found", fooManager.getFactories().contains(basicFactoryFoo));
        assertFalse("Unexpected factory found", fooManager.getFactories().contains(x509FactoryFoo));
        
        try {
            manager.deregisterFactory("BAZ", x509FactoryFoo);
            fail("Use of non-existent manager name should have caused an exception");
        } catch (IllegalArgumentException e) {
            // do nothing, should fail
        }        
    }
    
    /** Test access to manager names, and that can not be modified. */
    public void testGetManagerNames() {
        Collection<String> names = manager.getManagerNames();
        assertTrue("Names was not empty", names.isEmpty());
        
        manager.registerFactory(nameFoo, basicFactoryFoo);
        manager.registerFactory(nameBar, basicFactoryBar);
        names = manager.getManagerNames();
        assertEquals("Unexpected # of manager names", 2, names.size());
        
        assertTrue("Expected manager name not found", names.contains(nameFoo));
        assertTrue("Expected manager name not found", names.contains(nameBar));
        
        try {
            names.remove(basicFactoryFoo);
            fail("Returned names set should be unmodifiable");
        } catch (UnsupportedOperationException e) {
            // do nothing, should fail
        }        
        
    }
    
    /** Test that obtaining a manager by name works. */
    public void testGetManagerByName() {
        Collection<String> names = manager.getManagerNames();
        manager.registerFactory(nameFoo, basicFactoryFoo);
        manager.registerFactory(nameBar, basicFactoryBar);
        assertEquals("Unexpected # of manager names", 2, names.size());
        
        assertNotNull("Failed to find manager by name", manager.getManager(nameFoo));
        assertNotNull("Failed to find manager by name", manager.getManager(nameBar));
        
        assertFalse("Non-existent manager name found in name set", names.contains("BAZ"));
        assertNotNull("Failed to create new manager", manager.getManager("BAZ"));
        assertTrue("Expected manager name not found", names.contains("BAZ"));
    }
    
    /** Remove a manager by name. */
    public void testRemoveManagerByName() {
        Collection<String> names = manager.getManagerNames();
        manager.registerFactory(nameFoo, basicFactoryFoo);
        manager.registerFactory(nameFoo, x509FactoryFoo);
        manager.registerFactory(nameBar, basicFactoryBar);
        assertEquals("Unexpected # of manager names", 2, names.size());
        
        assertNotNull("Failed to find manager by name", manager.getManager(nameFoo));
        assertNotNull("Failed to find manager by name", manager.getManager(nameBar));
        assertTrue("Expected manager name not found", names.contains(nameFoo));
        assertTrue("Expected manager name not found", names.contains(nameBar));
        
        manager.removeManager(nameFoo);
        assertEquals("Unexpected # of manager names", 1, names.size());
        assertNotNull("Failed to find manager by name", manager.getManager(nameBar));
        assertFalse("Unexpected manager name found", names.contains(nameFoo));
        assertTrue("Expected manager name not found", names.contains(nameBar));
    }
    
    /** Test registering a factory in the default unnamed manager. */
    public void testRegisterDefaultFactory() {
        KeyInfoGeneratorManager defaultManager = manager.getDefaultManager();
        assertEquals("Unexpected # of default factories", 0, defaultManager.getFactories().size());
        manager.registerDefaultFactory(basicFactoryFoo);
        manager.registerDefaultFactory(x509FactoryFoo);
        assertEquals("Unexpected # of default factories", 2, defaultManager.getFactories().size());
    }
    
    /** Test de-registering a factory in the default unnamed manager. */
    public void testDeregisterDefaultFactory() {
        KeyInfoGeneratorManager defaultManager = manager.getDefaultManager();
        assertEquals("Unexpected # of default factories", 0, defaultManager.getFactories().size());
        manager.registerDefaultFactory(basicFactoryFoo);
        manager.registerDefaultFactory(x509FactoryFoo);
        assertEquals("Unexpected # of default factories", 2, defaultManager.getFactories().size());
        
        manager.deregisterDefaultFactory(x509FactoryFoo);
        assertEquals("Unexpected # of default factories", 1, defaultManager.getFactories().size());
    }
    
    /** Test lookup of factory from manager based on a credential instance. */
    public void testLookupFactory() {
        manager.registerFactory(nameFoo, basicFactoryFoo);
        manager.registerFactory(nameFoo, x509FactoryFoo);
        manager.registerFactory(nameBar, basicFactoryBar);
        manager.registerFactory(nameBar, x509FactoryBar);
        manager.getManager("BAZ");
        assertEquals("Unexpected # of managed factories", 2, manager.getManager(nameFoo).getFactories().size());
        assertEquals("Unexpected # of managed factories", 2, manager.getManager(nameBar).getFactories().size());
        assertEquals("Unexpected # of managed factories", 0, manager.getManager("BAZ").getFactories().size());
        assertEquals("Unexpected # of manager names", 3, manager.getManagerNames().size());
        
        Credential basicCred = new BasicCredential();
        X509Credential x509Cred = new BasicX509Credential();
        
        assertNotNull("Failed to find factory based on manager name and credential", 
                manager.getFactory(nameFoo, basicCred));
        assertTrue("Found incorrect factory based on name and credential", 
                basicFactoryFoo == manager.getFactory(nameFoo, basicCred));
        
        assertNotNull("Failed to find factory based on manager name and credential", 
                manager.getFactory(nameFoo, x509Cred));
        assertTrue("Found incorrect factory based on name and credential", 
                x509FactoryFoo == manager.getFactory(nameFoo, x509Cred));
        
        assertNotNull("Failed to find factory based on manager name and credential", 
                manager.getFactory(nameBar, x509Cred));
        assertTrue("Found incorrect factory based on name and credential", 
                x509FactoryBar == manager.getFactory(nameBar, x509Cred));
        
        assertNull("Found non-existent factory based on name and credential", 
                manager.getFactory("BAZ", x509Cred));
        try {
            manager.getFactory("ABC123", x509Cred);
            fail("Use of non-existent manager name should have caused an exception");
        } catch (IllegalArgumentException e) {
            // do nothing, should fail
        }        
    }
    
    /** Test proper functioning of option to use the default manager for unnamed factories. */
    public void testFallThroughToDefaultManager() {
        KeyInfoGeneratorFactory defaultX509Factory = new X509KeyInfoGeneratorFactory();
        manager.registerDefaultFactory(defaultX509Factory);
        manager.registerFactory(nameFoo, basicFactoryFoo);
        
        X509Credential x509Cred = new BasicX509Credential();
        
        manager.setUseDefaultManager(true);
        
        assertNotNull("Failed to find factory based on manager name and credential", 
                manager.getFactory(nameFoo, x509Cred));
        assertTrue("Found incorrect factory based on name and credential", 
                defaultX509Factory == manager.getFactory(nameFoo, x509Cred));
        
        manager.setUseDefaultManager(false);
        assertNull("Found factory in default manager even though useDefaultManager option set to false",
                manager.getFactory(nameFoo, x509Cred));
    }
}
