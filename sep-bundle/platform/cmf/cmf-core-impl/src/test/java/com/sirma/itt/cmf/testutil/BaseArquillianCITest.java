/**
 *
 */
/**
 * @author bbanchev
 */
package com.sirma.itt.cmf.testutil;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.enterprise.event.Event;
import javax.inject.Inject;

import org.jboss.arquillian.testng.Arquillian;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;

import com.sirma.itt.cmf.beans.definitions.CaseDefinition;
import com.sirma.itt.cmf.beans.definitions.TaskDefinition;
import com.sirma.itt.cmf.beans.definitions.TaskDefinitionRef;
import com.sirma.itt.cmf.beans.definitions.WorkflowDefinition;
import com.sirma.itt.cmf.beans.model.CaseInstance;
import com.sirma.itt.cmf.beans.model.DocumentInstance;
import com.sirma.itt.cmf.beans.model.SectionInstance;
import com.sirma.itt.cmf.beans.model.StandaloneTaskInstance;
import com.sirma.itt.cmf.beans.model.TaskInstance;
import com.sirma.itt.cmf.beans.model.WorkflowInstanceContext;
import com.sirma.itt.cmf.constants.DocumentProperties;
import com.sirma.itt.cmf.constants.allowed_action.ActionTypeConstants;
import com.sirma.itt.cmf.services.CaseService;
import com.sirma.itt.cmf.services.DocumentService;
import com.sirma.itt.cmf.services.StandaloneTaskService;
import com.sirma.itt.cmf.services.TaskService;
import com.sirma.itt.cmf.services.WorkflowService;
import com.sirma.itt.cmf.services.adapters.CMFUserServiceMock;
import com.sirma.itt.cmf.services.adapters.CodelistServerAccessorMock;
import com.sirma.itt.cmf.services.impl.GroupServiceImpl;
import com.sirma.itt.cmf.services.impl.MailNotificationServiceImpl;
import com.sirma.itt.cmf.services.impl.PeopleServiceImpl;
import com.sirma.itt.cmf.services.mock.CodelistServiceProviderMock;
import com.sirma.itt.cmf.services.mock.CommentServiceImplMock;
import com.sirma.itt.cmf.services.mock.GroupServiceImplMock;
import com.sirma.itt.cmf.services.mock.LinkProviderServiceMock;
import com.sirma.itt.cmf.services.mock.MailNotificationServiceMock;
import com.sirma.itt.cmf.services.mock.PeopleServiceImplMock;
import com.sirma.itt.cmf.states.PrimaryStates;
import com.sirma.itt.cmf.workflows.WorkflowHelper;
import com.sirma.itt.emf.adapter.FileAndPropertiesDescriptor;
import com.sirma.itt.emf.annotation.Proxy;
import com.sirma.itt.emf.configuration.ConfigurationFactory;
import com.sirma.itt.emf.configuration.RuntimeConfiguration;
import com.sirma.itt.emf.configuration.RuntimeConfigurationProperties;
import com.sirma.itt.emf.definition.DictionaryService;
import com.sirma.itt.emf.definition.event.LoadAllDefinitions;
import com.sirma.itt.emf.definition.load.DefinitionLoader;
import com.sirma.itt.emf.domain.model.DefinitionModel;
import com.sirma.itt.emf.forum.CommentService;
import com.sirma.itt.emf.forum.ForumProperties;
import com.sirma.itt.emf.forum.model.CommentInstance;
import com.sirma.itt.emf.forum.model.TopicInstance;
import com.sirma.itt.emf.instance.dao.InstanceService;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.properties.DefaultProperties;
import com.sirma.itt.emf.security.AuthenticationService;
import com.sirma.itt.emf.security.AuthenticationServiceMock;
import com.sirma.itt.emf.security.model.User;
import com.sirma.itt.emf.state.operation.Operation;

/**
 * The BaseArquillianCITest provides basic method to be used during ci test.
 *
 * @author bbanchev
 */
