package com.sirma.cmf.web.util;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;

import com.sirma.cmf.web.DocumentContext;
import com.sirma.cmf.web.entity.bookmark.BookmarkUtil;
import com.sirma.cmf.web.workflow.task.TaskDocument;
import com.sirma.itt.cmf.beans.model.AbstractTaskInstance;
import com.sirma.itt.cmf.beans.model.CaseInstance;
import com.sirma.itt.cmf.beans.model.DocumentInstance;
import com.sirma.itt.cmf.beans.model.SectionInstance;
import com.sirma.itt.cmf.beans.model.TaskInstance;
import com.sirma.itt.cmf.beans.model.TaskState;
import com.sirma.itt.cmf.beans.model.WorkflowInstanceContext;
import com.sirma.itt.cmf.constants.CaseProperties;
import com.sirma.itt.cmf.constants.CmfConfigurationProperties;
import com.sirma.itt.cmf.constants.DocumentProperties;
import com.sirma.itt.cmf.constants.SectionProperties;
import com.sirma.itt.cmf.constants.TaskProperties;
import com.sirma.itt.cmf.constants.WorkflowProperties;
import com.sirma.itt.cmf.services.TaskService;
import com.sirma.itt.cmf.workflows.WorkflowHelper;
import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.emf.codelist.CodelistService;
import com.sirma.itt.emf.codelist.model.CodeValue;
import com.sirma.itt.emf.configuration.Config;
import com.sirma.itt.emf.converter.TypeConverter;
import com.sirma.itt.emf.definition.DictionaryService;
import com.sirma.itt.emf.definition.model.PropertyDefinition;
import com.sirma.itt.emf.definition.model.RegionDefinition;
import com.sirma.itt.emf.definition.model.RegionDefinitionModel;
import com.sirma.itt.emf.domain.model.DefinitionModel;
import com.sirma.itt.emf.instance.model.CMInstance;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.instance.model.Lockable;
import com.sirma.itt.emf.label.LabelProvider;
import com.sirma.itt.emf.link.LinkConstants;
import com.sirma.itt.emf.link.LinkInstance;
import com.sirma.itt.emf.properties.DefaultProperties;
import com.sirma.itt.emf.properties.model.PropertyModel;
import com.sirma.itt.emf.resources.ResourceService;
import com.sirma.itt.emf.rest.model.ViewInstance;
import com.sirma.itt.emf.security.model.Action;
import com.sirma.itt.emf.util.CurrentLocale;
import com.sirma.itt.emf.util.EqualsHelper;
import com.sirma.itt.emf.web.util.DateUtil;

/**
 * Utility functions for building application specific compound text messages.
 * 
 * @author svelikov
 */
@Named
@ApplicationScoped
public class LabelBuilder {

	/** The logger. */
	@Inject
	private Logger logger;

	/** The case defintion cl. */
	@Inject
	@Config(name = CmfConfigurationProperties.CODELIST_CASE_DEFINITIONS)
	private Integer caseDefintionCL;

	/** The codelist service. */
	@Inject
	private CodelistService codelistService;

	/** The dictionary service. */
	@Inject
	private DictionaryService dictionaryService;

	/** The current locale. */
	@Inject
	@CurrentLocale
	private String currentLocale;

	/** The people service. */
	@Inject
	private ResourceService resourceService;

	/** The label provider. */
	@Inject
	protected LabelProvider labelProvider;

	/** The date util. */
	@Inject
	private DateUtil dateUtil;

	/** The task service. */
	@Inject
	private TaskService taskService;

	/** The type converter. */
	@Inject
	private TypeConverter typeConverter;

	/** The bookmark util. */
	@Inject
	private BookmarkUtil bookmarkUtil;

	/**
	 * Gets the display name for user or returns the passed username.
	 * 
	 * @param username
	 *            the username
	 * @return the display name for user or passed username.
	 */
	public String getDisplayNameForUser(String username) {
		if ("null".equals(username)) {
			return username;
		}
		return resourceService.getDisplayName(username);
	}

	/**
	 * Gets the display name for user.
	 * 
	 * @param documentContext
	 *            the document context
	 * @param property
	 *            the property
	 * @return the display name for user
	 */
	public String getUserDisplayName(DocumentContext documentContext, String property) {
		String result = "";
		if ((documentContext != null) && (property != null)) {
			Instance currentInstance = documentContext.getCurrentInstance();
			if (currentInstance != null) {
				Serializable serializable = currentInstance.getProperties().get(property);
				String displayName = resourceService.getDisplayName(serializable);
				if (displayName != null) {
					result = displayName;
				}
			}
		}
		return result;
	}

