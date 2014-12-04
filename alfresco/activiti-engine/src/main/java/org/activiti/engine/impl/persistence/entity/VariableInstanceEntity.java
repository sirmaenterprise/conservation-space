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
import java.util.HashMap;
import java.util.Map;

import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.db.DbSqlSession;
import org.activiti.engine.impl.db.PersistentObject;
import org.activiti.engine.impl.variable.ValueFields;
import org.activiti.engine.impl.variable.VariableType;

// TODO: Auto-generated Javadoc
/**
 * The Class VariableInstanceEntity.
 *
 * @author Tom Baeyens
 */
public class VariableInstanceEntity implements ValueFields, PersistentObject, Serializable {

  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = 1L;

  /** The id. */
  protected String id;
  
  /** The revision. */
  protected int revision;

  /** The name. */
  protected String name;

  /** The process instance id. */
  protected String processInstanceId;
  
  /** The execution id. */
  protected String executionId;
  
  /** The task id. */
  protected String taskId;

  /** The long value. */
  protected Long longValue;
  
  /** The double value. */
  protected Double doubleValue; 
  
  /** The text value. */
  protected String textValue;
  
  /** The text value2. */
  protected String textValue2;

  /** The byte array value. */
  protected ByteArrayEntity byteArrayValue;
  
  /** The byte array value id. */
  protected String byteArrayValueId;

  /** The cached value. */
  protected Object cachedValue;

  /** The type. */
  protected VariableType type;
  
  // Default constructor for SQL mapping
  /**
   * Instantiates a new variable instance entity.
   */
  protected VariableInstanceEntity() {
  }

  /**
   * Creates the and insert.
   *
   * @param name the name
   * @param type the type
   * @param value the value
   * @return the variable instance entity
   */
  public static VariableInstanceEntity createAndInsert(String name, VariableType type, Object value) {
    VariableInstanceEntity variableInstance = create(name, type, value);

    Context
      .getCommandContext()
      .getDbSqlSession()
      .insert(variableInstance);
  
    return variableInstance;
  }
  
  /**
   * Creates the.
   *
   * @param name the name
   * @param type the type
   * @param value the value
   * @return the variable instance entity
   */
  public static VariableInstanceEntity create(String name, VariableType type, Object value) {
    VariableInstanceEntity variableInstance = new VariableInstanceEntity();
    variableInstance.name = name;
    variableInstance.type = type;
    variableInstance.setValue(value);
    
    return variableInstance;
  }

  /**
   * Sets the execution.
   *
   * @param execution the new execution
   */
  public void setExecution(ExecutionEntity execution) {
    this.executionId = execution.getId();
    this.processInstanceId = execution.getProcessInstanceId();
  }