public class BaseArquillianCITest extends Arquillian {

	/** The Constant DEFINITIONS_RELOAD_FORCE. */
	protected static final String DEFINITIONS_RELOAD_FORCE = "definitions.reload.force";

	/** The Constant DEFAULT_DEFINITION_ID_CASE. */
	protected static final String DEFAULT_DEFINITION_ID_CASE = "DefaultCaseDevel";
	/** The Constant DEFAULT_DEFINITION_ID_DOC. */
	protected static final String DEFAULT_DEFINITION_ID_DOC = "OT210027";

	/** The Constant DEFAULT_DEFINITION_ID_WORKFLOW. */
	protected static final String DEFAULT_DEFINITION_ID_WORKFLOW = "activiti$WFTYPE999";

	/** The Constant DEFAULT_DEFINITION_ID_TASK_STANDALONE. */
	protected static final String DEFAULT_DEFINITION_ID_TASK_STANDALONE = "standaloneCommonTask";

	/** The thread pool. */
	protected static ExecutorService threadPool = Executors.newFixedThreadPool(100);

	/** The dictionary service. */
	@Inject
	protected DictionaryService dictionaryService;

	/** The configuration factory. */
	@Inject
	protected ConfigurationFactory configurationFactory;

	/** The authentication service. */
	@Inject
	protected AuthenticationService authenticationService;
	/** The all definitions event. */
	@Inject
	protected Event<LoadAllDefinitions> allDefinitionsEvent;

	/** The intance service. */
	@Inject
	@Proxy
	private InstanceService<Instance, DefinitionModel> instanceService;

	/** The testable users ids. */
	protected String[] testableUsersIds = new String[] { "admin", "bbanchev", "automatron",
			"Consumer", "tester" };

	/** The definition loader. */
	@Inject
	private DefinitionLoader definitionLoader;

	/** The default users. */
	private List<User> defaultUsers = null;

	/** The Constant LOGGER. */
	protected static final Logger LOGGER = LoggerFactory.getLogger(BaseArquillianCITest.class);

	/**
	 * Inits the builder.
	 *
	 * @param builder
	 *            is the test builder to use
	 * @return the test resouce builder wrapper
	 */
	@SuppressWarnings("unchecked")
	protected static TestResouceBuilderWrapper defaultBuilder(TestPackageBuilder builder) {
		return new TestResouceBuilderWrapper(builder.exclude(ModulesInfo.CMF_CORE_IMPL))
				.init(JarPackages.BASIC, JarPackages.DOZER, JarPackages.SECURITY,
						JarPackages.CASE_CREATION, JarPackages.ADAPTERS_MOCK, JarPackages.RESOURCES)
				.addClasess(BaseArquillianCITest.class, CmfTestResourcePackager.class,
						AuthenticationServiceMock.class, JarPackages.class,
						TestableJarModules.class, TestableWarModules.class, WarPackages.class,
						TestPackageBuilder.class, TestResouceBuilderWrapper.class,
						ResourceImporter.class, MailNotificationServiceMock.class,
						MailNotificationServiceImpl.class, CodelistServerAccessorMock.class,
						CodelistServiceProviderMock.class)
				.addClasess(LinkProviderServiceMock.class, GroupServiceImpl.class,
						PeopleServiceImpl.class, GroupServiceImplMock.class,
						PeopleServiceImplMock.class, CommentServiceImplMock.class);
	}

	/**
	 * Gets the defintions gor given class from the disctionary service.
	 *
	 * @param <T>
	 *            the generic type
	 * @param cls
	 *            the cls to load instances for as {@link
	 *            com.sirma.itt.cmf.beans.model.CaseInstance.class}
	 * @return the defintions for the class
	 */
	@SuppressWarnings("unchecked")
	protected <T extends DefinitionModel> List<T> getDefintions(
			@SuppressWarnings("rawtypes") Class cls) {
		List<T> allDefinitions = dictionaryService.getAllDefinitions(cls);
		String reloadDef = System.getProperty(DEFINITIONS_RELOAD_FORCE);
		if (allDefinitions.isEmpty() || Boolean.valueOf(reloadDef)) {
			startAndWait(new Callable<Void>() {

				@Override
				public Void call() throws Exception {
					definitionLoader.loadTemplateDefinitions();
					definitionLoader.loadDefinitions();
					return null;
				}
			});
			allDefinitions = dictionaryService.getAllDefinitions(cls);
		}
		return allDefinitions;
	}