	/**
	 * Gets the dashlet total count string.
	 * 
	 * @param resultCount
	 *            the result count
	 * @param totalCount
	 *            the total count
	 * @return the dashlet total count string
	 */
	public String getDashletTotalCountString(int resultCount, int totalCount) {
		String template = labelProvider.getValue("cmf.dashboard.dashlet.totalCount");
		String label = MessageFormat.format(template, resultCount, totalCount);
		return label;
	}

	/**
	 * Gets the display name for user or returns the passed username. Optionally use the current
	 * instance to check if this is pool task
	 * 
	 * @param username
	 *            the username
	 * @param task
	 *            is the current intance
	 * @return the display name for user or passed username.
	 */
	public String getDisplayNameForUser(String username, AbstractTaskInstance task) {

		String displayName = username;

		if (StringUtils.isNotNullOrEmpty(username)) {
			return getDisplayNameForUser(username);
		} else if (taskService.isPooledTask(task)) {
			return labelProvider.getValue(LabelConstants.TASK_UNASSIGNED_LABEL);
		}

		return displayName;
	}

	/**
	 * Gets the accepted file type message.
	 * 
	 * @param fileType
	 *            the file type
	 * @return the accepted file type message
	 */
	public String getAcceptedFileTypeMessage(String fileType) {
		String message = labelProvider.getValue(LabelConstants.DOCUMENT_UPLOAD_ACCEPTED_FILE_TYPE)
				+ " " + fileType;

		return message;
	}

	//
	// Case labels
	//

	/**
	 * Gets the case link label.
	 * 
	 * @param caseInstance
	 *            the case instance
	 * @return the case link label
	 */
	public String getCaseLinkLabel(CaseInstance caseInstance) {

		// CMF-6040: added null check
		// TODO: remove the use of the method - use header field
		if (caseInstance == null) {
			return "";
		}
		// String template =
		// "<span class='link-row'>{0}, {1} ({2})</span><span class='info-row'>актуализирана от: <b>{3}</b>, актуализирана на: <b>{4,date,dd.MM.yyyy, HH:mm}</b></span>";
		String template = labelProvider.getValue(LabelConstants.CMF_CASE_LINK_LABEL);
		Map<String, Serializable> properties = caseInstance.getProperties();

		String caseId = getCaseId(caseInstance);

		String type = getCaseType(properties);

		String state = getState(caseInstance, properties);

		String lastModifiedBy = getCaseLastModifiedBy(properties);

		Date lastModifiedOn = getLastModifiedOn(properties);

		String label = MessageFormat.format(template, caseId, type, state, lastModifiedBy,
				lastModifiedOn);

		return label.toString();
	}

	/**
	 * Assembles the link value.
	 * 
	 * @param caseInstance
	 *            the case instance
	 * @return string value. {@link CaseInstance}.
	 */
	public String getBackToCaseLinkValue(CaseInstance caseInstance) {

		return getCaseType(caseInstance.getProperties());
	}

	/**
	 * Gets the last modified on.
	 * 
	 * @param properties
	 *            the properties
	 * @return the last modified on
	 */
	private Date getLastModifiedOn(Map<String, Serializable> properties) {
		Date lastModifiedOn = (Date) properties.get(CaseProperties.MODIFIED_ON);
		return lastModifiedOn;
	}

	/**
	 * Gets the case last modified by.
	 * 
	 * @param properties
	 *            the properties
	 * @return the last modified by
	 */
	private String getCaseLastModifiedBy(Map<String, Serializable> properties) {
		String lastModifiedBy = "";
		String username = (String) properties.get(CaseProperties.MODIFIED_BY);
		lastModifiedBy = getDisplayNameForUser(username);
		if (StringUtils.isNullOrEmpty(lastModifiedBy)) {
			lastModifiedBy = username;
		}
		return lastModifiedBy;
	}

	/**
	 * Gets the state.
	 * 
	 * @param caseInstance
	 *            the case instance
	 * @param properties
	 *            the properties
	 * @return the state
	 */
	private String getState(CaseInstance caseInstance, Map<String, Serializable> properties) {
		PropertyDefinition propertyDefinition = dictionaryService.getProperty(
				CaseProperties.STATUS, caseInstance.getRevision(), caseInstance);

		String status = (String) properties.get(CaseProperties.STATUS);
		String state = status;
		if (propertyDefinition != null) {
			state = codelistService.getDescription(propertyDefinition.getCodelist(), status);
		}
		return state;
	}

