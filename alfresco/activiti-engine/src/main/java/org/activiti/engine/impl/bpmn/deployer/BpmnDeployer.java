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
package org.activiti.engine.impl.bpmn.deployer;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.delegate.Expression;
import org.activiti.engine.impl.bpmn.diagram.ProcessDiagramGenerator;
import org.activiti.engine.impl.bpmn.parser.BpmnParse;
import org.activiti.engine.impl.bpmn.parser.BpmnParser;
import org.activiti.engine.impl.bpmn.parser.EventSubscriptionDeclaration;
import org.activiti.engine.impl.cfg.IdGenerator;
import org.activiti.engine.impl.cmd.DeleteJobsCmd;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.db.DbSqlSession;
import org.activiti.engine.impl.el.ExpressionManager;
import org.activiti.engine.impl.event.MessageEventHandler;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.jobexecutor.TimerDeclarationImpl;
import org.activiti.engine.impl.jobexecutor.TimerStartEventJobHandler;
import org.activiti.engine.impl.persistence.deploy.Deployer;
import org.activiti.engine.impl.persistence.deploy.DeploymentCache;
import org.activiti.engine.impl.persistence.entity.DeploymentEntity;
import org.activiti.engine.impl.persistence.entity.EventSubscriptionEntity;
import org.activiti.engine.impl.persistence.entity.IdentityLinkEntity;
import org.activiti.engine.impl.persistence.entity.MessageEventSubscriptionEntity;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionManager;
import org.activiti.engine.impl.persistence.entity.ResourceEntity;
import org.activiti.engine.impl.persistence.entity.TimerEntity;
import org.activiti.engine.impl.util.IoUtil;
import org.activiti.engine.runtime.Job;
import org.activiti.engine.task.IdentityLinkType;

// TODO: Auto-generated Javadoc
/**
 * The Class BpmnDeployer.
 *
 * @author Tom Baeyens
 * @author Joram Barrez
 */
public class BpmnDeployer implements Deployer {

  /** The Constant LOG. */
  private static final Logger LOG = Logger.getLogger(BpmnDeployer.class.getName());;

  /** The Constant BPMN_RESOURCE_SUFFIXES. */
  public static final String[] BPMN_RESOURCE_SUFFIXES = new String[] { "bpmn20.xml", "bpmn" };
  
  /** The Constant DIAGRAM_SUFFIXES. */
  public static final String[] DIAGRAM_SUFFIXES = new String[]{"png", "jpg", "gif", "svg"};

  /** The expression manager. */
  protected ExpressionManager expressionManager;
  
  /** The bpmn parser. */
  protected BpmnParser bpmnParser;
  
  /** The id generator. */
  protected IdGenerator idGenerator;

