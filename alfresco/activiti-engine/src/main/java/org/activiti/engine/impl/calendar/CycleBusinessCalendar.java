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
package org.activiti.engine.impl.calendar;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.impl.util.ClockUtil;

import javax.xml.datatype.Duration;
import java.text.ParseException;
import java.util.Date;

// TODO: Auto-generated Javadoc
/**
 * The Class CycleBusinessCalendar.
 */
public class CycleBusinessCalendar implements BusinessCalendar {

  /** The name. */
  public static String NAME = "cycle";


  /* (non-Javadoc)
   * @see org.activiti.engine.impl.calendar.BusinessCalendar#resolveDuedate(java.lang.String)
   */
  public Date resolveDuedate(String duedateDescription) {
    try {
      if (duedateDescription.startsWith("R")) {
        return new DurationHelper(duedateDescription).getDateAfter();
      } else {
        CronExpression ce = new CronExpression(duedateDescription);
        return ce.getTimeAfter(ClockUtil.getCurrentTime());
      }

    } catch (Exception e) {
      throw new ActivitiException("Failed to parse cron expression: "+duedateDescription, e);
    }

  }

}