	/**
	 * Gets the case type.
	 * 
	 * @param properties
	 *            the properties
	 * @return the case type
	 */
	private String getCaseType(Map<String, Serializable> properties) {

		String typeKey = (String) properties.get(CaseProperties.TYPE);

		String caseType = codelistService.getDescription(caseDefintionCL, typeKey);

		if (caseType == null) {
			caseType = typeKey;
		}

		return caseType;
	}

	/**
	 * Gets the case id.
	 * 
	 * @param caseInstance
	 *            the case instance
	 * @return the case id
	 */
	private String getCaseId(CaseInstance caseInstance) {
		String caseId = caseInstance.getContentManagementId();
		return caseId;
	}

	/**
	 * Assembles a summary for metadata fields for given case.
	 * 
	 * @param caseInstance
	 *            Given case instance.
	 * @return Assembled summary string.
	 */
	public String getCaseContextDataSummary(CaseInstance caseInstance) {
		// Id: {0} State: {1} Modified by {2} on {3}
		Map<String, Serializable> properties = caseInstance.getProperties();

		PropertyDefinition propertyDefinition = dictionaryService.getProperty(
				CaseProperties.STATUS, caseInstance.getRevision(), caseInstance);

		// get the case status
		String state = (String) properties.get(CaseProperties.STATUS);
		String caseStatus = codelistService.getDescription(propertyDefinition.getCodelist(), state);

		// get the user that is changed the case
		String username = (String) properties.get(CaseProperties.MODIFIED_BY);
		String displayNameForUser = getDisplayNameForUser(username);
		if (StringUtils.isNullOrEmpty(displayNameForUser)) {
			displayNameForUser = username;
		}

		String summary = MessageFormat.format(labelProvider.getValue(
		// caseInstance.getId()
				LabelConstants.CASE_CONTEXT_DATA_SUMMARY), caseInstance.getContentManagementId(),
				caseStatus, displayNameForUser, properties.get(CaseProperties.MODIFIED_ON));
		return summary;
	}

	// -------------------------------------------------
	// Document labels
	// -------------------------------------------------

	/**
	 * Gets the document link label.
	 * 
	 * @param documentInstance
	 *            the document instance
	 * @param includeVersion
	 *            the include version
	 * @return the document link label
	 */
	public String getDocumentLinkLabel(Instance documentInstance, boolean includeVersion) {

		Map<String, Serializable> properties = documentInstance.getProperties();

		String documentId = (String) properties.get(DocumentProperties.UNIQUE_DOCUMENT_IDENTIFIER);
		if (documentId == null) {
			documentId = "";
		} else {
			documentId += ",";
		}

		String title = getDocumentTitle(documentInstance, true);

		String modifiedBy = getDisplayNameForUser((String) properties
				.get(DocumentProperties.MODIFIED_BY));

		Date modifiedOn = (Date) properties.get(DocumentProperties.MODIFIED_ON);

		String pattern = "";
		String label = "";
		if (includeVersion) {
			// pattern =
			// "<span class='link-row'>{0} {1}<span class='document-version'>{2}</span></span><span class='info-row'>актуализиран от: <b>{3}</b>, актуализиран на: <b>{4,date,dd.MM.yyyy, HH:mm}</b></span>";
			pattern = labelProvider.getValue(LabelConstants.CMF_DOCUMENT_WITH_VERSION_LINK_LABEL);

			String documentVersion = (properties.get(DocumentProperties.VERSION)) + "";
			label = MessageFormat.format(pattern, documentId, title, documentVersion, modifiedBy,
					modifiedOn);
		} else {

			// pattern =
			// "<span class='link-row'>{0} {1}</span><span class='info-row'>актуализиран от: <b>{2}</b>, актуализиран на: <b>{3,date,dd.MM.yyyy, HH:mm}</b></span>";
			pattern = labelProvider.getValue(LabelConstants.CMF_DOCUMENT_LINK_LABEL);
			label = MessageFormat.format(pattern, documentId, title, modifiedBy, modifiedOn);
		}
		return label;
	}

