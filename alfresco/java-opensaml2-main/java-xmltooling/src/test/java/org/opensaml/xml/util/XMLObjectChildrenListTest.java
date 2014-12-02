/*
 * Copyright [2005] [University Corporation for Advanced Internet Development, Inc.]
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

import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import junit.framework.TestCase;

import org.opensaml.xml.mock.SimpleXMLObject;
import org.opensaml.xml.mock.SimpleXMLObjectBuilder;

/**
 * Test case for {@link org.opensaml.xml.util.XMLObjectChildrenList}
 */
public class XMLObjectChildrenListTest extends TestCase {

    private SimpleXMLObjectBuilder sxoBuilder = new SimpleXMLObjectBuilder();
    
    /**
     * Tests the add methods of ths list.
     */
    public void testAdd() {
        SimpleXMLObject parentObject = sxoBuilder.buildObject();

        XMLObjectChildrenList<SimpleXMLObject> objectList = new XMLObjectChildrenList<SimpleXMLObject>(parentObject);
        assertEquals("XMLObject list was supposed to be empty but instead had " + objectList.size() + " elements", 0,
                objectList.size());

        objectList.add(null);
        assertEquals("XMLObject list allowed a null element to be added", 0, objectList.size());

        // Test adding a single element
        SimpleXMLObject child1 = sxoBuilder.buildObject();
        objectList.add(child1);
        assertEquals("XMLObject list was supposed to have 1 element but instead had " + objectList.size(), 1,
                objectList.size());
        assertEquals("Child 1 did not have the correct parent object", parentObject, child1.getParent());

        // Test adding an collection of children
        List<SimpleXMLObject> childList = new LinkedList<SimpleXMLObject>();
        SimpleXMLObject child2 = sxoBuilder.buildObject();
        childList.add(child2);
        SimpleXMLObject child3 = sxoBuilder.buildObject();
        childList.add(child3);

        objectList.addAll(childList);
        assertEquals("XMLObject list was supposed to have 3 element but instead had " + objectList.size(), 3,
                objectList.size());
        assertEquals("Child 2 did not have the correct parent object", parentObject, child2.getParent());
        assertEquals("Child 3 did not have the correct parent object", parentObject, child3.getParent());
    }

    /**
     * Tests the set method of the list.
     */
    public void testSet() {
        SimpleXMLObject parentObject = sxoBuilder.buildObject();

        XMLObjectChildrenList<SimpleXMLObject> objectList = new XMLObjectChildrenList<SimpleXMLObject>(parentObject);
        assertEquals("XMLObject list was supposed to be empty but instead had " + objectList.size() + " elements", 0,
                objectList.size());

        // Test adding a single element
        SimpleXMLObject child1 = sxoBuilder.buildObject();
        objectList.add(child1);
        assertEquals("XMLObject list was supposed to have 1 element but instead had " + objectList.size(), 1,
                objectList.size());
        assertEquals("Child 1 did not have the correct parent object", parentObject, child1.getParent());

        objectList.set(0, null);
        assertNotNull("XMLObject list allowed a null element to be set", objectList.get(0));

        SimpleXMLObject child2 = sxoBuilder.buildObject();
        SimpleXMLObject replacedChild = objectList.set(0, child2);
        assertEquals("XMLObject list was supposed to have 1 element but instead had " + objectList.size(), 1,
                objectList.size());

        // Make sure Child 2 got it's parent set correctly and that the element now in the list is Child 2
        assertEquals("Child 2 did not have the correct parent object", parentObject, child2.getParent());
        assertEquals("Child element was not Child 2", child2, objectList.get(0));

        // Make sure Child 1 got it's parent nulled out and is no longer in the list
        assertNull("Replaced child element parent was not null", replacedChild.getParent());
        assertFalse("Child1 still appears in the object list even though it should have been removed", objectList
                .contains(child1));
    }

