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

package org.activiti.engine.test.mock;

import java.beans.FeatureDescriptor;
import java.util.Iterator;

import org.activiti.engine.impl.javax.el.ELContext;
import org.activiti.engine.impl.javax.el.ELResolver;

// TODO: Auto-generated Javadoc
/**
 * The Class MockElResolver.
 */
public class MockElResolver extends ELResolver {

  /* (non-Javadoc)
   * @see org.activiti.engine.impl.javax.el.ELResolver#getCommonPropertyType(org.activiti.engine.impl.javax.el.ELContext, java.lang.Object)
   */
  @Override
  public Class< ? > getCommonPropertyType(ELContext context, Object base) {
    return Object.class;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.impl.javax.el.ELResolver#getFeatureDescriptors(org.activiti.engine.impl.javax.el.ELContext, java.lang.Object)
   */
  @Override
  public Iterator<FeatureDescriptor> getFeatureDescriptors(ELContext context, Object base) {
    return null;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.impl.javax.el.ELResolver#getType(org.activiti.engine.impl.javax.el.ELContext, java.lang.Object, java.lang.Object)
   */
  @Override
  public Class< ? > getType(ELContext context, Object base, Object property) {
    return null;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.impl.javax.el.ELResolver#getValue(org.activiti.engine.impl.javax.el.ELContext, java.lang.Object, java.lang.Object)
   */
  @Override
  public Object getValue(ELContext context, Object base, Object property) {
    Object bean = Mocks.get(property);
    if (bean != null) {
      context.setPropertyResolved(true);
    }
    return bean;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.impl.javax.el.ELResolver#isReadOnly(org.activiti.engine.impl.javax.el.ELContext, java.lang.Object, java.lang.Object)
   */
  @Override
  public boolean isReadOnly(ELContext context, Object base, Object property) {
    return false;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.impl.javax.el.ELResolver#setValue(org.activiti.engine.impl.javax.el.ELContext, java.lang.Object, java.lang.Object, java.lang.Object)
   */
  @Override
  public void setValue(ELContext context, Object base, Object property, Object value) {
  }

}