	/**
	 * Creates the case for given definition and sets the properties.
	 *
	 * @param parent
	 *            the parent
	 * @param definitionId
	 *            the definition id
	 * @param caseProperties
	 *            the case properties
	 * @return the case instance created
	 */
	protected CaseInstance createCase(Instance parent, String definitionId,
			Map<String, Serializable> caseProperties) {
		CaseDefinition definition = getDefinition(CaseDefinition.class, definitionId);
		CaseInstance caseInstance = getCaseService().createInstance(definition, parent);
		caseInstance.getProperties().putAll(caseProperties);
		getCaseService().save(caseInstance, new Operation(ActionTypeConstants.CREATE_CASE));
		return caseInstance;
	}

	/**
	 * Upload document to the section with some mocked data in the instance as well.
	 *
	 * @param parent
	 *            the section to add to
	 * @param descriptor
	 *            the upload descriptor
	 * @return the document instance attached
	 */
	protected DocumentInstance uploadDocument(SectionInstance parent,
			FileAndPropertiesDescriptor descriptor) {
		DocumentInstance createDocumentInstance = getDocumentService().createDocumentInstance(
				parent, DEFAULT_DEFINITION_ID_DOC);
		createDocumentInstance.getProperties().put(DefaultProperties.TITLE, descriptor.getId());
		createDocumentInstance.getProperties().put(DefaultProperties.DESCRIPTION,
				descriptor.getId() + "" + parent.toString());
		createDocumentInstance.getProperties().put(DocumentProperties.FILE_LOCATOR, descriptor);
		descriptor.getProperties().putAll(createDocumentInstance.getProperties());
		getDocumentService().upload(parent, true, createDocumentInstance);
		// instanceService.attach(parent, new Operation(ActionTypeConstants.UPLOAD),
		// createDocumentInstance);
		Instance caseInstance = parent.getOwningInstance();
		RuntimeConfiguration.setConfiguration(RuntimeConfigurationProperties.DO_NOT_SAVE_CHILDREN,
				Boolean.TRUE);
		instanceService.save(caseInstance, new Operation(ActionTypeConstants.UPLOAD));

		return createDocumentInstance;
	}

	/**
	 * Creates a new toptic attached to instance.
	 *
	 * @param instance
	 *            is the instance related to
	 * @param user
	 *            is the post user
	 * @return the topic created.
	 */
	protected TopicInstance createTopic(final Instance instance, User user) {

		// new topic is going to be created
		TopicInstance topicInstance = new TopicInstance();
		topicInstance.getProperties().put(DefaultProperties.TITLE,
				"Topic about " + instance.getProperties().get(DefaultProperties.TITLE));
		topicInstance.setTopicAbout(instance.toReference());
		topicInstance.setPostedDate(new Date());
		topicInstance.setFrom(user.getId().toString());
		topicInstance.getProperties().put(DefaultProperties.STATUS, PrimaryStates.IN_PROGRESS);
		topicInstance.getProperties().put(DefaultProperties.TYPE, "Note to self");
		String tags = "default";
		topicInstance.setTags(tags);
		getCommentService().save(topicInstance, new Operation(ActionTypeConstants.EDIT_DETAILS));
		return topicInstance;

	}

