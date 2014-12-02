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

package org.opensaml.saml2.metadata;

import java.util.List;

import javax.xml.namespace.QName;

import org.opensaml.common.xml.SAMLConstants;


/**
 * SAML 2.0 Metadata SSODescriptor
 */
public interface SSODescriptor extends RoleDescriptor {

    /** Element name, no namespace */
    public final static String DEFAULT_ELEMENT_LOCAL_NAME = "SSODescriptor";
    
    /** Default element name */
    public final static QName DEFAULT_ELEMENT_NAME = new QName(SAMLConstants.SAML20MD_NS, DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML20MD_PREFIX);
    
    /** Local name of the XSI type */
    public final static String TYPE_LOCAL_NAME = "SSODescriptorType"; 
        
    /** QName of the XSI type */
    public final static QName TYPE_NAME = new QName(SAMLConstants.SAML20MD_NS, TYPE_LOCAL_NAME, SAMLConstants.SAML20MD_PREFIX);
    
    /**
     * Gets a list of artifact resolution services for this service.
     * 
     * @return list of artifact resolution services for this service
     */
	public List<ArtifactResolutionService> getArtifactResolutionServices();
    
    /**
     * Gets the default artifact resolution service or null if no service is marked as the default.
     * 
     * @return default artifact resolution service or null if no service is marked as the default
     */
    public ArtifactResolutionService getDefaultArtificateResolutionService();

    /**
     * Gets a list of single logout services for this service.
     * 
     * @return list of single logout services for this service
     */
	public List<SingleLogoutService> getSingleLogoutServices();

    /**
     * Gets a list of manage NameId services for this service.
     * 
     * @return list of manage NameId services for this service
     */
	public List<ManageNameIDService> getManageNameIDServices();
    
    /**
     * Gets the list of NameID formats this service supports.
     * 
     * @return NameID formats this service supports
     */
    public List<NameIDFormat> getNameIDFormats();
}
