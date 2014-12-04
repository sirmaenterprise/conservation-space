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

package org.activiti.engine.impl.identity;


// TODO: Auto-generated Javadoc
/**
 * The Class Authentication.
 *
 * @author Tom Baeyens
 */
public abstract class Authentication {

  /** The authenticated user id thread local. */
  static ThreadLocal<String> authenticatedUserIdThreadLocal = new ThreadLocal<String>();
  
  /**
   * Sets the authenticated user id.
   *
   * @param authenticatedUserId the new authenticated user id
   */
  public static void setAuthenticatedUserId(String authenticatedUserId) {
    authenticatedUserIdThreadLocal.set(authenticatedUserId);
  }

  /**
   * Gets the authenticated user id.
   *
   * @return the authenticated user id
   */
  public static String getAuthenticatedUserId() {
    return authenticatedUserIdThreadLocal.get();
  }
}
