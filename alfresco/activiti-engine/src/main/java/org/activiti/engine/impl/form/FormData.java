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
package org.activiti.engine.impl.form;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.activiti.engine.impl.persistence.entity.TaskEntity;


// TODO: Auto-generated Javadoc
/**
 * The Class FormData.
 *
 * @author Tom Baeyens
 */
public class FormData implements Map<String, Object> {
  
  /** The task. */
  TaskEntity task;

  /**
   * Instantiates a new form data.
   *
   * @param task the task
   */
  public FormData(TaskEntity task) {
    this.task = task;
  }

  /* (non-Javadoc)
   * @see java.util.Map#clear()
   */
  public void clear() {
  }

  /* (non-Javadoc)
   * @see java.util.Map#containsKey(java.lang.Object)
   */
  public boolean containsKey(Object key) {
    return false;
  }

  /* (non-Javadoc)
   * @see java.util.Map#containsValue(java.lang.Object)
   */
  public boolean containsValue(Object value) {
    return false;
  }

  /* (non-Javadoc)
   * @see java.util.Map#entrySet()
   */
  public Set<java.util.Map.Entry<String, Object>> entrySet() {
    return null;
  }

  /* (non-Javadoc)
   * @see java.util.Map#get(java.lang.Object)
   */
  public Object get(Object key) {
    return null;
  }

  /* (non-Javadoc)
   * @see java.util.Map#isEmpty()
   */
  public boolean isEmpty() {
    return false;
  }

  /* (non-Javadoc)
   * @see java.util.Map#keySet()
   */
  public Set<String> keySet() {
    return null;
  }

  /* (non-Javadoc)
   * @see java.util.Map#put(java.lang.Object, java.lang.Object)
   */
  public Object put(String key, Object value) {
    return null;
  }

  /* (non-Javadoc)
   * @see java.util.Map#putAll(java.util.Map)
   */
  public void putAll(Map< ? extends String, ? extends Object> m) {
  }

  /* (non-Javadoc)
   * @see java.util.Map#remove(java.lang.Object)
   */
  public Object remove(Object key) {
    return null;
  }

  /* (non-Javadoc)
   * @see java.util.Map#size()
   */
  public int size() {
    return 0;
  }

  /* (non-Javadoc)
   * @see java.util.Map#values()
   */
  public Collection<Object> values() {
    return null;
  }

}