	/**
	 * Gets the task document link label. If there is no document instance inside the task document,
	 * then build custom label. Otherwise get the document instance header.
	 * 
	 * @param taskDocument
	 *            the task document
	 * @return the task document link label
	 */
	public String getTaskDocumentLinkLabel(TaskDocument taskDocument) {
		String label = "";
		if (taskDocument.getDocumentInstance() == null) {
			label = taskDocument.getDocumentTypeDescription() + " ("
					+ taskDocument.getDocumentType() + ")";
		} else {
			label = (String) taskDocument.getDocumentInstance().getProperties()
					.get(DefaultProperties.HEADER_COMPACT);
			label = bookmarkUtil.addTargetBlank(label);
		}
		return label;
	}

	/**
	 * Assembles a summary for metadata fields for given document.
	 * 
	 * @param documentInstance
	 *            Given document instance.
	 * @return Assembled summary string.
	 */
	public String getDocumentContextDataSummary(Instance documentInstance) {
		// Modified on {0} by {1}
		Map<String, Serializable> properties = documentInstance.getProperties();
		String summary = MessageFormat.format(labelProvider.getValue(
		//
				LabelConstants.DOCUMENT_CONTEXT_DATA_SUMMARY),
				properties.get(DocumentProperties.MODIFIED_ON),
				getDisplayNameForUser((String) properties.get(DocumentProperties.MODIFIED_BY)));
		return summary;
	}

	/**
	 * Gets upload document title from Code List.
	 * 
	 * @param instance
	 *            - {@link DocumentInstance}
	 * @param includeName
	 *            - to print or not doc's name
	 * @return title
	 */
	public String getDocumentTitle(Instance instance, boolean includeName) {

		if ((instance instanceof DocumentInstance) && !((DocumentInstance) instance).hasDocument()) {
			return labelProvider.getValue(LabelConstants.MISSING_DOCUMENT_TITLE_LABEL);
		}

		String title = null;
		String name = (String) instance.getProperties().get(DocumentProperties.NAME);

		Map<String, Serializable> properties = instance.getProperties();
		String code = (String) properties.get(DocumentProperties.TITLE);

		if (StringUtils.isNotNullOrEmpty(code)) {

			PropertyDefinition property = dictionaryService.getProperty(DocumentProperties.TITLE,
					instance.getRevision(), instance);

			if ((property != null) && (property.getCodelist() != null)) {

				Map<String, CodeValue> codelist = codelistService.getCodeValues(property
						.getCodelist());

				CodeValue codeValue = codelist.get(code);
				if (codeValue != null) {
					title = (String) codeValue.getProperties().get(currentLocale);
				}
			}
		}

		if (includeName) {
			if (StringUtils.isNullOrEmpty(title)) {
				return "" + name;
			}
			return title + " (" + name + ")";
		} else {
			if (StringUtils.isNullOrEmpty(title)) {
				return "" + name;
			}
			return title;
		}

	}

	/**
	 * Assembles the warning message when a document is locked by user.
	 * 
	 * @param documentInstance
	 *            Document instance.
	 * @return Message.
	 */
	public String getDocumentIsLockedMessage(Lockable documentInstance) {

		String name = documentInstance.getLockedBy();

		StringBuilder msg = new StringBuilder(
				labelProvider.getValue(LabelConstants.MSG_WARNING_DOCUMENT_IS_LOCKED));
		msg.append(" ").append(getDisplayNameForUser(name));

		return msg.toString();
	}

	/**
	 * Returns message for historical document versions.
	 * 
	 * @return formatted message
	 */
	public String getHistoricalDocumentMessage() {
		StringBuilder message = new StringBuilder(
				labelProvider.getValue(LabelConstants.HISTORICAL_VERSION));
		return message.toString();
	}

	// ----------------------------------------
	// Task and workflow labels
	// ----------------------------------------

