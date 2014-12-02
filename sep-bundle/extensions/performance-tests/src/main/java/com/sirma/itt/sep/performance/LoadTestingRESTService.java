package com.sirma.itt.sep.performance;

import java.io.File;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.sirma.itt.cmf.beans.ByteArrayFileDescriptor;
import com.sirma.itt.cmf.beans.LocalFileAndPropertiesDescriptor;
import com.sirma.itt.cmf.beans.definitions.CaseDefinition;
import com.sirma.itt.cmf.beans.definitions.DocumentDefinitionTemplate;
import com.sirma.itt.cmf.beans.definitions.TaskDefinition;
import com.sirma.itt.cmf.beans.definitions.TaskDefinitionRef;
import com.sirma.itt.cmf.beans.definitions.WorkflowDefinition;
import com.sirma.itt.cmf.beans.definitions.impl.DocumentDefinitionRefProxy;
import com.sirma.itt.cmf.beans.model.CaseInstance;
import com.sirma.itt.cmf.beans.model.DocumentInstance;
import com.sirma.itt.cmf.beans.model.SectionInstance;
import com.sirma.itt.cmf.beans.model.StandaloneTaskInstance;
import com.sirma.itt.cmf.beans.model.TaskInstance;
import com.sirma.itt.cmf.beans.model.WorkflowInstanceContext;
import com.sirma.itt.cmf.constants.DocumentProperties;
import com.sirma.itt.cmf.constants.TaskProperties;
import com.sirma.itt.cmf.constants.allowed_action.ActionTypeConstants;
import com.sirma.itt.cmf.event.task.standalone.AfterStandaloneTaskPersistEvent;
import com.sirma.itt.cmf.event.task.workflow.AfterTaskPersistEvent;
import com.sirma.itt.cmf.services.CaseService;
import com.sirma.itt.cmf.services.DocumentService;
import com.sirma.itt.cmf.services.DocumentTemplateService;
import com.sirma.itt.cmf.services.StandaloneTaskService;
import com.sirma.itt.cmf.services.TaskService;
import com.sirma.itt.cmf.services.WorkflowService;
import com.sirma.itt.cmf.workflows.WorkflowHelper;
import com.sirma.itt.emf.annotation.Proxy;
import com.sirma.itt.emf.converter.TypeConverter;
import com.sirma.itt.emf.db.DbDao;
import com.sirma.itt.emf.definition.DictionaryService;
import com.sirma.itt.emf.definition.model.PropertyDefinition;
import com.sirma.itt.emf.definition.model.RegionDefinition;
import com.sirma.itt.emf.definition.model.RegionDefinitionModel;
import com.sirma.itt.emf.domain.DisplayType;
import com.sirma.itt.emf.domain.model.DefinitionModel;
import com.sirma.itt.emf.exceptions.EmfRuntimeException;
import com.sirma.itt.emf.forum.CommentService;
import com.sirma.itt.emf.forum.model.CommentInstance;
import com.sirma.itt.emf.forum.model.TopicInstance;
import com.sirma.itt.emf.instance.dao.InstanceService;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.properties.DefaultProperties;
import com.sirma.itt.emf.resources.ResourceService;
import com.sirma.itt.emf.resources.ResourceType;
import com.sirma.itt.emf.resources.model.Resource;
import com.sirma.itt.emf.rest.model.RestInstance;
import com.sirma.itt.emf.security.AuthenticationService;
import com.sirma.itt.emf.security.SecurityModel;
import com.sirma.itt.emf.security.SecurityTokenService;
import com.sirma.itt.emf.security.context.SecurityContextManager;
import com.sirma.itt.emf.security.model.RoleIdentifier;
import com.sirma.itt.emf.security.model.User;
import com.sirma.itt.emf.security.model.UserWithCredentials;
import com.sirma.itt.emf.state.PrimaryStateType;
import com.sirma.itt.emf.state.operation.Operation;
import com.sirma.itt.emf.template.TemplateInstance;
import com.sirma.itt.emf.util.JsonUtil;
import com.sirma.itt.objects.domain.definitions.ObjectDefinition;
import com.sirma.itt.objects.domain.model.ObjectInstance;
import com.sirma.itt.objects.security.ObjectActionTypeConstants;
import com.sirma.itt.objects.services.ObjectService;
import com.sirma.itt.pm.constants.ProjectProperties;
import com.sirma.itt.pm.domain.definitions.ProjectDefinition;
import com.sirma.itt.pm.domain.model.ProjectInstance;
import com.sirma.itt.pm.security.PmActionTypeConstants;
import com.sirma.itt.pm.services.ProjectService;

/**
 * The Class LoadTestingRESTService.
 */
@Path("/performance")
@Produces(MediaType.TEXT_PLAIN)
@ApplicationScoped
public class LoadTestingRESTService {

	/** The Constant DEFAULT_DEFINITION_ID_PROJECT. */
	protected static final String DEFAULT_DEFINITION_ID_PROJECT = "GEP10002";

	/** The Constant DEFAULT_DEFINITION_ID_CASE. */
	protected static final String DEFAULT_DEFINITION_ID_CASE = "SEMANTIC_TEST";

	/** The Constant DEFAULT_DEFINITION_ID_DOC. */
	protected static final String DEFAULT_DEFINITION_ID_DOC = "contentdocument";

	/** The Constant DEFAULT_DEFINITION_ID_TASK_STANDALONE. */
	protected static final String DEFAULT_DEFINITION_ID_TASK_STANDALONE = "standaloneCommonTask";

