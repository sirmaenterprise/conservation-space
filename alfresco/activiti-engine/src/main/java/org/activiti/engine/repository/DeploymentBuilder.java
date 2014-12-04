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
package org.activiti.engine.repository;

import java.io.InputStream;
import java.util.zip.ZipInputStream;

// TODO: Auto-generated Javadoc
/**
 * Builder for creating new deployments.
 * 
 * A builder instance can be obtained through {@link org.activiti.engine.RuntimeService#createDeployment()}.
 * 
 * Multiple resources can be added to one deployment before calling the {@link #deploy()}
 * operation.
 * 
 * After deploying, no more changes can be made to the returned deployment
 * and the builder instance can be disposed.
 *
 * @author Tom Baeyens
 * @author Joram Barrez
 */
public interface DeploymentBuilder {
  
  /**
   * Adds the input stream.
   *
   * @param resourceName the resource name
   * @param inputStream the input stream
   * @return the deployment builder
   */
  DeploymentBuilder addInputStream(String resourceName, InputStream inputStream);
  
  /**
   * Adds the classpath resource.
   *
   * @param resource the resource
   * @return the deployment builder
   */
  DeploymentBuilder addClasspathResource(String resource);
  
  /**
   * Adds the string.
   *
   * @param resourceName the resource name
   * @param text the text
   * @return the deployment builder
   */
  DeploymentBuilder addString(String resourceName, String text);
  
  /**
   * Adds the zip input stream.
   *
   * @param zipInputStream the zip input stream
   * @return the deployment builder
   */
  DeploymentBuilder addZipInputStream(ZipInputStream zipInputStream);
  
  /**
   * Name.
   *
   * @param name the name
   * @return the deployment builder
   */
  DeploymentBuilder name(String name);
  
  /**
   * Enable duplicate filtering.
   *
   * @return the deployment builder
   */
  DeploymentBuilder enableDuplicateFiltering();

  /**
   * Deploy.
   *
   * @return the deployment
   */
  Deployment deploy();
}