	/**
	 * Gets the task link label.
	 * 
	 * @param taskInstance
	 *            the task instance
	 * @return the task link label
	 */
	public String getTaskLinkLabel(AbstractTaskInstance taskInstance) {

		// String template =
		// "<span class='link-row'>{0}, {1} ({2})</span><span class='info-row'>изпълнител: <b>{3}</b>, създадена на: <b>{4,date,dd.MM.yyyy, HH:mm}</b></span>";
		String template = labelProvider.getValue(LabelConstants.CMF_TASK_LINK_LABEL);
		Map<String, Serializable> properties = taskInstance.getProperties();

		String taskId = (String) properties.get(DefaultProperties.UNIQUE_IDENTIFIER);

		String type = getCodelistDisplayValue(taskInstance, TaskProperties.TYPE);

		String status = getTaskStatus(taskInstance, properties);

		Date modifiedOn = (Date) properties.get(TaskProperties.ACTUAL_START_DATE);

		String modifiedBy = (String) taskInstance.getProperties().get(TaskProperties.TASK_OWNER);
		if (StringUtils.isNotNullOrEmpty(modifiedBy)) {
			modifiedBy = getDisplayNameForUser(modifiedBy);
		} else if (taskService.isPooledTask(taskInstance)) {
			modifiedBy = labelProvider.getValue(LabelConstants.TASK_UNASSIGNED_LABEL);
		}

		String label = MessageFormat.format(template, taskId, type, status, modifiedBy, modifiedOn);

		return label.toString();
	}

	/**
	 * Gets the task link label.
	 * 
	 * @param taskInstance
	 *            the task instance
	 * @return the task link label
	 */
	public String getTaskLinkShortLabel(AbstractTaskInstance taskInstance) {

		String template = "{0}, {1} ({2})";

		Map<String, Serializable> properties = taskInstance.getProperties();

		String taskId = WorkflowHelper.getInstanceId(taskInstance.getTaskInstanceId());

		String type = getCodelistDisplayValue(taskInstance, TaskProperties.TYPE);

		String status = getTaskStatus(taskInstance, properties);

		String label = MessageFormat.format(template, taskId, type, status);

		return label.toString();
	}

	/**
	 * Gets the task status.
	 * 
	 * @param taskInstance
	 *            the task instance
	 * @param properties
	 *            the properties
	 * @return the task status
	 */
	private String getTaskStatus(AbstractTaskInstance taskInstance,
			Map<String, Serializable> properties) {
		String value = getCodelistDisplayValue(taskInstance, TaskProperties.STATUS);
		return value;
	}

	/**
	 * Gets the workflow link label.
	 * 
	 * @param workflowInstanceContext
	 *            the workflow instance context
	 * @return the workflow link label
	 */
	public String getWorkflowLinkLabel(WorkflowInstanceContext workflowInstanceContext) {

		// String pattern =
		// "<span class='link-row'>{0}, {1} ({2})</span><span class='info-row'>стартиран от: <b>{3}</b>, стартиран на: <b>{4,date,dd.MM.yyyy, HH:mm}</b></span>";
		String pattern = labelProvider.getValue(LabelConstants.CMF_WORKFLOW_LINK_LABEL);
		String workflowId = WorkflowHelper.stripEngineId(workflowInstanceContext
				.getWorkflowInstanceId());

		String workflowType = getCodelistDisplayValue(workflowInstanceContext,
				WorkflowProperties.TYPE);

		String workflowStatus = getCodelistDisplayValue(workflowInstanceContext,
				WorkflowProperties.STATUS);

		Map<String, Serializable> properties = workflowInstanceContext.getProperties();

		String startedBy = (String) properties.get(WorkflowProperties.STARTED_BY);
		if (StringUtils.isNotNullOrEmpty(startedBy)) {
			startedBy = getDisplayNameForUser(startedBy);
		}

		Date startTime = (Date) properties.get(WorkflowProperties.ACTUAL_START_DATE);

		String label = "";
		label = MessageFormat.format(pattern, workflowId, workflowType, workflowStatus, startedBy,
				startTime);

		return label;
	}

	/**
	 * Gets the workflow summary.
	 * 
	 * @param workflowInstanceContext
	 *            the workflow instance context
	 * @return the workflow summary
	 */
	public String getWorkflowSummary(WorkflowInstanceContext workflowInstanceContext) {

		String pattern = "{0}, {1} ({2})";

		String workflowId = (String) workflowInstanceContext.getProperties().get(
				DefaultProperties.UNIQUE_IDENTIFIER);

		String workflowType = getCodelistDisplayValue(workflowInstanceContext,
				WorkflowProperties.TYPE);

		String workflowStatus = getCodelistDisplayValue(workflowInstanceContext,
				WorkflowProperties.STATUS);

		return MessageFormat.format(pattern, workflowId, workflowType, workflowStatus);
	}

