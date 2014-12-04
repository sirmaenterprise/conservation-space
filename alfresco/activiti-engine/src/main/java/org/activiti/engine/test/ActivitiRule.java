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

package org.activiti.engine.test;

import java.util.Date;

import org.activiti.engine.FormService;
import org.activiti.engine.HistoryService;
import org.activiti.engine.IdentityService;
import org.activiti.engine.ManagementService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.impl.test.TestHelper;
import org.activiti.engine.impl.util.ClockUtil;
import org.junit.rules.TestWatchman;
import org.junit.runners.model.FrameworkMethod;


// TODO: Auto-generated Javadoc
/** Convenience for ProcessEngine and services initialization in the form of a JUnit rule.
 * 
 * <p>Usage:</p>
 * <pre>public class YourTest {
 * 
 *   &#64;Rule
 *   public ActivitiRule activitiRule = new ActivitiRule();
 *   
 *   ...
 * }
 * </pre>
 * 
 * <p>The ProcessEngine and the services will be made available to the test class 
 * through the getters of the activitiRule.  
 * The processEngine will be initialized by default with the activiti.cfg.xml resource 
 * on the classpath.  To specify a different configuration file, pass the 
 * resource location in {@link #ActivitiRule(String) the appropriate constructor}.
 * Process engines will be cached statically.  Right before the first time the setUp is called for a given 
 * configuration resource, the process engine will be constructed.</p>
 * 
 * <p>You can declare a deployment with the {@link Deployment} annotation.
 * This base class will make sure that this deployment gets deployed before the
 * setUp and {@link RepositoryService#deleteDeployment(String, boolean) cascade deleted}
 * after the tearDown.
 * </p>
 * 
 * <p>The activitiRule also lets you {@link ActivitiRule#setCurrentTime(Date) set the current time used by the 
 * process engine}. This can be handy to control the exact time that is used by the engine
 * in order to verify e.g. e.g. due dates of timers.  Or start, end and duration times
 * in the history service.  In the tearDown, the internal clock will automatically be 
 * reset to use the current system time rather then the time that was set during 
 * a test method.  In other words, you don't have to clean up your own time messing mess ;-)
 * </p>
 *  
 * @author Tom Baeyens
 */
public class ActivitiRule extends TestWatchman {

  /** The configuration resource. */
  protected String configurationResource = "activiti.cfg.xml";
  
  /** The deployment id. */
  protected String deploymentId = null;

  /** The process engine. */
  protected ProcessEngine processEngine;
  
  /** The repository service. */
  protected RepositoryService repositoryService;
  
  /** The runtime service. */
  protected RuntimeService runtimeService;
  
  /** The task service. */
  protected TaskService taskService;
  
  /** The history service. */
  protected HistoryService historyService;
  
  /** The identity service. */
  protected IdentityService identityService;
  
  /** The management service. */
  protected ManagementService managementService;
  
  /** The form service. */
  protected FormService formService;
  
  /**
   * Instantiates a new activiti rule.
   */
  public ActivitiRule() {
  }

  /**
   * Instantiates a new activiti rule.
   *
   * @param configurationResource the configuration resource
   */
  public ActivitiRule(String configurationResource) {
    this.configurationResource = configurationResource;
  }
  
  /**
   * Instantiates a new activiti rule.
   *
   * @param processEngine the process engine
   */
  public ActivitiRule(ProcessEngine processEngine) {
    this.processEngine = processEngine;
  }

  /* (non-Javadoc)
   * @see org.junit.rules.TestWatchman#starting(org.junit.runners.model.FrameworkMethod)
   */
  @Override
  public void starting(FrameworkMethod method) {
    if (processEngine==null) {
      initializeProcessEngine();
      initializeServices();
    }

    deploymentId = TestHelper.annotationDeploymentSetUp(processEngine, method.getMethod().getDeclaringClass(), method.getName());
  }
  
