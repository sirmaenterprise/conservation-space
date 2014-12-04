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

import java.util.Date;

import org.activiti.engine.history.HistoricVariableUpdate;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.db.DbSqlSession;
import org.activiti.engine.impl.db.PersistentObject;
import org.activiti.engine.impl.util.ClockUtil;
import org.activiti.engine.impl.variable.ValueFields;
import org.activiti.engine.impl.variable.VariableType;


// TODO: Auto-generated Javadoc
/**
 * The Class HistoricVariableUpdateEntity.
 *
 * @author Tom Baeyens
 */
public class HistoricVariableUpdateEntity extends HistoricDetailEntity implements ValueFields, HistoricVariableUpdate, PersistentObject {
  
  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = 1L;
  
  /** The name. */
  protected String name;
  
  /** The revision. */
  protected int revision;
  
  /** The variable type. */
  protected VariableType variableType;

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

  /**
   * Instantiates a new historic variable update entity.
   */
  public HistoricVariableUpdateEntity() {
  }

  /**
   * Instantiates a new historic variable update entity.
   *
   * @param variableInstance the variable instance
   */
  public HistoricVariableUpdateEntity(VariableInstanceEntity variableInstance) {
    this.processInstanceId = variableInstance.getProcessInstanceId();
    this.executionId = variableInstance.getExecutionId();
    this.taskId = variableInstance.getTaskId();
    this.revision = variableInstance.getRevision();
    this.name = variableInstance.getName();
    this.variableType = variableInstance.getType();
    this.time = ClockUtil.getCurrentTime();
    if (variableInstance.getByteArrayValueId()!=null) {
      // TODO test and review.  name ok here?
      this.byteArrayValue = new ByteArrayEntity(name, variableInstance.getByteArrayValue().getBytes());
      Context
        .getCommandContext()
        .getDbSqlSession()
        .insert(byteArrayValue);
      this.byteArrayValueId = byteArrayValue.getId();
    }
    this.textValue = variableInstance.getTextValue();
    this.textValue2 = variableInstance.getTextValue2();
    this.doubleValue = variableInstance.getDoubleValue();
    this.longValue = variableInstance.getLongValue();
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.history.HistoricVariableUpdate#getValue()
   */
  public Object getValue() {
    if (!variableType.isCachable() || cachedValue==null) {
      cachedValue = variableType.getValue(this);
    }
    return cachedValue;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.impl.persistence.entity.HistoricDetailEntity#delete()
   */
  public void delete() {
    super.delete();

    if (byteArrayValueId != null) {
      // the next apparently useless line is probably to ensure consistency in the DbSqlSession 
      // cache, but should be checked and docced here (or removed if it turns out to be unnecessary)
      // @see also HistoricVariableInstanceEntity
      getByteArrayValue();
      Context
        .getCommandContext()
        .getSession(DbSqlSession.class)
        .delete(ByteArrayEntity.class, byteArrayValueId);
    }
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.impl.variable.ValueFields#getByteArrayValue()
   */
  public ByteArrayEntity getByteArrayValue() {
    // Aren't we forgetting lazy initialization here?
    return byteArrayValue;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.impl.persistence.entity.HistoricDetailEntity#getPersistentState()
   */
  public Object getPersistentState() {
    // HistoricVariableUpdateEntity is immutable, so always the same object is returned
    return HistoricVariableUpdateEntity.class;
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.history.HistoricVariableUpdate#getVariableTypeName()
   */
  public String getVariableTypeName() {
    return (variableType!=null ? variableType.getTypeName() : null);
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.impl.persistence.entity.HistoricDetailEntity#getTime()
   */
  public Date getTime() {
    return time;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.impl.persistence.entity.HistoricDetailEntity#setTime(java.util.Date)
   */
  public void setTime(Date time) {
    this.time = time;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.history.HistoricVariableUpdate#getVariableName()
   */
  public String getVariableName() {
    return name;
  }

  /**
   * Gets the variable type.
   *
   * @return the variable type
   */
  public VariableType getVariableType() {
    return variableType;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.history.HistoricVariableUpdate#getRevision()
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

  
  /* (non-Javadoc)
   * @see org.activiti.engine.impl.variable.ValueFields#getName()
   */
  public String getName() {
    return name;
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

  
  /* (non-Javadoc)
   * @see org.activiti.engine.impl.variable.ValueFields#getTextValue()
   */
  public String getTextValue() {
    return textValue;
  }

  
  /* (non-Javadoc)
   * @see org.activiti.engine.impl.variable.ValueFields#setTextValue(java.lang.String)
   */
  public void setTextValue(String textValue) {
    this.textValue = textValue;
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

  
  /* (non-Javadoc)
   * @see org.activiti.engine.impl.variable.ValueFields#setByteArrayValue(org.activiti.engine.impl.persistence.entity.ByteArrayEntity)
   */
  public void setByteArrayValue(ByteArrayEntity byteArrayValue) {
    this.byteArrayValue = byteArrayValue;
  }

  
  /* (non-Javadoc)
   * @see org.activiti.engine.impl.variable.ValueFields#getByteArrayValueId()
   */
  public String getByteArrayValueId() {
    return byteArrayValueId;
  }

  
  /**
   * Sets the byte array value id.
   *
   * @param byteArrayValueId the new byte array value id
   */
  public void setByteArrayValueId(String byteArrayValueId) {
    this.byteArrayValueId = byteArrayValueId;
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

  
  /**
   * Sets the variable type.
   *
   * @param variableType the new variable type
   */
  public void setVariableType(VariableType variableType) {
    this.variableType = variableType;
  }
}
