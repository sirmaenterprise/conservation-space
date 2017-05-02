package com.sirmaenterprise.sep.bpm.camunda.bpmn.parse;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.imageio.ImageIO;
import javax.inject.Inject;
import javax.json.JsonObject;

import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.export.renders.BaseRenderer;
import com.sirma.itt.seip.export.renders.IdocRenderer;
import com.sirma.itt.seip.export.renders.utils.ImageUtils;
import com.sirma.itt.seip.plugin.Extension;
import com.sirma.itt.seip.rest.utils.JSON;
import com.sirmaenterprise.sep.bpm.camunda.bpmn.CamundaBPMNService;
import com.sirmaenterprise.sep.camunda.diagram.ProcessDiagramGenerator;
import com.sirmaenterprise.sep.content.idoc.ContentNode;
import com.sirmaenterprise.sep.content.idoc.Widget;
import com.sirmaenterprise.sep.content.idoc.WidgetConfiguration;
import com.sirmaenterprise.sep.export.renders.html.table.HtmlTableBuilder;
import com.sirmaenterprise.sep.export.renders.html.table.HtmlValueImageBuilder;

/**
 * Implementation for exporting business-process-diagram-widget for export to Word.
 * 
 * @author simeon iliev
 */
@ApplicationScoped
@Extension(target = IdocRenderer.PLUGIN_NAME, order = 32)
public class BPMWidgetRenderer extends BaseRenderer {

	private static final String BPM_WIDGET_NAME = "business-process-diagram-widget";

	private static final String NO_SELECTION = "widgets.process.empty";
	
	private static final Logger LOGGER = LoggerFactory.getLogger(BPMWidgetRenderer.class);
	
	@Inject
	private CamundaBPMNService processService;

	@Inject
	private RepositoryService repositoryService;
	
	@Override
	public boolean accept(ContentNode node) {
		
		if (node.isWidget() && BPM_WIDGET_NAME.equals(((Widget) node).getName())) {
			return true;
		}

		return false;
	}

	@Override
	public Element render(String currentInstanceId, ContentNode node) {
		Widget processWidget = (Widget) node;
		WidgetConfiguration configuration = processWidget.getConfiguration();
		JsonObject jsonConfiguration = IdocRenderer.toJson(configuration);
		List<Instance> selectedInstancesFromCriteria = (List<Instance>) getSelectedInstances(currentInstanceId, configuration.getSelectionMode().toString(), jsonConfiguration);
		// get the widget title
		String widgetTitle = IdocRenderer.getWidgetTitle(jsonConfiguration);
		// create table
		HtmlTableBuilder tableBuilder = new HtmlTableBuilder(widgetTitle);
		if (selectedInstancesFromCriteria.isEmpty()) {
			HtmlTableBuilder.addNoResultRow(tableBuilder, NO_SELECTION);
			return tableBuilder.build();
		}		
		// We get the first element of the results.
		Instance processInstance = selectedInstancesFromCriteria.get(0);
		if(CamundaBPMNService.isProcess(processInstance)) {
			try {
				BufferedImage srcImage = loadImage(processInstance);
				ImageIO.write(srcImage, "jpg", new File("Test.jpg"));
				HtmlValueImageBuilder htmlValueImageBuilder = new HtmlValueImageBuilder(srcImage, processInstance.get("title").toString());
				tableBuilder.addTdValue(0, 0, htmlValueImageBuilder);
			} catch (IOException e) {
				LOGGER.warn(e.getMessage(), e);
				return new HtmlTableBuilder(widgetTitle).build();
			}	
		} else {
			return new HtmlTableBuilder(widgetTitle).build();
		}
		
		return tableBuilder.build();
	}

	/**
	 * Retrieves the definition of the instance and creates a diagram of the process.
	 * 
	 * @param processInstance the instance we want to add to the diagram.
	 * 
	 * @return the buffered image of the diagram.
	 * @throws IOException
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
			return loadInstances(Arrays.asList(currentInstanceId));
		}
		if (MANUALLY.equalsIgnoreCase(selectionMode)) {
			if (jsonConfiguration.containsKey(SELECTED_OBJECTS)) {
				Collection<Serializable> selectedObjects = new ArrayList<>();
				for (Serializable selectdObject : JSON.jsonToList(jsonConfiguration.getJsonArray(SELECTED_OBJECTS))) {
					selectedObjects.add((String) selectdObject);
				}
				return loadInstances(selectedObjects);
			}
			return Collections.emptyList();
		}
		return getSelectedInstancesFromCriteria(currentInstanceId, jsonConfiguration);
	}
}