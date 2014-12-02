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

package org.opensaml.common;

/**
 * Interface for identifier generators.  This identifier can be used for things like
 * digital signature identifiers, opaque principal identifiers, etc. 
 */
public interface IdentifierGenerator {

    /**
     * Generates a 16 byte identifier.
     * 
     * @return an hex encoded identifier
     */
    public String generateIdentifier();
    
    /** Generates a random identifier.
     * 
     * @param size number of bytes in the identifier
     * 
     * @return the hex encoded identifier
     */
    public String generateIdentifier(int size);
}