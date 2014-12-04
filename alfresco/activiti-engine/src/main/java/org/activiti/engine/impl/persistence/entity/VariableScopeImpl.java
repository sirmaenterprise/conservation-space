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
package org.activiti.engine.impl.persistence.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.delegate.VariableScope;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.javax.el.ELContext;
import org.activiti.engine.impl.variable.VariableType;
import org.activiti.engine.impl.variable.VariableTypes;



// TODO: Auto-generated Javadoc
/**
 * The Class VariableScopeImpl.
 *
 * @author Tom Baeyens
 * @author Joram Barrez
 */
public abstract class VariableScopeImpl implements Serializable, VariableScope {
  
  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = 1L;
  
  /** The variable instances. */
  protected Map<String, VariableInstanceEntity> variableInstances = null;
  
  /** The cached el context. */
  protected ELContext cachedElContext;

  /**
   * Load variable instances.
   *
   * @return the list
   */
  protected abstract List<VariableInstanceEntity> loadVariableInstances();
  
  /**
   * Gets the parent variable scope.
   *
   * @return the parent variable scope
   */
  protected abstract VariableScopeImpl getParentVariableScope();
  
  /**
   * Initialize variable instance back pointer.
   *
   * @param variableInstance the variable instance
   */
  protected abstract void initializeVariableInstanceBackPointer(VariableInstanceEntity variableInstance);