    /**
     * Test the remove methods of the list.
     */
    public void testRemove() {
        SimpleXMLObject parentObject = sxoBuilder.buildObject();
        XMLObjectChildrenList<SimpleXMLObject> objectList = new XMLObjectChildrenList<SimpleXMLObject>(parentObject);

        // Test removing a single element
        SimpleXMLObject child1 = sxoBuilder.buildObject();
        objectList.add(child1);
        assertEquals("XMLObject list was supposed to have 1 element but instead had " + objectList.size(), 1,
                objectList.size());

        objectList.remove(child1);
        assertEquals("XMLObject list was supposed to have 0 element but instead had " + objectList.size(), 0,
                objectList.size());
        assertNull("Child 1 parent was not null", child1.getParent());

        // Test removing an collection of children
        List<SimpleXMLObject> childList = new LinkedList<SimpleXMLObject>();
        SimpleXMLObject child2 = sxoBuilder.buildObject();
        childList.add(child2);
        SimpleXMLObject child3 = sxoBuilder.buildObject();
        childList.add(child3);

        objectList.addAll(childList);
        assertEquals("XMLObject list was supposed to have 2 element but instead had " + objectList.size(), 2,
                objectList.size());

        objectList.removeAll(childList);
        assertEquals("XMLObject list was supposed to have 0 element but instead had " + objectList.size(), 0,
                objectList.size());
        assertNull("Child 2 parent was not null", child2.getParent());
        assertNull("Child 3 parent was not null", child3.getParent());
    }

    /**
     * Test the iterator methods of the list.
     */
    public void testIterator() {
        SimpleXMLObject parentObject = sxoBuilder.buildObject();
        XMLObjectChildrenList<SimpleXMLObject> objectList = new XMLObjectChildrenList<SimpleXMLObject>(parentObject);
        SimpleXMLObject child1 = sxoBuilder.buildObject();
        objectList.add(child1);
        SimpleXMLObject child2 = sxoBuilder.buildObject();
        objectList.add(child2);
        SimpleXMLObject child3 = sxoBuilder.buildObject();
        objectList.add(child3);

        Iterator<SimpleXMLObject> itr = objectList.iterator();

        SimpleXMLObject firstObject = itr.next();
        assertNotNull("First iterator was null and should not have been", firstObject);
        assertEquals("First iterator object should have been child 1 but was not", child1, firstObject);

        itr.next();
        itr.remove();
        SimpleXMLObject thirdObject = itr.next();
        assertEquals("Third iterator object should have been child 3 but was not", child3, thirdObject);
        assertNull("Child 2 parent was not null", child2.getParent());

        SimpleXMLObject child4 = sxoBuilder.buildObject();
        objectList.add(child4);

        try {
            itr.next();
            fail("Iterator allowed list to change underneath it without failing");
        } catch (ConcurrentModificationException e) {
            // DO NOTHING, THIS IS SUPPOSED TO FAIL
        }
    }

    /**
     * Test the clear method of the list.
     */
    public void testClear() {
        SimpleXMLObject parentObject = sxoBuilder.buildObject();
        XMLObjectChildrenList<SimpleXMLObject> objectList = new XMLObjectChildrenList<SimpleXMLObject>(parentObject);

        List<SimpleXMLObject> childList = new LinkedList<SimpleXMLObject>();
        SimpleXMLObject child1 = sxoBuilder.buildObject();
        childList.add(child1);
        SimpleXMLObject child2 = sxoBuilder.buildObject();
        childList.add(child2);
        SimpleXMLObject child3 = sxoBuilder.buildObject();
        childList.add(child3);
        objectList.addAll(childList);

        objectList.clear();
        assertEquals("XMLObject list was supposed to have 0 element buts instead had " + objectList.size(), 0,
                objectList.size());
        assertNull("Child 1 parent was not null", child1.getParent());
        assertNull("Child 2 parent was not null", child2.getParent());
        assertNull("Child 2 parent was not null", child3.getParent());
    }
}