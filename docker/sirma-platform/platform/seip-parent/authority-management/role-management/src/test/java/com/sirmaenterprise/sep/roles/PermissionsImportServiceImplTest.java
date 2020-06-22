package com.sirmaenterprise.sep.roles;

import static com.sirma.itt.seip.permissions.SecurityModel.BaseRoles.CONSUMER;
import static com.sirma.itt.seip.permissions.SecurityModel.BaseRoles.CONTRIBUTOR;
import static com.sirma.itt.seip.permissions.SecurityModel.BaseRoles.CREATOR;
import static com.sirma.itt.seip.permissions.SecurityModel.BaseRoles.MANAGER;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.dozer.DozerBeanMapper;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.definition.label.LabelDefinition;
import com.sirma.itt.seip.definition.label.LabelService;
import com.sirma.itt.seip.domain.definition.label.LabelProvider;
import com.sirma.itt.seip.domain.security.Action;
import com.sirma.itt.seip.mapping.ObjectMapper;
import com.sirma.itt.seip.mapping.dozer.DozerObjectMapper;
import com.sirma.itt.seip.permissions.action.ActionProvider;
import com.sirma.itt.seip.permissions.action.EmfAction;
import com.sirma.itt.seip.permissions.action.RoleActionFilterService;
import com.sirma.itt.seip.permissions.role.Role;
import com.sirma.itt.seip.permissions.role.RoleIdentifier;
import com.sirma.itt.seip.permissions.role.RoleProviderExtension;
import com.sirma.itt.seip.util.ReflectionUtils;
import com.sirmaenterprise.sep.roles.provider.SecurityDozerProvider;
import com.sirmaenterprise.sep.roles.validation.RolesValidator;
import com.sirmaenterprise.sep.roles.validation.RolesValidatorTest;

public class PermissionsImportServiceImplTest {

	@InjectMocks
	private PermissionsImportServiceImpl permissionsImportService;

	@Mock
	private DefaultRolesProvider defaultRolesProvider;

	@Spy
	private ObjectMapper mapper = new DozerObjectMapper(
			new DozerBeanMapper(Collections.singletonList(SecurityDozerProvider.DOZER_SECURITY_MAPPING_XML)));

	@Mock
	private LabelProvider labelProvider;

	@InjectMocks
	private ExternalRoleParser roleParser;

	@Mock
	private ActionProvider actionProvider;

	@Mock
	private LabelService labelService;

	@Spy
	private List<ActionProvider> actionProviders = new ArrayList<>();

	@Spy
	private List<RoleProviderExtension> roleProviders = new ArrayList<>();

	@Rule
	public TemporaryFolder tempFolder = new TemporaryFolder();

	@Mock
	private RoleManagement roleManagement;

	@Mock
	private RolesValidator roleValidator;

	@Mock
	private RoleActionFilterService filterService;

	@Before
	public void init() {
		MockitoAnnotations.initMocks(this);
		// set it using a reflection to be able to create a broad-level component test
		ReflectionUtils.setFieldValue(permissionsImportService, "roleParser", roleParser);
		stubAdditionalClasses();
	}

	@Test
	public void should_Invoke_RoleValidator_With_The_Loaded_Definition_Files() throws IOException {
		withRoleDefinitions("validData/consumer.xml", "validData/contributor.xml");

		permissionsImportService.validate(tempFolder.getRoot().getPath());

		ArgumentCaptor<List<File>> captor = ArgumentCaptor.forClass(List.class);
		verify(roleValidator).validate(captor.capture());
		assertEquals(2, captor.getValue().size());
	}

	@Test
	public void should_Correctly_Persist_All_Default_Plus_All_Loaded_From_FS_Roles() throws IOException {
		withDefaultRoles(MANAGER, CREATOR);
		withRoleDefinitions("validData/consumer.xml", "validData/contributor.xml");

		permissionsImportService.importPermissions(tempFolder.getRoot().getPath());

		verifyRolesPersisted(MANAGER, CREATOR, CONSUMER, CONTRIBUTOR);
	}

	@Test
	public void should_Correctly_Persist_All_Loaded_Actions() throws IOException {
		withDefaultRoles(MANAGER);
		withRoleDefinitions("validData/consumer.xml", "validData/contributor.xml");

		permissionsImportService.importPermissions(tempFolder.getRoot().getPath());

		verifyActionsPersisted("viewDetails", "download", "replyComment", "create", "clone");
	}

	@Test
	public void should_Copy_Action_Label_And_Tooltip() throws Exception {
		withRoleDefinitions("validData/consumer_single_action.xml");

		EmfAction viewDetails = new EmfAction("viewDetails");
		viewDetails.setLabel(viewDetails.getActionId());
		viewDetails.setTooltip(viewDetails.getActionId());
		withExistingActionsInDB(viewDetails);

		LabelDefinition labelDefinition = mock(LabelDefinition.class);
		when(labelService.getLabel("viewDetails")).thenReturn(labelDefinition);


		permissionsImportService.importPermissions(tempFolder.getRoot().getPath());

		ArgumentCaptor<List<LabelDefinition>> argCaptor = ArgumentCaptor.forClass(List.class);
		verify(labelService).saveLabels(argCaptor.capture());
		Set<String> labelIds = argCaptor.getValue()
				.stream()
				.map(LabelDefinition::getIdentifier)
				.collect(Collectors.toSet());
		assertTrue("There should be label with id 'viewDetails.label'", labelIds.contains("viewDetails.label"));
		assertTrue("There should be label with id 'viewDetails.tooltip'", labelIds.contains("viewDetails.tooltip"));
	}

