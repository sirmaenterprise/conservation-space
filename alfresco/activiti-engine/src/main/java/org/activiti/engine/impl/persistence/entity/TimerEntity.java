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
import java.util.logging.Level;
import java.util.logging.Logger;

import org.activiti.engine.impl.calendar.BusinessCalendar;
import org.activiti.engine.impl.calendar.CycleBusinessCalendar;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.jobexecutor.TimerDeclarationImpl;


// TODO: Auto-generated Javadoc
/**
 * The Class TimerEntity.
 *
 * @author Tom Baeyens
 */
public class TimerEntity extends JobEntity {

  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = 1L;

  /** The log. */
  private static Logger log = Logger.getLogger(TimerEntity.class.getName());

  /** The repeat. */
  protected String repeat;

  /**
   * Instantiates a new timer entity.
   */
  public TimerEntity() {
  }

  /**
   * Instantiates a new timer entity.
   *
   * @param timerDeclaration the timer declaration
   */
  public TimerEntity(TimerDeclarationImpl timerDeclaration) {
    jobHandlerType = timerDeclaration.getJobHandlerType();
    jobHandlerConfiguration = timerDeclaration.getJobHandlerConfiguration();
    isExclusive = timerDeclaration.isExclusive();
    repeat = timerDeclaration.getRepeat();
    retries = timerDeclaration.getRetries();
  }

  /**
   * Instantiates a new timer entity.
   *
   * @param te the te
   */
  private TimerEntity(TimerEntity te) {
    jobHandlerConfiguration = te.jobHandlerConfiguration;
    jobHandlerType = te.jobHandlerType;
    isExclusive = te.isExclusive;
    repeat = te.repeat;
    retries = te.retries;
    executionId = te.executionId;
    processInstanceId = te.processInstanceId;

  }

  /* (non-Javadoc)
   * @see org.activiti.engine.impl.persistence.entity.JobEntity#execute(org.activiti.engine.impl.interceptor.CommandContext)
   */
  @Override
  public void execute(CommandContext commandContext) {

    super.execute(commandContext);

    if (repeat == null) {

      if (log.isLoggable(Level.FINE)) {
        log.fine("Timer " + getId() + " fired. Deleting timer.");
      }
      delete();
    } else {
      delete();
      Date newTimer = calculateRepeat();
      if (newTimer != null) {
        TimerEntity te = new TimerEntity(this);
        te.setDuedate(newTimer);
        Context
            .getCommandContext()
            .getJobManager()
            .schedule(te);
      }
    }

  }

  /**
   * Calculate repeat.
   *
   * @return the date
   */
  private Date calculateRepeat() {
    BusinessCalendar businessCalendar = Context
        .getProcessEngineConfiguration()
        .getBusinessCalendarManager()
        .getBusinessCalendar(CycleBusinessCalendar.NAME);
    return businessCalendar.resolveDuedate(repeat);
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
}
