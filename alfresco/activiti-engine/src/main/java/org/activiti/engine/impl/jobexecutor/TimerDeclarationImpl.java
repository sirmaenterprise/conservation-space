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
package org.activiti.engine.impl.jobexecutor;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.delegate.Expression;
import org.activiti.engine.delegate.VariableScope;
import org.activiti.engine.impl.calendar.BusinessCalendar;
import org.activiti.engine.impl.calendar.CycleBusinessCalendar;
import org.activiti.engine.impl.calendar.DueDateBusinessCalendar;
import org.activiti.engine.impl.calendar.DurationBusinessCalendar;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.TimerEntity;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.impl.util.ClockUtil;
import org.springframework.util.StringUtils;

import java.io.Serializable;
import java.rmi.activation.ActivationException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.regex.Pattern;


// TODO: Auto-generated Javadoc
/**
 * The Class TimerDeclarationImpl.
 *
 * @author Tom Baeyens
 */
public class TimerDeclarationImpl implements Serializable {

  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = 1L;

  /** The description. */
  protected Expression description;
  
  /** The type. */
  protected TimerDeclarationType type;

  /** The job handler type. */
  protected String jobHandlerType;
  
  /** The job handler configuration. */
  protected String jobHandlerConfiguration = null;
  
  /** The repeat. */
  protected String repeat;
  
  /** The exclusive. */
  protected boolean exclusive = TimerEntity.DEFAULT_EXCLUSIVE;
  
  /** The retries. */
  protected int retries = TimerEntity.DEFAULT_RETRIES;

  /**
   * Instantiates a new timer declaration impl.
   *
   * @param expression the expression
   * @param type the type
   * @param jobHandlerType the job handler type
   */
  public TimerDeclarationImpl(Expression expression, TimerDeclarationType type, String jobHandlerType) {
    this.jobHandlerType = jobHandlerType;
    this.description = expression;
    this.type= type;
  }

  /**
   * Gets the job handler type.
   *
   * @return the job handler type
   */
  public String getJobHandlerType() {
    return jobHandlerType;
  }

  /**
   * Gets the job handler configuration.
   *
   * @return the job handler configuration
   */
  public String getJobHandlerConfiguration() {
    return jobHandlerConfiguration;
  }

  /**
   * Sets the job handler configuration.
   *
   * @param jobHandlerConfiguration the new job handler configuration
   */
  public void setJobHandlerConfiguration(String jobHandlerConfiguration) {
    this.jobHandlerConfiguration = jobHandlerConfiguration;
  }

  /**
   * Gets the repeat.
   *
   * @return the repeat
   */
  public String getRepeat() {
    return repeat;
  }

  /**
   * Sets the repeat.
   *
   * @param repeat the new repeat
   */
  public void setRepeat(String repeat) {
    this.repeat = repeat;
  }

  /**
   * Checks if is exclusive.
   *
   * @return true, if is exclusive
   */
  public boolean isExclusive() {
    return exclusive;
  }

  /**
   * Sets the exclusive.
   *
   * @param exclusive the new exclusive
   */
  public void setExclusive(boolean exclusive) {
    this.exclusive = exclusive;
  }

  /**
   * Gets the retries.
   *
   * @return the retries
   */
  public int getRetries() {
    return retries;
  }

  /**
   * Sets the retries.
   *
   * @param retries the new retries
   */
  public void setRetries(int retries) {
    this.retries = retries;
  }

  /**
   * Sets the job handler type.
   *
   * @param jobHandlerType the new job handler type
   */
  public void setJobHandlerType(String jobHandlerType) {
    this.jobHandlerType = jobHandlerType;
  }

  /**
   * Prepare timer entity.
   *
   * @param executionEntity the execution entity
   * @return the timer entity
   */
  public TimerEntity prepareTimerEntity(ExecutionEntity executionEntity) {
    BusinessCalendar businessCalendar = Context
        .getProcessEngineConfiguration()
        .getBusinessCalendarManager()
        .getBusinessCalendar(type.caledarName);
    
    if (description==null) {
      // Prefent NPE from happening in the next line
      throw new ActivitiException("Timer '"+executionEntity.getActivityId()+"' was not configured with a valid duration/time");
    }
    
    String dueDateString = null;
    Date duedate = null;
    if (executionEntity == null) {
      dueDateString = description.getExpressionText();
    }
    else {
      Object dueDateValue = description.getValue(executionEntity);
      if (dueDateValue instanceof String) {
        dueDateString = (String)dueDateValue;
      }
      else if (dueDateValue instanceof Date) {
        duedate = (Date)dueDateValue;
      }
      else {
        throw new ActivitiException("Timer '"+executionEntity.getActivityId()+"' was not configured with a valid duration/time, either hand in a java.util.Date or a String in format 'yyyy-MM-dd'T'hh:mm:ss'");
      }
    }
    if (duedate==null) {      
      duedate = businessCalendar.resolveDuedate(dueDateString);
    }

    TimerEntity timer = new TimerEntity(this);
    timer.setDuedate(duedate);
    if (executionEntity != null) {
      timer.setExecution(executionEntity);
    }
    if (type == TimerDeclarationType.CYCLE) {
      String prepared = prepareRepeat(dueDateString);
      timer.setRepeat(prepared);

    }
    
    return timer;
  }
  
  /**
   * Prepare repeat.
   *
   * @param dueDate the due date
   * @return the string
   */
  private String prepareRepeat(String dueDate) {
    if (dueDate.startsWith("R") && dueDate.split("/").length==2) {
      SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
      return dueDate.replace("/","/"+sdf.format(ClockUtil.getCurrentTime())+"/");
    }
    return dueDate;
  }
}