	/** The date format. */
	private final SimpleDateFormat dateFormat = new SimpleDateFormat("ddMMHHmmss");

	/** The logger. */
	private Logger logger = Logger.getLogger(LoadTestingRESTService.class);

	// ----------------------- services -------------
	/** The instance service. */
	@Inject
	@Proxy
	protected InstanceService<Instance, DefinitionModel> instanceService;

	/** The authentication service. */
	@Inject
	private AuthenticationService authenticationService;

	/** The intance service. */
	@Inject
	@Proxy
	private InstanceService<Instance, DefinitionModel> intanceService;

	/** The object service. */
	@Inject
	private ObjectService objectService;

	/** The comment service. */
	@Inject
	private CommentService commentService;

	/** The project service. */
	@Inject
	private ProjectService projectService;

	/** The case service. */
	@Inject
	private CaseService caseService;

	/** The task service. */
	@Inject
	private TaskService taskService;

	/** The standalone task service. */
	@Inject
	private StandaloneTaskService standaloneTaskService;

	/** The workflow service. */
	@Inject
	private WorkflowService workflowService;

	/** The document service. */
	@Inject
	private DocumentService documentService;

	/** The dictionary service. */
	@Inject
	private DictionaryService dictionaryService;

	/** The resource service. */
	@Inject
	private ResourceService resourceService;

	/** The dao. */
	@Inject
	private DbDao dao;

	/** The security token service. */
	@Inject
	private SecurityTokenService securityTokenService;

	/** The type converter. */
	@Inject
	private TypeConverter typeConverter;

	/** The document template service. */
	@Inject
	private DocumentTemplateService documentTemplateService;

	/** The helper service. */
	@Inject
	private LoadTestingHelperService helperService;

	/** The Constant SESSIONS_IDS. */
	private static final ThreadLocal<String> SESSIONS_IDS = new ThreadLocal<>();

	/**
	 * Import test data.
	 *
	 * @param sessionId
	 *            the session id
	 * @param pmCount
	 *            the pm count
	 * @param taskCount
	 *            the task count
	 * @param commentCount
	 *            the comment count
	 * @return the string
	 */
	@Path("/create/{sessionId}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public String createRootData(final @PathParam("sessionId") String sessionId,
			@QueryParam("projects") String pmCount, @QueryParam("tasks") String taskCount,
			@QueryParam("comments") String commentCount) {
		if (sessionId == null || sessionId.isEmpty()) {
			throw new IllegalArgumentException("Invalid client id");
		}
		authenticationService.authenticateAsAdmin();
		SESSIONS_IDS.set(sessionId);
		final int baseScale = Integer.parseInt(pmCount == null ? "1" : pmCount);
		// up to maxFiles

		final int taskScale = Integer.parseInt(taskCount == null ? "10" : taskCount);
		final int commentScale = Integer.parseInt(commentCount == null ? "5" : commentCount);
		// the path (project, case, section, doc)
		JSONArray projects = new JSONArray();
		for (int i = 0; i < baseScale; i++) {
			final ProjectInstance parent = dao.invokeInNewTx(new Callable<ProjectInstance>() {

				@Override
				public ProjectInstance call() throws Exception {
					return createProject(sessionId);
				}

			});

			// helperService.get(getCurrentClientId()).link(null, parent);
			commentOnInstance(parent, commentScale);
			projects.put(parent.getId());
			// finally start some tasks for project
			dao.invokeInNewTx(new Callable<Void>() {

				@Override
				public Void call() throws Exception {
					// start only some tasks for project
					startActivities(parent, 0, taskScale);
					return null;
				}

			});

		}
		try {
			JSONObject result = new JSONObject();
			JsonUtil.addToJson(result, "instances", projects);
			JSONObject statistics = helperService.get(sessionId).getStatistics();
			JsonUtil.addToJson(result, "statistics", statistics);
			return result.toString();
		} catch (Exception e) {
			return "{\"Error\":\"" + e.getMessage() + "\"}";
		}
	}

	/**
	 * Adds the test data.
	 *
	 * @param sessionId
	 *            the session id
	 * @param projectId
	 *            the project id
	 * @param caseCount
	 *            the case count
	 * @param workfklowCount
	 *            the workfklow count
	 * @param taskCount
	 *            the task count
	 * @param docCount
	 *            the doc count
	 * @param objectCount
	 *            the object count
	 * @param idocCount
	 *            the idoc count
	 * @param commentCount
	 *            the comment count
	 * @return the string
	 */
	@Path("/add/{sessionId}/{projectId}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public String addTestData(final @PathParam("sessionId") String sessionId,
			@PathParam("projectId") String projectId, @QueryParam("cases") String caseCount,
			@QueryParam("workflows") String workfklowCount, @QueryParam("tasks") String taskCount,
			@QueryParam("docs") String docCount, @QueryParam("objects") String objectCount,
			@QueryParam("idocs") String idocCount, @QueryParam("comments") String commentCount) {
		SESSIONS_IDS.set(sessionId);
		authenticationService.authenticateAsAdmin();
		// up to maxFiles
		final int caseScale = Integer.parseInt(caseCount == null ? "50" : caseCount);
		final int workflowScale = Integer.parseInt(workfklowCount == null ? "2" : workfklowCount);
		final int taskScale = Integer.parseInt(taskCount == null ? "10" : taskCount);
		final int fileScale = Integer.parseInt(docCount == null ? "5" : docCount);
		final int idocScale = Integer.parseInt(idocCount == null ? "5" : idocCount);
		final int objectScale = Integer.parseInt(objectCount == null ? "5" : objectCount);
		final int commentScale = Integer.parseInt(commentCount == null ? "5" : commentCount);
		// the path (project, case, section, doc)
		final ProjectInstance parent = projectService.load(projectId);
		addProjectInstances(parent, caseScale, workflowScale, taskScale, fileScale, idocScale,
				objectScale, commentScale);

		JSONObject statistics = helperService.get(sessionId).getStatistics();
		try {
			return statistics.toString();
		} catch (Exception e) {
			return "{\"Error\":\"" + e.getMessage() + "\"}";
		}
	}

