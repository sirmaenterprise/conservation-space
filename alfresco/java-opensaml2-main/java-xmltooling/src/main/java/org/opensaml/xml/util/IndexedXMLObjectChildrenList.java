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

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.xml.namespace.QName;

import org.opensaml.xml.XMLObject;

/**
 * A list which indexes XMLObjects by their schema type and element QName for quick retrival based on those items.
 * 
 * @param <ElementType> the type of element added to the list
 */
public class IndexedXMLObjectChildrenList<ElementType extends XMLObject> extends XMLObjectChildrenList<ElementType> {

    /** Index of objects by type and name. */
    private Map<QName, ArrayList<ElementType>> objectIndex;

    /**
     * Constructor.
     * 
     * @param parent the parent of the {@link XMLObject}s added to the list
     */
    public IndexedXMLObjectChildrenList(XMLObject parent) {
        super(parent);
        objectIndex = new ConcurrentHashMap<QName, ArrayList<ElementType>>();
    }

    /**
     * Constructor.
     * 
     * @param parent the parent of all elements
     * @param col collection to add to this list
     */
    public IndexedXMLObjectChildrenList(XMLObject parent, Collection<ElementType> col) {
        super(parent);
        objectIndex = new ConcurrentHashMap<QName, ArrayList<ElementType>>();
        addAll(col);
    }

    /** {@inheritDoc} */
    public void clear() {
        super.clear();
        objectIndex.clear();
    }

    /**
     * Retrieves all the SAMLObjects that have given schema type or element name.
     * 
     * @param typeOrName the schema type or element name
     * 
     * @return list of SAMLObjects that have given schema type or element name or null
     */
    public List<ElementType> get(QName typeOrName) {
        return objectIndex.get(typeOrName);
    }

    /**
     * Replaces the element at the specified position in this list with the specified element.
     * 
     * @param index index of element to replace
     * @param element element to be stored at the specified position
     * 
     * @return the element previously at the specified position
     */
    public ElementType set(int index, ElementType element) {
        ElementType returnValue = super.set(index, element);

        removeElementFromIndex(returnValue);

        indexElement(element);
        return returnValue;
    }

    /**
     * Inserts the specified element at the specified position in this list. Shifts the element currently at that
     * position (if any) and any subsequent elements to the right (adds one to their indices).
     * 
     * @param index index of element to add
     * @param element element to be stored at the specified position
     */
    public void add(int index, ElementType element) {
        super.add(index, element);
        indexElement(element);
    }

    /**
     * Removes the element at the specified position in this list. Shifts any subsequent elements to the left (subtracts
     * one from their indices). Returns the element that was removed from the list
     * 
     * @param index the index of the element to remove
     * 
     * @return the element removed from the list
     */
    public ElementType remove(int index) {
        ElementType returnValue = super.remove(index);

        removeElementFromIndex(returnValue);

        return returnValue;
    }

    /**
     * Removes a given element from the list and index.
     * 
     * @param element the element to be removed
     * 
     * @return true if the element was in the list and removed, false if not
     */
    public boolean remove(ElementType element) {
        boolean elementRemoved = false;

        elementRemoved = super.remove(element);
        if (elementRemoved) {
            removeElementFromIndex(element);
        }

        return elementRemoved;
    }

    /**
     * Returns a view of the list that only contains elements stored under the given index. The returned list is backed
     * by this list so and supports all optional operations, so changes made to the returned list are reflected in this
     * list.
     * 
     * @param index index of the elements returned in the list view
     * 
     * @return a view of this list that contains only the elements stored under the given index
     */
    public List<? extends ElementType> subList(QName index) {
        if (!objectIndex.containsKey(index)) {
            objectIndex.put(index, new ArrayList<ElementType>());
        }

        return new ListView<ElementType>(this, index);
    }

    /**
     * Indexes the given SAMLObject by type and element name.
     * 
     * @param element the SAMLObject to index
     */
    protected void indexElement(ElementType element) {
        if (element == null) {
            return;
        }

        QName type = element.getSchemaType();
        if (type != null) {
            indexElement(type, element);
        }

        indexElement(element.getElementQName(), element);
    }

