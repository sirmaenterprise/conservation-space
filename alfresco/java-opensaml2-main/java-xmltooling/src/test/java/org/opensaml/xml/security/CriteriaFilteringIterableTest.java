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

package org.opensaml.xml.security;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import junit.framework.TestCase;

/**
 * Test the iterable for filtering on a set of criteria.
 */
public class CriteriaFilteringIterableTest extends TestCase {
    
    private Collection<Thing> things;
    
    private Set<EvaluableCriteria<Thing>> criteriaSet;
    
    private Thing foofoo, foobar, foobaz, foonull;
    
    private String FOO = "foo";
    private String BAR = "bar";
    private String BAZ = "baz";
    private String OTHER = "other";

    /** {@inheritDoc} */
    protected void setUp() throws Exception {
        things = new HashSet<Thing>();
        foofoo = new Thing(FOO, FOO);
        foobar = new Thing(FOO, BAR);
        foobaz = new Thing(FOO, BAZ);
        foonull = new Thing(FOO, null);
        
        things.add(foofoo);
        things.add(foobar);
        things.add(foobaz);
        things.add(foonull);
        
        criteriaSet = new HashSet<EvaluableCriteria<Thing>>();
    }
    
    /**
     * Test empty criteria set.
     */
    public void testNoCriteria() {
        Set<Thing> matches = null;
        matches = getMatches( new CriteriaFilteringIterable<Thing>(things, criteriaSet, true, true) );
        checkMatches(matches, 4, foofoo, foobar, foobaz, foonull);
        matches = getMatches( new CriteriaFilteringIterable<Thing>(things, criteriaSet, true, false) );
        checkMatches(matches, 4, foofoo, foobar, foobaz, foonull);
        matches = getMatches( new CriteriaFilteringIterable<Thing>(things, criteriaSet, false, true) );
        checkMatches(matches, 4, foofoo, foobar, foobaz, foonull);
        matches = getMatches( new CriteriaFilteringIterable<Thing>(things, criteriaSet, false, false) );
        checkMatches(matches, 4, foofoo, foobar, foobaz, foonull);
    }
    
    /**
     * Test meetAll = true, unevaluableSatisfies = true.
     */
    public void testMeetAllUnevaluableSatisfies() {
        Set<Thing> matches = null;
        
        // Present value 1
        criteriaSet.clear();
        criteriaSet.add( new ThingCriteria1(FOO) );
        matches = getMatches( new CriteriaFilteringIterable<Thing>(things, criteriaSet, true, true) );
        checkMatches(matches, 4, foofoo, foobar, foobaz, foonull);
        
        // Not present value1
        criteriaSet.clear();
        criteriaSet.add( new ThingCriteria1(BAR) );
        matches = getMatches( new CriteriaFilteringIterable<Thing>(things, criteriaSet, true, true) );
        checkMatches(matches, 0);
        
        // Present value 2
        criteriaSet.clear();
        criteriaSet.add( new ThingCriteria2(BAZ) );
        matches = getMatches( new CriteriaFilteringIterable<Thing>(things, criteriaSet, true, true) );
        checkMatches(matches, 2, foobaz, foonull);
        
        // Not present value2
        criteriaSet.clear();
        criteriaSet.add( new ThingCriteria2(OTHER) );
        matches = getMatches( new CriteriaFilteringIterable<Thing>(things, criteriaSet, true, true) );
        checkMatches(matches, 1, foonull);
        
        // Present value1, present value2
        criteriaSet.clear();
        criteriaSet.add( new ThingCriteria1(FOO) );
        criteriaSet.add( new ThingCriteria2(BAR) );
        matches = getMatches( new CriteriaFilteringIterable<Thing>(things, criteriaSet, true, true) );
        checkMatches(matches, 2, foobar, foonull);
        
        // Not present value1, not present value2
        criteriaSet.clear();
        criteriaSet.add( new ThingCriteria1(OTHER) );
        criteriaSet.add( new ThingCriteria2(OTHER) );
        matches = getMatches( new CriteriaFilteringIterable<Thing>(things, criteriaSet, true, true) );
        checkMatches(matches, 0);
        
        // Present value1, not present value2
        criteriaSet.clear();
        criteriaSet.add( new ThingCriteria1(FOO) );
        criteriaSet.add( new ThingCriteria2(OTHER) );
        matches = getMatches( new CriteriaFilteringIterable<Thing>(things, criteriaSet, true, true) );
        checkMatches(matches, 1, foonull);
        
        // Not present value1, present value2
        criteriaSet.clear();
        criteriaSet.add( new ThingCriteria1(OTHER) );
        criteriaSet.add( new ThingCriteria2(BAR) );
        matches = getMatches( new CriteriaFilteringIterable<Thing>(things, criteriaSet, true, true) );
        checkMatches(matches, 0);
        
        // Contradictory value2
        criteriaSet.clear();
        criteriaSet.add( new ThingCriteria2(OTHER) );
        criteriaSet.add( new ThingCriteria2(FOO) );
        matches = getMatches( new CriteriaFilteringIterable<Thing>(things, criteriaSet, true, true) );
        checkMatches(matches, 1, foonull);
    }
    