	/**
	 * Gets the info.
	 *
	 * @param sessionId
	 *            the session id
	 * @return the info
	 */
	@Path("/getinfo/{sessionId}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public String getInfo(final @PathParam("sessionId") String sessionId) {
		authenticationService.authenticateAsAdmin();
		JSONObject statistics = helperService.get(sessionId).getStatistics();
		try {
			return statistics.toString();
		} catch (Exception e) {
			return "{\"Error\":\"" + e.getMessage() + "\"}";
		}
	}

	/**
	 * Adds the project instances to the specified project.
	 *
	 * @param parent
	 *            the parent project to add to
	 * @param caseScale
	 *            the cases to add
	 * @param workflowScale
	 *            the workflows to start
	 * @param taskScale
	 *            the tasks to add
	 * @param fileScale
	 *            the files to add
	 * @param idocScale
	 *            the idocs to add
	 * @param objectScale
	 *            the objects to add
	 * @param commentScale
	 *            is the comments to add
	 */
	private void addProjectInstances(final ProjectInstance parent, int caseScale,
			final int workflowScale, final int taskScale, final int fileScale, final int idocScale,
			final int objectScale, final int commentScale) {
		// LoadTestingRESTServiceStateWrapper infoWrapper = helperService.get(getCurrentClientId());
		for (int j = 0; j < caseScale; j++) {

			logger.info("Progress " + "cases :" + ((float) j / (float) caseScale) * 100);
			// next case
			// infoWrapper.getCurrentTreePaths()[1]++;
			// infoWrapper.getCurrentTreePaths()[2] = 0;
			// infoWrapper.getCurrentTreePaths()[3] = 0;
			final CaseInstance caseInstance = dao.invokeInNewTx(new Callable<CaseInstance>() {

				@Override
				public CaseInstance call() throws Exception {
					CaseInstance caseInstance = createCaseInstance(parent);
					commentOnInstance(caseInstance, commentScale);
					return caseInstance;
				}

			});
			dao.invokeInNewTx(new Callable<Void>() {

				@Override
				public Void call() throws Exception {
					startActivities(caseInstance, workflowScale, taskScale);
					return null;
				}

			});
			// store the objects and use them for upload
			final List<Instance> createdObjects = dao.invokeInNewTx(new Callable<List<Instance>>() {

				@Override
				public List<Instance> call() throws Exception {
					return createObjects(caseInstance, objectScale, commentScale);
				}

			});
			createdObjects.add(caseInstance);
			dao.invokeInNewTx(new Callable<Void>() {

				@Override
				public Void call() throws Exception {
					createDocuments(createdObjects, fileScale, commentScale);
					return null;
				}

			});
			dao.invokeInNewTx(new Callable<Void>() {

				@Override
				public Void call() throws Exception {
					createIdocs(caseInstance, idocScale, commentScale);
					return null;
				}

			});
		}
	}

	/**
	 * On task created.
	 *
	 * @param event
	 *            the event
	 */
	public void onTaskCreated(@Observes AfterTaskPersistEvent event) {
		String currentClientId = getCurrentClientId();
		if (currentClientId == null) {
			return;
		}
		TaskInstance instance = event.getInstance();
		helperService.get(currentClientId).updateInstancesStatistic(TaskInstance.class, 1);
		commentOnInstance(instance, 5);
	}

	/**
	 * Gets the current client id.
	 *
	 * @return the current client id
	 */
	private String getCurrentClientId() {
		logger.info(Thread.currentThread().getId() + " is " + SESSIONS_IDS.get());
		return SESSIONS_IDS.get();
	}

	/**
	 * On standalone task created.
	 *
	 * @param event
	 *            the event
	 */
	public void onStandaloneTaskCreated(@Observes AfterStandaloneTaskPersistEvent event) {
		String sessionId = getCurrentClientId();
		if (sessionId == null) {
			return;
		}
		StandaloneTaskInstance instance = event.getInstance();
		helperService.get(sessionId).updateInstancesStatistic(StandaloneTaskInstance.class, 1);
		commentOnInstance(instance, 5);
	}

	/**
	 * Creates the documents.
	 *
	 * @param parents
	 *            the parents
	 * @param docScale
	 *            the doc scale
	 * @param commentScale
	 *            the comment scale
	 */
	private void createDocuments(List<Instance> parents, int docScale, int commentScale) {
		// six sections = 3 types *2
		String[] sectionsIds = new String[2];
		sectionsIds[0] = "official";
		sectionsIds[1] = "public";
		final String sessionId = getCurrentClientId();
		LoadTestingRESTServiceStateWrapper infoWrapper = helperService.get(sessionId);
		for (int i = 0; i < docScale; i++) {
			Instance parent = parents.get((int) (Math.random() * parents.size()));
			if (parent instanceof CaseInstance) {
				SectionInstance[] sections = new SectionInstance[sectionsIds.length];
				for (SectionInstance section : ((CaseInstance) parent).getSections()) {
					if (sectionsIds[0].equals(section.getIdentifier())) {
						sections[0] = section;
					} else if (sectionsIds[1].equals(section.getIdentifier())) {
						sections[1] = section;
					}
				}
				SectionInstance targetInstance = sections[(int) (Math.random() * sections.length)];
				// infoWrapper.getCurrentTreePaths()[2] = ((CaseInstance) parent).getSections()
				// .indexOf(targetInstance);
				// infoWrapper.getCurrentTreePaths()[3]++;
				uploadDocument(targetInstance, infoWrapper.getNextTestableFile(), commentScale);
			} else {
				uploadDocument(parent, infoWrapper.getNextTestableFile(), commentScale);
			}
		}
	}

