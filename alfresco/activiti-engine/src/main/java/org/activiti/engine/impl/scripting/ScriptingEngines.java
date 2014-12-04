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

import java.util.List;
import java.util.logging.Logger;

import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.delegate.VariableScope;

// TODO: Auto-generated Javadoc
/**
 * The Class ScriptingEngines.
 *
 * @author Tom Baeyens
 */
public class ScriptingEngines {

  /** The log. */
  private static Logger log = Logger.getLogger(ScriptingEngines.class.getName());
  
  /** The Constant DEFAULT_SCRIPTING_LANGUAGE. */
  public static final String DEFAULT_SCRIPTING_LANGUAGE = "juel";

  /** The script engine manager. */
  private final ScriptEngineManager scriptEngineManager;
  
  /** The script bindings factory. */
  protected ScriptBindingsFactory scriptBindingsFactory;

  /**
   * Instantiates a new scripting engines.
   *
   * @param scriptBindingsFactory the script bindings factory
   */
  public ScriptingEngines(ScriptBindingsFactory scriptBindingsFactory) {
    this(new ScriptEngineManager());
    this.scriptBindingsFactory = scriptBindingsFactory;
  }

  /**
   * Instantiates a new scripting engines.
   *
   * @param scriptEngineManager the script engine manager
   */
  public ScriptingEngines(ScriptEngineManager scriptEngineManager) {
    this.scriptEngineManager = scriptEngineManager;
  }

  /**
   * Adds the script engine factory.
   *
   * @param scriptEngineFactory the script engine factory
   * @return the scripting engines
   */
  public ScriptingEngines addScriptEngineFactory(ScriptEngineFactory scriptEngineFactory) {
    scriptEngineManager.registerEngineName(scriptEngineFactory.getEngineName(), scriptEngineFactory);
    return this;
  }

  /**
   * Sets the script engine factories.
   *
   * @param scriptEngineFactories the new script engine factories
   */
  public void setScriptEngineFactories(List<ScriptEngineFactory> scriptEngineFactories) {
    if (scriptEngineFactories != null) {
      for (ScriptEngineFactory scriptEngineFactory : scriptEngineFactories) {
        scriptEngineManager.registerEngineName(scriptEngineFactory.getEngineName(), scriptEngineFactory);
      }
    }
  }

  /**
   * Evaluate.
   *
   * @param script the script
   * @param language the language
   * @param variableScope the variable scope
   * @return the object
   */
  public Object evaluate(String script, String language, VariableScope variableScope) {
    Bindings bindings = createBindings(variableScope);
    ScriptEngine scriptEngine = scriptEngineManager.getEngineByName(language);

    if (scriptEngine == null) {
      throw new ActivitiException("Can't find scripting engine for '" + language + "'");
    }

    try {
      return scriptEngine.eval(script, bindings);
    } catch (ScriptException e) {
      throw new ActivitiException("problem evaluating script: " + e.getMessage(), e);
    }
  }

  /**
   * override to build a spring aware ScriptingEngines.
   *
   * @param variableScope the variable scope
   * @return the bindings
   */
  protected Bindings createBindings(VariableScope variableScope) {
    return scriptBindingsFactory.createBindings(variableScope); 
  }
  
  /**
   * Gets the script bindings factory.
   *
   * @return the script bindings factory
   */
  public ScriptBindingsFactory getScriptBindingsFactory() {
    return scriptBindingsFactory;
  }
  
  /**
   * Sets the script bindings factory.
   *
   * @param scriptBindingsFactory the new script bindings factory
   */
  public void setScriptBindingsFactory(ScriptBindingsFactory scriptBindingsFactory) {
    this.scriptBindingsFactory = scriptBindingsFactory;
  }
}
