package com.sirma.sep.instance.template;

import java.io.IOException;
import java.io.Serializable;
import java.util.Map;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.domain.ObjectTypes;
import com.sirma.itt.seip.domain.event.AuditableEvent;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstancePropertyNameResolver;
import com.sirma.itt.seip.domain.rest.EmfApplicationException;
import com.sirma.itt.seip.domain.search.SearchArguments;
import com.sirma.itt.seip.domain.security.ActionTypeConstants;
import com.sirma.itt.seip.event.EventService;
import com.sirma.itt.seip.instance.DomainInstanceService;
import com.sirma.itt.seip.instance.relation.LinkConstants;
import com.sirma.itt.seip.instance.template.update.InstanceTemplateUpdateItem;
import com.sirma.itt.seip.instance.template.update.InstanceTemplateUpdateJobProperties;
import com.sirma.itt.seip.instance.template.update.InstanceTemplateUpdater;
import com.sirma.itt.seip.search.SearchService;
import com.sirma.itt.seip.security.annotation.RunAsAdmin;
import com.sirma.itt.seip.template.Template;
import com.sirma.itt.seip.template.TemplateProperties;
import com.sirma.itt.seip.template.TemplateService;
import com.sirma.sep.content.Content;
import com.sirma.sep.content.ContentInfo;
import com.sirma.sep.content.InstanceContentService;
import com.sirma.sep.content.idoc.Idoc;
import com.sirma.sep.content.idoc.SectionNode;
import com.sirma.sep.content.idoc.nodes.widgets.objectdata.ObjectDataWidget;
import com.sirma.sep.content.idoc.nodes.widgets.objectdata.ObjectDataWidgetConfiguration;
import com.sirma.sep.instance.batch.BatchRequest;
import com.sirma.sep.instance.batch.BatchRequestBuilder;
import com.sirma.sep.instance.batch.BatchService;

/**
 * Implementation of {@link InstanceTemplateService} API methods.
 *
 * @author Adrian Mitev
 */
public class InstanceTemplateServiceImpl implements InstanceTemplateService {

	private static final Logger LOGGER = LoggerFactory.getLogger(InstanceTemplateServiceImpl.class);

	@Inject
	private SearchService searchService;

	@Inject
	private DomainInstanceService domainInstanceService;

	@Inject
	private BatchService batchService;

	@Inject
	private TemplateService templateService;

	@Inject
	private InstanceContentService instanceContentService;

	@Inject
	private InstanceTemplateUpdater instanceTemplateUpdater;

	@Inject
	private EventService eventService;

	@Inject
	private InstancePropertyNameResolver nameResolver;

	@Override
	@RunAsAdmin
	public void updateInstanceViews(String templateInstanceId) {
		String templateVersion = getTemplatePublishedVersion(templateInstanceId);

		Map<String, Serializable> properties = CollectionUtils.createHashMap(2);
		properties.put(LinkConstants.HAS_TEMPLATE, templateInstanceId);

		SearchArguments<Instance> searchArguments = new SearchArguments<>();
		searchArguments.setArguments(properties);

		BatchRequest batchRequest = BatchRequestBuilder.fromSearch("instanceTemplateUpdateJob", searchArguments,
				"instance", searchService);

		batchRequest.getProperties().put(InstanceTemplateUpdateJobProperties.TEMPLATE_INSTANCE_ID, templateInstanceId);
		batchRequest.getProperties().put(InstanceTemplateUpdateJobProperties.TEMPLATE_VERSION, templateVersion);

		// log execution of the action for the template instance
		eventService.fire(new AuditableEvent(domainInstanceService.loadInstance(templateInstanceId),
				ActionTypeConstants.UPDATE_EXISTING_OBJECTS));

		batchService.execute(batchRequest);
	}

	@Override
	@RunAsAdmin
	public Instance updateInstanceView(String instanceId) {
		String templateInstanceId = domainInstanceService
				.loadInstance(instanceId)
				.getAsString(LinkConstants.HAS_TEMPLATE, nameResolver);

		InstanceTemplateUpdateItem updatedInstance = instanceTemplateUpdater.updateItem(instanceId, templateInstanceId);

		if (updatedInstance != null) {
			return instanceTemplateUpdater.saveItem(updatedInstance, getTemplatePublishedVersion(templateInstanceId),
					ActionTypeConstants.UPDATE_SINGLE_INSTANCE_TEMPLATE);
		}

		return null;
	}