	/**
	 * Upload document.
	 *
	 * @param parent
	 *            the parent
	 * @param file
	 *            the file
	 * @param commentScale
	 *            the comment scale
	 * @return the document instance
	 */
	private DocumentInstance uploadDocument(Instance parent, File file, int commentScale) {
		final String sessionId = getCurrentClientId();
		LoadTestingRESTServiceStateWrapper infoWrapper = helperService.get(sessionId);
		// int[] treePath = infoWrapper.getCurrentTreePaths();
		chooseNewUser(ActionTypeConstants.UPLOAD);

		String formatedDate = dateFormat.format(new Date());
		DocumentDefinitionTemplate definition = dictionaryService.getDefinition(
				DocumentDefinitionTemplate.class, DEFAULT_DEFINITION_ID_DOC);
		DocumentInstance createDocumentInstance = documentService.createInstance(
				new DocumentDefinitionRefProxy(definition), parent, new Operation(
						ActionTypeConstants.UPLOAD));
		createDocumentInstance.setStandalone(true);
		createDocumentInstance.getProperties().put(DefaultProperties.TITLE,
		// infoWrapper.arrayAsString(treePath, "") + " " +
				formatedDate);
		createDocumentInstance.getProperties().put(DefaultProperties.DESCRIPTION,
				infoWrapper.generateRandomString(20));
		createDocumentInstance.getProperties().put(
				DocumentProperties.FILE_LOCATOR,
				new LocalFileAndPropertiesDescriptor(file, null, createDocumentInstance
						.getProperties()));

		documentService.upload(parent, true, createDocumentInstance);
		instanceService.attach(parent, new Operation(ActionTypeConstants.UPLOAD),
				createDocumentInstance);
		if (parent instanceof CaseInstance) {
			// Instance caseInstance = parent.getOwningInstance();
			// RuntimeConfiguration.setConfiguration(
			// RuntimeConfigurationProperties.DO_NOT_SAVE_CHILDREN, Boolean.TRUE);
			// instanceService.save(caseInstance, new Operation(ActionTypeConstants.UPLOAD));
		}
		infoWrapper.updateInstancesStatistic(DocumentInstance.class, 1);

		commentOnInstance(createDocumentInstance, commentScale);
		logger.info("Document uploaded: " + file + " to section " + parent);
		return createDocumentInstance;
	}

	/**
	 * Creates the objects.
	 *
	 * @param caseInstance
	 *            the case instance
	 * @param objectScale
	 *            the object scale
	 * @param commentScale
	 *            the comment scale
	 * @return the list
	 */
	@SuppressWarnings("rawtypes")
	private List<Instance> createObjects(CaseInstance caseInstance, int objectScale,
			int commentScale) {
		final String sessionId = getCurrentClientId();
		LoadTestingRESTServiceStateWrapper infoWrapper = helperService.get(sessionId);
		String[] definitionsIds = new String[2];
		definitionsIds[0] = "EO007004";
		definitionsIds[1] = "EO007007";

		String[] sectionsIds = new String[2];
		sectionsIds[0] = "objects";
		sectionsIds[1] = "objects2";
		SectionInstance[] sections = new SectionInstance[sectionsIds.length];
		for (SectionInstance section : caseInstance.getSections()) {
			if (sectionsIds[0].equals(section.getIdentifier())) {
				sections[0] = section;
			} else if (sectionsIds[1].equals(section.getIdentifier())) {
				sections[1] = section;
			}
		}
		ArrayList<Instance> createdObjects = new ArrayList<>(objectScale);
		for (int i = 0; i < objectScale; i++) {

			chooseNewUser(ObjectActionTypeConstants.CREATE_OBJECT);
			SectionInstance targetInstance = sections[(int) (Math.random() * sections.length)];
			// infoWrapper.getCurrentTreePaths()[2] = caseInstance.getSections().indexOf(
			// targetInstance);
			// infoWrapper.getCurrentTreePaths()[3]++;
			String defId = definitionsIds[(int) (Math.random() * definitionsIds.length)];
			ObjectDefinition definition = dictionaryService.getDefinition(ObjectDefinition.class,
					defId);
			ObjectInstance createdObjectInstance = objectService.createInstance(definition, null,
					new Operation(ObjectActionTypeConstants.CREATE_OBJECT));
			String formatedDate = dateFormat.format(new Date());
			createdObjectInstance.getProperties().put(DefaultProperties.TITLE,
			// infoWrapper.arrayAsString(infoWrapper.getCurrentTreePaths(), "") + " "+
					formatedDate);
			createdObjectInstance.getProperties().put(DefaultProperties.DESCRIPTION,
					infoWrapper.generateRandomString(20));
			RestInstance converted = typeConverter.convert(RestInstance.class,
					createdObjectInstance);

			Set<Entry<String, Object>> properties = converted.getProperties().entrySet();
			Map<String, Object> newProps = new HashMap<>(properties.size());
			for (Entry<String, Object> entry : properties) {
				if (entry.getValue() instanceof HashMap) {
					newProps.put(entry.getKey(), new JSONObject((Map) entry.getValue()).toString());
				} else {
					newProps.put(entry.getKey(), entry.getValue());
				}
			}
			converted.setProperties(newProps);
			createdObjectInstance = typeConverter.convert(ObjectInstance.class, converted);
			fillObjectProperties(createdObjectInstance, true, true, infoWrapper);
			instanceService.save(createdObjectInstance, new Operation(
					ObjectActionTypeConstants.CREATE_OBJECT));
			intanceService.attach(targetInstance, new Operation(
					ObjectActionTypeConstants.ATTACH_OBJECT), createdObjectInstance);
			infoWrapper.updateInstancesStatistic(ObjectInstance.class, 1);
			logger.info("Object created: " + definition.getIdentifier() + " in section "
					+ targetInstance);
			createdObjects.add(createdObjectInstance);
			commentOnInstance(createdObjectInstance, commentScale);

		}
		return createdObjects;
	}

