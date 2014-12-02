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

import org.opensaml.xml.security.Criteria;
import org.opensaml.xml.signature.KeyInfo;

/**
 * An implementation of {@link Criteria} which specifies criteria based
 * on the contents of a {@link KeyInfo} element.
 */
public final class KeyInfoCriteria implements Criteria {
    
    /** The KeyInfo which serves as the source for credential criteria. */
    private KeyInfo keyInfo;
    
    /**
     * Constructor.
     *
     * @param newKeyInfo the KeyInfo credential criteria to use
     */
    public KeyInfoCriteria(KeyInfo newKeyInfo) {
       setKeyInfo(newKeyInfo);
    }

    /**
     * Gets the KeyInfo which is the source of credential criteria.
     * 
     * @return the KeyInfo credential criteria
     */
    public KeyInfo getKeyInfo() {
        return keyInfo;
    }
    
    /**
     * Sets the KeyInfo which is the source of credential criteria.
     * 
     * @param newKeyInfo the KeyInfo to use as credential criteria
     * 
     */
    public void setKeyInfo(KeyInfo newKeyInfo) {
        // Note: we allow KeyInfo to be null to handle case where application context,
        // other accompanying criteria, etc should be used to resolve credentials.
        keyInfo = newKeyInfo;
    }

}