	/**
	 * Gets the task info.
	 * 
	 * @param taskInstance
	 *            the task instance
	 * @param includeUser
	 *            the include user
	 * @return the task info
	 */
	public String getTaskInfo(AbstractTaskInstance taskInstance, boolean includeUser) {

		String summary = "";
		//
		// String definitionId = taskInstance.getTaskDefinitionId();
		// definitionId = WorkflowHelper.stripEngineId(definitionId);
		// // get the definitionId as default
		// summary = definitionId;

		// TODO task type can be retrieved from the field
		// TaskConstants.TASK_TYPE
		// TODO change to use the common method

		String type = getCodelistDisplayValue(taskInstance, TaskProperties.TYPE);
		if (type != null) {
			summary = type;
		}

		String owner = (String) taskInstance.getProperties().get(TaskProperties.TASK_OWNER);
		if (StringUtils.isNotNullOrEmpty(owner)) {
			String displayNameForUser = getDisplayNameForUser(owner);
			summary += " (" + displayNameForUser + ")";
		}

		return summary;
	}

	/**
	 * Gets the task id.
	 * 
	 * @param taskInstance
	 *            the task instance
	 * @return the task id
	 */
	public String getTaskSummary(TaskInstance taskInstance) {
		// Task Type: <b>{0}</b> Status: <b>{1}</b>, Due date: <b>{2}</b>,
		// Assignee: <b>{3}</b><br/><b>{4}</b>
		Map<String, Serializable> properties = taskInstance.getProperties();

		// get the task type
		String type = getCodelistDisplayValue(taskInstance, TaskProperties.TYPE);

		String status = getTaskStatus(taskInstance, properties);

		// get the due date
		Date dueDate = (Date) taskInstance.getContext().getProperties()
				.get(WorkflowProperties.PLANNED_END_DATE);

		// get the assignee display name
		String assignee = (String) properties.get(TaskProperties.TASK_OWNER);
		String displayNameForTaskAssignee = getDisplayNameForUser(assignee);

		// build the summary
		String message = labelProvider.getValue(LabelConstants.WORKFLOW_TASK_SUMMARY);
		String summary = MessageFormat.format(message, type, status, dueDate,
				displayNameForTaskAssignee);
		return summary;
	}

	/**
	 * Gets the task search summary.
	 * 
	 * @param taskInstance
	 *            the task instance
	 * @return the task id
	 */
	public String getTaskSearchSummary(TaskInstance taskInstance) {
		// Status: <b>{0}</b>, Due date: <b>{1}</b>, Assignee: <b>{2}</b><br
		// /><b>{3}</b>
		Map<String, Serializable> properties = taskInstance.getProperties();

		String status = getTaskStatus(taskInstance, properties);

		// get the due date
		Date dueDate = (Date) taskInstance.getContext().getProperties()
				.get(WorkflowProperties.PLANNED_END_DATE);

		// get the assignee display name
		String assignee = (String) properties.get(TaskProperties.TASK_OWNER);
		String displayNameForTaskAssignee = getDisplayNameForUser(assignee);

		// get the case id
		WorkflowInstanceContext workflowInstance = taskInstance.getContext();
		String caseId = (String) workflowInstance.getOwningInstance().getProperties()
				.get(DefaultProperties.UNIQUE_IDENTIFIER);
		// backward compatible
		if ((caseId == null) && (workflowInstance.getOwningInstance() instanceof CMInstance)) {
			caseId = ((CMInstance) workflowInstance.getOwningInstance()).getContentManagementId();
		}

		// build the task summary message
		String message = labelProvider.getValue(LabelConstants.WORKFLOW_TASK_SEARCH_SUMMARY);
		String summary = MessageFormat.format(message, status, dueDate, displayNameForTaskAssignee,
				caseId);
		return summary;
	}

	/**
	 * Checks if is overdue task.
	 * 
	 * @param taskInstance
	 *            the task instance
	 * @return true, if is overdue task
	 */
	public boolean isOverdueTask(AbstractTaskInstance taskInstance) {

		boolean isOverdue = taskInstance.isOverDue();

		boolean isCompleted = false;

		if (TaskState.COMPLETED == taskInstance.getState()) {
			isCompleted = true;
		}

		return isOverdue && !isCompleted;
	}

	/**
	 * Assemble image path.
	 * 
	 * @param imageName
	 *            Image name.
	 * @return Image path.
	 */
	public String getIconImagePath(String imageName) {
		return "images:icon_" + imageName + ".png";
	}

	/**
	 * Assemble image path using given extension.
	 * 
	 * @param imageName
	 *            Image name.
	 * @param extension
	 *            Extension.
	 * @return Image path.
	 */
	public String getIconImagePath(String imageName, String extension) {
		return "images:icon_" + imageName + "." + extension;
	}

