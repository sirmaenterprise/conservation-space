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

package org.activiti.engine.query;

import java.util.List;

// TODO: Auto-generated Javadoc
/**
 * Describes basic methods for querying.
 *
 * @param <T> the generic type
 * @param <U> the generic type
 * @author Frederik Heremans
 */
public interface Query<T extends Query< ? , ? >, U extends Object> {

  /**
   * Order the results ascending on the given property as defined in this
   * class (needs to come after a call to one of the orderByXxxx methods).
   *
   * @return the t
   */
  T asc();

  /**
   * Order the results descending on the given property as defined in this
   * class (needs to come after a call to one of the orderByXxxx methods).
   *
   * @return the t
   */
  T desc();

  /**
   * Executes the query and returns the number of results.
   *
   * @return the long
   */
  long count();

  /**
   * Executes the query and returns the resulting entity or null if no
   * entity matches the query criteria.
   *
   * @return the u
   */
  U singleResult();

  /**
   * Executes the query and get a list of entities as the result.
   *
   * @return the list
   */
  List<U> list();

  /**
   * Executes the query and get a list of entities as the result.
   *
   * @param firstResult the first result
   * @param maxResults the max results
   * @return the list
   */
  List<U> listPage(int firstResult, int maxResults);
}
