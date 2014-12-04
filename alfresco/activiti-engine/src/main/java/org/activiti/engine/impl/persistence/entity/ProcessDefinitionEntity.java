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

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.activiti.engine.delegate.Expression;
import org.activiti.engine.impl.bpmn.parser.BpmnParse;
import org.activiti.engine.impl.cfg.IdGenerator;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.db.DbSqlSession;
import org.activiti.engine.impl.db.PersistentObject;
import org.activiti.engine.impl.form.StartFormHandler;
import org.activiti.engine.impl.identity.Authentication;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.impl.pvm.process.ProcessDefinitionImpl;
import org.activiti.engine.impl.pvm.runtime.InterpretableExecution;
import org.activiti.engine.impl.task.TaskDefinition;
import org.activiti.engine.impl.util.ClockUtil;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.task.IdentityLinkType;


// TODO: Auto-generated Javadoc
/**
 * The Class ProcessDefinitionEntity.
 *
 * @author Tom Baeyens
 * @author Daniel Meyer
 */
public class ProcessDefinitionEntity extends ProcessDefinitionImpl implements ProcessDefinition, PersistentObject {

  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = 1L;

  /** The key. */
  protected String key;
  
  /** The revision. */
  protected int revision = 1;
  
  /** The version. */
  protected int version;
  
  /** The category. */
  protected String category;
  
  /** The deployment id. */
  protected String deploymentId;
  
  /** The resource name. */
  protected String resourceName;
  
  /** The history level. */
  protected Integer historyLevel;
  
  /** The start form handler. */
  protected StartFormHandler startFormHandler;
  
  /** The diagram resource name. */
  protected String diagramResourceName;
  
  /** The is graphical notation defined. */
  protected boolean isGraphicalNotationDefined;
  
  /** The task definitions. */
  protected Map<String, TaskDefinition> taskDefinitions;
  
  /** The has start form key. */
  protected boolean hasStartFormKey;
  
  /** The suspension state. */
  protected int suspensionState = SuspensionState.ACTIVE.getStateCode();
  
  /** The is identity links initialized. */
  protected boolean isIdentityLinksInitialized = false;
  
  /** The definition identity link entities. */
  protected List<IdentityLinkEntity> definitionIdentityLinkEntities = new ArrayList<IdentityLinkEntity>();
  
  /** The candidate starter user id expressions. */
  protected Set<Expression> candidateStarterUserIdExpressions = new HashSet<Expression>();
  
  /** The candidate starter group id expressions. */
  protected Set<Expression> candidateStarterGroupIdExpressions = new HashSet<Expression>();
  
  /**
   * Instantiates a new process definition entity.
   */
  public ProcessDefinitionEntity() {
    super(null);
  }
  
  /**
   * Creates the process instance.
   *
   * @param businessKey the business key
   * @param initial the initial
   * @return the execution entity
   */
  public ExecutionEntity createProcessInstance(String businessKey, ActivityImpl initial) {
    ExecutionEntity processInstance = null;
  
    if(initial == null) {
      processInstance = (ExecutionEntity) super.createProcessInstance();
    }else {
      processInstance = (ExecutionEntity) super.createProcessInstanceForInitial(initial);
    }

    CommandContext commandContext = Context.getCommandContext();
  
    processInstance.setExecutions(new ArrayList<ExecutionEntity>());
    processInstance.setProcessDefinition(processDefinition);
    // Do not initialize variable map (let it happen lazily)

    if (businessKey != null) {
    	processInstance.setBusinessKey(businessKey);
    }
    
    // reset the process instance in order to have the db-generated process instance id available
    processInstance.setProcessInstance(processInstance);
    
    String initiatorVariableName = (String) getProperty(BpmnParse.PROPERTYNAME_INITIATOR_VARIABLE_NAME);
    if (initiatorVariableName!=null) {
      String authenticatedUserId = Authentication.getAuthenticatedUserId();
      processInstance.setVariable(initiatorVariableName, authenticatedUserId);
    }
    
    int historyLevel = Context.getProcessEngineConfiguration().getHistoryLevel();
    // TODO: This smells bad, as the rest of the history is done via the ParseListener
    if (historyLevel>=ProcessEngineConfigurationImpl.HISTORYLEVEL_ACTIVITY) {
      HistoricProcessInstanceEntity historicProcessInstance = new HistoricProcessInstanceEntity(processInstance);

      commandContext
        .getSession(DbSqlSession.class)
        .insert(historicProcessInstance);

      // do basically the same as in ActivityInstanceStanrtHandler
      IdGenerator idGenerator = Context.getProcessEngineConfiguration().getIdGenerator();
      
      String processDefinitionId = processInstance.getProcessDefinitionId();
      String processInstanceId = processInstance.getProcessInstanceId();
      String executionId = processInstance.getId();

      HistoricActivityInstanceEntity historicActivityInstance = new HistoricActivityInstanceEntity();
      historicActivityInstance.setId(idGenerator.getNextId());
      historicActivityInstance.setProcessDefinitionId(processDefinitionId);
      historicActivityInstance.setProcessInstanceId(processInstanceId);
      historicActivityInstance.setExecutionId(executionId);
      historicActivityInstance.setActivityId(processInstance.getActivityId());
      historicActivityInstance.setActivityName((String) processInstance.getActivity().getProperty("name"));
      historicActivityInstance.setActivityType((String) processInstance.getActivity().getProperty("type"));
      Date now = ClockUtil.getCurrentTime();
      historicActivityInstance.setStartTime(now);
      
      commandContext
        .getDbSqlSession()
        .insert(historicActivityInstance);
    }

    return processInstance;
  }
  
