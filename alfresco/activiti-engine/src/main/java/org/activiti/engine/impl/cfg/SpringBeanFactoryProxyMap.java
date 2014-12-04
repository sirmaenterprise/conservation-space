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

package org.activiti.engine.impl.cfg;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.activiti.engine.ActivitiException;
import org.springframework.beans.factory.BeanFactory;


// TODO: Auto-generated Javadoc
/**
 * The Class SpringBeanFactoryProxyMap.
 *
 * @author Tom Baeyens
 */
public class SpringBeanFactoryProxyMap implements Map<Object, Object> {

  /** The bean factory. */
  protected BeanFactory beanFactory;
  
  /**
   * Instantiates a new spring bean factory proxy map.
   *
   * @param beanFactory the bean factory
   */
  public SpringBeanFactoryProxyMap(BeanFactory beanFactory) {
    this.beanFactory = beanFactory;
  }

  /* (non-Javadoc)
   * @see java.util.Map#get(java.lang.Object)
   */
  public Object get(Object key) {
    if ( (key==null) || (!String.class.isAssignableFrom(key.getClass())) ) {
      return null;
    }
    return beanFactory.getBean((String) key);
  }

  /* (non-Javadoc)
   * @see java.util.Map#containsKey(java.lang.Object)
   */
  public boolean containsKey(Object key) {
    if ( (key==null) || (!String.class.isAssignableFrom(key.getClass())) ) {
      return false;
    }
    return beanFactory.containsBean((String) key);
  }

  /* (non-Javadoc)
   * @see java.util.Map#keySet()
   */
  public Set<Object> keySet() {
    throw new ActivitiException("unsupported operation on configuration beans");
//    List<String> beanNames = Arrays.asList(beanFactory.getBeanDefinitionNames());
//    return new HashSet<Object>(beanNames);
  }

  /* (non-Javadoc)
   * @see java.util.Map#clear()
   */
  public void clear() {
    throw new ActivitiException("can't clear configuration beans");
  }

  /* (non-Javadoc)
   * @see java.util.Map#containsValue(java.lang.Object)
   */
  public boolean containsValue(Object value) {
    throw new ActivitiException("can't search values in configuration beans");
  }

  /* (non-Javadoc)
   * @see java.util.Map#entrySet()
   */
  public Set<java.util.Map.Entry<Object, Object>> entrySet() {
    throw new ActivitiException("unsupported operation on configuration beans");
  }

  /* (non-Javadoc)
   * @see java.util.Map#isEmpty()
   */
  public boolean isEmpty() {
    throw new ActivitiException("unsupported operation on configuration beans");
  }

  /* (non-Javadoc)
   * @see java.util.Map#put(java.lang.Object, java.lang.Object)
   */
  public Object put(Object key, Object value) {
    throw new ActivitiException("unsupported operation on configuration beans");
  }

  /* (non-Javadoc)
   * @see java.util.Map#putAll(java.util.Map)
   */
  public void putAll(Map< ? extends Object, ? extends Object> m) {
    throw new ActivitiException("unsupported operation on configuration beans");
  }

  /* (non-Javadoc)
   * @see java.util.Map#remove(java.lang.Object)
   */
  public Object remove(Object key) {
    throw new ActivitiException("unsupported operation on configuration beans");
  }

  /* (non-Javadoc)
   * @see java.util.Map#size()
   */
  public int size() {
    throw new ActivitiException("unsupported operation on configuration beans");
  }

  /* (non-Javadoc)
   * @see java.util.Map#values()
   */
  public Collection<Object> values() {
    throw new ActivitiException("unsupported operation on configuration beans");
  }
}
