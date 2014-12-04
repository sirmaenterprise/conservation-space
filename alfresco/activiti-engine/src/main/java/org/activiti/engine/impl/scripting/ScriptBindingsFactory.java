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

import java.util.ArrayList;
import java.util.List;

import javax.script.Bindings;

import org.activiti.engine.delegate.VariableScope;


// TODO: Auto-generated Javadoc
/**
 * A factory for creating ScriptBindings objects.
 *
 * @author Tom Baeyens
 */
public class ScriptBindingsFactory {
  
  /** The resolver factories. */
  protected List<ResolverFactory> resolverFactories;
  
  /**
   * Instantiates a new script bindings factory.
   *
   * @param resolverFactories the resolver factories
   */
  public ScriptBindingsFactory(List<ResolverFactory> resolverFactories) {
    this.resolverFactories = resolverFactories;
  }

  /**
   * Creates a new ScriptBindings object.
   *
   * @param variableScope the variable scope
   * @return the bindings
   */
  public Bindings createBindings(VariableScope variableScope) {
    List<Resolver> scriptResolvers = new ArrayList<Resolver>();
    for (ResolverFactory scriptResolverFactory: resolverFactories) {
      Resolver resolver = scriptResolverFactory.createResolver(variableScope);
      if (resolver!=null) {
        scriptResolvers.add(resolver);
      }
    }
    return new ScriptBindings(scriptResolvers, variableScope);
  }
  
  /**
   * Gets the resolver factories.
   *
   * @return the resolver factories
   */
  public List<ResolverFactory> getResolverFactories() {
    return resolverFactories;
  }
  
  /**
   * Sets the resolver factories.
   *
   * @param resolverFactories the new resolver factories
   */
  public void setResolverFactories(List<ResolverFactory> resolverFactories) {
    this.resolverFactories = resolverFactories;
  }
}
