package com.sirma.sep.emf.hash;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.dozer.DozerBeanMapper;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import com.sirma.itt.seip.Message;
import com.sirma.itt.seip.definition.DefinitionService;
import com.sirma.itt.seip.definition.dozer.DefinitionsDozerProvider;
import com.sirma.itt.seip.definition.jaxb.Definition;
import com.sirma.itt.seip.definition.model.GenericDefinitionImpl;
import com.sirma.itt.seip.definition.schema.BaseSchemas;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.instance.dozer.CommonDozerProvider;
import com.sirma.itt.seip.instance.script.ScriptImporter;
import com.sirma.itt.seip.mapping.ObjectMapper;
import com.sirma.itt.seip.mapping.dozer.DozerObjectMapper;
import com.sirma.itt.seip.script.GlobalBindingsExtension;
import com.sirma.itt.seip.testutil.io.FileTestUtils;
import com.sirma.sep.xml.JAXBHelper;

/**
 * The Class BaseScriptDefinitionTest.
 *
 * @author BBonev
 */
public abstract class BaseScriptDefinitionTest extends CmfScriptTest {

	@Mock
	protected DefinitionService definitionService;

	@InjectMocks
	protected ScriptImporter scriptImporter;

	protected ObjectMapper mapper;

	private File tempFolder;

	@Override
	@BeforeMethod
	public void beforeMethod() {
		super.beforeMethod();

		List<String> files = new LinkedList<>();
		files.add(DefinitionsDozerProvider.DOZER_COMMON_MAPPING_XML);
		files.add(CommonDozerProvider.DOZER_COMMON_MAPPING_XML);
		files.add(DefinitionsDozerProvider.DOZER_GENERIC_DEFINITION_MAPPING_XML);

		mapper = new DozerObjectMapper(new DozerBeanMapper(files));
	}

	@Override
	protected void provideBindings(List<GlobalBindingsExtension> bindingsExtensions) {
		super.provideBindings(bindingsExtensions);
		bindingsExtensions.add(scriptImporter);
	}

	protected List<DefinitionModel> loadDefinitions(Class<?> base, String... paths) {
		File temp = FileTestUtils.copyResourcesToTempFolder(base, Arrays.asList(paths));
		try {
			File[] files = temp.listFiles();
			List<DefinitionModel> models = new ArrayList<>(files.length);
			for (File file : files) {
				List<Message> messages = new LinkedList<>();
				assertTrue(JAXBHelper.validateFile(file, BaseSchemas.GENERIC_DEFINITION, messages),
						messages.toString());
				assertTrue(messages.isEmpty());

				Definition definition = JAXBHelper.load(file, Definition.class);
				GenericDefinitionImpl genericDefinition = mapper.map(definition, GenericDefinitionImpl.class);
				models.add(genericDefinition);
			}
			return models;
		} finally {
			FileTestUtils.cleanDirectory(temp);
		}
	}

	protected void mockDefinitionService(Class<?> base, String... paths) {
		tempFolder = FileTestUtils.copyResourcesToTempFolder(base, Arrays.asList(paths));
		File[] files = tempFolder.listFiles();
		for (File file : files) {
			Definition definition = JAXBHelper.load(file, Definition.class);
			GenericDefinitionImpl genericDefinition = mapper.map(definition, GenericDefinitionImpl.class);
			when(definitionService.find(eq(genericDefinition.getIdentifier()))).thenReturn(genericDefinition);
		}
	}

	protected void mockDefinitionService(String... paths) {
		tempFolder = FileTestUtils.copyFilesToTempFolder(Arrays.asList(paths));
		File[] files = tempFolder.listFiles();
		for (File file : files) {
			Definition definition = JAXBHelper.load(file, Definition.class);
			GenericDefinitionImpl genericDefinition = mapper.map(definition, GenericDefinitionImpl.class);
			when(definitionService.find(eq(genericDefinition.getIdentifier()))).thenReturn(genericDefinition);
		}
	}

	@AfterMethod
	public void cleanTempDir() {
		if (tempFolder != null) {
			FileTestUtils.cleanDirectory(tempFolder);
		}
	}
}