	/**
	 * Creates a new comment attached to topic.
	 *
	 * @param topicInstance
	 *            is the topic instance related to
	 * @param user
	 *            is the post user
	 * @return the comment created.
	 */
	protected CommentInstance createComment(final TopicInstance topicInstance, User user) {
		CommentInstance comment = new CommentInstance();
		String tags = "default";
		comment.getProperties().put(DefaultProperties.TITLE, "comment title");
		comment.getProperties().put(DefaultProperties.TYPE, "Note to self");
		comment.getProperties().put(DefaultProperties.STATUS, PrimaryStates.IN_PROGRESS);
		comment.getProperties().put(ForumProperties.TAGS, tags);
		comment.setComment("My comment @" + user.getIdentifier());
		comment.setFrom(user.getId().toString());
		comment.setPostedDate(new Date());
		getCommentService().postComment(topicInstance, comment);
		return comment;
	}

	/**
	 * Select randomly user and optionally authenticated it.
	 *
	 * @param authenticate
	 *            whether after select to automatically set it authenticated.
	 * @param skip
	 *            is list of user to skip
	 * @return the selected user
	 */
	protected User chooseNewUser(boolean authenticate, String... skip) {
		if (defaultUsers == null) {
			defaultUsers = CMFUserServiceMock.defaultUsers();
		}

		User user = null;
		boolean selected = (skip == null) || (skip.length == 0);
		while (!selected) {
			selected = true;
			user = defaultUsers.get((int) (Math.random() * defaultUsers.size()));
			for (String skipId : skip) {
				if (user.getIdentifier().equals(skipId)) {
					selected = false;
				}
			}
		}
		if (authenticate) {
			authenticationService.setAuthenticatedUser(user);
		}
		return user;
	}

	/**
	 * Start workflow helper method.
	 *
	 * @param owningInstance
	 *            the owning instance
	 * @param wfDefinitionId
	 *            the wf definition id
	 * @param wfProps
	 *            are the startup properties
	 * @return the workflow instance context
	 */
	protected WorkflowInstanceContext startWorkflow(Instance owningInstance, String wfDefinitionId,
			Map<String, Serializable> wfProps) {
		WorkflowDefinition wfDefinition = getDefinition(WorkflowInstanceContext.class,
				wfDefinitionId);
		WorkflowInstanceContext wfInstance = getWorkflowService().createInstance(wfDefinition,
				owningInstance);
		// start wf
		TaskDefinitionRef startTaskDefinition = WorkflowHelper.getStartTask(wfDefinition);
		TaskInstance startTask = (TaskInstance) getTaskService().createInstance(
				startTaskDefinition, wfInstance);
		startTask.getProperties().putAll(wfProps);
		getWorkflowService().startWorkflow(wfInstance, startTask);
		return wfInstance;
	}

	/**
	 * Start standalone task helper method.
	 *
	 * @param owningInstance
	 *            the owning instance to attach to
	 * @param taskDefinitionId
	 *            the task definition id
	 * @param taskProps
	 *            the task props to start with
	 * @return the standalone task instance that is started
	 */
	protected StandaloneTaskInstance startStandaloneTask(Instance owningInstance,
			String taskDefinitionId, Map<String, Serializable> taskProps) {
		TaskDefinition standaloneTaskDefinition = getDefinition(TaskDefinition.class,
				taskDefinitionId);
		StandaloneTaskInstance standaloneTask = (StandaloneTaskInstance) getTaskService()
				.createInstance(standaloneTaskDefinition, owningInstance);
		standaloneTask.getProperties().putAll(taskProps);
		getStandaloneTaskService().start(standaloneTask,
				new Operation(ActionTypeConstants.CREATE_TASK));
		return standaloneTask;
	}

	/**
	 * Gets the workflow service. Must be override to start workflow.
	 *
	 * @return the workflow service
	 */
	protected WorkflowService getWorkflowService() {
		return null;
	}

	/**
	 * Gets the task service. Must be override to start task.
	 *
	 * @return the task service
	 */
	protected TaskService getTaskService() {
		return null;
	}

