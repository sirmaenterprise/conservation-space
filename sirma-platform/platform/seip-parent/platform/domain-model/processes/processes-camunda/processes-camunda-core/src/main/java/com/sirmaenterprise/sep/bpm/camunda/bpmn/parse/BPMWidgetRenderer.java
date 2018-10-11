package com.sirmaenterprise.sep.bpm.camunda.bpmn.parse;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.imageio.ImageIO;
import javax.inject.Inject;
import javax.json.JsonObject;

import com.sirma.itt.seip.io.TempFileProvider;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.plugin.Extension;
import com.sirma.itt.seip.rest.utils.JSON;
import com.sirma.sep.content.idoc.ContentNode;
import com.sirma.sep.content.idoc.Widget;
import com.sirma.sep.content.idoc.WidgetConfiguration;
import com.sirma.sep.export.renders.BaseRenderer;
import com.sirma.sep.export.renders.IdocRenderer;
import com.sirma.sep.export.renders.html.table.HtmlTableBuilder;
import com.sirma.sep.export.renders.html.table.HtmlValueImageBuilder;
import com.sirma.sep.export.renders.utils.ImageUtils;
import com.sirmaenterprise.sep.bpm.camunda.bpmn.CamundaBPMNService;
import com.sirmaenterprise.sep.camunda.diagram.ProcessDiagramGenerator;

/**
 * Implementation for exporting business-process-diagram-widget for export to Word.
 *
 * @author simeon iliev
 */
@ApplicationScoped
@Extension(target = IdocRenderer.PLUGIN_NAME, order = 32)
public class BPMWidgetRenderer extends BaseRenderer {

	private static final String BPM_WIDGET_NAME = "business-process-diagram-widget";

	protected static final String NO_SELECTION = "widgets.process.empty";
	protected static final String EXPORT_DIR = "ExportToWordProcessWidget";

	private static final Logger LOGGER = LoggerFactory.getLogger(BPMWidgetRenderer.class);

	@Inject
	private CamundaBPMNService processService;

	@Inject
	private RepositoryService repositoryService;

	@Inject
	private TempFileProvider tempFileProvider;

	@Override
	public boolean accept(ContentNode node) {
		return node.isWidget() && BPM_WIDGET_NAME.equals(((Widget) node).getName());
	}

	@Override
	public Element render(String currentInstanceId, ContentNode node) {
		Widget processWidget = (Widget) node;
		WidgetConfiguration configuration = processWidget.getConfiguration();
		JsonObject jsonConfiguration = IdocRenderer.toJson(configuration);
		// get the widget title
		String widgetTitle = IdocRenderer.getWidgetTitle(jsonConfiguration);
		Boolean showHeader = IdocRenderer.getShowWidgetHeader(jsonConfiguration);
		// create table
		HtmlTableBuilder tableBuilder = new HtmlTableBuilder(widgetTitle, showHeader);
		setWidth(tableBuilder, node.getElement(), NIGHTY_NINE_PRECENT);
		String selectionMode = configuration.getSelectionMode().toString();
		if (IdocRenderer.AUTOMATICALLY.equalsIgnoreCase(selectionMode) && !hasSearchCriteria(jsonConfiguration)) {
			return buildOneRowMessageTable(tableBuilder, KEY_LABEL_SELECTED_OBJECT_UNDEFINED_CRITERIA);
		}
		try {
            List<Instance> selectedInstancesFromCriteria = (List<Instance>) getSelectedInstances(currentInstanceId,
																				  selectionMode, jsonConfiguration);
			if (selectedInstancesFromCriteria.isEmpty()) {
				return buildOneRowMessageTable(tableBuilder, NO_SELECTION);
			}
			// We get the first element of the results.
			return buildProcessTable(tableBuilder, selectedInstancesFromCriteria.get(0));
		} catch (IllegalArgumentException e) {
			LOGGER.info("Widget has not search configuration!", e);
		}
		return buildOneRowMessageTable(tableBuilder, KEY_LABEL_SELECTED_OBJECT_UNDEFINED_CRITERIA);
	}

	/**
	 * Build and populate html table for <code>processInstance</code>.
	 * @param tableBuilder - the table builder.
	 * @param processInstance - the process instance.
	 * @return filed html table of process.
	 */
	private Element buildProcessTable(HtmlTableBuilder tableBuilder, Instance processInstance) {
		if(CamundaBPMNService.isProcess(processInstance)) {
			File exportDir = tempFileProvider.createLongLifeTempDir(EXPORT_DIR);
			File imageFileHolder = tempFileProvider.createTempFile(UUID.randomUUID().toString(), ".jpg", exportDir);
			try {
				BufferedImage srcImage = loadImage(processInstance);
				ImageIO.write(srcImage, "jpg", imageFileHolder);
				HtmlValueImageBuilder htmlValueImageBuilder = new HtmlValueImageBuilder(srcImage, processInstance.get("title").toString());
				tableBuilder.addTdValue(0, 0, htmlValueImageBuilder);
			} catch (IOException e) {
				LOGGER.warn(e.getMessage(), e);
			} finally {
				tempFileProvider.deleteFile(imageFileHolder);
			}
		}
		return tableBuilder.build();
	}

	/**
	 * Retrieves the definition of the instance and creates a diagram of the process.
	 *
	 * @param processInstance the instance we want to add to the diagram.
	 *
	 * @return the buffered image of the diagram.
	 * @throws IOException if an error occurs during loading.
	 */
	private BufferedImage loadImage(Instance processInstance) throws IOException {
		ProcessDefinition processDefinition = processService.getProcessDefinition(processInstance);
		return ImageUtils.convertPNGToJPEG(ImageIO.read(ProcessDiagramGenerator.generatePngDiagram((ProcessDefinitionEntity) repositoryService.getProcessDefinition(processDefinition.getId()))));
	}

	/**
	 * Gets the selected instances from widget json configuration.
	 *
	 * @param currentInstanceId
	 *            the current instance id
	 * @param jsonConfiguration
	 *            the json configuration
	 * @return the selected instances
	 */
	private Collection<Instance> getSelectedInstances(String currentInstanceId, String selectionMode,
			JsonObject jsonConfiguration) {
		if (CURRENT.equalsIgnoreCase(selectionMode)) {
			return loadInstances(Collections.singletonList(currentInstanceId));
		}
		if (MANUALLY.equalsIgnoreCase(selectionMode)) {
			if (jsonConfiguration.containsKey(SELECTED_OBJECTS)) {
				return loadInstances(JSON.jsonToList(jsonConfiguration.getJsonArray(SELECTED_OBJECTS)));
			}
			return Collections.emptyList();
		}
		return getSelectedInstancesFromCriteria(currentInstanceId, jsonConfiguration);
	}
}