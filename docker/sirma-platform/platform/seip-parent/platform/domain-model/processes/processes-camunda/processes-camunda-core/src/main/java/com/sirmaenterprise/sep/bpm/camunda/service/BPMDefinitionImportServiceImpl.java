package com.sirmaenterprise.sep.bpm.camunda.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.camunda.bpm.application.impl.ProcessApplicationLogger;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.camunda.bpm.engine.repository.Deployment;
import org.camunda.bpm.engine.repository.DeploymentBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.domain.rest.EmfApplicationException;
import com.sirmaenterprise.sep.bpm.camunda.exception.CamundaIntegrationRuntimeException;

/**
 * Default implementation of {@link BPMDefinitionImportService}.
 *
 * @author Vilizar Tsonev
 */
@Singleton
public class BPMDefinitionImportServiceImpl implements BPMDefinitionImportService {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	protected static final ProcessApplicationLogger APP_LOGGER = ProcessEngineLogger.PROCESS_APPLICATION_LOGGER;

	@Inject
	private Instance<ProcessEngine> processEngineInstance;

	@Override
	public void importDefinitions(String directoryPath) {
		ProcessEngine processEngine = null;
		try {
			processEngine = processEngineInstance.get();

			List<File> definitions = loadFromPath(directoryPath);
			LOGGER.info("Initiating import of {} BPM definitions", Long.valueOf(definitions.size()));

			DeploymentBuilder deploymentBuilder = prepareDeploymentBuilder(processEngine, definitions);

			if (!deploymentBuilder.getResourceNames().isEmpty()) {
				APP_LOGGER.registrationSummary("Going to deploy BPM models \t" + deploymentBuilder.getResourceNames());

				Deployment deployment = deploymentBuilder.deploy();

				APP_LOGGER.registrationSummary("Deployed BPM models with deployment id: " + deployment.getId());
				LOGGER.info("Successfully deployed {} BPM models with deployment ID {}",
						Long.valueOf(deploymentBuilder.getResourceNames().size()), deployment.getId());
			}
		} catch (ProcessEngineException e) {
			String message = e.getCause() != null ? e.getMessage() + " " + e.getCause().getMessage() : e.getMessage();
			throw new IllegalArgumentException(message, e);
		} finally {
			processEngineInstance.destroy(processEngine);
		}
	}

	private static DeploymentBuilder prepareDeploymentBuilder(ProcessEngine processEngine, List<File> definitionFiles) {
		DeploymentBuilder deploymentBuilder = processEngine.getRepositoryService().createDeployment();
		deploymentBuilder.name(processEngine.getName());
		deploymentBuilder.enableDuplicateFiltering(true);
		try {
			deploymentBuilder.source("FS");
			for (File file : definitionFiles) {
				deploymentBuilder.addInputStream(file.getName(), new FileInputStream(file));
			}
			return deploymentBuilder;
		} catch (FileNotFoundException e) {
			throw new CamundaIntegrationRuntimeException("Failed to prepare deployment!", e);
		}
	}

	private static List<File> loadFromPath(String path) {
		try (Stream<Path> pathsStream = Files.walk(Paths.get(path))) {
			return pathsStream
					.filter(Files::isRegularFile)
					.map(Path::toFile)
					.collect(Collectors.toList());
		} catch (IOException e) {
			throw new EmfApplicationException("Failed to read BPM files from directory " + path, e);
		}
	}
}
