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
package org.activiti.engine.impl.util.io;

import java.io.InputStream;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.impl.util.ReflectUtil;


// TODO: Auto-generated Javadoc
/**
 * The Class ResourceStreamSource.
 *
 * @author Tom Baeyens
 */
public class ResourceStreamSource implements StreamSource {

  /** The resource. */
  String resource;
  
  /** The class loader. */
  ClassLoader classLoader;
  
  /**
   * Instantiates a new resource stream source.
   *
   * @param resource the resource
   */
  public ResourceStreamSource(String resource) {
    this.resource = resource;
  }

  /**
   * Instantiates a new resource stream source.
   *
   * @param resource the resource
   * @param classLoader the class loader
   */
  public ResourceStreamSource(String resource, ClassLoader classLoader) {
    this.resource = resource;
    this.classLoader = classLoader;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.impl.util.io.StreamSource#getInputStream()
   */
  public InputStream getInputStream() {
    InputStream inputStream = null;
    if (classLoader==null) {
      inputStream = ReflectUtil.getResourceAsStream(resource);
    } else {
      classLoader.getResourceAsStream(resource);
    }
    if (inputStream==null) {
      throw new ActivitiException("resource '"+resource+"' doesn't exist");
    }
    return inputStream;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  public String toString() {
    return "Resource["+resource+"]";
  }
}
