package com.sirmaenterprise.sep.bpm.camunda.service.custom;

import static org.camunda.bpm.container.impl.deployment.Attachments.PROCESSES_XML_RESOURCES;

import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.camunda.bpm.application.impl.metadata.ProcessArchiveXmlImpl;
import org.camunda.bpm.application.impl.metadata.ProcessesXmlImpl;
import org.camunda.bpm.application.impl.metadata.spi.ProcessArchiveXml;
import org.camunda.bpm.application.impl.metadata.spi.ProcessesXml;
import org.camunda.bpm.container.impl.ContainerIntegrationLogger;
import org.camunda.bpm.container.impl.metadata.ProcessEngineXmlImpl;
import org.camunda.bpm.container.impl.spi.DeploymentOperation;
import org.camunda.bpm.container.impl.spi.DeploymentOperationStep;
import org.camunda.bpm.engine.impl.ProcessEngineLogger;

import com.sirmaenterprise.sep.bpm.camunda.configuration.SepProcessEngineConfigurator;

/**
 * Constructs new step for single tenant based on the tenant info.
 * 
 * @author bbanchev
 */
public class RuntimeProcessesStep extends DeploymentOperationStep {
	private static final ContainerIntegrationLogger LOG = ProcessEngineLogger.CONTAINER_INTEGRATION_LOGGER;

	private ProcessEngineXmlImpl processEngineXml;
	private ProcessArchiveXmlImpl processArchiveXml;
	private String engineName;

	/**
	 * Constructs new xml bean model based on provided engine name
	 * 
	 * @param engineName
	 *            the engine name to use
	 */
	public RuntimeProcessesStep(String engineName) {
		this.engineName = engineName;
		init();
	}

	private void init() {
		processEngineXml = new ProcessEngineXmlImpl();
		processEngineXml.setName(engineName);
		processEngineXml.setConfigurationClass(SepProcessEngineConfigurator.class.getName());
		processEngineXml.setPlugins(new LinkedList<>());
		processEngineXml.setProperties(new HashMap<>());

		processArchiveXml = new ProcessArchiveXmlImpl();
		processArchiveXml.setProcessEngineName(processEngineXml.getName());
		processArchiveXml.setName(processEngineXml.getName());
		processArchiveXml.setProcessResourceNames(new LinkedList<>());
		processArchiveXml.setProperties(new HashMap<>());
		processArchiveXml.getProperties().put(ProcessArchiveXml.PROP_IS_SCAN_FOR_PROCESS_DEFINITIONS,
				Boolean.FALSE.toString());
		processArchiveXml.getProperties().put(ProcessArchiveXml.PROP_IS_DELETE_UPON_UNDEPLOY, Boolean.FALSE.toString());
	}

	@Override
	public String getName() {
		return "Generate structure like one contained in processes.xml deployment descriptor files.";
	}

	@Override
	public void performOperationStep(DeploymentOperation operationContext) {
		Map<URL, ProcessesXml> generatedResources = null; // NOSONAR
		try {
			generatedResources = Collections.singletonMap(new URL("https://sep.ses.bg/" + engineName),
					new ProcessesXmlImpl(Collections.singletonList(processEngineXml),
							Collections.singletonList(processArchiveXml)));
			// attach parsed metadata
			operationContext.addAttachment(PROCESSES_XML_RESOURCES, generatedResources);
		} catch (Exception e) {
			LOG.exceptionWhileInitializingProcessengine(e);
		}
	}

}