	@Override
	public String createTemplate(Template template, String sourceId) {
		String sourceInstanceContent = getSourceContent(sourceId);
		Idoc idoc = Idoc.parse(sourceInstanceContent);

		turnTabsIntoNonUserDefined(idoc);

		Instance sourceInstance = domainInstanceService.loadInstance(sourceId);
		bindSelectedPropertiesInObjectDataWidgetToTheNewForType(idoc, getSourceType(sourceInstance),
				template.getForType());

		String templateId = templateService.create(template, idoc.asHtml());

		eventService.fire(new AuditableEvent(sourceInstance, "saveAsTemplate"));

		return templateId;
	}

	private static String getSourceType(Instance sourceInstance) {
		// when the source instance is template, the widgets are configured according to its 'for object type' property
		if (sourceInstance.type().is(ObjectTypes.TEMPLATE)) {
			return sourceInstance.getAsString(TemplateProperties.FOR_OBJECT_TYPE);
		}

		return sourceInstance.getIdentifier();
	}

	private String getSourceContent(String sourceId) {
		ContentInfo contentInfo = instanceContentService.getContent(sourceId, Content.PRIMARY_VIEW);
		if (!contentInfo.exists()) {
			throw new IllegalArgumentException("No view found for instance '" + sourceId + "'");
		}

		try {
			return contentInfo.asString();
		} catch (IOException e) {
			LOGGER.error("Cannot load view for instance '{}'", sourceId);
			throw new EmfApplicationException("Could not extract content for source instance '" + sourceId + "'", e);
		}
	}

	/**
	 * Sets all tabs to be non-user defined because the template is expected to have only non-user defined tabs and the
	 * view from which the template is constructed may contain user-defined tabs.
	 *
	 * @param idoc
	 *            parsed view.
	 */
	private static void turnTabsIntoNonUserDefined(Idoc idoc) {
		idoc.getSections().forEach(section -> section.addProperty(SectionNode.SECTION_USER_DEFINED, "false"));
	}

	/**
	 * Workarounds the issue when using a view configured for a particular type to instances of another type the
	 * selected properties in object data widget (ODW) are tightly coupled with the type for which the widget is
	 * configured and cannot be reused for another type.
	 *
	 * @param idoc
	 *            parsed view.
	 * @param baseType
	 *            type for which the view is configured.
	 * @param newType
	 *            type to which to bind the selected properties in ODW.
	 */
	private static void bindSelectedPropertiesInObjectDataWidgetToTheNewForType(Idoc idoc, String baseType,
			String newType) {
		if (!baseType.equals(newType)) {
			idoc.widgets(ObjectDataWidget.NAME).forEach(widget -> {
				ObjectDataWidgetConfiguration config = new ObjectDataWidget(widget.getElement()).getConfiguration();
				JsonObject selectedProperties = config.getSelectedProperties();
				JsonElement baseTypeProperties = selectedProperties.get(baseType);

				if (baseTypeProperties != null) {
					selectedProperties.remove(baseType);
					selectedProperties.add(newType, baseTypeProperties);
				}

				config.writeConfiguration();
			});
		}
	}

	@Override
	public String getInstanceTemplateVersion(String instanceId) {
		return getTemplatePublishedVersion(
				domainInstanceService.loadInstance(instanceId).getAsString(LinkConstants.HAS_TEMPLATE, nameResolver));
	}

	@Override
	public boolean hasTemplate(Serializable instance) {
		if (instance instanceof Instance) {
			Instance localInstance = (Instance) instance;
			if (localInstance.type().is("template")) {
				// Templates don't have templates
				return false;
			}
			String templateInstanceId = localInstance.getAsString(LinkConstants.HAS_TEMPLATE, nameResolver);
			return templateInstanceId != null && templateService.hasTemplate(templateInstanceId);
		}
		return false;
	}

	private String getTemplatePublishedVersion(String templateInstanceId) {
		return templateService.getTemplate(templateInstanceId).getPublishedInstanceVersion();
	}
}
