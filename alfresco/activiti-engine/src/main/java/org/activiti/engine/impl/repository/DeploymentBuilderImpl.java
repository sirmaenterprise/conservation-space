/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.activiti.engine.impl.repository;

import java.io.InputStream;
import java.io.Serializable;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.impl.RepositoryServiceImpl;
import org.activiti.engine.impl.persistence.entity.DeploymentEntity;
import org.activiti.engine.impl.persistence.entity.ResourceEntity;
import org.activiti.engine.impl.util.IoUtil;
import org.activiti.engine.impl.util.ReflectUtil;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.DeploymentBuilder;

// TODO: Auto-generated Javadoc
/**
 * The Class DeploymentBuilderImpl.
 *
 * @author Tom Baeyens
 */
public class DeploymentBuilderImpl implements DeploymentBuilder, Serializable {

  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = 1L;

  /** The repository service. */
  protected transient RepositoryServiceImpl repositoryService;
  
  /** The deployment. */
  protected DeploymentEntity deployment = new DeploymentEntity();
  
  /** The is duplicate filter enabled. */
  protected boolean isDuplicateFilterEnabled = false;

  /**
   * Instantiates a new deployment builder impl.
   *
   * @param repositoryService the repository service
   */
  public DeploymentBuilderImpl(RepositoryServiceImpl repositoryService) {
    this.repositoryService = repositoryService;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.repository.DeploymentBuilder#addInputStream(java.lang.String, java.io.InputStream)
   */
  public DeploymentBuilder addInputStream(String resourceName, InputStream inputStream) {
    if (inputStream==null) {
      throw new ActivitiException("inputStream for resource '"+resourceName+"' is null");
    }
    byte[] bytes = IoUtil.readInputStream(inputStream, resourceName);
    ResourceEntity resource = new ResourceEntity();
    resource.setName(resourceName);
    resource.setBytes(bytes);
    deployment.addResource(resource);
    return this;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.repository.DeploymentBuilder#addClasspathResource(java.lang.String)
   */
  public DeploymentBuilder addClasspathResource(String resource) {
    InputStream inputStream = ReflectUtil.getResourceAsStream(resource);
    if (inputStream==null) {
      throw new ActivitiException("resource '"+resource+"' not found");
    }
    return addInputStream(resource, inputStream);
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.repository.DeploymentBuilder#addString(java.lang.String, java.lang.String)
   */
  public DeploymentBuilder addString(String resourceName, String text) {
    if (text==null) {
      throw new ActivitiException("text is null");
    }
    ResourceEntity resource = new ResourceEntity();
    resource.setName(resourceName);
    resource.setBytes(text.getBytes());
    deployment.addResource(resource);
    return this;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.repository.DeploymentBuilder#addZipInputStream(java.util.zip.ZipInputStream)
   */
  public DeploymentBuilder addZipInputStream(ZipInputStream zipInputStream) {
    try {
      ZipEntry entry = zipInputStream.getNextEntry();
      while (entry != null) {
        if (!entry.isDirectory()) {
          String entryName = entry.getName();
          byte[] bytes = IoUtil.readInputStream(zipInputStream, entryName);
          ResourceEntity resource = new ResourceEntity();
          resource.setName(entryName);
          resource.setBytes(bytes);
          deployment.addResource(resource);
        }
        entry = zipInputStream.getNextEntry();
      }
    } catch (Exception e) {
      throw new ActivitiException("problem reading zip input stream", e);
    }
    return this;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.repository.DeploymentBuilder#name(java.lang.String)
   */
  public DeploymentBuilder name(String name) {
    deployment.setName(name);
    return this;
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.repository.DeploymentBuilder#enableDuplicateFiltering()
   */
  public DeploymentBuilder enableDuplicateFiltering() {
    isDuplicateFilterEnabled = true;
    return this;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.repository.DeploymentBuilder#deploy()
   */
  public Deployment deploy() {
    return repositoryService.deploy(this);
  }
  
  // getters and setters //////////////////////////////////////////////////////
  
  /**
   * Gets the deployment.
   *
   * @return the deployment
   */
  public DeploymentEntity getDeployment() {
    return deployment;
  }
  
  /**
   * Checks if is duplicate filter enabled.
   *
   * @return true, if is duplicate filter enabled
   */
  public boolean isDuplicateFilterEnabled() {
    return isDuplicateFilterEnabled;
  }
}
