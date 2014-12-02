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

package org.opensaml.xml.security.credential;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import junit.framework.TestCase;

import org.opensaml.xml.security.CriteriaSet;
import org.opensaml.xml.security.SecurityException;

/**
 * Testing the chaining credential resolver.
 */
public class ChainingCredentialResolverTest extends TestCase {
    
    private ChainingCredentialResolver chainingResolver;
    private CriteriaSet criteriaSet;
    
    private StaticCredentialResolver staticResolver12, staticResolver3, staticResolver45, staticResolverEmpty;
    private Credential cred1, cred2, cred3, cred4, cred5;
    
    
    /** Constructor. */
    public ChainingCredentialResolverTest() {
    }

    /** {@inheritDoc} */
    protected void setUp() throws Exception {
        super.setUp();
        
        cred1 = new BasicCredential();
        cred2 = new BasicCredential();
        cred3 = new BasicCredential();
        cred4 = new BasicCredential();
        cred5 = new BasicCredential();
        
        criteriaSet = new CriteriaSet();
        
        chainingResolver = new ChainingCredentialResolver();
        
        ArrayList<Credential> temp;
        
        temp  = new ArrayList<Credential>();
        temp.add(cred1);
        temp.add(cred2);
        staticResolver12 = new StaticCredentialResolver(temp);
        
        temp  = new ArrayList<Credential>();
        temp.add(cred3);
        staticResolver3 = new StaticCredentialResolver(temp);
        
        temp  = new ArrayList<Credential>();
        temp.add(cred4);
        temp.add(cred5);
        staticResolver45 = new StaticCredentialResolver(temp);
        
        temp = new ArrayList<Credential>();
        staticResolverEmpty = new StaticCredentialResolver(temp);
    }
    
    /**
     * Test a single chain member, which returns no credentials.
     * @throws SecurityException
     */
    public void testOneEmptyMember() throws SecurityException {
        chainingResolver.getResolverChain().add(staticResolverEmpty);
        
        List<Credential> resolved = getResolved(chainingResolver.resolve(criteriaSet));
        checkResolved(resolved, 0);
    }
    
    /**
     * Test multiple chain members, all of which return no credentials.
     * @throws SecurityException
     */
    public void testMultipleEmptyMember() throws SecurityException {
        chainingResolver.getResolverChain().add(staticResolverEmpty);
        chainingResolver.getResolverChain().add(staticResolverEmpty);
        chainingResolver.getResolverChain().add(staticResolverEmpty);
        
        List<Credential> resolved = getResolved(chainingResolver.resolve(criteriaSet));
        checkResolved(resolved, 0);
    }
    
    /**
     * Test one chain member, returning credentials.
     * @throws SecurityException
     */
    public void testOneMember() throws SecurityException {
        chainingResolver.getResolverChain().add(staticResolver12);
        
        List<Credential> resolved = getResolved(chainingResolver.resolve(criteriaSet));
        checkResolved(resolved, 2, cred1, cred2);
    }
    
    /**
     * Test multiple chain members, returning credentials.
     * @throws SecurityException
     */
    public void testMultipleMembers() throws SecurityException {
        chainingResolver.getResolverChain().add(staticResolver12);
        chainingResolver.getResolverChain().add(staticResolver3);
        chainingResolver.getResolverChain().add(staticResolverEmpty);
        chainingResolver.getResolverChain().add(staticResolver45);
        
        List<Credential> resolved = getResolved(chainingResolver.resolve(criteriaSet));
        checkResolved(resolved, 5, cred1, cred2, cred3, cred4, cred5);
    }
    
    /**
     * Test that order of returned credentials is the expected ordering,
     * based on the ordering in the resolver chain.
     * @throws SecurityException
     */
    public void testOrderingMultipleMembers() throws SecurityException {
        chainingResolver.getResolverChain().add(staticResolverEmpty);
        chainingResolver.getResolverChain().add(staticResolver45);
        chainingResolver.getResolverChain().add(staticResolverEmpty);
        chainingResolver.getResolverChain().add(staticResolver3);
        chainingResolver.getResolverChain().add(staticResolver12);
        
        List<Credential> resolved = getResolved(chainingResolver.resolve(criteriaSet));
        checkResolved(resolved, 5, cred1, cred2, cred3, cred4, cred5);
        
        assertEquals("Credential found out-of-order", cred4, resolved.get(0));
        assertEquals("Credential found out-of-order", cred5, resolved.get(1));
        assertEquals("Credential found out-of-order", cred3, resolved.get(2));
        assertEquals("Credential found out-of-order", cred1, resolved.get(3));
        assertEquals("Credential found out-of-order", cred2, resolved.get(4));
    }
    
    /**
     * Test empty resolver chain, i.e. no underlying resolver members.
     * @throws SecurityException
     */
    public void testEmptyResolverChain() throws SecurityException {
        Iterable<Credential> credentials = null;
        try {
            credentials = chainingResolver.resolve(criteriaSet);
            fail("Should have thrown an illegal state exception due to no chain members");
        } catch (IllegalStateException e) {
            // do nothing, expected to fail
        }
    }
    
    /**
     * Test exception on attempt to call remove() on iterator.
     * @throws SecurityException
     */
    public void testRemove() throws SecurityException {
        chainingResolver.getResolverChain().add(staticResolver12);
        
        Iterator<Credential> iter = chainingResolver.resolve(criteriaSet).iterator();
        assertTrue("Iterator was empty", iter.hasNext());
        Credential cred = iter.next();
        try {
            iter.remove();
            fail("Remove from iterator is unsupported, should have thrown exception");
        } catch (UnsupportedOperationException e) {
            // do nothing, expected to fail
        }
        
    }
    
    /**
     * Test exception on attempt to call next() on iterator when no more members.
     * @throws SecurityException
     */
    public void testNoMoreMembers() throws SecurityException {
        chainingResolver.getResolverChain().add(staticResolver12);
        chainingResolver.getResolverChain().add(staticResolver3);
        
        Iterator<Credential> iter = chainingResolver.resolve(criteriaSet).iterator();
        Credential cred = null;
        assertTrue("Should have next member", iter.hasNext());
        cred = iter.next();
        assertTrue("Should have next member", iter.hasNext());
        cred = iter.next();
        assertTrue("Should have next member", iter.hasNext());
        cred = iter.next();
        
        assertFalse("Should NOT have next member", iter.hasNext());
        try {
            cred = iter.next();
            fail("Should have thrown exception due to next() call with no more members");
        } catch (NoSuchElementException e) {
            // do nothing, expected to fail
        }
        
    }
    
    /**
     * Get a set of the things that matched the set of criteria.
     * 
     * @param iter credential iterator
     * @return set of all credentials that were resolved
     */
    private List<Credential> getResolved(Iterable<Credential> iter) {
        ArrayList<Credential> resolved = new ArrayList<Credential>();
        for (Credential cred : iter) {
            resolved.add(cred);
        }
        return resolved;
    }
    
    /**
     * Helper method to evaluate the results of getResolved.
     * 
     * @param resolved set of resolved credentials
     * @param expectedNum expected number of resolved credentials
     * @param expectedCreds the vararg list of the credentials expected
     */
    private void checkResolved(List<Credential> resolved, int expectedNum, Credential... expectedCreds) {
        assertEquals("Unexpected number of matches", expectedNum, resolved.size());
        for (Credential expectedCred : expectedCreds) {
            assertTrue("Expected member not found: " + expectedCred, resolved.contains(expectedCred));
        }
    }
    

}
