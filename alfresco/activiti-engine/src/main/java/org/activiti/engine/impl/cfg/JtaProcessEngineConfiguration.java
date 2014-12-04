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

package org.activiti.engine.impl.cfg;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.transaction.TransactionManager;

import org.activiti.engine.impl.cfg.jta.JtaTransactionContextFactory;
import org.activiti.engine.impl.interceptor.CommandContextInterceptor;
import org.activiti.engine.impl.interceptor.CommandInterceptor;
import org.activiti.engine.impl.interceptor.JtaTransactionInterceptor;
import org.activiti.engine.impl.interceptor.LogInterceptor;


// TODO: Auto-generated Javadoc
/**
 * The Class JtaProcessEngineConfiguration.
 *
 * @author Tom Baeyens
 */
public class JtaProcessEngineConfiguration extends ProcessEngineConfigurationImpl {

  /** The transaction manager. */
  protected TransactionManager transactionManager;
  
  /* (non-Javadoc)
   * @see org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl#getDefaultCommandInterceptorsTxRequired()
   */
  @Override
  protected Collection< ? extends CommandInterceptor> getDefaultCommandInterceptorsTxRequired() {
    List<CommandInterceptor> defaultCommandInterceptorsTxRequired = new ArrayList<CommandInterceptor>();
    defaultCommandInterceptorsTxRequired.add(new LogInterceptor());
    defaultCommandInterceptorsTxRequired.add(new JtaTransactionInterceptor(transactionManager, false));
    defaultCommandInterceptorsTxRequired.add(new CommandContextInterceptor(commandContextFactory, this));
    return defaultCommandInterceptorsTxRequired;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl#getDefaultCommandInterceptorsTxRequiresNew()
   */
  @Override
  protected Collection< ? extends CommandInterceptor> getDefaultCommandInterceptorsTxRequiresNew() {
    List<CommandInterceptor> defaultCommandInterceptorsTxRequiresNew = new ArrayList<CommandInterceptor>();
    defaultCommandInterceptorsTxRequiresNew.add(new LogInterceptor());
    defaultCommandInterceptorsTxRequiresNew.add(new JtaTransactionInterceptor(transactionManager, true));
    defaultCommandInterceptorsTxRequiresNew.add(new CommandContextInterceptor(commandContextFactory, this));
    return defaultCommandInterceptorsTxRequiresNew;
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl#initTransactionContextFactory()
   */
  @Override
  protected void initTransactionContextFactory() {
    if(transactionContextFactory == null) {
      transactionContextFactory = new JtaTransactionContextFactory(transactionManager);
    }
  }
  
  /**
   * Gets the transaction manager.
   *
   * @return the transaction manager
   */
  public TransactionManager getTransactionManager() {
    return transactionManager;
  }

  /**
   * Sets the transaction manager.
   *
   * @param transactionManager the new transaction manager
   */
  public void setTransactionManager(TransactionManager transactionManager) {
    this.transactionManager = transactionManager;
  }
}
