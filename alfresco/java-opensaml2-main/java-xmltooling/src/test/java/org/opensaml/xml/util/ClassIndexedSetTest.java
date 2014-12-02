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

package org.opensaml.xml.util;

import java.util.HashSet;
import java.util.Iterator;
import java.util.NoSuchElementException;

import junit.framework.TestCase;

import org.opensaml.xml.security.Criteria;
import org.opensaml.xml.security.criteria.EntityIDCriteria;
import org.opensaml.xml.security.criteria.KeyAlgorithmCriteria;
import org.opensaml.xml.security.criteria.KeyLengthCriteria;
import org.opensaml.xml.security.keyinfo.KeyInfoCriteria;

/**
 * Tests the ClassIndexedSet, using Criteria as the underlying type.
 */
public class ClassIndexedSetTest extends TestCase {
    
    /** Criteria set to use as target for tests. */
    private ClassIndexedSet<Criteria> criteriaSet;

    /** {@inheritDoc} */
    protected void setUp() throws Exception {
        super.setUp();
        criteriaSet = new ClassIndexedSet<Criteria>();
    }
    
    /**
     *  Test failure of adding a duplicate instance.
     */
    public void testDupInstance() {
        EntityIDCriteria  entityCriteria = new EntityIDCriteria("owner");
        criteriaSet.add(entityCriteria);
        
        try {
            criteriaSet.add(entityCriteria);
            fail("Set already contained the specified instance");
        } catch (IllegalArgumentException e) {
            // it should fail
        }
    }
    
    /**
     *  Test failure of adding a duplicate criteria type.
     */
    public void testDupType() {
        EntityIDCriteria  entityCriteria1 = 
            new EntityIDCriteria("owner");
        EntityIDCriteria  entityCriteria2 = 
            new EntityIDCriteria("owner#2");
        criteriaSet.add(entityCriteria1);
        
        try {
            criteriaSet.add(entityCriteria2);
            fail("Set already contained an instance of the specified class");
        } catch (IllegalArgumentException e) {
            // it should fail
        }
    }
    
    /**
     *  Test success of adding a duplicate criteria type with replacement.
     */
    public void testDupTypeWithReplacement() {
        EntityIDCriteria  entityCriteria1 = 
            new EntityIDCriteria("owner");
        EntityIDCriteria  entityCriteria2 = 
            new EntityIDCriteria("owner#2");
        criteriaSet.add(entityCriteria1);
        
        try {
            criteriaSet.add(entityCriteria2, true);
        } catch (IllegalArgumentException e) {
            fail("Set should have replaced existing criteria type");
        }
        
        assertFalse("Did not find the expected criteria instance",
                entityCriteria1 == criteriaSet.get(EntityIDCriteria.class) );
        assertTrue("Did not find the expected criteria instance",
                entityCriteria2 == criteriaSet.get(EntityIDCriteria.class) );
        
    }
    
    /**
     *  Test getting criteria instance from set by type.
     */
    public void testGetType() {
        EntityIDCriteria  entityCriteria = new EntityIDCriteria("owner");
        criteriaSet.add(entityCriteria);
        KeyAlgorithmCriteria  keyCriteria = new KeyAlgorithmCriteria("algorithm");
        criteriaSet.add(keyCriteria);
        
        assertTrue("Did not find the expected criteria instance",
                entityCriteria == criteriaSet.get(EntityIDCriteria.class) );
        assertTrue("Did not find the expected criteria instance",
                keyCriteria == criteriaSet.get(KeyAlgorithmCriteria.class) );
        assertTrue("Did not find the expected (null) criteria instance",
                null == criteriaSet.get(KeyInfoCriteria.class) );
    }
    
    /** Tests removing criteria from set by instance. */
    public void testRemove() {
        EntityIDCriteria  entityCriteria = new EntityIDCriteria("owner");
        criteriaSet.add(entityCriteria);
        KeyAlgorithmCriteria  keyCriteria = new KeyAlgorithmCriteria("algorithm");
        criteriaSet.add(keyCriteria);
        
        assertEquals("Set had unexpected size", 2, criteriaSet.size());
        
        criteriaSet.remove(keyCriteria);
        assertEquals("Set had unexpected size", 1, criteriaSet.size());
        assertNull("Set returned removed value", criteriaSet.get(KeyAlgorithmCriteria.class));
        
        criteriaSet.remove(entityCriteria);
        assertEquals("Set had unexpected size", 0, criteriaSet.size());
        assertNull("Set returned removed value", criteriaSet.get(EntityIDCriteria.class));
    }
    
    /** Tests clearing the set. */
    public void testClear() {
        EntityIDCriteria  entityCriteria = new EntityIDCriteria("owner");
        criteriaSet.add(entityCriteria);
        KeyAlgorithmCriteria  keyCriteria = new KeyAlgorithmCriteria("algorithm");
        criteriaSet.add(keyCriteria);
        
        assertEquals("Set had unexpected size", 2, criteriaSet.size());
        
        criteriaSet.clear();
        assertEquals("Set had unexpected size", 0, criteriaSet.size());
        
        assertNull("Set returned removed value", criteriaSet.get(KeyAlgorithmCriteria.class));
        assertNull("Set returned removed value", criteriaSet.get(EntityIDCriteria.class));
    }
    