	/**
	 * Fill the object properties with random values.
	 *
	 * @param instance
	 *            the instance
	 * @param unsetOnly
	 *            the unset only
	 * @param excludeSystem
	 *            the exclude system
	 * @param infoWrapper
	 *            the info wrapper
	 */
	public void fillObjectProperties(Instance instance, boolean unsetOnly, boolean excludeSystem,
			LoadTestingRESTServiceStateWrapper infoWrapper) {
		DefinitionModel instanceDefinition = dictionaryService.getInstanceDefinition(instance);

		String domainClass = typeConverter.convert(String.class,
				instance.getProperties().get("rdf:type"));

		Set<String> fieldHistorySet = new HashSet<>();

		// get properties defined in the base object definition
		List<PropertyDefinition> fields = instanceDefinition.getFields();
		for (PropertyDefinition propertyDefinition : fields) {
			fillObjectProperty(propertyDefinition, instance, unsetOnly, fieldHistorySet,
					domainClass, excludeSystem, infoWrapper);
		}

		// if it's a region definition get the regions and the properties defined in each region
		if (instanceDefinition instanceof RegionDefinitionModel) {
			List<RegionDefinition> regions = ((RegionDefinitionModel) instanceDefinition)
					.getRegions();
			for (RegionDefinition region : regions) {
				fields = region.getFields();
				for (PropertyDefinition propertyDefinition : fields) {
					fillObjectProperty(propertyDefinition, instance, unsetOnly, fieldHistorySet,
							domainClass, excludeSystem, infoWrapper);
				}
			}
		}
	}

	/**
	 * Adds the property from definition to the instance properties.
	 *
	 * @param definition
	 *            the definition
	 * @param ownerInstance
	 *            the owner instance
	 * @param unsetOnly
	 *            the unset only
	 * @param fieldHistorySet
	 *            the field history set
	 * @param domainClass
	 *            the domain class
	 * @param excludeSystem
	 *            the exclude system
	 * @param infoWrapper
	 *            the info wrapper
	 * @return true, if successful
	 */
	private boolean fillObjectProperty(PropertyDefinition definition, Instance ownerInstance,
			boolean unsetOnly, Set<String> fieldHistorySet, String domainClass,
			boolean excludeSystem, LoadTestingRESTServiceStateWrapper infoWrapper) {
		DisplayType displayType = definition.getDisplayType();
		if (excludeSystem
				&& ((displayType == DisplayType.HIDDEN) || (displayType == DisplayType.SYSTEM))) {
			return false;
		}

		String name = definition.getName();
		if (unsetOnly && (ownerInstance.getProperties().get(name) != null)) {
			return false;
		}
		String label = definition.getLabel();
		if (org.apache.commons.lang.StringUtils.isBlank(name)
				|| org.apache.commons.lang.StringUtils.isBlank(label) || name.contains("header")) {
			return false;
		}
		Class<?> javaClass = definition.getDataType().getJavaClass();
		if (javaClass == String.class) {
			ownerInstance.getProperties().put(name, infoWrapper.generateRandomString(5));
		} else if (javaClass == Float.class) {
			ownerInstance.getProperties().put(name, new Float(Math.random() * 10000));
		} else if (javaClass == Integer.class) {
			ownerInstance.getProperties().put(name, new Integer((int) (Math.random() * 10000)));
		} else if (javaClass == Double.class) {
			ownerInstance.getProperties().put(name, new Double(Math.random() * 10000));
		} else if (javaClass.isAssignableFrom(Serializable.class)) {
			ownerInstance.getProperties().put(name, infoWrapper.generateRandomString(5));
		} else {
			logger.warn("MISSING " + name + " with type: " + javaClass);
			return false;
		}
		return true;

	}

