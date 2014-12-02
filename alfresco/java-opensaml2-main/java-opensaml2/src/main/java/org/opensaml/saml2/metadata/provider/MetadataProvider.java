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

package org.opensaml.saml2.metadata.provider;

import java.util.List;

import javax.xml.namespace.QName;

import org.opensaml.saml2.metadata.EntitiesDescriptor;
import org.opensaml.saml2.metadata.EntityDescriptor;
import org.opensaml.saml2.metadata.RoleDescriptor;
import org.opensaml.xml.XMLObject;

/**
 * A local store into which metadata can be loaded and queried. Specific implemenations may perform additional logic
 * such as caching (and refreshing) metadata and merging metadata, about a single entity, from multiple sources.
 */
public interface MetadataProvider {

    /**
     * Gets whether the metadata returned by queries must be valid. At a minimum, metadata is valid only if the date
     * expressed in entity's validUntil attribute has not passed. Specific implementations may add additional
     * constraints.
     * 
     * @return whether the metadata returned by queries must be valid
     */
    public boolean requireValidMetadata();

    /**
     * Sets whether the metadata returned by queries must be valid.
     * 
     * @param requireValidMetadata whether the metadata returned by queries must be valid
     */
    public void setRequireValidMetadata(boolean requireValidMetadata);

    /**
     * Gets the metadata filter applied to the metadata.
     * 
     * @return the metadata filter applied to the metadata
     */
    public MetadataFilter getMetadataFilter();

    /**
     * Sets the metadata filter applied to the metadata.
     * 
     * @param newFilter the metadata filter applied to the metadata
     * 
     * @throws MetadataProviderException thrown if the provider can not apply the filter to the metadata
     */
    public void setMetadataFilter(MetadataFilter newFilter) throws MetadataProviderException;

    /**
     * Gets the entire metadata tree, after the registered filter has been applied.
     * 
     * @return the entire metadata tree
     * 
     * @throws MetadataProviderException thrown if the provider can not fetch the metadata
     */
    public XMLObject getMetadata() throws MetadataProviderException;

    /**
     * Gets a named EntitiesDescriptor from the metadata.
     * 
     * @param name the name of the EntitiesDescriptor
     * 
     * @return the EntitiesDescriptor or null
     * 
     * @throws MetadataProviderException thrown if the provider can not fetch the metadata
     */
    public EntitiesDescriptor getEntitiesDescriptor(String name) throws MetadataProviderException;

    /**
     * Gets the metadata for a given entity if the metadata is valid.
     * 
     * @param entityID the ID of the entity
     * 
     * @return the entity's metadata or null if there is no metadata or no valid metadata
     * 
     * @throws MetadataProviderException thrown if the provider can not fetch the metadata
     */
    public EntityDescriptor getEntityDescriptor(String entityID) throws MetadataProviderException;

    /**
     * Gets the role descriptors of a given type for a given entity from valid metadata.
     * 
     * @param entityID the ID of the entity
     * @param roleName the role type
     * 
     * @return the role descriptors
     * 
     * @throws MetadataProviderException thrown if the provider can not fetch the metadata
     */
    public List<RoleDescriptor> getRole(String entityID, QName roleName) throws MetadataProviderException;

    /**
     * Gets the role descriptors of a given type for a given entity that support the given protocol from valid metadata.
     * 
     * @param entityID the ID of the entity
     * @param roleName the role type
     * @param supportedProtocol the protocol supported by the role
     * 
     * @return the role descriptors
     * 
     * @throws MetadataProviderException thrown if the provider can not fetch the metadata
     */
    public RoleDescriptor getRole(String entityID, QName roleName, String supportedProtocol)
            throws MetadataProviderException;
}