	/**
	 * Gets the document service. Must be override to upload document
	 *
	 * @return the document service
	 */
	protected DocumentService getDocumentService() {
		return null;
	}

	/**
	 * Gets the standalone task service.
	 *
	 * @return the standalone task service
	 */
	protected StandaloneTaskService getStandaloneTaskService() {
		return null;
	}

	/**
	 * Gets the case service. Should be implemented to create cases.
	 *
	 * @return the case service
	 */
	protected CaseService getCaseService() {
		return null;
	}

	/**
	 * Gets the comment service. Should be implemented to create comments.
	 *
	 * @return the comment service
	 */
	protected CommentService getCommentService() {
		return null;
	}

	/**
	 * Gets the definition by id for given type. If not definitions are found exceptions is thrown
	 *
	 * @param <T>
	 *            the generic type
	 * @param cls
	 *            the cls
	 * @param modelId
	 *            the model id
	 * @return the defintion
	 */
	protected <T extends DefinitionModel> T getDefinition(Class<?> cls, String modelId) {
		List<T> defintions = getDefintions(cls);
		T loadDefinitionInternal = loadDefinitionInternal(modelId, defintions);
		if (loadDefinitionInternal != null) {
			return loadDefinitionInternal;
		}
		Boolean reload = Boolean.FALSE;
		try {
			reload = Boolean.valueOf(System.getProperty(DEFINITIONS_RELOAD_FORCE));
			System.setProperty(DEFINITIONS_RELOAD_FORCE, "true");
			defintions = getDefintions(cls);
			loadDefinitionInternal = loadDefinitionInternal(modelId, defintions);
			if (loadDefinitionInternal != null) {
				return loadDefinitionInternal;
			}
		} finally {
			System.setProperty(DEFINITIONS_RELOAD_FORCE,
					new Boolean(reload.booleanValue()).toString());
		}
		fail("Model " + modelId + " is not found");
		return null;
	}

	/**
	 * Load definition internal
	 *
	 * @param <T>
	 *            the generic type
	 * @param modelId
	 *            the model id to look for
	 * @param defintions
	 *            the defintions for this particular class
	 * @return the model definition or null if not found
	 */
	private <T extends DefinitionModel> T loadDefinitionInternal(String modelId, List<T> defintions) {
		if ((defintions == null) || defintions.isEmpty()) {
			throw new IllegalArgumentException(
					"The provided class does not have any valid definition");
		}
		for (T def : defintions) {
			if (modelId.equals(def.getIdentifier())) {
				return def;
			}
		}
		return null;
	}

	/**
	 * Start and wait a new callable thread.
	 *
	 * @param <T>
	 *            the generic type
	 * @param check
	 *            the check
	 * @return the callable
	 */
	protected <T> T startAndWait(Callable<T> check) {

		Future<T> invokedInThread = threadPool.submit(check);
		try {
			return invokedInThread.get();
		} catch (Exception e) {
			Assert.fail("Thread execution failed with exception!", e);
		}
		return null;
	}

	/**
	 * Fail test.
	 *
	 * @param error
	 *            the error
	 */
	protected void fail(String error) {
		Assert.fail(error);
	}

	/**
	 * Fail on error.
	 *
	 * @param exception
	 *            the exception
	 */
	protected void fail(Throwable exception) {
		if (exception.getMessage() != null) {
			Assert.fail(exception.getMessage(), exception);
		} else {
			Assert.fail("Unexpected event", exception);
		}
	}

	/**
	 * Assert true a condition.
	 *
	 * @param condition
	 *            the condition
	 * @param msg
	 *            the msg
	 */
	protected void assertTrue(boolean condition, String msg) {
		Assert.assertTrue(condition, msg);
	}

	/**
	 * Assert equals.
	 *
	 * @param actual
	 *            the actual
	 * @param expected
	 *            the expected
	 * @param msg
	 *            the msg
	 */
	protected void assertEquals(Serializable actual, Serializable expected, String msg) {
		Assert.assertEquals(actual, expected, msg);
	}
}