    /**
     * Test meetAll = true, unevaluableSatisfies = false.
     */
    public void testMeetAllUnevaluableNotSatisfies() {
        Set<Thing> matches = null;
        
        // Present value 1
        criteriaSet.clear();
        criteriaSet.add( new ThingCriteria1(FOO) );
        matches = getMatches( new CriteriaFilteringIterable<Thing>(things, criteriaSet, true, false) );
        checkMatches(matches, 4, foofoo, foobar, foobaz, foonull);
        
        // Not present value1
        criteriaSet.clear();
        criteriaSet.add( new ThingCriteria1(BAR) );
        matches = getMatches( new CriteriaFilteringIterable<Thing>(things, criteriaSet, true, false) );
        checkMatches(matches, 0);
        
        // Present value 2
        criteriaSet.clear();
        criteriaSet.add( new ThingCriteria2(BAZ) );
        matches = getMatches( new CriteriaFilteringIterable<Thing>(things, criteriaSet, true, false) );
        checkMatches(matches, 1, foobaz);
        
        // Not present value2
        criteriaSet.clear();
        criteriaSet.add( new ThingCriteria2(OTHER) );
        matches = getMatches( new CriteriaFilteringIterable<Thing>(things, criteriaSet, true, false) );
        checkMatches(matches, 0);
        
        // Present value1, present value2
        criteriaSet.clear();
        criteriaSet.add( new ThingCriteria1(FOO) );
        criteriaSet.add( new ThingCriteria2(BAR) );
        matches = getMatches( new CriteriaFilteringIterable<Thing>(things, criteriaSet, true, false) );
        checkMatches(matches, 1, foobar);
        
        // Not present value1, not present value2
        criteriaSet.clear();
        criteriaSet.add( new ThingCriteria1(OTHER) );
        criteriaSet.add( new ThingCriteria2(OTHER) );
        matches = getMatches( new CriteriaFilteringIterable<Thing>(things, criteriaSet, true, false) );
        checkMatches(matches, 0);
        
        // Present value1, not present value2
        criteriaSet.clear();
        criteriaSet.add( new ThingCriteria1(FOO) );
        criteriaSet.add( new ThingCriteria2(OTHER) );
        matches = getMatches( new CriteriaFilteringIterable<Thing>(things, criteriaSet, true, false) );
        checkMatches(matches, 0);
        
        // Not present value1, present value2
        criteriaSet.clear();
        criteriaSet.add( new ThingCriteria1(OTHER) );
        criteriaSet.add( new ThingCriteria2(BAR) );
        matches = getMatches( new CriteriaFilteringIterable<Thing>(things, criteriaSet, true, false) );
        checkMatches(matches, 0);
        
        // Contradictory value2
        criteriaSet.clear();
        criteriaSet.add( new ThingCriteria2(OTHER) );
        criteriaSet.add( new ThingCriteria2(FOO) );
        matches = getMatches( new CriteriaFilteringIterable<Thing>(things, criteriaSet, true, false) );
        checkMatches(matches, 0);
    }
    
