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

import java.util.Arrays;
import java.util.HashSet;

import junit.framework.TestCase;

/**
 * Tests the ValueTypeIndexedMap.
 */
public class ValueTypeIndexedMapTest extends TestCase {

    /** Instance used for testing. */
    private ValueTypeIndexedMap<String, Object> map;

    /** {@inheritDoc} */
    public void setUp() {
        map = new ValueTypeIndexedMap<String, Object>();
        map.setTypes(Arrays.asList(new Class[] {Integer.class, String.class}));
        map.rebuildIndex();
    }

    /**
     * Test basic functionality.
     */
    public void testBasic() {
        map.put("i1", Integer.parseInt("4"));
        map.put("s1", "first string");
        map.put("s2", "second string");

        assertEquals(3, map.size());
        assertEquals(1, map.subMap(Integer.class).size());
        assertEquals(2, map.subMap(String.class).size());
                
        map.remove("s1");
        assertEquals(2, map.size());
        assertEquals(1, map.subMap(Integer.class).size());
        assertEquals(1, map.subMap(String.class).size());
    }

    /**
     * Test null key support.
     */
    public void testNullKeys() {
        map.put("i1", Integer.parseInt("2"));
        map.put(null, Integer.parseInt("3"));
        map.put("s1", "first string");
        
        assertEquals(3, map.size());
        assertEquals(2, map.subMap(Integer.class).size());
        assertEquals(1, map.subMap(String.class).size());
                
        map.put(null, "new string");
        assertEquals(3, map.size());
        assertEquals(1, map.subMap(Integer.class).size());
        assertEquals(2, map.subMap(String.class).size());
        
        assertTrue(map.containsKey(null));
        map.remove(null);
        assertFalse(map.containsKey(null));
    }

    /**
     * Test null value support.
     */
    public void testNullValues() {
        map.getTypes().add(null);
        map.rebuildIndex();
        
        map.put("i1", Integer.parseInt("3"));
        map.put("n1", null);
        map.put("s1", "first string");

        assertEquals(3, map.size());
        assertEquals(1, map.subMap(Integer.class).size());
        assertEquals(1, map.subMap(String.class).size());
        assertEquals(1, map.subMap(null).size());

        map.put("i1", "new string");
        assertEquals(3, map.size());
        assertEquals(0, map.subMap(Integer.class).size());
        assertEquals(2, map.subMap(String.class).size());
        assertEquals(1, map.subMap(null).size());

        map.put("i1", null);
        assertEquals(3, map.size());
        assertEquals(0, map.subMap(Integer.class).size());
        assertEquals(1, map.subMap(String.class).size());
        assertEquals(2, map.subMap(null).size());
    }

}