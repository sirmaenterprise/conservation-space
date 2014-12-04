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

package org.activiti.engine.impl;

import java.util.ArrayList;
import java.util.List;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.interceptor.CommandExecutor;
import org.activiti.engine.impl.variable.VariableTypes;
import org.activiti.engine.query.Query;


// TODO: Auto-generated Javadoc
/**
 * Abstract query class that adds methods to query for variable values.
 *
 * @param <T> the generic type
 * @param <U> the generic type
 * @author Frederik Heremans
 */
public abstract class ExecutionVariableQueryImpl<T extends Query<?,?>, U> extends AbstractQuery<T, U> {

  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = 1L;
  
  /** The query variable values. */
  protected List<QueryVariableValue> queryVariableValues = new ArrayList<QueryVariableValue>();
  
  /**
   * Instantiates a new execution variable query impl.
   */
  public ExecutionVariableQueryImpl() {
  }

  /**
   * Instantiates a new execution variable query impl.
   *
   * @param commandContext the command context
   */
  public ExecutionVariableQueryImpl(CommandContext commandContext) {
    super(commandContext);
  }

  /**
   * Instantiates a new execution variable query impl.
   *
   * @param commandExecutor the command executor
   */
  public ExecutionVariableQueryImpl(CommandExecutor commandExecutor) {
    super(commandExecutor);
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.impl.AbstractQuery#executeCount(org.activiti.engine.impl.interceptor.CommandContext)
   */
  @Override
  public abstract long executeCount(CommandContext commandContext) ;

  /* (non-Javadoc)
   * @see org.activiti.engine.impl.AbstractQuery#executeList(org.activiti.engine.impl.interceptor.CommandContext, org.activiti.engine.impl.Page)
   */
  @Override
  public abstract List<U> executeList(CommandContext commandContext, Page page);
  
  
  /**
   * Variable value equals.
   *
   * @param name the name
   * @param value the value
   * @return the t
   */
  @SuppressWarnings("unchecked")
  public T variableValueEquals(String name, Object value) {
    addVariable(name, value, QueryOperator.EQUALS);
    return (T) this;
  }
  
  /**
   * Variable value not equals.
   *
   * @param name the name
   * @param value the value
   * @return the t
   */
  @SuppressWarnings("unchecked")
  public T variableValueNotEquals(String name, Object value) {
    addVariable(name, value, QueryOperator.NOT_EQUALS);
    return (T) this;
  }
  
  /**
   * Variable value greater than.
   *
   * @param name the name
   * @param value the value
   * @return the t
   */
  @SuppressWarnings("unchecked")
  public T variableValueGreaterThan(String name, Object value) {
    addVariable(name, value, QueryOperator.GREATER_THAN);
    return (T) this;
  } 
  
  /**
   * Variable value greater than or equal.
   *
   * @param name the name
   * @param value the value
   * @return the t
   */
  @SuppressWarnings("unchecked")
  public T variableValueGreaterThanOrEqual(String name, Object value) {
    addVariable(name, value, QueryOperator.GREATER_THAN_OR_EQUAL);
    return (T) this;
  }
  
  /**
   * Variable value less than.
   *
   * @param name the name
   * @param value the value
   * @return the t
   */
  @SuppressWarnings("unchecked")
  public T variableValueLessThan(String name, Object value) {
    addVariable(name, value, QueryOperator.LESS_THAN);
    return (T) this;
  }
  
  /**
   * Variable value less than or equal.
   *
   * @param name the name
   * @param value the value
   * @return the t
   */
  @SuppressWarnings("unchecked")
  public T variableValueLessThanOrEqual(String name, Object value) {
    addVariable(name, value, QueryOperator.LESS_THAN_OR_EQUAL);
    return (T) this;
  }
  
  /**
   * Variable value like.
   *
   * @param name the name
   * @param value the value
   * @return the t
   */
  @SuppressWarnings("unchecked")
  public T variableValueLike(String name, String value) {
    addVariable(name, value, QueryOperator.LIKE);
    return (T)this;
  }
  
  /**
   * Adds the variable.
   *
   * @param name the name
   * @param value the value
   * @param operator the operator
   */
  private void addVariable(String name, Object value, QueryOperator operator) {
    if(name == null) {
      throw new ActivitiException("name is null");
    }
    if(value == null || isBoolean(value)) {
      // Null-values and booleans can only be used in EQUALS and NOT_EQUALS
      switch(operator) {
      case GREATER_THAN:
        throw new ActivitiException("Booleans and null cannot be used in 'greater than' condition");
      case LESS_THAN:
        throw new ActivitiException("Booleans and null cannot be used in 'less than' condition");
      case GREATER_THAN_OR_EQUAL:
        throw new ActivitiException("Booleans and null cannot be used in 'greater than or equal' condition");
      case LESS_THAN_OR_EQUAL:
        throw new ActivitiException("Booleans and null cannot be used in 'less than or equal' condition");
      case LIKE:
        throw new ActivitiException("Booleans and null cannot be used in 'like' condition");
      }
    }
    queryVariableValues.add(new QueryVariableValue(name, value, operator));
  }
  
  /**
   * Checks if is boolean.
   *
   * @param value the value
   * @return true, if is boolean
   */
  private boolean isBoolean(Object value) {
    if (value == null) {
      return false;
    }
    return Boolean.class.isAssignableFrom(value.getClass()) || boolean.class.isAssignableFrom(value.getClass());
  }

  /**
   * Ensure variables initialized.
   */
  protected void ensureVariablesInitialized() {
    if (!queryVariableValues.isEmpty()) {
      VariableTypes variableTypes = Context
              .getProcessEngineConfiguration()
              .getVariableTypes();
      for(QueryVariableValue queryVariableValue : queryVariableValues) {
        queryVariableValue.initialize(variableTypes);
      }
    }
  }

  /**
   * Gets the query variable values.
   *
   * @return the query variable values
   */
  public List<QueryVariableValue> getQueryVariableValues() {
    return queryVariableValues;
  }

  
}