    /** Tests proper iterator iterating behavior. */
    public void testIterator() {
        EntityIDCriteria  entityCriteria = new EntityIDCriteria("owner");
        criteriaSet.add(entityCriteria);
        KeyAlgorithmCriteria  keyCriteria = new KeyAlgorithmCriteria("algorithm");
        criteriaSet.add(keyCriteria);
        KeyInfoCriteria keyInfoCriteria = new KeyInfoCriteria(null);
        criteriaSet.add(keyInfoCriteria);
        
        assertEquals("Set had unexpected size", 3, criteriaSet.size());
        
        int count = 0;
        HashSet<Criteria> unique = new HashSet<Criteria>();
        for ( Criteria criteria : criteriaSet) {
            count++;
            assertTrue("Duplicate was returned by iterator", unique.add(criteria));
        }
        assertEquals("Set iteration had unexpected count", 3, count);
        
        Iterator<Criteria> iterator = criteriaSet.iterator();
        assertTrue("Iterator should have more elements", iterator.hasNext());
        iterator.next();
        assertTrue("Iterator should have more elements", iterator.hasNext());
        iterator.next();
        assertTrue("Iterator should have more elements", iterator.hasNext());
        iterator.next();
        assertFalse("Iterator should have no more elements", iterator.hasNext());
        try {
            iterator.next();
            fail("Should have seen a iterator exception, no more elements available in set");
        } catch (NoSuchElementException e) {
            // do nothing, should fail
        }
        
    }
    
    /** Tests proper iterator remove() behavior. */
    public void testIteratorRemove() {
        criteriaSet = new ClassIndexedSet<Criteria>();
        EntityIDCriteria  entityCriteria = new EntityIDCriteria("owner");
        criteriaSet.add(entityCriteria);
        KeyAlgorithmCriteria  algorithmCriteria = new KeyAlgorithmCriteria("algorithm");
        criteriaSet.add(algorithmCriteria);
        KeyInfoCriteria keyInfoCriteria = new KeyInfoCriteria(null);
        criteriaSet.add(keyInfoCriteria);
        KeyLengthCriteria lengthCriteria = new KeyLengthCriteria(128);
        criteriaSet.add(lengthCriteria);
        
        assertEquals("Set had unexpected size", 4, criteriaSet.size());
        
        Iterator<Criteria> iterator = criteriaSet.iterator();
        Criteria criteria = null;
        while ( iterator.hasNext() ) {
            criteria = iterator.next();
            if (criteria instanceof KeyAlgorithmCriteria) {
                iterator.remove();
            }
        }
        assertEquals("Set iteration had unexpected size", 3, criteriaSet.size());
        
        assertTrue("Set did not contain expected instance", criteriaSet.contains(entityCriteria));
        assertTrue("Set did not contain expected instance", criteriaSet.contains(keyInfoCriteria));
        assertTrue("Set did not contain expected instance", criteriaSet.contains(lengthCriteria));
        assertFalse("Set contained unexpected instance", criteriaSet.contains(algorithmCriteria));
        
        assertTrue("Set did not contain expected class type", 
                criteriaSet.contains(EntityIDCriteria.class));
        assertTrue("Set did not contain expected class type", 
                criteriaSet.contains(KeyInfoCriteria.class));
        assertTrue("Set did not contain expected class type", 
                criteriaSet.contains(KeyLengthCriteria.class));
        assertFalse("Set contained unexpected class type", 
                criteriaSet.contains(KeyAlgorithmCriteria.class));
    }
        
    /** Tests proper iterator remove() behavior when called illegally. */
    public void testIteratorRemoveIllegal() {
        criteriaSet = new ClassIndexedSet<Criteria>();
        EntityIDCriteria  entityCriteria = new EntityIDCriteria("owner");
        criteriaSet.add(entityCriteria);
        KeyAlgorithmCriteria  keyCriteria = new KeyAlgorithmCriteria("algorithm");
        criteriaSet.add(keyCriteria);
        KeyInfoCriteria keyInfoCriteria = new KeyInfoCriteria(null);
        criteriaSet.add(keyInfoCriteria);
        KeyLengthCriteria lengthCriteria = new KeyLengthCriteria(128);
        criteriaSet.add(lengthCriteria);
        
        assertEquals("Set had unexpected size", 4, criteriaSet.size());
        
        Iterator<Criteria> iterator = criteriaSet.iterator();
        try {
            iterator.remove();
            fail("Should have seen a iterator exception, remove() called before first next()");
        } catch (IllegalStateException e) {
            // do nothing, should fail
        }
        
        iterator = criteriaSet.iterator();
        iterator.next();
        iterator.remove();
        try {
            iterator.remove();
            fail("Should have seen a iterator exception, remove() called twice on same element");
        } catch (IllegalStateException e) {
            // do nothing, should fail
        }
    }

}