    /**
     * Test meetAll = false, unevaluableSatisfies = true.
     */
    public void testMeetAnyUnevaluableSatisfies() {
        
        Set<Thing> matches = null;
        
        // Present value 1
        criteriaSet.clear();
        criteriaSet.add( new ThingCriteria1(FOO) );
        matches = getMatches( new CriteriaFilteringIterable<Thing>(things, criteriaSet, false, true) );
        checkMatches(matches, 4, foofoo, foobar, foobaz, foonull);
        
        // Not present value1
        criteriaSet.clear();
        criteriaSet.add( new ThingCriteria1(BAR) );
        matches = getMatches( new CriteriaFilteringIterable<Thing>(things, criteriaSet, false, true) );
        checkMatches(matches, 0);
        
        // Present value 2
        criteriaSet.clear();
        criteriaSet.add( new ThingCriteria2(BAZ) );
        matches = getMatches( new CriteriaFilteringIterable<Thing>(things, criteriaSet, false, true) );
        checkMatches(matches, 2, foobaz, foonull);
        
        // Not present value2
        criteriaSet.clear();
        criteriaSet.add( new ThingCriteria2(OTHER) );
        matches = getMatches( new CriteriaFilteringIterable<Thing>(things, criteriaSet, false, true) );
        checkMatches(matches, 1, foonull);
        
        // Present value1, present value2
        criteriaSet.clear();
        criteriaSet.add( new ThingCriteria1(FOO) );
        criteriaSet.add( new ThingCriteria2(BAR) );
        matches = getMatches( new CriteriaFilteringIterable<Thing>(things, criteriaSet, false, true) );
        checkMatches(matches, 4, foofoo, foobar, foobaz, foonull);
        
        // Not present value1, not present value2
        criteriaSet.clear();
        criteriaSet.add( new ThingCriteria1(OTHER) );
        criteriaSet.add( new ThingCriteria2(OTHER) );
        matches = getMatches( new CriteriaFilteringIterable<Thing>(things, criteriaSet, false, true) );
        checkMatches(matches, 1, foonull);
        
        // Present value1, not present value2
        criteriaSet.clear();
        criteriaSet.add( new ThingCriteria1(FOO) );
        criteriaSet.add( new ThingCriteria2(OTHER) );
        matches = getMatches( new CriteriaFilteringIterable<Thing>(things, criteriaSet, false, true) );
        checkMatches(matches, 4, foofoo, foobar, foobaz, foonull);
        
        // Not present value1, present value2
        criteriaSet.clear();
        criteriaSet.add( new ThingCriteria1(OTHER) );
        criteriaSet.add( new ThingCriteria2(BAR) );
        matches = getMatches( new CriteriaFilteringIterable<Thing>(things, criteriaSet, false, true) );
        checkMatches(matches, 2, foobar, foonull);
        
        // Contradictory value2
        criteriaSet.clear();
        criteriaSet.add( new ThingCriteria2(OTHER) );
        criteriaSet.add( new ThingCriteria2(FOO) );
        matches = getMatches( new CriteriaFilteringIterable<Thing>(things, criteriaSet, false, true) );
        checkMatches(matches, 2, foofoo, foonull);
    }
    
