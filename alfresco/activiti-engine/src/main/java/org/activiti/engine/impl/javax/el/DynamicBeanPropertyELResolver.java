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
package org.activiti.engine.impl.javax.el;

import java.beans.FeatureDescriptor;
import java.util.Iterator;

import org.activiti.engine.impl.util.ReflectUtil;

// TODO: Auto-generated Javadoc
/**
 * A {@link ELResolver} for dynamic bean properties.
 *
 * @author Esteban Robles Luna
 */
public class DynamicBeanPropertyELResolver extends ELResolver {

  /** The subject. */
  protected Class<?> subject;
  
  /** The read method name. */
  protected String readMethodName;
  
  /** The write method name. */
  protected String writeMethodName;
  
  /** The read only. */
  protected boolean readOnly;
  
  /**
   * Instantiates a new dynamic bean property el resolver.
   *
   * @param readOnly the read only
   * @param subject the subject
   * @param readMethodName the read method name
   * @param writeMethodName the write method name
   */
  public DynamicBeanPropertyELResolver(boolean readOnly, Class<?> subject, String readMethodName, String writeMethodName) {
    this.readOnly = readOnly;
    this.subject = subject;
    this.readMethodName = readMethodName;
    this.writeMethodName = writeMethodName;
  }

  /**
   * Instantiates a new dynamic bean property el resolver.
   *
   * @param subject the subject
   * @param readMethodName the read method name
   * @param writeMethodName the write method name
   */
  public DynamicBeanPropertyELResolver(Class<?> subject, String readMethodName, String writeMethodName) {
    this(false, subject, readMethodName, writeMethodName);
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.impl.javax.el.ELResolver#getCommonPropertyType(org.activiti.engine.impl.javax.el.ELContext, java.lang.Object)
   */
  @Override
  public Class<?> getCommonPropertyType(ELContext context, Object base) {
    if (this.subject.isInstance(base)) {
      return Object.class;
    } else {
      return null;
    }
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
  public Class<?> getType(ELContext context, Object base, Object property) {
    return Object.class;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.impl.javax.el.ELResolver#getValue(org.activiti.engine.impl.javax.el.ELContext, java.lang.Object, java.lang.Object)
   */
  @Override
  public Object getValue(ELContext context, Object base, Object property) {
    if (base == null || this.getCommonPropertyType(context, base) == null) {
      return null;
    }
    
    String propertyName = property.toString();

    try {
      Object value = ReflectUtil.invoke(base, this.readMethodName, new Object[] { propertyName });
      context.setPropertyResolved(true);
      return value;
    } catch (Exception e) {
      throw new ELException(e);
    }
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.impl.javax.el.ELResolver#isReadOnly(org.activiti.engine.impl.javax.el.ELContext, java.lang.Object, java.lang.Object)
   */
  @Override
  public boolean isReadOnly(ELContext context, Object base, Object property) {
    return this.readOnly;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.impl.javax.el.ELResolver#setValue(org.activiti.engine.impl.javax.el.ELContext, java.lang.Object, java.lang.Object, java.lang.Object)
   */
  @Override
  public void setValue(ELContext context, Object base, Object property, Object value) {
    if (base == null || this.getCommonPropertyType(context, base) == null) {
      return;
    }
    
    String propertyName = property.toString();
    try {
      ReflectUtil.invoke(base, this.writeMethodName, new Object[] { propertyName, value });
      context.setPropertyResolved(true);
    } catch (Exception e) {
      throw new ELException(e);
    }
  }
}
