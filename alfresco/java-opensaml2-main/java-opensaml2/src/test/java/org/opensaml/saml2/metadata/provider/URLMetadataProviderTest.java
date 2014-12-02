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

package org.opensaml.saml2.metadata.provider;

import java.util.List;

import org.opensaml.common.BaseTestCase;
import org.opensaml.saml2.metadata.EntitiesDescriptor;
import org.opensaml.saml2.metadata.EntityDescriptor;
import org.opensaml.saml2.metadata.IDPSSODescriptor;
import org.opensaml.saml2.metadata.RoleDescriptor;

/**
 * Unit tests for {@link HTTPMetadataProvider}.
 */
public class URLMetadataProviderTest extends BaseTestCase {

    private String inCommonMDURL;
    private String entitiesDescriptorName;
    private String entityID;
    private String supportedProtocol;
    private HTTPMetadataProvider metadataProvider;
    
    /**{@inheritDoc} */
    protected void setUp() throws Exception {
        super.setUp();
        
        inCommonMDURL = "http://wayf.incommonfederation.org/InCommon/InCommon-metadata.xml";
        entitiesDescriptorName = "urn:mace:incommon";
        entityID = "urn:mace:incommon:washington.edu";
        supportedProtocol ="urn:oasis:names:tc:SAML:1.1:protocol";
        metadataProvider = new HTTPMetadataProvider(inCommonMDURL, 1000 * 5);
        metadataProvider.setParserPool(parser);
        metadataProvider.initialize();
    }
    
    /**
     * Tests the {@link HTTPMetadataProvider#getMetadata()} method.
     */
    public void testGetMetadata() throws MetadataProviderException {
        EntitiesDescriptor descriptor = (EntitiesDescriptor) metadataProvider.getMetadata();
        assertNotNull("Retrieved metadata was null", descriptor);
        assertEquals("EntitiesDescriptor name was not expected value", entitiesDescriptorName, descriptor.getName());
    }
    
    /**
     * Tests the {@link HTTPMetadataProvider#getEntitiesDescriptor(String)} method.
     */
    public void testGetEntitiesDescriptor() throws MetadataProviderException{
        EntitiesDescriptor descriptor = (EntitiesDescriptor) metadataProvider.getEntitiesDescriptor(entitiesDescriptorName);
        assertNotNull("Retrieved metadata was null", descriptor);
        assertEquals("EntitiesDescriptor name was not expected value", entitiesDescriptorName, descriptor.getName());
    }
    
    /**
     * Tests the {@link HTTPMetadataProvider#getEntityDescriptor(String)} method.
     */
    public void testGetEntityDescriptor() throws MetadataProviderException{
        EntityDescriptor descriptor = metadataProvider.getEntityDescriptor(entityID);
        assertNotNull("Retrieved entity descriptor was null", descriptor);
        assertEquals("Entity's ID does not match requested ID", entityID, descriptor.getEntityID());
    }
    
    /**
     * Tests the {@link HTTPMetadataProvider#getRole(String, javax.xml.namespace.QName) method.
     */
    public void testGetRole() throws MetadataProviderException{
        List<RoleDescriptor> roles = metadataProvider.getRole(entityID, IDPSSODescriptor.DEFAULT_ELEMENT_NAME);
        assertNotNull("Roles for entity descriptor was null", roles);
        assertEquals("Unexpected number of roles", 1, roles.size());
    }
    
    /**
     * Test the {@link HTTPMetadataProvider#getRole(String, javax.xml.namespace.QName, String) method.
     */
    public void testGetRoleWithSupportedProtocol() throws MetadataProviderException{
        RoleDescriptor role = metadataProvider.getRole(entityID, IDPSSODescriptor.DEFAULT_ELEMENT_NAME, supportedProtocol);
        assertNotNull("Roles for entity descriptor was null", role);
    }
}