    /**
     * Test meetAll = false, unevaluableSatisfies = false.
     */
    public void testMeetAnyUnevaluableNotSatisfies() {
        Set<Thing> matches = null;
        
        // Present value 1
        criteriaSet.clear();
        criteriaSet.add( new ThingCriteria1(FOO) );
        matches = getMatches( new CriteriaFilteringIterable<Thing>(things, criteriaSet, false, false) );
        checkMatches(matches, 4, foofoo, foobar, foobaz, foonull);
        
        // Not present value1
        criteriaSet.clear();
        criteriaSet.add( new ThingCriteria1(BAR) );
        matches = getMatches( new CriteriaFilteringIterable<Thing>(things, criteriaSet, false, false) );
        checkMatches(matches, 0);
        
        // Present value 2
        criteriaSet.clear();
        criteriaSet.add( new ThingCriteria2(BAZ) );
        matches = getMatches( new CriteriaFilteringIterable<Thing>(things, criteriaSet, false, false) );
        checkMatches(matches, 1, foobaz);
        
        // Not present value2
        criteriaSet.clear();
        criteriaSet.add( new ThingCriteria2(OTHER) );
        matches = getMatches( new CriteriaFilteringIterable<Thing>(things, criteriaSet, false, false) );
        checkMatches(matches, 0);
        
        // Present value1, present value2
        criteriaSet.clear();
        criteriaSet.add( new ThingCriteria1(FOO) );
        criteriaSet.add( new ThingCriteria2(BAR) );
        matches = getMatches( new CriteriaFilteringIterable<Thing>(things, criteriaSet, false, false) );
        checkMatches(matches, 4, foofoo, foobar, foobaz, foonull);
        
        // Not present value1, not present value2
        criteriaSet.clear();
        criteriaSet.add( new ThingCriteria1(OTHER) );
        criteriaSet.add( new ThingCriteria2(OTHER) );
        matches = getMatches( new CriteriaFilteringIterable<Thing>(things, criteriaSet, false, false) );
        checkMatches(matches, 0);
        
        // Present value1, not present value2
        criteriaSet.clear();
        criteriaSet.add( new ThingCriteria1(FOO) );
        criteriaSet.add( new ThingCriteria2(OTHER) );
        matches = getMatches( new CriteriaFilteringIterable<Thing>(things, criteriaSet, false, false) );
        checkMatches(matches, 4, foofoo, foobar, foobaz, foonull);
        
        // Not present value1, present value2
        criteriaSet.clear();
        criteriaSet.add( new ThingCriteria1(OTHER) );
        criteriaSet.add( new ThingCriteria2(BAR) );
        matches = getMatches( new CriteriaFilteringIterable<Thing>(things, criteriaSet, false, false) );
        checkMatches(matches, 1, foobar);
        
        // Contradictory value2
        criteriaSet.clear();
        criteriaSet.add( new ThingCriteria2(OTHER) );
        criteriaSet.add( new ThingCriteria2(FOO) );
        matches = getMatches( new CriteriaFilteringIterable<Thing>(things, criteriaSet, false, false) );
        checkMatches(matches, 1, foofoo);
    }
    
    /**
     * Get a set of the things that matched the set of criteria.
     * 
     * @param thingIter filtering iterator
     * @return set of candidates that matched the criteria
     */
    private Set<Thing> getMatches(CriteriaFilteringIterable<Thing> thingIter) {
        HashSet<Thing> matches = new HashSet<Thing>();
        for (Thing thing : thingIter) {
            matches.add(thing);
        }
        return matches;
    }
    
    /**
     * Helper method to evaluate the results of getMatches.
     * 
     * @param matches set of matched candidates
     * @param expectedNum expected number of matches
     * @param expectedThings the vararg list of the matches expected
     */
    private void checkMatches(Set<Thing> matches, int expectedNum, Thing... expectedThings) {
        assertEquals("Unexpected number of matches", expectedNum, matches.size());
        for (Thing expectedThing : expectedThings) {
            assertTrue("Expected match not found: " + expectedThing, matches.contains(expectedThing));
        }
    }
     
    
    /** Mock evaluation target for testing. */
    private class Thing {
        
        private String value1;
        private String value2;
        
        public Thing(String value1, String value2) {
            this.value1  = value1;
            this.value2  = value2;
        }
        
        public String getValue1() {
            return this.value1;
        }
        
        public String getValue2() {
            return this.value2;
        }

        /** {@inheritDoc} */
        public String toString() {
            return value1 + value2;
        }
    }
    
    /** Mock criteria for testing. */
    private class ThingCriteria1 implements EvaluableCriteria<Thing> {
        
        private String value;
        
        public ThingCriteria1(String value) {
            this.value = value;
        }
        
        /** {@inheritDoc} */
        public Boolean evaluate(Thing target) {
            // defining target null value as meaning can't evaluate
            if (target.getValue1() == null) {
                return null;
            }
            return target.getValue1().equals(this.value);
        }
    }

    /** Mock criteria for testing. */
    private class ThingCriteria2 implements EvaluableCriteria<Thing> {
        
        private String value;
        
        public ThingCriteria2(String value) {
            this.value = value;
        }
        
        /** {@inheritDoc} */
        public Boolean evaluate(Thing target) {
            // defining target null value as meaning can't evaluate
            if (target.getValue2() == null) {
                return null;
            }
            return target.getValue2().equals(this.value);
        }
    }
}
