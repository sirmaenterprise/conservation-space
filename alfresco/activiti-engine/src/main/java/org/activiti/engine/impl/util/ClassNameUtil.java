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

package org.activiti.engine.impl.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


// TODO: Auto-generated Javadoc
/**
 * The Class ClassNameUtil.
 *
 * @author Tom Baeyens
 */
public abstract class ClassNameUtil {

  /** The Constant cachedNames. */
  protected static final Map<Class<?>, String> cachedNames = new ConcurrentHashMap<Class<?>, String>();  
  
  /**
   * Gets the class name without package.
   *
   * @param object the object
   * @return the class name without package
   */
  public static String getClassNameWithoutPackage(Object object) {
    return getClassNameWithoutPackage(object.getClass());
  }

  /**
   * Gets the class name without package.
   *
   * @param clazz the clazz
   * @return the class name without package
   */
  public static String getClassNameWithoutPackage(Class<?> clazz) {
    String unqualifiedClassName = cachedNames.get(clazz);
    if (unqualifiedClassName==null) {
      String fullyQualifiedClassName = clazz.getName();
      unqualifiedClassName = fullyQualifiedClassName.substring(fullyQualifiedClassName.lastIndexOf('.')+1);
      cachedNames.put(clazz, unqualifiedClassName);
    }
    return unqualifiedClassName;
  }
}