  /**
   * Delete.
   */
  public void delete() {
    // delete variable
    DbSqlSession dbSqlSession = Context
      .getCommandContext()
      .getDbSqlSession();
    
    dbSqlSession.delete(VariableInstanceEntity.class, id);

    if (byteArrayValueId != null) {
      // the next apparently useless line is probably to ensure consistency in the DbSqlSession 
      // cache, but should be checked and docced here (or removed if it turns out to be unnecessary)
      // @see also HistoricVariableUpdateEntity
      getByteArrayValue();
      dbSqlSession.delete(ByteArrayEntity.class, byteArrayValueId);
    }
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.impl.variable.ValueFields#setByteArrayValue(org.activiti.engine.impl.persistence.entity.ByteArrayEntity)
   */
  public void setByteArrayValue(ByteArrayEntity byteArrayValue) {
    this.byteArrayValue = byteArrayValue;
    if (byteArrayValue != null) {
      this.byteArrayValueId = byteArrayValue.getId();
    } else {
      this.byteArrayValueId = null;
    }
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.impl.db.PersistentObject#getPersistentState()
   */
  public Object getPersistentState() {
    Map<String, Object> persistentState = new HashMap<String, Object>();
    if (longValue != null) {
      persistentState.put("longValue", longValue);
    }
    if (doubleValue != null) {
      persistentState.put("doubleValue", doubleValue);
    }
    if (textValue != null) {
      persistentState.put("textValue", textValue);
    }
    if (byteArrayValueId != null) {
      persistentState.put("byteArrayValueId", byteArrayValueId);
    }
    return persistentState;
  }
  
  /**
   * Gets the revision next.
   *
   * @return the revision next
   */
  public int getRevisionNext() {
    return revision+1;
  }

  // lazy initialized relations ///////////////////////////////////////////////

  /**
   * Sets the process instance id.
   *
   * @param processInstanceId the new process instance id
   */
  public void setProcessInstanceId(String processInstanceId) {
    this.processInstanceId = processInstanceId;
  }

  /**
   * Sets the execution id.
   *
   * @param executionId the new execution id
   */
  public void setExecutionId(String executionId) {
    this.executionId = executionId;
  }

  /**
   * Sets the byte array value id.
   *
   * @param byteArrayValueId the new byte array value id
   */
  public void setByteArrayValueId(String byteArrayValueId) {
    this.byteArrayValueId = byteArrayValueId;
    this.byteArrayValue = null;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.impl.variable.ValueFields#getByteArrayValue()
   */
  public ByteArrayEntity getByteArrayValue() {
    if ((byteArrayValue == null) && (byteArrayValueId != null)) {
      byteArrayValue = Context
        .getCommandContext()
        .getDbSqlSession()
        .selectById(ByteArrayEntity.class, byteArrayValueId);
    }
    return byteArrayValue;
  }
  
  // type /////////////////////////////////////////////////////////////////////

  /**
   * Gets the value.
   *
   * @return the value
   */
  public Object getValue() {
    if (!type.isCachable() || cachedValue==null) {
      cachedValue = type.getValue(this);
    }
    return cachedValue;
  }

  /**
   * Sets the value.
   *
   * @param value the new value
   */
  public void setValue(Object value) {
    type.setValue(value, this);
    cachedValue = value;
  }

  // getters and setters //////////////////////////////////////////////////////

  /* (non-Javadoc)
   * @see org.activiti.engine.impl.db.PersistentObject#getId()
   */
  public String getId() {
    return id;
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.impl.db.PersistentObject#setId(java.lang.String)
   */
  public void setId(String id) {
    this.id = id;
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.impl.variable.ValueFields#getByteArrayValueId()
   */
  public String getByteArrayValueId() {
    return byteArrayValueId;
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.impl.variable.ValueFields#getTextValue()
   */
  public String getTextValue() {
    return textValue;
  }
  
  /**
   * Gets the process instance id.
   *
   * @return the process instance id
   */
  public String getProcessInstanceId() {
    return processInstanceId;
  }
  
  /**
   * Gets the execution id.
   *
   * @return the execution id
   */
  public String getExecutionId() {
    return executionId;
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.impl.variable.ValueFields#getLongValue()
   */
  public Long getLongValue() {
    return longValue;
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.impl.variable.ValueFields#setLongValue(java.lang.Long)
   */
  public void setLongValue(Long longValue) {
    this.longValue = longValue;
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.impl.variable.ValueFields#getDoubleValue()
   */
  public Double getDoubleValue() {
    return doubleValue;
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.impl.variable.ValueFields#setDoubleValue(java.lang.Double)
   */
  public void setDoubleValue(Double doubleValue) {
    this.doubleValue = doubleValue;
  }
  
  /**
   * Sets the name.
   *
   * @param name the new name
   */
  public void setName(String name) {
    this.name = name;
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.impl.variable.ValueFields#setTextValue(java.lang.String)
   */
  public void setTextValue(String textValue) {
    this.textValue = textValue;
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.impl.variable.ValueFields#getName()
   */
  public String getName() {
    return name;
  }
  
  /**
   * Gets the revision.
   *
   * @return the revision
   */
  public int getRevision() {
    return revision;
  }
  
  /**
   * Sets the revision.
   *
   * @param revision the new revision
   */
  public void setRevision(int revision) {
    this.revision = revision;
  }
  
  /**
   * Sets the type.
   *
   * @param type the new type
   */
  public void setType(VariableType type) {
    this.type = type;
  }
  
  /**
   * Gets the type.
   *
   * @return the type
   */
  public VariableType getType() {
    return type;
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.impl.variable.ValueFields#getCachedValue()
   */
  public Object getCachedValue() {
    return cachedValue;
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.impl.variable.ValueFields#setCachedValue(java.lang.Object)
   */
  public void setCachedValue(Object cachedValue) {
    this.cachedValue = cachedValue;
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.impl.variable.ValueFields#getTextValue2()
   */
  public String getTextValue2() {
    return textValue2;
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.impl.variable.ValueFields#setTextValue2(java.lang.String)
   */
  public void setTextValue2(String textValue2) {
    this.textValue2 = textValue2;
  }
  
  /**
   * Gets the task id.
   *
   * @return the task id
   */
  public String getTaskId() {
    return taskId;
  }
  
  /**
   * Sets the task id.
   *
   * @param taskId the new task id
   */
  public void setTaskId(String taskId) {
    this.taskId = taskId;
  }
}