  /* (non-Javadoc)
   * @see org.activiti.engine.impl.persistence.deploy.Deployer#deploy(org.activiti.engine.impl.persistence.entity.DeploymentEntity)
   */
  public void deploy(DeploymentEntity deployment) {
    List<ProcessDefinitionEntity> processDefinitions = new ArrayList<ProcessDefinitionEntity>();
    Map<String, ResourceEntity> resources = deployment.getResources();

    for (String resourceName : resources.keySet()) {

      LOG.info("Processing resource " + resourceName);
      if (isBpmnResource(resourceName)) {
        ResourceEntity resource = resources.get(resourceName);
        byte[] bytes = resource.getBytes();
        ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
        
        BpmnParse bpmnParse = bpmnParser
          .createParse()
          .sourceInputStream(inputStream)
          .deployment(deployment)
          .name(resourceName);
        
        if (!deployment.isValidatingSchema()) {
          bpmnParse.setSchemaResource(null);
        }
        
        bpmnParse.execute();
        
        for (ProcessDefinitionEntity processDefinition: bpmnParse.getProcessDefinitions()) {
          processDefinition.setResourceName(resourceName);
          
          String diagramResourceName = getDiagramResourceForProcess(resourceName, processDefinition.getKey(), resources);
                   
          // Only generate the resource when deployment is new to prevent modification of deployment resources 
          // after the process-definition is actually deployed. Also to prevent resource-generation failure every
          // time the process definition is added to the deployment-cache when diagram-generation has failed the first time.
          if(deployment.isNew()) {
            if (Context.getProcessEngineConfiguration().isCreateDiagramOnDeploy() &&
                  diagramResourceName==null && processDefinition.isGraphicalNotationDefined()) {
              try {
                byte[] diagramBytes = IoUtil.readInputStream(ProcessDiagramGenerator.generatePngDiagram(processDefinition), null);
                diagramResourceName = getProcessImageResourceName(resourceName, processDefinition.getKey(), "png");
                createResource(diagramResourceName, diagramBytes, deployment);
              } catch (Throwable t) { // if anything goes wrong, we don't store the image (the process will still be executable).
                LOG.log(Level.WARNING, "Error while generating process diagram, image will not be stored in repository", t);
              }
            } 
          }
          
          processDefinition.setDiagramResourceName(diagramResourceName);
          processDefinitions.add(processDefinition);
        }
      }
    }
    
    CommandContext commandContext = Context.getCommandContext();
    ProcessDefinitionManager processDefinitionManager = commandContext.getProcessDefinitionManager();
    DeploymentCache deploymentCache = Context.getProcessEngineConfiguration().getDeploymentCache();
    DbSqlSession dbSqlSession = commandContext.getSession(DbSqlSession.class);
    for (ProcessDefinitionEntity processDefinition : processDefinitions) {
      if (deployment.isNew()) {
        int processDefinitionVersion;

        ProcessDefinitionEntity latestProcessDefinition = processDefinitionManager.findLatestProcessDefinitionByKey(processDefinition.getKey());
        if (latestProcessDefinition != null) {
          processDefinitionVersion = latestProcessDefinition.getVersion() + 1;
        } else {
          processDefinitionVersion = 1;
        }

        processDefinition.setVersion(processDefinitionVersion);
        processDefinition.setDeploymentId(deployment.getId());

        String nextId = idGenerator.getNextId();
        String processDefinitionId = processDefinition.getKey() 
          + ":" + processDefinition.getVersion()
          + ":" + nextId; // ACT-505
                   
        // ACT-115: maximum id length is 64 charcaters
        if (processDefinitionId.length() > 64) {          
          processDefinitionId = nextId; 
        }
        processDefinition.setId(processDefinitionId);

        removeObsoleteTimers(processDefinition);
        addTimerDeclarations(processDefinition);
        
        removeObsoleteMessageEventSubscriptions(processDefinition, latestProcessDefinition);
        addMessageEventSubscriptions(processDefinition);

        dbSqlSession.insert(processDefinition);
        deploymentCache.addProcessDefinition(processDefinition);
        addAuthorizations(processDefinition);

        
      } else {
        String deploymentId = deployment.getId();
        processDefinition.setDeploymentId(deploymentId);
        ProcessDefinitionEntity persistedProcessDefinition = processDefinitionManager.findProcessDefinitionByDeploymentAndKey(deploymentId, processDefinition.getKey());
        processDefinition.setId(persistedProcessDefinition.getId());
        processDefinition.setVersion(persistedProcessDefinition.getVersion());
        deploymentCache.addProcessDefinition(processDefinition);
        addAuthorizations(processDefinition);

      }

      Context
        .getProcessEngineConfiguration()
        .getDeploymentCache()
        .addProcessDefinition(processDefinition);
    }
  }

  /**
   * Adds the timer declarations.
   *
   * @param processDefinition the process definition
   */
  @SuppressWarnings("unchecked")
  private void addTimerDeclarations(ProcessDefinitionEntity processDefinition) {
    List<TimerDeclarationImpl> timerDeclarations = (List<TimerDeclarationImpl>) processDefinition.getProperty(BpmnParse.PROPERTYNAME_START_TIMER);
    if (timerDeclarations!=null) {
      for (TimerDeclarationImpl timerDeclaration : timerDeclarations) {
        TimerEntity timer = timerDeclaration.prepareTimerEntity(null);
        Context
          .getCommandContext()
          .getJobManager()
          .schedule(timer);
      }
    }
  }

  /**
   * Removes the obsolete timers.
   *
   * @param processDefinition the process definition
   */
  private void removeObsoleteTimers(ProcessDefinitionEntity processDefinition) {
    List<Job> jobsToDelete = Context
      .getCommandContext()
      .getJobManager()
      .findJobsByConfiguration(TimerStartEventJobHandler.TYPE, processDefinition.getKey());
    
    for (Job job :jobsToDelete) {
        new DeleteJobsCmd(job.getId()).execute(Context.getCommandContext());
    }
  }
  
  /**
   * Removes the obsolete message event subscriptions.
   *
   * @param processDefinition the process definition
   * @param latestProcessDefinition the latest process definition
   */
  protected void removeObsoleteMessageEventSubscriptions(ProcessDefinitionEntity processDefinition, ProcessDefinitionEntity latestProcessDefinition) {
    // remove all subscriptions for the previous version    
    if(latestProcessDefinition != null) {
      CommandContext commandContext = Context.getCommandContext();
      
      List<EventSubscriptionEntity> subscriptionsToDelete = commandContext
        .getEventSubscriptionManager()
        .findEventSubscriptionsByConfiguration(MessageEventHandler.EVENT_HANDLER_TYPE, latestProcessDefinition.getId());
      
      for (EventSubscriptionEntity eventSubscriptionEntity : subscriptionsToDelete) {
        eventSubscriptionEntity.delete();        
      } 
      
    }
  }
  