	/**
	 * Gets the document section title.
	 * 
	 * @param sectionInstance
	 *            the section instance
	 * @return the document section title
	 */
	public String getDocumentSectionTitle(SectionInstance sectionInstance) {
		PropertyDefinition propertyDefinition = dictionaryService.getProperty(
				SectionProperties.TITLE, sectionInstance.getRevision(), sectionInstance);
		if (propertyDefinition == null) {
			return sectionInstance.getIdentifier();
		}
		return propertyDefinition.getLabel();
	}

	/**
	 * User and version date processing into a single string.
	 * 
	 * @param user
	 *            - user link string
	 * @param then
	 *            - version date
	 * @return concatenated left & then as string
	 */

	public String concatVersionStr(String user, Date then) {
		String durationString = "";
		String space = " ";
		String patternDays = labelProvider.getValue(LabelConstants.DOC_VERSIONS_DAYS);
		String patternHours = labelProvider.getValue(LabelConstants.DOC_VERSIONS_HOURS);
		String patternMinutes = labelProvider.getValue(LabelConstants.DOC_VERSIONS_MINUTES);
		long day = 1L * 24 * 60 * 60 * 1000;
		long hour = 1L * 60 * 60 * 1000;
		long minute = 1L * 60 * 1000;
		Calendar calThen = Calendar.getInstance();
		calThen.setTime(then);
		Calendar calNow = Calendar.getInstance();
		long diff = calNow.getTimeInMillis() - calThen.getTimeInMillis();
		long days = diff / day;
		long hours = diff / hour;
		long minutes = diff / minute;

		if (days >= 1) {
			durationString = MessageFormat.format(patternDays, days);
		} else if ((hours >= 1) && (hours < 24)) {
			durationString = MessageFormat.format(patternHours, hours);
		} else if ((minutes >= 1) && (minutes < 60)) {
			durationString = MessageFormat.format(patternMinutes, minutes);
		}
		return getDisplayNameForUser(user) + space + durationString;
	}

	/**
	 * Generates internationalized alert message.
	 * 
	 * @param label
	 *            - string place holder
	 * @param extension
	 *            - string to embed in pace holder
	 * @return assembled string
	 */
	public String getWarningMessage(String label, String extension) {
		String str = String.format(label, extension);
		return str;
	}

	/**
	 * Gets the action confirmation message.
	 * 
	 * @param message
	 *            the message
	 * @param action
	 *            the action
	 * @return the action confirmation message
	 */
	public String getActionConfirmationMessage(String message, Action action) {

		return message + " '" + action.getLabel() + "'"
				+ (action.getLabel().endsWith("?") ? "" : "?");
	}

	/**
	 * Gets the confirm ok button label.
	 * 
	 * @return the confirm ok button label
	 */
	public String getConfirmOkButtonLabel() {
		return labelProvider.getValue(LabelConstants.CONFIRM_OK_BUTTON);
	}

	/**
	 * Gets the confirm cancel button label.
	 * 
	 * @return the confirm cancel button label
	 */
	public String getConfirmCancelButtonLabel() {
		return labelProvider.getValue(LabelConstants.CONFIRM_CANCEL_BUTTON);
	}

	/**
	 * Gets the confirm no button label
	 * 
	 * @return the confirm no button label
	 */
	public String getConfirmNoButtonLabel() {
		return labelProvider.getValue(LabelConstants.CONFIRM_NO_BUTTON);
	}

	/**
	 * Gets the codelist display value.
	 * 
	 * @param model
	 *            the model
	 * @param property
	 *            the property
	 * @return the codelist display value
	 */
	public String getCodelistDisplayValue(PropertyModel model, String property) {
		String type = (String) model.getProperties().get(property);
		if (type == null) {
			return null;
		}
		type = WorkflowHelper.stripEngineId(type);
		PropertyDefinition definition = null;
		definition = dictionaryService.getProperty(property, model.getRevision(), model);
		if ((definition != null) && (definition.getCodelist() != null)) {
			String description = codelistService.getDescription(definition.getCodelist(), type);
			if (StringUtils.isNotNullOrEmpty(description)) {
				return description;
			}
		} else {
			logger.debug("Property definition was not found or no codelist set: " + definition);
		}
		return type;
	}

