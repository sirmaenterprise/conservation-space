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

package org.activiti.engine.impl.scripting;

import org.activiti.engine.delegate.VariableScope;
import org.activiti.engine.impl.context.Context;


// TODO: Auto-generated Javadoc
/**
 * A factory for creating BeansResolver objects.
 *
 * @author Tom Baeyens
 */
public class BeansResolverFactory implements ResolverFactory, Resolver {

  /* (non-Javadoc)
   * @see org.activiti.engine.impl.scripting.ResolverFactory#createResolver(org.activiti.engine.delegate.VariableScope)
   */
  public Resolver createResolver(VariableScope variableScope) {
    return this;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.impl.scripting.Resolver#containsKey(java.lang.Object)
   */
  public boolean containsKey(Object key) {
    return Context.getProcessEngineConfiguration().getBeans().containsKey(key);
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.impl.scripting.Resolver#get(java.lang.Object)
   */
  public Object get(Object key) {
    return Context.getProcessEngineConfiguration().getBeans().get(key);
  }
}
