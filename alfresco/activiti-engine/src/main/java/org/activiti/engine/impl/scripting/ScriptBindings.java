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

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.script.Bindings;

import org.activiti.engine.delegate.VariableScope;


// TODO: Auto-generated Javadoc
/**
 * The Class ScriptBindings.
 *
 * @author Tom Baeyens
 */
public class ScriptBindings implements Bindings {

  /**
   * The script engine implementations put some key/value pairs into the binding.
   * This list contains those keys, such that they wouldn't be stored as process variable.
   * 
   * This list contains the keywords for JUEL, Javascript and Groovy.
   */
  protected static final Set<String> UNSTORED_KEYS = 
    new HashSet<String>(Arrays.asList("out", "out:print", "lang:import", "context", "elcontext", "print", "println"));

  /** The script resolvers. */
  protected List<Resolver> scriptResolvers;
  
  /** The variable scope. */
  protected VariableScope variableScope;

  /**
   * Instantiates a new script bindings.
   *
   * @param scriptResolvers the script resolvers
   * @param variableScope the variable scope
   */
  public ScriptBindings(List<Resolver> scriptResolvers, VariableScope variableScope) {
    this.scriptResolvers = scriptResolvers;
    this.variableScope = variableScope;
  }

  /* (non-Javadoc)
   * @see javax.script.Bindings#containsKey(java.lang.Object)
   */
  public boolean containsKey(Object key) {
    for (Resolver scriptResolver: scriptResolvers) {
      if (scriptResolver.containsKey(key)) {
        return true;
      }
    }
    return false;
  }

  /* (non-Javadoc)
   * @see javax.script.Bindings#get(java.lang.Object)
   */
  public Object get(Object key) {
    for (Resolver scriptResolver: scriptResolvers) {
      if (scriptResolver.containsKey(key)) {
        return scriptResolver.get(key);
      }
    }
    return null;
  }

  /* (non-Javadoc)
   * @see javax.script.Bindings#put(java.lang.String, java.lang.Object)
   */
  public Object put(String name, Object value) {
    Object oldValue = null;
    if (!UNSTORED_KEYS.contains(name)) {
      oldValue = variableScope.getVariable(name);
      variableScope.setVariable(name, value);
    }
    return oldValue;
  }

  /* (non-Javadoc)
   * @see java.util.Map#entrySet()
   */
  public Set<java.util.Map.Entry<String, Object>> entrySet() {
    throw new UnsupportedOperationException();
//    return variableScope.getVariables().entrySet();
  }

  /* (non-Javadoc)
   * @see java.util.Map#keySet()
   */
  public Set<String> keySet() {
    throw new UnsupportedOperationException();
//    return variableScope.getVariables().keySet();
  }

  /* (non-Javadoc)
   * @see java.util.Map#size()
   */
  public int size() {
    throw new UnsupportedOperationException();
//    return variableScope.getVariables().size();
  }

  /* (non-Javadoc)
   * @see java.util.Map#values()
   */
  public Collection<Object> values() {
    throw new UnsupportedOperationException();
//    return variableScope.getVariables().values();
  }

  /* (non-Javadoc)
   * @see javax.script.Bindings#putAll(java.util.Map)
   */
  public void putAll(Map< ? extends String, ? extends Object> toMerge) {
    throw new UnsupportedOperationException();
  }

  /* (non-Javadoc)
   * @see javax.script.Bindings#remove(java.lang.Object)
   */
  public Object remove(Object key) {
    if (UNSTORED_KEYS.contains(key)) {
      return null;
    }
    throw new UnsupportedOperationException();
  }

  /* (non-Javadoc)
   * @see java.util.Map#clear()
   */
  public void clear() {
    throw new UnsupportedOperationException();
  }

  /* (non-Javadoc)
   * @see java.util.Map#containsValue(java.lang.Object)
   */
  public boolean containsValue(Object value) {
    throw new UnsupportedOperationException();
  }

  /* (non-Javadoc)
   * @see java.util.Map#isEmpty()
   */
  public boolean isEmpty() {
    throw new UnsupportedOperationException();
  }

}