    /**
     * Indexes the given SAMLobject by the given index.
     * 
     * @param index the index for the element
     * @param element the element to be indexed
     */
    protected void indexElement(QName index, ElementType element) {
        ArrayList<ElementType> objects = objectIndex.get(index);
        if (objects == null) {
            objects = new ArrayList<ElementType>();
            objectIndex.put(index, objects);
        }

        objects.add(element);
    }

    /**
     * Removes the given element from the schema type and element qname index.
     * 
     * @param element the element to remove from the index
     */
    protected void removeElementFromIndex(ElementType element) {
        if (element == null) {
            return;
        }

        QName type = element.getSchemaType();
        if (type != null) {
            removeElementFromIndex(type, element);
        }

        removeElementFromIndex(element.getElementQName(), element);
    }

    /**
     * Removes an object from the given index id.
     * 
     * @param index the id of the index
     * @param element the element to be removed from that index
     */
    protected void removeElementFromIndex(QName index, ElementType element) {
        ArrayList<ElementType> objects = objectIndex.get(index);
        if (objects != null) {
            objects.remove(element);
        }

        if (objects.size() == 0) {
            objectIndex.remove(index);
        }
    }
}

/**
 * A special list that works as a view of an IndexedXMLObjectChildrenList showing only the sublist associated with a
 * given index. Operations performed on this sublist are reflected in the backing list.
 * 
 * @param <ElementType> the XMLObject type that this list operates on
 */
class ListView<ElementType extends XMLObject> extends AbstractList<ElementType> {

    /** List that backs this view. */
    private IndexedXMLObjectChildrenList<ElementType> backingList;

    /** Index that points to the list, in the backing list, that this view operates on. */
    private QName index;

    /** List, in the backing list, that the given index points to. */
    private List<ElementType> indexList;

    /**
     * Constructor.
     * 
     * @param newBackingList the list that backs this view
     * @param newIndex the element schema type or name of the sublist this view operates on
     */
    public ListView(IndexedXMLObjectChildrenList<ElementType> newBackingList, QName newIndex) {
        backingList = newBackingList;
        index = newIndex;
        indexList = backingList.get(index);
    }

    /**
     * Checks to see if the given element is contained in this list.
     * 
     * @param element the element to check for
     * 
     * @return true if the element is in this list, false if not
     */
    public boolean contains(ElementType element) {
        return indexList.contains(element);
    }

    /** {@inheritDoc} */
    public ElementType get(int newIndex) {
        return indexList.get(newIndex);
    }

    /** {@inheritDoc} */
    public int size() {
        return indexList.size();
    }

    /** {@inheritDoc} */
    public ElementType set(int newIndex, ElementType element) {

        if (newIndex < 0 && newIndex > indexList.size()) {
            throw new IndexOutOfBoundsException();
        }

        ElementType replacedElement;
        int elementIndex;

        replacedElement = indexList.get(newIndex);
        elementIndex = backingList.indexOf(replacedElement);
        backingList.set(elementIndex, element);

        return replacedElement;
    }

    /** {@inheritDoc} */
    public void add(int newIndex, ElementType element) {
        indexCheck(element);
        backingList.add(element);
    }

    /** {@inheritDoc} */
    public ElementType remove(int newIndex) {
        return backingList.remove(newIndex);
    }

    /**
     * Checks to make sure the given element schema type or name matches the index given at construction time.
     * 
     * @param element the element to check
     * 
     * @throws IllegalArgumentException thrown if the element schema type or name does not match the index
     */
    protected void indexCheck(ElementType element) throws IllegalArgumentException {
        if (index.equals(element.getSchemaType()) || index.equals(element.getElementQName())) {
            return;
        } else {
            throw new IllegalArgumentException("Element " + element.getElementQName() + " is not of type " + index);
        }
    }
}