  /**
   * Adds the message event subscriptions.
   *
   * @param processDefinition the process definition
   */
  @SuppressWarnings("unchecked")
  protected void addMessageEventSubscriptions(ProcessDefinitionEntity processDefinition) {
    CommandContext commandContext = Context.getCommandContext();
    List<EventSubscriptionDeclaration> messageEventDefinitions = (List<EventSubscriptionDeclaration>) processDefinition.getProperty(BpmnParse.PROPERTYNAME_EVENT_SUBSCRIPTION_DECLARATION);
    if(messageEventDefinitions != null) {     
      for (EventSubscriptionDeclaration messageEventDefinition : messageEventDefinitions) {
        if(messageEventDefinition.isStartEvent()) {
          // look for subscriptions for the same name in db:
          List<EventSubscriptionEntity> subscriptionsForSameMessageName = commandContext
            .getEventSubscriptionManager()
            .findEventSubscriptionsByName(MessageEventHandler.EVENT_HANDLER_TYPE, messageEventDefinition.getEventName());
          // also look for subscriptions created in the session:
          List<MessageEventSubscriptionEntity> cachedSubscriptions = commandContext
            .getDbSqlSession()
            .findInCache(MessageEventSubscriptionEntity.class);
          for (MessageEventSubscriptionEntity cachedSubscription : cachedSubscriptions) {
            if(messageEventDefinition.getEventName().equals(cachedSubscription.getEventName())
                    && !subscriptionsForSameMessageName.contains(cachedSubscription)) {
              subscriptionsForSameMessageName.add(cachedSubscription);
            }
          }      
          // remove subscriptions deleted in the same command
          subscriptionsForSameMessageName = commandContext
                  .getDbSqlSession()
                  .pruneDeletedEntities(subscriptionsForSameMessageName);
                
          if(!subscriptionsForSameMessageName.isEmpty()) {
            throw new ActivitiException("Cannot deploy process definition '" + processDefinition.getResourceName()
                    + "': there already is a message event subscription for the message with name '" + messageEventDefinition.getEventName() + "'.");
          }
          
          MessageEventSubscriptionEntity newSubscription = new MessageEventSubscriptionEntity();
          newSubscription.setEventName(messageEventDefinition.getEventName());
          newSubscription.setActivityId(messageEventDefinition.getActivityId());
          newSubscription.setConfiguration(processDefinition.getId());
          
          newSubscription.insert();
        }
      }
    }      
  }
  
  /**
   * The Enum ExprType.
   */
  enum ExprType {
	  
  	/** The user. */
  	USER, 
 /** The group. */
 GROUP
  }
  
  /**
   * Adds the authorizations from iterator.
   *
   * @param exprSet the expr set
   * @param processDefinition the process definition
   * @param exprType the expr type
   */
  private void addAuthorizationsFromIterator(Set<Expression> exprSet, ProcessDefinitionEntity processDefinition, ExprType exprType) {
    CommandContext commandContext = Context.getCommandContext();
    if (exprSet != null) {
      Iterator<Expression> iterator = exprSet.iterator();
      while (iterator.hasNext()) {
        Expression expr = (Expression) iterator.next();
        IdentityLinkEntity identityLink = new IdentityLinkEntity();
        identityLink.setProcessDef(processDefinition);
        if (exprType.equals(ExprType.USER)) {
           identityLink.setUserId(expr.toString());
        } else if (exprType.equals(ExprType.GROUP)) {
          identityLink.setGroupId(expr.toString());
        }
        identityLink.setType(IdentityLinkType.CANDIDATE);
        commandContext.getDbSqlSession().insert(identityLink);
      }
    }
  }

  /**
   * Adds the authorizations.
   *
   * @param processDefinition the process definition
   */
  protected void addAuthorizations(ProcessDefinitionEntity processDefinition) {
    addAuthorizationsFromIterator(processDefinition.getCandidateStarterUserIdExpressions(), processDefinition, ExprType.USER);
    addAuthorizationsFromIterator(processDefinition.getCandidateStarterGroupIdExpressions(), processDefinition, ExprType.GROUP);
  }