	@Test
	public void should_Disable_Not_Defined_Actions() throws IOException {
		withRoleDefinitions("validData/consumer.xml");

		EmfAction viewDetails = new EmfAction("viewDetails");
		viewDetails.setLabel(viewDetails.getActionId());
		viewDetails.setTooltip(viewDetails.getActionId());
		withExistingActionsInDB(viewDetails);

		permissionsImportService.importPermissions(tempFolder.getRoot().getPath());

		ArgumentCaptor<Collection> argCaptor = ArgumentCaptor.forClass(Collection.class);
		verify(roleManagement).saveActions(argCaptor.capture());
		List<ActionDefinition> actions = new ArrayList<>(argCaptor.getValue());
		assertTrue(actions.size() == 3);
		assertTrue(actions.get(0).isEnabled());
		assertFalse(actions.get(1).isEnabled());
		assertFalse(actions.get(2).isEnabled());
	}

	@Test
	public void should_DeleteRoleActionMappings_BeforeImport() throws IOException {
		withRoleDefinitions("validData/consumer.xml");

		permissionsImportService.importPermissions(tempFolder.getRoot().getPath());

		verify(roleManagement).deleteRoleActionMappings();
	}

	@Test
	public void should_Import_All_Actions_When_Definitions_Change() {
		EmfAction viewDetails = new EmfAction("viewDetails");
		EmfAction replyComment = new EmfAction("replyComment");
		EmfAction download = new EmfAction("download");
		withExistingActionsInDB(viewDetails, replyComment, download);

		permissionsImportService.onDefinitionChange(null);

		verifyActionsPersisted("viewDetails", "replyComment", "download");
	}

	@Test(expected = IllegalArgumentException.class)
	public void should_ThrowException_When_PermissionDefinition_ContainsNotExisting_ActionFilter() throws IOException {
		withActionFilters("LOCKEDBY", "CREATEDBY");
		withRoleDefinitions("validData/collaborator.xml");

		permissionsImportService.importPermissions(tempFolder.getRoot().getPath());
	}

	@Test
	public void should_Import_PermissionDefinition_When_ContainsExisting_ActionFilter() throws IOException {
		withActionFilters("LOCKEDBY", "CREATEDBY", "NOT_MAILBOX_SUPPORTABLE");
		withRoleDefinitions("validData/collaborator.xml");

		permissionsImportService.importPermissions(tempFolder.getRoot().getPath());

		verifyActionsPersisted("contactPerson", "editDetails", "download", "composeEmail", "editComment",
				"deleteComment", "suspendComment", "restartComment", "uploadNewVersion");
	}

	private void verifyRolesPersisted(RoleIdentifier... expectedRoleIds) {
		ArgumentCaptor<List<RoleDefinition>> captor = ArgumentCaptor.forClass(List.class);
		verify(roleManagement).saveRoles(captor.capture());
		verify(roleManagement).updateRoleActionMappings(any());

		Set<String> persistedRoleIds = captor
				.getValue()
					.stream()
					.map(RoleDefinition::getId)
					.collect(Collectors.toSet());

		Set<String> expected = Arrays
				.asList(expectedRoleIds)
					.stream()
					.map(RoleIdentifier::getIdentifier)
					.collect(Collectors.toSet());

		assertEquals(expected, persistedRoleIds);
	}

	private void verifyActionsPersisted(String... expectedActionIds) {
		ArgumentCaptor<List<ActionDefinition>> captor = ArgumentCaptor.forClass(List.class);
		verify(roleManagement).saveActions(captor.capture());

		Set<String> persistedActionIds = captor
				.getValue()
					.stream()
					.map(ActionDefinition::getId)
					.collect(Collectors.toSet());

		Set<String> expected = new HashSet<>(Arrays.asList(expectedActionIds));

		assertEquals(expected, persistedActionIds);
	}

	private void withDefaultRoles(RoleIdentifier... roles) {
		Map<RoleIdentifier, Role> defaultRoles = new HashMap<>();
		for (RoleIdentifier roleId : roles) {
			RoleProviderExtension.getOrCreateRole(defaultRoles, roleId);
		}
		when(defaultRolesProvider.getDefaultRoles()).thenReturn(defaultRoles);
	}

	private void withExistingActionsInDB(Action... actions) {
		Map<String, Action> actionsMap = CollectionUtils.toIdentityMap(Arrays.asList(actions), Action::getActionId);
		when(actionProvider.provide()).thenReturn(actionsMap);
	}

	private void withRoleDefinitions(String... fileNames) throws IOException {
		for (String fileName : fileNames) {
			// test files are located in the validation package, so that's why RolesValidatorTest is used here
			File file = new File(RolesValidatorTest.class
					.getClassLoader()
						.getResource(RolesValidatorTest.class.getPackage().getName().replace('.', '/') + "/" + fileName)
						.getFile());
			File temporaryTestFile = tempFolder.newFile(UUID.randomUUID().toString());
			FileUtils.copyFile(file, temporaryTestFile);
		}
	}

	private void withActionFilters(String... filters) {
		when(filterService.getFilters()).thenReturn(new HashSet<>(Arrays.asList(filters)));
	}

	private void stubAdditionalClasses() {
		actionProviders.clear();
		actionProviders.add(actionProvider);
		// Not needed for the test. Just leave it empty
		roleProviders.clear();
		when(labelService.getLabel(anyString())).thenReturn(mock(LabelDefinition.class));
	}
}