	/**
	 * Creates the idocs.
	 *
	 * @param caseInstance
	 *            the case instance
	 * @param idocScale
	 *            the idoc scale
	 * @param commentScale
	 *            the comment scale
	 */
	private void createIdocs(CaseInstance caseInstance, int idocScale, int commentScale) {
		final String sessionId = getCurrentClientId();
		LoadTestingRESTServiceStateWrapper infoWrapper = helperService.get(sessionId);
		String[] definitionsIds = new String[2];
		definitionsIds[0] = "DT210009";
		definitionsIds[1] = "DT210011";
		String[] sectionsIds = new String[2];
		sectionsIds[0] = "idocs";
		sectionsIds[1] = "idocs2";
		SectionInstance[] sections = new SectionInstance[sectionsIds.length];
		for (SectionInstance section : caseInstance.getSections()) {
			if (sectionsIds[0].equals(section.getIdentifier())) {
				sections[0] = section;
			} else if (sectionsIds[1].equals(section.getIdentifier())) {
				sections[1] = section;
			}
		}
		for (int i = 0; i < idocScale; i++) {
			chooseNewUser(ActionTypeConstants.CREATE_IDOC);
			SectionInstance targetInstance = sections[(int) (Math.random() * sections.length)];
			String defId = definitionsIds[(int) (Math.random() * definitionsIds.length)];
			TemplateInstance loadedContent = documentTemplateService
					.loadContent(documentTemplateService.getPrimaryTemplate(defId));
			// int[] treePath = helperService.get(sessionId).getCurrentTreePaths();
			String formatedDate = dateFormat.format(new Date());
			DocumentDefinitionTemplate definition = dictionaryService.getDefinition(
					DocumentDefinitionTemplate.class, defId);
			DocumentInstance createDocumentInstance = documentService.createInstance(
					new DocumentDefinitionRefProxy(definition), targetInstance, new Operation(
							ActionTypeConstants.CREATE_IDOC));
			createDocumentInstance.setPurpose("iDoc");
			createDocumentInstance.setStandalone(true);
			createDocumentInstance.getProperties().put(DefaultProperties.TITLE,
			// infoWrapper.arrayAsString(treePath, "") + " " +
					formatedDate);
			createDocumentInstance.getProperties().put(DefaultProperties.DESCRIPTION,
					infoWrapper.generateRandomString(20));
			createDocumentInstance.getProperties().put(
					DocumentProperties.FILE_LOCATOR,
					new ByteArrayFileDescriptor(UUID.randomUUID().toString(), loadedContent
							.getContent().getBytes()));

			documentService.upload(targetInstance, true, createDocumentInstance);
			instanceService.attach(targetInstance, new Operation(ActionTypeConstants.UPLOAD),
					createDocumentInstance);
			infoWrapper.updateInstancesStatistic(DocumentInstance.class, 1);

			commentOnInstance(createDocumentInstance, commentScale);

		}

	}

	/**
	 * Creates the project.
	 *
	 * @param sessionId
	 *            the session id
	 * @return the project instance
	 */
	private ProjectInstance createProject(final String sessionId) {

		return dao.invokeInNewTx(new Callable<ProjectInstance>() {

			@Override
			public ProjectInstance call() throws Exception {

				LoadTestingRESTServiceStateWrapper infoWrapper = helperService.get(sessionId);
				User choosenUser = chooseNewUser(PmActionTypeConstants.CREATE_PROJECT);
				Map<String, Serializable> additionalProps = new HashMap<String, Serializable>();
				additionalProps.put(DefaultProperties.TITLE, // infoWrapper.getCurrentTreePaths()[0]
																// + " " +
						dateFormat.format(new Date()));
				ProjectDefinition definition = dictionaryService.getDefinition(
						ProjectDefinition.class, DEFAULT_DEFINITION_ID_PROJECT);
				ProjectInstance parent = projectService.createInstance(definition, null);
				parent.getProperties().put(DefaultProperties.TITLE, DEFAULT_DEFINITION_ID_PROJECT);
				parent.getProperties().put(ProjectProperties.OWNER, choosenUser.getIdentifier());
				if (additionalProps != null) {
					parent.getProperties().putAll(additionalProps);
				}
				ProjectInstance saved = projectService.save(parent, new Operation(
						PmActionTypeConstants.CREATE_PROJECT));
				Map<Resource, RoleIdentifier> roles = new HashMap<>(infoWrapper.getAuthorityRoles());
				roles.put(resourceService.getResource("admin", ResourceType.USER),
						SecurityModel.BaseRoles.ADMINISTRATOR);
				resourceService.assignResources(parent, roles);
				infoWrapper.updateInstancesStatistic(ProjectInstance.class, 1);
				return saved;
			}

		});
	}

	/**
	 * Start activities.
	 *
	 * @param instance
	 *            the instance
	 * @param workflowScale
	 *            the workflow scale
	 * @param taskScale
	 *            the task scale
	 */
	private void startActivities(final Instance instance, int workflowScale, int taskScale) {
		final String sessionId = getCurrentClientId();
		logger.info("Starting activities for: " + instance.getIdentifier() + " " + workflowScale
				+ " " + taskScale);
		LoadTestingRESTServiceStateWrapper infoWrapper = helperService.get(sessionId);
		for (int i = 0; i < workflowScale; i += 2) {

			chooseNewUser(ActionTypeConstants.START_WORKFLOW);
			// // start wf with 5 tasks
			startWorkflow995(instance);
			infoWrapper.updateInstancesStatistic(WorkflowInstanceContext.class, 1);
			// if even number start a new type wf
			if (workflowScale % 2 == 0) {
				chooseNewUser(ActionTypeConstants.START_WORKFLOW);
				startWorkflow999(instance);
				infoWrapper.updateInstancesStatistic(WorkflowInstanceContext.class, 1);
			}

		}
		for (int i = 0; i < taskScale; i++) {

			Map<String, Serializable> workflowProperties = new HashMap<>(2);
			User chooseNewUser = chooseNewUser(ActionTypeConstants.CREATE_TASK);
			workflowProperties.put(TaskProperties.TASK_ASSIGNEE, chooseNewUser.getIdentifier());
			workflowProperties.put(DefaultProperties.TITLE, infoWrapper.generateRandomString(5));
			// stay in progress
			startStandaloneTask(instance, DEFAULT_DEFINITION_ID_TASK_STANDALONE, workflowProperties);
		}

	}