  /**
   * Returns the default name of the image resource for a certain process.
   * 
   * It will first look for an image resource which matches the process
   * specifically, before resorting to an image resource which matches the BPMN
   * 2.0 xml file resource.
   * 
   * Example: if the deployment contains a BPMN 2.0 xml resource called
   * 'abc.bpmn20.xml' containing only one process with key 'myProcess', then
   * this method will look for an image resources called 'abc.myProcess.png'
   * (or .jpg, or .gif, etc.) or 'abc.png' if the previous one wasn't found.
   * 
   * Example 2: if the deployment contains a BPMN 2.0 xml resource called
   * 'abc.bpmn20.xml' containing three processes (with keys a, b and c),
   * then this method will first look for an image resource called 'abc.a.png'
   * before looking for 'abc.png' (likewise for b and c).
   * Note that if abc.a.png, abc.b.png and abc.c.png don't exist, all
   * processes will have the same image: abc.png.
   *
   * @param bpmnFileResource the bpmn file resource
   * @param processKey the process key
   * @param resources the resources
   * @return null if no matching image resource is found.
   */
  protected String getDiagramResourceForProcess(String bpmnFileResource, String processKey, Map<String, ResourceEntity> resources) {
    for (String diagramSuffix: DIAGRAM_SUFFIXES) {
      String diagramForBpmnFileResource = getBpmnFileImageResourceName(bpmnFileResource, diagramSuffix);
      String processDiagramResource = getProcessImageResourceName(bpmnFileResource, processKey, diagramSuffix);
      if (resources.containsKey(processDiagramResource)) {
        return processDiagramResource;
      } else if (resources.containsKey(diagramForBpmnFileResource)) {
        return diagramForBpmnFileResource;
      }
    }
    return null;
  }
  
  /**
   * Gets the bpmn file image resource name.
   *
   * @param bpmnFileResource the bpmn file resource
   * @param diagramSuffix the diagram suffix
   * @return the bpmn file image resource name
   */
  protected String getBpmnFileImageResourceName(String bpmnFileResource, String diagramSuffix) {
    String bpmnFileResourceBase = stripBpmnFileSuffix(bpmnFileResource);
    return bpmnFileResourceBase + diagramSuffix;
  }

  /**
   * Gets the process image resource name.
   *
   * @param bpmnFileResource the bpmn file resource
   * @param processKey the process key
   * @param diagramSuffix the diagram suffix
   * @return the process image resource name
   */
  protected String getProcessImageResourceName(String bpmnFileResource, String processKey, String diagramSuffix) {
    String bpmnFileResourceBase = stripBpmnFileSuffix(bpmnFileResource);
    return bpmnFileResourceBase + processKey + "." + diagramSuffix;
  }

  /**
   * Strip bpmn file suffix.
   *
   * @param bpmnFileResource the bpmn file resource
   * @return the string
   */
  protected String stripBpmnFileSuffix(String bpmnFileResource) {
    for (String suffix : BPMN_RESOURCE_SUFFIXES) {
      if (bpmnFileResource.endsWith(suffix)) {
        return bpmnFileResource.substring(0, bpmnFileResource.length() - suffix.length());
      }
    }
    return bpmnFileResource;
  }

  /**
   * Creates the resource.
   *
   * @param name the name
   * @param bytes the bytes
   * @param deploymentEntity the deployment entity
   */
  protected void createResource(String name, byte[] bytes, DeploymentEntity deploymentEntity) {
    ResourceEntity resource = new ResourceEntity();
    resource.setName(name);
    resource.setBytes(bytes);
    resource.setDeploymentId(deploymentEntity.getId());
    
    // Mark the resource as 'generated'
    resource.setGenerated(true);
    
    Context
      .getCommandContext()
      .getDbSqlSession()
      .insert(resource);
  }
  
  /**
   * Checks if is bpmn resource.
   *
   * @param resourceName the resource name
   * @return true, if is bpmn resource
   */
  protected boolean isBpmnResource(String resourceName) {
    for (String suffix : BPMN_RESOURCE_SUFFIXES) {
      if (resourceName.endsWith(suffix)) {
        return true;
      }
    }
    return false;
  }
  
  /**
   * Gets the expression manager.
   *
   * @return the expression manager
   */
  public ExpressionManager getExpressionManager() {
    return expressionManager;
  }
  
  /**
   * Sets the expression manager.
   *
   * @param expressionManager the new expression manager
   */
  public void setExpressionManager(ExpressionManager expressionManager) {
    this.expressionManager = expressionManager;
  }
  
  /**
   * Gets the bpmn parser.
   *
   * @return the bpmn parser
   */
  public BpmnParser getBpmnParser() {
    return bpmnParser;
  }
  
  /**
   * Sets the bpmn parser.
   *
   * @param bpmnParser the new bpmn parser
   */
  public void setBpmnParser(BpmnParser bpmnParser) {
    this.bpmnParser = bpmnParser;
  }
  
  /**
   * Gets the id generator.
   *
   * @return the id generator
   */
  public IdGenerator getIdGenerator() {
    return idGenerator;
  }
  
  /**
   * Sets the id generator.
   *
   * @param idGenerator the new id generator
   */
  public void setIdGenerator(IdGenerator idGenerator) {
    this.idGenerator = idGenerator;
  }
  
}
