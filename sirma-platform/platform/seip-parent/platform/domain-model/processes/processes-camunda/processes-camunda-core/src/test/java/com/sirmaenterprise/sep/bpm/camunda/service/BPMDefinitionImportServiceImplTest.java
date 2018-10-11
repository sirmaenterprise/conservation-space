package com.sirmaenterprise.sep.bpm.camunda.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileInputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.enterprise.inject.Instance;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.repository.Deployment;
import org.camunda.bpm.engine.repository.DeploymentBuilder;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.domain.rest.EmfApplicationException;
import com.sirma.itt.seip.io.TempFileProvider;
import com.sirma.itt.seip.monitor.Statistics;
import com.sirma.itt.seip.time.TimeTracker;


/**
 * Tests {@link BPMDefinitionImportServiceImpl}.
 * 
 * @author Vilizar Tsonev
 */
public class BPMDefinitionImportServiceImplTest {

	@InjectMocks
	private BPMDefinitionImportServiceImpl bPMDefinitionImportService;

	@Mock
	private TempFileProvider tempFileProvider;

	@Mock
	private Instance<ProcessEngine> instance;

	@Mock
	private DeploymentBuilder deploymentBuilder;

	@Mock
	private Statistics statistics;

	@Before
	public void init() {
		MockitoAnnotations.initMocks(this);

		when(deploymentBuilder.getResourceNames()).thenReturn(Collections.emptyList());
		when(deploymentBuilder.deploy()).thenReturn(mock(Deployment.class));

		RepositoryService repoService = mock(RepositoryService.class);
		when(repoService.createDeployment()).thenReturn(deploymentBuilder);

		ProcessEngine engine = mock(ProcessEngine.class);
		when(engine.getRepositoryService()).thenReturn(repoService);
		when(engine.getName()).thenReturn("testEngine");

		when(instance.get()).thenReturn(engine);
		when(statistics.createTimeStatistics(any(), anyString())).thenReturn(TimeTracker.createAndStart());
	}

	@Test
	public void should_Pass_Loaded_Files_To_Deployment_Builder() {
		bPMDefinitionImportService.importDefinitions(getTestFilesDirectory());
		verifyBPMFilesPersisted("test_bmpm1.bpmn", "test_bmpm2.bpmn");
	}

	@Test
	public void should_Deploy_When_BPM_Files_Successfully_Loaded() {
		when(deploymentBuilder.getResourceNames()).thenReturn(Arrays.asList("test_bmpm1.bpmn", "test_bmpm2.bpmn"));
		bPMDefinitionImportService.importDefinitions(getTestFilesDirectory());
		verify(deploymentBuilder).deploy();
	}

	@Test
	public void should_Destroy_Process_Engine_Instance_After_Use() {
		bPMDefinitionImportService.importDefinitions(getTestFilesDirectory());
		verify(instance).destroy(any(ProcessEngine.class));
	}

	@Test(expected = EmfApplicationException.class)
	public void should_Throw_Exception_If_Files_Not_Found() {
		bPMDefinitionImportService.importDefinitions(getTestFilesDirectory() + "/someRandomDir123asdfg");
		verify(instance).destroy(any(ProcessEngine.class));
	}

	private void verifyBPMFilesPersisted(String... filenames) {
		List<String> expectedFilenames = Arrays.asList(filenames);
		
		ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
		verify(deploymentBuilder, times(expectedFilenames.size())).addInputStream(captor.capture(),
				any(FileInputStream.class));
		assertEquals(expectedFilenames, captor.getAllValues());
	}

	private static String getTestFilesDirectory() {
		String resourcesDirectory = "src" + File.separator + "test" + File.separator + "resources";
		return new File(resourcesDirectory).getAbsolutePath() + File.separator
				+ BPMDefinitionImportServiceImplTest.class.getPackage().getName().replace('.', File.separatorChar);
	}
}