	/**
	 * Start workflow995.
	 *
	 * @param owningInstance
	 *            the owning instance
	 * @return the workflow instance context
	 */
	public WorkflowInstanceContext startWorkflow995(Instance owningInstance) {
		WorkflowDefinition wfDefinition = dictionaryService.getDefinition(WorkflowDefinition.class,
				"activiti$WFTYPE995");
		WorkflowInstanceContext wfInstance = workflowService.createInstance(wfDefinition,
				owningInstance);
		// start wf
		TaskDefinitionRef startTaskDefinition = WorkflowHelper.getStartTask(wfDefinition);
		TaskInstance startupTask = (TaskInstance) taskService.createInstance(startTaskDefinition,
				wfInstance);
		Set<String> poolAssignees = new HashSet<String>();
		String currentUserId = authenticationService.getCurrentUserId();
		poolAssignees.add("bbanchev");
		poolAssignees.add("admin");
		poolAssignees.add("TEST2");
		poolAssignees.add(currentUserId);
		poolAssignees.add("GROUP_Consumers");
		startupTask.getProperties().put(TaskProperties.TASK_MULTI_ASSIGNEES,
				new ArrayList<String>(poolAssignees));
		// ###################### TRANSITION ##############
		List<TaskInstance> startedWorkflow = workflowService.startWorkflow(wfInstance, startupTask);

		// claim and continue
		claimTask(startedWorkflow.get(0), currentUserId);
		List<TaskInstance> updatedWorkflow = workflowService.updateWorkflow(wfInstance,
				startedWorkflow.get(0), "RT0097");
		// assign next
		TaskInstance nextAssign = updatedWorkflow.get(0);
		nextAssign.getProperties().put(TaskProperties.TASK_MULTI_ASSIGNEES,
				new ArrayList<String>(poolAssignees));
		// new random
		List<TaskInstance> updateWorkflow = workflowService.updateWorkflow(wfInstance,
				updatedWorkflow.get(0), "RT0095");
		claimTask(updateWorkflow.get(0), currentUserId);

		// go to the end
		workflowService.updateWorkflow(wfInstance, updateWorkflow.get(0), "RT0097");

		return wfInstance;
	}

	/**
	 * Start workflow999.
	 *
	 * @param owningInstance
	 *            the owning instance
	 * @return the workflow instance context
	 */
	public WorkflowInstanceContext startWorkflow999(Instance owningInstance) {
		WorkflowDefinition wfDefinition = dictionaryService.getDefinition(WorkflowDefinition.class,
				"activiti$WFTYPE999");
		WorkflowInstanceContext wfInstance = workflowService.createInstance(wfDefinition,
				owningInstance);
		// start wf
		TaskDefinitionRef startTaskDefinition = WorkflowHelper.getStartTask(wfDefinition);
		TaskInstance startupTask = (TaskInstance) taskService.createInstance(startTaskDefinition,
				wfInstance);
		String currentUserId = authenticationService.getCurrentUserId();
		startupTask.getProperties().put(TaskProperties.TASK_ASSIGNEE, currentUserId);
		// ###################### TRANSITION ##############
		List<TaskInstance> startedWorkflow = workflowService.startWorkflow(wfInstance, startupTask);

		// claim and continue
		List<TaskInstance> updatedWorkflow = workflowService.updateWorkflow(wfInstance,
				startedWorkflow.get(0), "RT0097");
		// assign next
		TaskInstance nextAssign = updatedWorkflow.get(0);
		nextAssign.getProperties().put(TaskProperties.TASK_ASSIGNEE, currentUserId);
		// new random
		List<TaskInstance> updateWorkflow = workflowService.updateWorkflow(wfInstance,
				updatedWorkflow.get(0), "RT0095");

		// go to the end
		workflowService.updateWorkflow(wfInstance, updateWorkflow.get(0), "RT0098");
		// claim and continue
		// workflowService.updateWorkflow(context, taskInstance, operation)
		return wfInstance;
	}

	/**
	 * Claim task.
	 *
	 * @param taskInstance
	 *            the task instance
	 * @param userId
	 *            the user id
	 * @return the task instance
	 */
	public TaskInstance claimTask(TaskInstance taskInstance, String userId) {
		Collection<String> poolResources = taskService.getPoolUsers(taskInstance);
		if (poolResources.contains(userId)) {
			taskInstance.getProperties().put(TaskProperties.TASK_OWNER, userId);
			taskInstance.getProperties().put(TaskProperties.TASK_ASSIGNEE, userId);
			workflowService.updateTaskInstance(taskInstance);
			return taskInstance;
		}
		return null;
	}

	/**
	 * Start standalone task.
	 *
	 * @param owningInstance
	 *            the owning instance
	 * @param taskDefinitionId
	 *            the task definition id
	 * @param taskProps
	 *            the task props
	 * @return the standalone task instance
	 */
	protected StandaloneTaskInstance startStandaloneTask(Instance owningInstance,
			String taskDefinitionId, Map<String, Serializable> taskProps) {
		TaskDefinition standaloneTaskDefinition = dictionaryService.getDefinition(
				TaskDefinition.class, taskDefinitionId);
		StandaloneTaskInstance standaloneTask = (StandaloneTaskInstance) taskService
				.createInstance(standaloneTaskDefinition, owningInstance);
		standaloneTask.getProperties().putAll(taskProps);
		standaloneTaskService.start(standaloneTask, new Operation(ActionTypeConstants.CREATE_TASK));
		return standaloneTask;
	}