  /**
   * Initialize process engine.
   */
  protected void initializeProcessEngine() {
    processEngine = TestHelper.getProcessEngine(configurationResource);
  }

  /**
   * Initialize services.
   */
  protected void initializeServices() {
    repositoryService = processEngine.getRepositoryService();
    runtimeService = processEngine.getRuntimeService();
    taskService = processEngine.getTaskService();
    historyService = processEngine.getHistoryService();
    identityService = processEngine.getIdentityService();
    managementService = processEngine.getManagementService();
    formService = processEngine.getFormService();
  }

  /* (non-Javadoc)
   * @see org.junit.rules.TestWatchman#finished(org.junit.runners.model.FrameworkMethod)
   */
  @Override
  public void finished(FrameworkMethod method) {
    TestHelper.annotationDeploymentTearDown(processEngine, deploymentId, method.getMethod().getDeclaringClass(), method.getName());

    ClockUtil.reset();
  }
  
  /**
   * Sets the current time.
   *
   * @param currentTime the new current time
   */
  public void setCurrentTime(Date currentTime) {
    ClockUtil.setCurrentTime(currentTime);
  }

  /**
   * Gets the configuration resource.
   *
   * @return the configuration resource
   */
  public String getConfigurationResource() {
    return configurationResource;
  }
  
  /**
   * Sets the configuration resource.
   *
   * @param configurationResource the new configuration resource
   */
  public void setConfigurationResource(String configurationResource) {
    this.configurationResource = configurationResource;
  }
  
  /**
   * Gets the process engine.
   *
   * @return the process engine
   */
  public ProcessEngine getProcessEngine() {
    return processEngine;
  }
  
  /**
   * Sets the process engine.
   *
   * @param processEngine the new process engine
   */
  public void setProcessEngine(ProcessEngine processEngine) {
    this.processEngine = processEngine;
  }
  
  /**
   * Gets the repository service.
   *
   * @return the repository service
   */
  public RepositoryService getRepositoryService() {
    return repositoryService;
  }
  
  /**
   * Sets the repository service.
   *
   * @param repositoryService the new repository service
   */
  public void setRepositoryService(RepositoryService repositoryService) {
    this.repositoryService = repositoryService;
  }
  
  /**
   * Gets the runtime service.
   *
   * @return the runtime service
   */
  public RuntimeService getRuntimeService() {
    return runtimeService;
  }
  
  /**
   * Sets the runtime service.
   *
   * @param runtimeService the new runtime service
   */
  public void setRuntimeService(RuntimeService runtimeService) {
    this.runtimeService = runtimeService;
  }
  
  /**
   * Gets the task service.
   *
   * @return the task service
   */
  public TaskService getTaskService() {
    return taskService;
  }
  
  /**
   * Sets the task service.
   *
   * @param taskService the new task service
   */
  public void setTaskService(TaskService taskService) {
    this.taskService = taskService;
  }
  
  /**
   * Gets the history service.
   *
   * @return the history service
   */
  public HistoryService getHistoryService() {
    return historyService;
  }

  /**
   * Sets the historic data service.
   *
   * @param historicDataService the new historic data service
   */
  public void setHistoricDataService(HistoryService historicDataService) {
    this.historyService = historicDataService;
  }
  
  /**
   * Gets the identity service.
   *
   * @return the identity service
   */
  public IdentityService getIdentityService() {
    return identityService;
  }
  
  /**
   * Sets the identity service.
   *
   * @param identityService the new identity service
   */
  public void setIdentityService(IdentityService identityService) {
    this.identityService = identityService;
  }
  
  /**
   * Gets the management service.
   *
   * @return the management service
   */
  public ManagementService getManagementService() {
    return managementService;
  }
  
  /**
   * Gets the form service.
   *
   * @return the form service
   */
  public FormService getFormService() {
    return formService;
  }
  
  /**
   * Sets the management service.
   *
   * @param managementService the new management service
   */
  public void setManagementService(ManagementService managementService) {
    this.managementService = managementService;
  }
}