  /**
   * Ensure variable instances initialized.
   */
  protected void ensureVariableInstancesInitialized() {
    if (variableInstances==null) {
      variableInstances = new HashMap<String, VariableInstanceEntity>();
      CommandContext commandContext = Context.getCommandContext();
      if (commandContext == null) {
        throw new ActivitiException("lazy loading outside command context");
      }
      List<VariableInstanceEntity> variableInstancesList = loadVariableInstances();
      for (VariableInstanceEntity variableInstance : variableInstancesList) {
        variableInstances.put(variableInstance.getName(), variableInstance);
      }
    }
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.delegate.VariableScope#getVariables()
   */
  public Map<String, Object> getVariables() {
    return collectVariables(new HashMap<String, Object>());
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.delegate.VariableScope#getVariablesLocal()
   */
  public Map<String, Object> getVariablesLocal() {
    Map<String, Object> variables = new HashMap<String, Object>();
    ensureVariableInstancesInitialized();
    for (VariableInstanceEntity variableInstance: variableInstances.values()) {
      variables.put(variableInstance.getName(), variableInstance.getValue());
    }
    return variables;
  }

  /**
   * Collect variables.
   *
   * @param variables the variables
   * @return the map
   */
  protected Map<String, Object> collectVariables(HashMap<String, Object> variables) {
    ensureVariableInstancesInitialized();
    VariableScopeImpl parentScope = getParentVariableScope();
    if (parentScope!=null) {
      variables.putAll(parentScope.collectVariables(variables));
    }
    for (VariableInstanceEntity variableInstance: variableInstances.values()) {
      variables.put(variableInstance.getName(), variableInstance.getValue());
    }
    return variables;
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.delegate.VariableScope#getVariable(java.lang.String)
   */
  public Object getVariable(String variableName) {
    ensureVariableInstancesInitialized();
    VariableInstanceEntity variableInstance = variableInstances.get(variableName);
    if (variableInstance!=null) {
      return variableInstance.getValue();
    }
    VariableScope parentScope = getParentVariableScope();
    if (parentScope!=null) {
      return parentScope.getVariable(variableName);
    }
    return null;
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.delegate.VariableScope#getVariableLocal(java.lang.Object)
   */
  public Object getVariableLocal(Object variableName) {
    ensureVariableInstancesInitialized();
    VariableInstanceEntity variableInstance = variableInstances.get(variableName);
    if (variableInstance!=null) {
      return variableInstance.getValue();
    }
    return null;
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.delegate.VariableScope#hasVariables()
   */
  public boolean hasVariables() {
    ensureVariableInstancesInitialized();
    if (!variableInstances.isEmpty()) {
      return true;
    }
    VariableScope parentScope = getParentVariableScope();
    if (parentScope!=null) {
      return parentScope.hasVariables();
    }
    return false;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.delegate.VariableScope#hasVariablesLocal()
   */
  public boolean hasVariablesLocal() {
    ensureVariableInstancesInitialized();
    return !variableInstances.isEmpty();
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.delegate.VariableScope#hasVariable(java.lang.String)
   */
  public boolean hasVariable(String variableName) {
    if (hasVariableLocal(variableName)) {
      return true;
    }
    VariableScope parentScope = getParentVariableScope();
    if (parentScope!=null) {
      return parentScope.hasVariable(variableName);
    }
    return false;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.delegate.VariableScope#hasVariableLocal(java.lang.String)
   */
  public boolean hasVariableLocal(String variableName) {
    ensureVariableInstancesInitialized();
    return variableInstances.containsKey(variableName);
  }

  /**
   * Collect variable names.
   *
   * @param variableNames the variable names
   * @return the sets the
   */
  protected Set<String> collectVariableNames(Set<String> variableNames) {
    ensureVariableInstancesInitialized();
    VariableScopeImpl parentScope = getParentVariableScope();
    if (parentScope!=null) {
      variableNames.addAll(parentScope.collectVariableNames(variableNames));
    }
    for (VariableInstanceEntity variableInstance: variableInstances.values()) {
      variableNames.add(variableInstance.getName());
    }
    return variableNames;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.delegate.VariableScope#getVariableNames()
   */
  public Set<String> getVariableNames() {
    return collectVariableNames(new HashSet<String>());
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.delegate.VariableScope#getVariableNamesLocal()
   */
  public Set<String> getVariableNamesLocal() {
    ensureVariableInstancesInitialized();
    return variableInstances.keySet();
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.delegate.VariableScope#setVariable(java.lang.String, java.lang.Object)
   */
  public void setVariable(String variableName, Object value) {
    if (hasVariableLocal(variableName)) {
      setVariableLocal(variableName, value);
      return;
    } 
    VariableScope parentVariableScope = getParentVariableScope();
    if (parentVariableScope!=null) {
      parentVariableScope.setVariable(variableName, value);
      return;
    }
    createVariableLocal(variableName, value);
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.delegate.VariableScope#setVariableLocal(java.lang.String, java.lang.Object)
   */
  public Object setVariableLocal(String variableName, Object value) {
    ensureVariableInstancesInitialized();
    VariableInstanceEntity variableInstance = variableInstances.get(variableName);
    if ((variableInstance != null) && (!variableInstance.getType().isAbleToStore(value))) {
      // delete variable
      removeVariable(variableName);
      variableInstance = null;
    }
    if (variableInstance == null) {
      createVariableLocal(variableName, value);
    } else {
      setVariableInstanceValue(value, variableInstance);
    }
    
    return null;
  }

  /**
   * Sets the variable instance value.
   *
   * @param value the value
   * @param variableInstance the variable instance
   */
  protected void setVariableInstanceValue(Object value, VariableInstanceEntity variableInstance) {
    variableInstance.setValue(value);
    
    int historyLevel = Context.getProcessEngineConfiguration().getHistoryLevel();
    if (historyLevel==ProcessEngineConfigurationImpl.HISTORYLEVEL_FULL) {
      HistoricVariableUpdateEntity historicVariableUpdate = new HistoricVariableUpdateEntity(variableInstance);
      initializeActivityInstanceId(historicVariableUpdate);
      Context
        .getCommandContext()
        .getDbSqlSession()
        .insert(historicVariableUpdate);
    }
  }
  
  /**
   * Initialize activity instance id.
   *
   * @param historicVariableUpdate the historic variable update
   */
  protected void initializeActivityInstanceId(HistoricVariableUpdateEntity historicVariableUpdate) {
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.delegate.VariableScope#createVariableLocal(java.lang.String, java.lang.Object)
   */
  public void createVariableLocal(String variableName, Object value) {
    ensureVariableInstancesInitialized();
    
    if (variableInstances.containsKey(variableName)) {
      throw new ActivitiException("variable '"+variableName+"' already exists. Use setVariableLocal if you want to overwrite the value");
    }
    
    VariableTypes variableTypes = Context
      .getProcessEngineConfiguration()
      .getVariableTypes();
    
    VariableType type = variableTypes.findVariableType(value);
 
    VariableInstanceEntity variableInstance = VariableInstanceEntity.createAndInsert(variableName, type, value);
    initializeVariableInstanceBackPointer(variableInstance);
    variableInstances.put(variableName, variableInstance);
    
    setVariableInstanceValue(value, variableInstance);
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.delegate.VariableScope#createVariablesLocal(java.util.Map)
   */
  public void createVariablesLocal(Map<String, ? extends Object> variables) {
    if (variables!=null) {
      for (Map.Entry<String, ? extends Object> entry: variables.entrySet()) {
        createVariableLocal(entry.getKey(), entry.getValue());
      }
    }
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.delegate.VariableScope#removeVariable(java.lang.String)
   */
  public void removeVariable(String variableName) {
    ensureVariableInstancesInitialized();
    VariableInstanceEntity variableInstance = variableInstances.remove(variableName);
    if (variableInstance != null) {
      variableInstance.delete();
      return;
    }
    VariableScope parentVariableScope = getParentVariableScope();
    if (parentVariableScope!=null) {
      parentVariableScope.removeVariable(variableName);
    }
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.delegate.VariableScope#removeVariableLocal(java.lang.String)
   */
  public void removeVariableLocal(String variableName) {
    ensureVariableInstancesInitialized();
    VariableInstanceEntity variableInstance = variableInstances.remove(variableName);
    if (variableInstance != null) {
      variableInstance.delete();
    }
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.delegate.VariableScope#setVariables(java.util.Map)
   */
  public void setVariables(Map<String, ? extends Object> variables) {
    if (variables!=null) {
      for (String variableName : variables.keySet()) {
        setVariable(variableName, variables.get(variableName));
      }
    }
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.delegate.VariableScope#setVariablesLocal(java.util.Map)
   */
  public void setVariablesLocal(Map<String, ? extends Object> variables) {
    if (variables!=null) {
      for (String variableName : variables.keySet()) {
        setVariableLocal(variableName, variables.get(variableName));
      }
    }
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.delegate.VariableScope#removeVariables()
   */
  public void removeVariables() {
    ensureVariableInstancesInitialized();
    Set<String> variableNames = new HashSet<String>(variableInstances.keySet());
    for (String variableName: variableNames) {
      removeVariable(variableName);
    }
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.delegate.VariableScope#removeVariablesLocal()
   */
  public void removeVariablesLocal() {
    List<String> variableNames = new ArrayList<String>(getVariableNamesLocal());
    for (String variableName: variableNames) {
      removeVariableLocal(variableName);
    }
  }

  /**
   * Gets the cached el context.
   *
   * @return the cached el context
   */
  public ELContext getCachedElContext() {
    return cachedElContext;
  }
  
  /**
   * Sets the cached el context.
   *
   * @param cachedElContext the new cached el context
   */
  public void setCachedElContext(ELContext cachedElContext) {
    this.cachedElContext = cachedElContext;
  }
}