	/**
	 * Choose new user.
	 *
	 * @param operation
	 *            the operation
	 * @return the user
	 */
	private User chooseNewUser(String operation) {

		LoadTestingRESTServiceStateWrapper infoWrapper = helperService.get(getCurrentClientId());
		String requestToken = null;
		List<User> users = infoWrapper.getUsers();
		User user = null;
		while (requestToken == null) {
			user = users.get((int) (Math.random() * users.size()));
			String pass = user.getIdentifier().toLowerCase().equals("admin") ? "admin" : "123456";
			try {
				requestToken = securityTokenService.requestToken(user.getIdentifier(), pass);
			} catch (Exception e) {
				throw new EmfRuntimeException("Security token request failed", e);
			}
		}
		((UserWithCredentials) user).setTicket(SecurityContextManager.encrypt(requestToken));
		authenticationService.setAuthenticatedUser(user);
		infoWrapper.updateUserOperationsStatistic(operation, user);
		return user;
	}

	/**
	 * Creates the case instance.
	 *
	 * @param parent
	 *            the parent
	 * @return the case instance
	 */
	private CaseInstance createCaseInstance(Instance parent) {
		final String sessionId = getCurrentClientId();
		LoadTestingRESTServiceStateWrapper infoWrapper = helperService.get(sessionId);
		chooseNewUser(ActionTypeConstants.CREATE_CASE);
		// int[] treePathInfo = Arrays.copyOfRange(infoWrapper.getCurrentTreePaths(), 0, 2);
		CaseDefinition definition = dictionaryService.getDefinition(CaseDefinition.class,
				DEFAULT_DEFINITION_ID_CASE);
		CaseInstance createInstance = caseService.createInstance(definition, parent);
		createInstance.getProperties().put(DefaultProperties.TITLE,
		// infoWrapper.arrayAsString(treePathInfo, "")
				infoWrapper.generateRandomString(5));
		createInstance.getProperties().put("information", dateFormat.format(new Date()));
		createInstance.getProperties().put(
				DefaultProperties.DESCRIPTION,
				infoWrapper.generateRandomString(20).toString()
						+ createInstance.getProperties().get("information"));
		CaseInstance saved = caseService.save(createInstance, new Operation(
				ActionTypeConstants.CREATE_CASE));
		infoWrapper.updateInstancesStatistic(CaseInstance.class, 1);
		return saved;
	}

	/**
	 * Comment on instance.
	 *
	 * @param instance
	 *            the instance
	 * @param commentScale
	 *            the comment scale
	 */
	private void commentOnInstance(final Instance instance, final int commentScale) {
		final LoadTestingRESTServiceStateWrapper infoWrapper = helperService
				.get(getCurrentClientId());

		User currentUser = null;
		try {
			currentUser = authenticationService.getCurrentUser();
			dao.invokeInNewTx(new Callable<Void>() {

				@Override
				public Void call() throws Exception {
					// new topic is going to be created
					TopicInstance topicInstance = new TopicInstance();
					topicInstance.getProperties().put(DefaultProperties.TITLE,
							"Topic about " + instance.getProperties().get(DefaultProperties.TITLE));
					topicInstance.setComment(topicInstance.getProperties()
							.get(DefaultProperties.TITLE).toString());
					topicInstance.setTopicAbout(instance.toReference());
					topicInstance.setPostedDate(new Date());
					User user = authenticationService.getCurrentUser();
					topicInstance.setFrom(user.getId().toString());
					topicInstance.getProperties().put(DefaultProperties.STATUS,
							PrimaryStateType.IN_PROGRESS);
					topicInstance.getProperties().put(DefaultProperties.TYPE, "Note to self");
					topicInstance.getProperties().put(DefaultProperties.IS_DELETED, false);
					topicInstance.setTags("");
					topicInstance.setSubSectionId(instance.getId().toString());
					CommentInstance[] comments = new CommentInstance[commentScale];
					// helperService.updateInstancesStatistic(TopicInstance.class, 1);
					for (int i = 0; i < commentScale; i++) {
						comments[i] = new CommentInstance();

						comments[i].getProperties().put(DefaultProperties.TITLE,
								infoWrapper.generateRandomString(3));
						comments[i].getProperties().put(DefaultProperties.TYPE, "Note to self");
						comments[i].getProperties().put(DefaultProperties.STATUS,
								PrimaryStateType.IN_PROGRESS);
						comments[i].getProperties().put(DefaultProperties.IS_DELETED, false);
						// comment was deleted so the user edited old/stale comment
						comments[i].setComment("My comment " + infoWrapper.generateRandomString(10));
						User commentUser = chooseNewUser(ActionTypeConstants.TOPIC_REPLY);
						comments[i].setFrom(commentUser.getId().toString());
						comments[i].setPostedDate(new Date());
						if (i > 3) {
							// replay of random comment <3
							comments[i].setReplayOf(comments[(int) (Math.random() * 3)]);
						}

						commentService.postComment(topicInstance, comments[i]);
					}
					infoWrapper.updateInstancesStatistic(CommentInstance.class, commentScale);
					return null;
				}

			});
		} finally {
			authenticationService.setAuthenticatedUser(currentUser);
		}
	}
}