	/**
	 * Gets the codelist display value.
	 * 
	 * @param model
	 *            the model
	 * @param key
	 *            the key
	 * @param value
	 *            the value
	 * @return the codelist display value
	 */
	public String getCodelistDisplayValue(DefinitionModel model, String key, String value) {
		Integer cl = null;
		PropertyDefinition fieldDefinition = getFieldDefinition(model, key);
		if (fieldDefinition == null) {
			if (model instanceof RegionDefinitionModel) {
				for (RegionDefinition regionDefinition : ((RegionDefinitionModel) model)
						.getRegions()) {
					fieldDefinition = getFieldDefinition(regionDefinition, key);
					if (fieldDefinition != null) {
						cl = fieldDefinition.getCodelist();
						break;
					}
				}
			}
		} else {
			cl = fieldDefinition.getCodelist();
		}
		String descr = value;
		if (cl != null) {
			String description = codelistService.getDescription(cl, value);
			if (StringUtils.isNotNullOrEmpty(description)) {
				descr = description;
			}
		}
		return descr;
	}

	/**
	 * Gets the field definition.
	 * 
	 * @param model
	 *            the model
	 * @param name
	 *            the name
	 * @return the field definition
	 */
	private PropertyDefinition getFieldDefinition(DefinitionModel model, String name) {
		for (PropertyDefinition definition : model.getFields()) {
			if (EqualsHelper.nullSafeEquals(definition.getName(), name, true)) {
				return definition;
			}
		}
		return null;
	}

	/**
	 * Gets the check box modifier.
	 * 
	 * @param instance
	 *            the instance
	 * @param instanceProperty
	 *            the instance property
	 * @param checkBoxProperty
	 *            the check box property
	 * @return the check box modifier
	 */
	public String getCheckBoxModifier(Instance instance, String instanceProperty,
			String checkBoxProperty) {
		String userId = null;
		Date modifiedOn = null;

		Serializable serializable = instance.getProperties().get(instanceProperty);

		if (serializable instanceof Instance) {
			Serializable serializable2 = ((Instance) serializable).getProperties().get(
					checkBoxProperty);
			if (serializable2 instanceof Instance) {
				Map<String, Serializable> properties = ((Instance) serializable2).getProperties();
				if (properties.isEmpty()) {
					return "";
				}
				userId = (String) properties.get(DefaultProperties.CHECK_BOX_MODIFIED_FROM);
				modifiedOn = (Date) properties.get(DefaultProperties.CHECK_BOX_MODIFIED_ON);
			}
		}

		if ((userId == null) && (modifiedOn == null)) {
			return "";
		}
		String template = labelProvider.getValue(LabelConstants.CMF_CHECKBOX_MODIFIER_LINK_LABEL);

		String label = MessageFormat.format(template, getDisplayNameForUser(userId),
				(modifiedOn == null ? "" : dateUtil.getFormattedDateTime(modifiedOn)));
		return label;
	}

	/**
	 * Gets the link description label.
	 * 
	 * @param linkInstance
	 *            the link instance
	 * @return the link description label
	 */
	public String getLinkDescriptionLabel(LinkInstance linkInstance) {
		// "<span class='info-row'>описание на връзка: <b>{0}</b></span>"
		Instance instance = linkInstance.getTo();
		if (instance instanceof CaseInstance) {
			String linkDescriptionLabel = MessageFormat.format(
					labelProvider.getValue(LabelConstants.CMF_CASE_LINK_DESCRIPTION_LABEL),
					linkInstance.getProperties().get(LinkConstants.LINK_DESCRIPTION));
			return linkDescriptionLabel;
		} else {
			return "Invalid link: " + linkInstance.getIdentifier();
		}
	}

	/**
	 * Escapes javascript specific symbols in a string
	 * 
	 * @param text
	 *            String to escape.
	 * @return Escaped string.
	 */
	public String escapeJavascipt(String text) {
		return StringEscapeUtils.escapeJavaScript(text);
	}

	/**
	 * Assembles the warning message when an object is locked by user.
	 * 
	 * @param objectInstance
	 *            Object instance.
	 * @return Message.
	 */
	public String getObjectIsLockedMessage(Instance objectInstance) {
		ViewInstance viewInstance = typeConverter.convert(ViewInstance.class, objectInstance);
		String name = viewInstance.getLockedBy();

		StringBuilder msg = new StringBuilder(
				labelProvider.getValue(LabelConstants.MSG_WARNING_OBJECT_IS_LOCKED));
		msg.append(" ").append(getDisplayNameForUser(name));

		return msg.toString();
	}
}
