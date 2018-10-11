package com.sirma.itt.seip.instance.template.update;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.instance.DomainInstanceService;
import com.sirma.itt.seip.instance.InstanceSaveContext;
import com.sirma.itt.seip.instance.lock.LockService;
import com.sirma.itt.seip.instance.state.Operation;
import com.sirma.itt.seip.template.TemplateService;
import com.sirma.sep.content.Content;
import com.sirma.sep.content.ContentInfo;
import com.sirma.sep.content.InstanceContentService;
import com.sirma.sep.content.idoc.Idoc;
import com.sirma.sep.content.idoc.SectionNode;
import com.sirma.sep.content.idoc.Sections;

/**
 * Class responsible for updating and saving the instance view based on instance template version.
 *
 * @author T. Dossev
 */
@ApplicationScoped
public class InstanceTemplateUpdater {
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@Inject
	private DomainInstanceService domainInstanceService;

	@Inject
	private LockService lockService;

	@Inject
	private InstanceContentService instanceContentService;

	@Inject
	private TemplateService templateService;

	/**
	 * Saves new instance view based on instance template update process.
	 *
	 * @param item
	 *            updated view
	 * @param templateVersion
	 *            template's last published version
	 * @param operation
	 *            the operation with which was triggered save
	 * @return saved instance
	 */
	public Instance saveItem(InstanceTemplateUpdateItem item, String templateVersion, String operation) {
		Instance instance = domainInstanceService.loadInstance(item.getInstanceId());

		if (!lockService.lockStatus(instance.toReference()).isLocked()) {
			instance.add(DefaultProperties.TEMP_CONTENT_VIEW, item.getMergedContent());
			instance.add(DefaultProperties.TEMPLATE_VERSION, templateVersion);

			InstanceSaveContext context = InstanceSaveContext.create(instance, new Operation(operation));
			context.disableValidation(
					"Instance template update operation updates the view and should not be blocked by illegal instance state");
			return domainInstanceService.save(context);
		}
		return null;
	}

	/**
	 * Template content view parser.
	 *
	 * @param templateInstanceId
	 *            id of the template
	 * @return idoc instance from the parsed content
	 */
	private Idoc parseTemplateView(String templateInstanceId) {
		String templateView = templateService.getContent(templateInstanceId);

		return Idoc.parse(templateView);
	}

	/**
	 * Updates the content view of an instance with the latest published template view.
	 *
	 * @param instanceId
	 *            instance tpo be updated
	 * @param templateInstanceId
	 *            latest published template view
	 * @return InstanceTemplateUpdateItem instance
	 */
	public InstanceTemplateUpdateItem updateItem(String instanceId, String templateInstanceId) {
		try {
			ContentInfo contentInfo = instanceContentService.getContent(instanceId, Content.PRIMARY_VIEW);

			String content = contentInfo.asString();

			Idoc mergedContent = merge(content, parseTemplateView(templateInstanceId));

			if (mergedContent != null) {
				return new InstanceTemplateUpdateItem(instanceId, mergedContent.asHtml());
			}
		} catch (IOException e) {
			LOGGER.warn("Cannot load view for [" + instanceId + "]", e);
		}

		return null;
	}

	/**
	 * Merges the instance view and the template view based on the following algorithm: <br/>
	 * Resulting Idoc object is created using the instance view, but the sections gets removed. The purpose of this is
	 * to get the Idoc specific attributes that are not part of the sections.<br/>
	 * The template sections are iterated (because the resulting view should follow the template). <br/>
	 * - if the current template section is locked it gets added directly to the result, because locked sections must be
	 * updated. <br/>
	 * If the current template section is not locked: <br/>
	 * - if there is no corresponding section in the instance view (with the same id), the section is considered as new
	 * and is directly appended to the result. <br />
	 * - if there is a corresponding section in the instance view and if it is also not locked, add the corresponding
	 * instance section to the result. <br />
	 * - if there is a corresponding section in the instance view and it is locked, the section from the template is
	 * added to the result because it should replace the section from the instance. <br/>
	 * The user-defined tabs from the instance view are appended after the template tabs. <br/>
	 * When the merge is complete, the logic compares if there is a actual change after the merge and returns no result
	 * if there is no change.
	 *
	 * @param instanceViewContent
	 *            view of the instance.
	 * @param templateView
	 *            parsed view of the template.
	 * @return optional containing the result of the merge, or empty if there is no actual change during the merge
	 *         process.
	 */
	private static Idoc merge(String instanceViewContent, Idoc templateView) {
		Idoc instanceView = Idoc.parse(instanceViewContent);

		Idoc result = Idoc.parse(instanceViewContent);
		Sections mergedSections = result.getSections();
		mergedSections.removeAll();

		Sections instanceSections = instanceView.getSections();

		boolean changed = false;

		for (SectionNode currentSection : templateView.getSections()) {
			if (currentSection.isLocked()) {
				mergedSections.addLast(currentSection);
				continue;
			}

			Optional<SectionNode> correspondingInstanceSectionOptional = instanceSections
					.getSectionById(currentSection.getId());

			if (correspondingInstanceSectionOptional.isPresent()) {
				SectionNode correspondingInstanceSection = correspondingInstanceSectionOptional.get();

				if (correspondingInstanceSection.isLocked()) {
					mergedSections.addLast(currentSection);
					changed = true;
				} else {
					// the section is not locked in the instance and in the template
					mergedSections.addLast(correspondingInstanceSection);
				}
			} else {
				mergedSections.addLast(currentSection);
				changed = true;
			}
		}

		mergedSections.addAll(extractUserDefinedSections(instanceSections, mergedSections));

		if (!changed) {
			changed = mergedSections.count() != instanceSections.count();
		}

		if (!changed) {
			changed = isInstanceViewChanged(instanceSections, mergedSections);
		}

		if (!changed) {
			return null;
		}

		return result;
	}

	private static List<SectionNode> extractUserDefinedSections(Sections instanceSections, Sections mergedSections) {
		return instanceSections
				.stream()
				.filter(SectionNode::isUserDefined)
				.filter(section -> !mergedSections.getSectionById(section.getId()).isPresent())
				.collect(Collectors.toList());
	}

	private static boolean isInstanceViewChanged(Sections instanceSections, Sections mergedSections) {
		for (int i = 0; i < mergedSections.count(); i++) {
			SectionNode instanceSection = instanceSections.getSectionByIndex(i);
			SectionNode mergedSection = mergedSections.getSectionByIndex(i);

			if (!instanceSection.asHtml().equals(mergedSection.asHtml())) {
				return true;
			}
		}

		return false;
	}
}
