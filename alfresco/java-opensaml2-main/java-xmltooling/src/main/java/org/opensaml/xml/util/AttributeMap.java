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

package org.opensaml.xml.util;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

import javax.xml.namespace.QName;

import org.opensaml.xml.Configuration;
import org.opensaml.xml.XMLObject;

/**
 * A map of attribute names and attribute values that invalidates the DOM of the attribute owning XMLObject when the
 * attributes change.
 * 
 * <strong>Note:</strong> 
 */
public class AttributeMap implements Map<QName, String> {

    /** XMLObject owning the attributes. */
    private XMLObject attributeOwner;

    /** Map of attributes. */
    private Map<QName, String> attributes;
    
    /** Set of attribute QNames which have been locally registered as having an ID type within this 
     * AttributeMap instance. */
    private Set<QName> idAttribNames;

    /**
     * Constructor.
     *
     * @param newOwner the XMLObject that owns these attributes
     * 
     * @throws NullPointerException thrown if the given XMLObject is null
     */
    public AttributeMap(XMLObject newOwner) throws NullPointerException {
        if (newOwner == null) {
            throw new NullPointerException("Attribute owner XMLObject may not be null");
        }

        attributeOwner = newOwner;
        attributes = new ConcurrentHashMap<QName, String>();
        idAttribNames = new CopyOnWriteArraySet<QName>();
    }

    /** {@inheritDoc} */
    public String put(QName attributeName, String value) {
        String oldValue = get(attributeName);
        if (value != oldValue) {
            releaseDOM();
            attributes.put(attributeName, value);
            if (isIDAttribute(attributeName) || Configuration.isIDAttribute(attributeName)) {
                attributeOwner.getIDIndex().deregisterIDMapping(oldValue);
                attributeOwner.getIDIndex().registerIDMapping(value, attributeOwner);
            }
        }
        
        return oldValue;
    }

    /** {@inheritDoc} */
    public void clear() {
        for (QName attributeName : attributes.keySet()) {
            remove(attributeName);
        }
    }

    /**
     * Returns the set of keys.
     * 
     * @return unmodifiable set of keys
     */
    public Set<QName> keySet() {
        return Collections.unmodifiableSet(attributes.keySet());
    }

    /** {@inheritDoc} */
    public int size() {
        return attributes.size();
    }

    /** {@inheritDoc} */
    public boolean isEmpty() {
        return attributes.isEmpty();
    }

    /** {@inheritDoc} */
    public boolean containsKey(Object key) {
        return attributes.containsKey(key);
    }

    /** {@inheritDoc} */
    public boolean containsValue(Object value) {
        return attributes.containsValue(value);
    }

    /** {@inheritDoc} */
    public String get(Object key) {
        return attributes.get(key);
    }

    /** {@inheritDoc} */
    public String remove(Object key) {
        String removedValue = attributes.remove(key);
        if (removedValue != null) {
            releaseDOM();
            QName attributeName = (QName) key;
            if (isIDAttribute(attributeName) || Configuration.isIDAttribute(attributeName)) {
                attributeOwner.getIDIndex().deregisterIDMapping(removedValue);
            }
        }

        return removedValue;
    }

    /** {@inheritDoc} */
    public void putAll(Map<? extends QName, ? extends String> t) {
        if (t != null && t.size() > 0) {
            for (Entry<? extends QName, ? extends String> entry : t.entrySet()) {
                put(entry.getKey(), entry.getValue());
            }
        }
    }

    /**
     * Returns the values in this map.
     * 
     * @return an unmodifiable collection of values
     */
    public Collection<String> values() {
        return Collections.unmodifiableCollection(attributes.values());
    }

    /**
     * Returns the set of entries.
     * 
     * @return unmodifiable set of entries
     */
    public Set<Entry<QName, String>> entrySet() {
        return Collections.unmodifiableSet(attributes.entrySet());
    }
    
    /**
     * Register an attribute as having a type of ID.
     * 
     * @param attributeName the QName of the ID attribute to be registered
     */
    public void registerID(QName attributeName) {
        if (! idAttribNames.contains(attributeName)) {
            idAttribNames.add(attributeName);
        }
        
        // In case attribute already has a value,
        // register the current value mapping with the XMLObject owner.
        if (containsKey(attributeName)) {
            attributeOwner.getIDIndex().registerIDMapping(get(attributeName), attributeOwner);
        }
    }
    
    /**
     * Deregister an attribute as having a type of ID.
     * 
     * @param attributeName the QName of the ID attribute to be de-registered
     */
    public void deregisterID(QName attributeName) {
        if (idAttribNames.contains(attributeName)) {
            idAttribNames.remove(attributeName);
        }
        
        // In case attribute already has a value,
        // deregister the current value mapping with the XMLObject owner.
        if (containsKey(attributeName)) {
            attributeOwner.getIDIndex().deregisterIDMapping(get(attributeName));
        }
    }
    
    /**
     * Check whether a given attribute is locally registered as having an ID type within
     * this AttributeMap instance.
     * 
     * @param attributeName the QName of the attribute to be checked for ID type.
     * @return true if attribute is registered as having an ID type.
     */
    public boolean isIDAttribute(QName attributeName) {
        return idAttribNames.contains(attributeName);
    }
    
    /**
     * Releases the DOM caching associated XMLObject and its ancestors.
     */
    private void releaseDOM() {
        attributeOwner.releaseDOM();
        attributeOwner.releaseParentDOM(true);
    }
}