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

package org.activiti.engine.impl.rules;

import java.util.ArrayList;
import java.util.List;

import org.drools.runtime.rule.Activation;
import org.drools.runtime.rule.AgendaFilter;

// TODO: Auto-generated Javadoc
/**
 * The Class RulesAgendaFilter.
 *
 * @author Tijs Rademakers
 */
public class RulesAgendaFilter implements AgendaFilter {

  /** The suffix list. */
  protected List<String> suffixList = new ArrayList<String>();
  
  /** The accept. */
  protected boolean accept;

  /**
   * Instantiates a new rules agenda filter.
   */
  public RulesAgendaFilter() {}

  /* (non-Javadoc)
   * @see org.drools.runtime.rule.AgendaFilter#accept(org.drools.runtime.rule.Activation)
   */
  public boolean accept(Activation activation) {
    String ruleName = activation.getRule().getName();
    for (String suffix : suffixList) {
      if (ruleName.endsWith(suffix)) {
        return this.accept;
      }
    }
    return !this.accept;
  }
  
  /**
   * Adds the suffic.
   *
   * @param suffix the suffix
   */
  public void addSuffic(String suffix) {
    this.suffixList.add(suffix);
  }
  
  /**
   * Sets the accept.
   *
   * @param accept the new accept
   */
  public void setAccept(boolean accept) {
    this.accept = accept;
  }
}
