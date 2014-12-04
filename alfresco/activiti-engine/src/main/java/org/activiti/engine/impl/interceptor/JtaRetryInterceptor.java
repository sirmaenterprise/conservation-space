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
package org.activiti.engine.impl.interceptor;

import java.util.logging.Logger;

import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.TransactionManager;

import org.activiti.engine.ActivitiException;

// TODO: Auto-generated Javadoc
/**
 * We cannot perform a retry if we are called in an existing transaction. In
 * that case, the transaction will be marked "rollback-only" after the first
 * ActivitiOptimisticLockingException.
 * 
 * @author Daniel Meyer
 */
public class JtaRetryInterceptor extends RetryInterceptor {

  /** The log. */
  private final Logger log = Logger.getLogger(JtaRetryInterceptor.class.getName());

  /** The transaction manager. */
  protected final TransactionManager transactionManager;

  /**
   * Instantiates a new jta retry interceptor.
   *
   * @param transactionManager the transaction manager
   */
  public JtaRetryInterceptor(TransactionManager transactionManager) {
    this.transactionManager = transactionManager;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.impl.interceptor.RetryInterceptor#execute(org.activiti.engine.impl.interceptor.Command)
   */
  @Override
  public <T> T execute(Command<T> command) {
    if (calledInsideTransaction()) {
      log.finest("Called inside transaction, skipping the retry interceptor.");
      return next.execute(command);
    } else {
      return super.execute(command);
    }
  }

  /**
   * Called inside transaction.
   *
   * @return true, if successful
   */
  protected boolean calledInsideTransaction() {
    try {
      return transactionManager.getStatus() != Status.STATUS_NO_TRANSACTION;
    } catch (SystemException e) {
      throw new ActivitiException("Could not determine the current status of the transaction manager: " + e.getMessage(), e);
    }
  }

}