  /**
   * Creates the process instance.
   *
   * @param businessKey the business key
   * @return the execution entity
   */
  public ExecutionEntity createProcessInstance(String businessKey) {
    return createProcessInstance(businessKey, null);
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.impl.pvm.process.ProcessDefinitionImpl#createProcessInstance()
   */
  public ExecutionEntity createProcessInstance() {
    return createProcessInstance(null);
  }
  
  
  /* (non-Javadoc)
   * @see org.activiti.engine.impl.pvm.process.ProcessDefinitionImpl#newProcessInstance(org.activiti.engine.impl.pvm.process.ActivityImpl)
   */
  @Override
  protected InterpretableExecution newProcessInstance(ActivityImpl activityImpl) {
    ExecutionEntity processInstance = new ExecutionEntity(activityImpl);
    processInstance.insert();
    return processInstance;
  }
  
  /**
   * Adds the identity link.
   *
   * @param userId the user id
   * @param groupId the group id
   * @return the identity link entity
   */
  public IdentityLinkEntity addIdentityLink(String userId, String groupId) {
    IdentityLinkEntity identityLinkEntity = IdentityLinkEntity.createAndInsert();
    getIdentityLinks().add(identityLinkEntity);
    identityLinkEntity.setProcessDef(this);
    identityLinkEntity.setUserId(userId);
    identityLinkEntity.setGroupId(groupId);
    identityLinkEntity.setType(IdentityLinkType.CANDIDATE);
    return identityLinkEntity;
  }
  
  /**
   * Delete identity link.
   *
   * @param userId the user id
   * @param groupId the group id
   */
  public void deleteIdentityLink(String userId, String groupId) {
    List<IdentityLinkEntity> identityLinks = Context
      .getCommandContext()
      .getIdentityLinkManager()
      .findIdentityLinkByProcessDefinitionUserAndGroup(id, userId, groupId);
    
    for (IdentityLinkEntity identityLink: identityLinks) {
      Context
        .getCommandContext()
        .getDbSqlSession()
        .delete(IdentityLinkEntity.class, identityLink.getId());
    }
  }
  
  /**
   * Gets the identity links.
   *
   * @return the identity links
   */
  public List<IdentityLinkEntity> getIdentityLinks() {
    if (!isIdentityLinksInitialized) {
      definitionIdentityLinkEntities = Context
        .getCommandContext()
        .getIdentityLinkManager()
        .findIdentityLinksByProcessDefinitionId(id);
      isIdentityLinksInitialized = true;
    }
    
    return definitionIdentityLinkEntities;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.impl.pvm.process.ProcessDefinitionImpl#toString()
   */
  public String toString() {
    return "ProcessDefinitionEntity["+id+"]";
  }


  // getters and setters //////////////////////////////////////////////////////
  
  /* (non-Javadoc)
   * @see org.activiti.engine.impl.db.PersistentObject#getPersistentState()
   */
  public Object getPersistentState() {
    Map<String, Object> persistentState = new HashMap<String, Object>();  
    persistentState.put("suspensionState", this.suspensionState);
    return persistentState;
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.repository.ProcessDefinition#getKey()
   */
  public String getKey() {
    return key;
  }

  /**
   * Sets the key.
   *
   * @param key the new key
   */
  public void setKey(String key) {
    this.key = key;
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.impl.pvm.process.ProcessDefinitionImpl#getDescription()
   */
  public String getDescription() {
    return (String) getProperty(BpmnParse.PROPERTYNAME_DOCUMENTATION);
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.impl.pvm.process.ProcessDefinitionImpl#getDeploymentId()
   */
  public String getDeploymentId() {
    return deploymentId;
  }

  /**
   * Sets the deployment id.
   *
   * @param deploymentId the new deployment id
   */
  public void setDeploymentId(String deploymentId) {
    this.deploymentId = deploymentId;
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.repository.ProcessDefinition#getVersion()
   */
  public int getVersion() {
    return version;
  }
  
  /**
   * Sets the version.
   *
   * @param version the new version
   */
  public void setVersion(int version) {
    this.version = version;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.impl.db.PersistentObject#setId(java.lang.String)
   */
  public void setId(String id) {
    this.id = id;
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.repository.ProcessDefinition#getResourceName()
   */
  public String getResourceName() {
    return resourceName;
  }

  /**
   * Sets the resource name.
   *
   * @param resourceName the new resource name
   */
  public void setResourceName(String resourceName) {
    this.resourceName = resourceName;
  }

  /**
   * Gets the history level.
   *
   * @return the history level
   */
  public Integer getHistoryLevel() {
    return historyLevel;
  }

  /**
   * Sets the history level.
   *
   * @param historyLevel the new history level
   */
  public void setHistoryLevel(Integer historyLevel) {
    this.historyLevel = historyLevel;
  }

  /**
   * Gets the start form handler.
   *
   * @return the start form handler
   */
  public StartFormHandler getStartFormHandler() {
    return startFormHandler;
  }

  /**
   * Sets the start form handler.
   *
   * @param startFormHandler the new start form handler
   */
  public void setStartFormHandler(StartFormHandler startFormHandler) {
    this.startFormHandler = startFormHandler;
  }

  /**
   * Gets the task definitions.
   *
   * @return the task definitions
   */
  public Map<String, TaskDefinition> getTaskDefinitions() {
    return taskDefinitions;
  }

  /**
   * Sets the task definitions.
   *
   * @param taskDefinitions the task definitions
   */
  public void setTaskDefinitions(Map<String, TaskDefinition> taskDefinitions) {
    this.taskDefinitions = taskDefinitions;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.repository.ProcessDefinition#getCategory()
   */
  public String getCategory() {
    return category;
  }

  /**
   * Sets the category.
   *
   * @param category the new category
   */
  public void setCategory(String category) {
    this.category = category;
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.impl.pvm.process.ProcessDefinitionImpl#getDiagramResourceName()
   */
  public String getDiagramResourceName() {
    return diagramResourceName;
  }

  /**
   * Sets the diagram resource name.
   *
   * @param diagramResourceName the new diagram resource name
   */
  public void setDiagramResourceName(String diagramResourceName) {
    this.diagramResourceName = diagramResourceName;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.repository.ProcessDefinition#hasStartFormKey()
   */
  public boolean hasStartFormKey() {
    return hasStartFormKey;
  }
  
  /**
   * Gets the checks for start form key.
   *
   * @return the checks for start form key
   */
  public boolean getHasStartFormKey() {
    return hasStartFormKey;
  }
  
  /**
   * Sets the start form key.
   *
   * @param hasStartFormKey the new start form key
   */
  public void setStartFormKey(boolean hasStartFormKey) {
    this.hasStartFormKey = hasStartFormKey;
  }

  /**
   * Sets the checks for start form key.
   *
   * @param hasStartFormKey the new checks for start form key
   */
  public void setHasStartFormKey(boolean hasStartFormKey) {
    this.hasStartFormKey = hasStartFormKey;
  }
  
  /**
   * Checks if is graphical notation defined.
   *
   * @return true, if is graphical notation defined
   */
  public boolean isGraphicalNotationDefined() {
    return isGraphicalNotationDefined;
  }
  
  /**
   * Sets the graphical notation defined.
   *
   * @param isGraphicalNotationDefined the new graphical notation defined
   */
  public void setGraphicalNotationDefined(boolean isGraphicalNotationDefined) {
    this.isGraphicalNotationDefined = isGraphicalNotationDefined;
  }
  
  /**
   * Gets the revision.
   *
   * @return the revision
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
  
  /**
   * Gets the revision next.
   *
   * @return the revision next
   */
  public int getRevisionNext() {
    return revision+1;
  }
  
  /**
   * Gets the suspension state.
   *
   * @return the suspension state
   */
  public int getSuspensionState() {
    return suspensionState;
  }
  
  /**
   * Sets the suspension state.
   *
   * @param suspensionState the new suspension state
   */
  public void setSuspensionState(int suspensionState) {
    this.suspensionState = suspensionState;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.repository.ProcessDefinition#isSuspended()
   */
  public boolean isSuspended() {
    return suspensionState == SuspensionState.SUSPENDED.getStateCode();
  }
  
  /**
   * Gets the candidate starter user id expressions.
   *
   * @return the candidate starter user id expressions
   */
  public Set<Expression> getCandidateStarterUserIdExpressions() {
    return candidateStarterUserIdExpressions;
  }

  /**
   * Adds the candidate starter user id expression.
   *
   * @param userId the user id
   */
  public void addCandidateStarterUserIdExpression(Expression userId) {
    candidateStarterUserIdExpressions.add(userId);
  }

  /**
   * Gets the candidate starter group id expressions.
   *
   * @return the candidate starter group id expressions
   */
  public Set<Expression> getCandidateStarterGroupIdExpressions() {
    return candidateStarterGroupIdExpressions;
  }

  /**
   * Adds the candidate starter group id expression.
   *
   * @param groupId the group id
   */
  public void addCandidateStarterGroupIdExpression(Expression groupId) {
    candidateStarterGroupIdExpressions.add(groupId);
  }
}
