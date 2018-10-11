package com.sirma.itt.seip.instance.template.update;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertNotNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.domain.security.ActionTypeConstants;
import com.sirma.itt.seip.instance.DomainInstanceService;
import com.sirma.itt.seip.instance.InstanceSaveContext;
import com.sirma.itt.seip.instance.lock.LockInfo;
import com.sirma.itt.seip.instance.lock.LockService;
import com.sirma.itt.seip.template.TemplateService;
import com.sirma.sep.content.Content;
import com.sirma.sep.content.ContentInfo;
import com.sirma.sep.content.InstanceContentService;
import com.sirma.sep.content.idoc.Idoc;
import com.sirma.sep.content.idoc.SectionNode;
import com.sirma.sep.content.idoc.Sections;

public class InstanceTemplateUpdaterTest {

	private final static String TEMPLATE_VERSION = "1.23";
	private final static String INSTANCE_ID = "testId";
	private final static String TEMPLATE_ID = "template1";

	private Idoc savedView;
	private InstanceTemplateUpdateItem result;

	@InjectMocks
	private InstanceTemplateUpdater updater;

	@Mock
	private DomainInstanceService domainInstanceService;

	@Mock
	private LockService lockService;

	@Mock
	private InstanceContentService instanceContentService;

	@Mock
	private TemplateService templateService;

	@Captor
	private ArgumentCaptor<InstanceSaveContext> instanceSaveCaptor;

	@Before
	public void init() {
		updater = new InstanceTemplateUpdater();
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void should_ReturnNoResult_When_ThereIsNoChangeBetweenTheTemplateAndExistingView() {
		withInstance("equal/instance-view.xml");

		withTemplate("equal/template.xml");

		expectNoResult();
	}

	@Test
	public void should_ReturnNoResult_When_TheInstanceViewCannotBeLoaded() {
		withInstance("non-existing.xml");

		withTemplate("equal/template.xml");

		expectNoResult();
	}

	@Test
	public void should_ReplaceLockedSectionsInTheInstance_With_TheLockedSectionsFromTheTemplate() {
		withInstance("updated-locked-tabs/instance-view.xml");

		withTemplate("updated-locked-tabs/template.xml");

		expectMergedTemplateToHaveSections("ct-tab", "prp-tab");

		expectSectionToContain("ct-tab", "info-box");
		expectSectionToContain("prp-tab", "data-table");
	}

	@Test
	public void should_RemoveSectionFromInstanceView_When_SectionIsRemovedFromTemplate() {
		withInstance("renamed-and-new-tabs/instance-view.xml");

		withTemplate("renamed-and-new-tabs/template.xml");

		expectMergedTemplateToHaveSections("ct-tab", "rel-obj-tab", "properties-tab", "todo-tab");

		expectSectionToHaveTitle("rel-obj-tab", "Related Objects");
	}

	@Test
	public void should_UpdateLockedSectionsInInstance_When_TheSameSectionIsUnclockedInTemplate() {
		withInstance("mismatching-locked-tabs/instance-view.xml");

		withTemplate("mismatching-locked-tabs/template.xml");

		expectMergedTemplateToHaveSections("ct-tab", "prp-tab");

		expectSectionToContain("ct-tab", "image");
		expectSectionToContain("prp-tab", "object-data");
	}

	@Test
	public void should_RestoreOrderOfMovedSections() {
		withInstance("reordered-sections/instance-view.xml");

		withTemplate("reordered-sections/template.xml");

		expectMergedTemplateToHaveSections("tab1", "tab2", "tab3");
	}

	@Test
	public void should_NotAddAnAlreadyAddedUserDefinedTab() {
		withInstance("user-defined-in-template/instance-view.xml");

		withTemplate("user-defined-in-template/template.xml");

		expectNoResult();
	}


	@Test
	public void should_UpdateContentAndTemplateVersion() throws Exception {
		List<Object> items = new ArrayList<>();

		InstanceTemplateUpdateItem item1 = new InstanceTemplateUpdateItem("instance1",
				"<sections><section data-id=\"section1\"></section></sections>");
		items.add(item1);

		InstanceTemplateUpdateItem item2 = new InstanceTemplateUpdateItem("instance2",
				"<sections><section data-id=\"section2\"></section></sections>");
		items.add(item2);

		withInstance("instance1", false);
		withInstance("instance2", false);

		updater.saveItem(item1, TEMPLATE_VERSION, ActionTypeConstants.UPDATE_INSTANCE_TEMPLATE);

		verify(domainInstanceService, times(1)).save(instanceSaveCaptor.capture());

		verifyInstanceSaved(0, "instance1", "section1");
	}

	@Test
	public void should_SkipLockedInstances() throws Exception {
		List<Object> items = new ArrayList<>();

		InstanceTemplateUpdateItem item1 = new InstanceTemplateUpdateItem("instance1",
				"<sections><section data-id=\"section1\"></section></sections>");
		items.add(item1);

		InstanceTemplateUpdateItem item2 = new InstanceTemplateUpdateItem("instance2",
				"<sections><section data-id=\"section2\"></section></sections>");
		items.add(item2);

		withInstance("instance1", true);
		withInstance("instance2", false);

		updater.saveItem(item1, TEMPLATE_VERSION, ActionTypeConstants.UPDATE_SINGLE_INSTANCE_TEMPLATE);

		verify(domainInstanceService, times(0)).save(instanceSaveCaptor.capture());

		assertEquals(0, instanceSaveCaptor.getAllValues().size());
	}

	private void expectMergedTemplateToHaveSections(String... expectedSections) {
		performOperation();
		Sections sections = savedView.getSections();

		assertEquals(expectedSections.length, sections.count());

		for (int i = 0; i < expectedSections.length; i++) {
			assertEquals(expectedSections[i], sections.getSectionByIndex(i).getId());

			assertTrue(result.getMergedContent().contains(expectedSections[i]));
		}
	}

	private void expectSectionToContain(String sectionId, String content) {
		assertThat(getSection(sectionId).asHtml(), containsString(content));
	}

	private void expectSectionToHaveTitle(String sectionId, String title) {
		assertEquals(title, getSection(sectionId).getTitle());
	}

	private SectionNode getSection(String sectionId) {
		performOperation();

		Optional<SectionNode> section = savedView.getSections().getSectionById(sectionId);
		return section.get();
	}

	private void performOperation() {
		if (savedView == null) {
			try {

				result = updater.updateItem(INSTANCE_ID, TEMPLATE_ID);

				if (result != null) {
					assertEquals(INSTANCE_ID, result.getInstanceId());

					savedView = Idoc.parse(result.getMergedContent());
				}
			} catch (Exception e) {
				fail(e.getMessage());
			}
		}
	}

	private void withInstance(String source) {
		ContentInfo info = mock(ContentInfo.class);

		InputStream resourceStream = InstanceTemplateUpdateJobProcessorTest.class.getResourceAsStream(source);

		try {
			if (resourceStream != null) {
				when(info.asString()).thenReturn(IOUtils.toString(resourceStream));
			} else {
				when(info.asString()).thenThrow(new IOException());
			}
		} catch (IOException e) {
			fail(e.getMessage());
		}

		when(info.exists()).thenReturn(true);

		when(instanceContentService.getContent(INSTANCE_ID, Content.PRIMARY_VIEW)).thenReturn(info);
	}

	private void expectNoResult() {
		try {
			performOperation();

			assertNull(result);
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}

	private void withTemplate(String source) {
		try {
			String content = IOUtils.toString(InstanceTemplateUpdateJobProcessorTest.class.getResourceAsStream(source));
			when(templateService.getContent(TEMPLATE_ID)).thenReturn(content);
		} catch (IOException e) {
			fail("Cannot extract file content: " + e);
		}
	}

	private void verifyInstanceSaved(int executionNumber, String expectedInstanceId, String expectedContent) {
		InstanceSaveContext savedContext = instanceSaveCaptor.getAllValues().get(executionNumber);
		assertNotNull(savedContext.getDisableValidationReason(), "Instance validation should be disabled");
		Instance savedInstance = savedContext.getInstance();

		assertEquals(ActionTypeConstants.UPDATE_INSTANCE_TEMPLATE, savedContext.getOperation().getOperation());
		assertEquals(expectedInstanceId, savedInstance.getId());
		assertEquals(TEMPLATE_VERSION, savedInstance.getString(DefaultProperties.TEMPLATE_VERSION));
		assertTrue(savedInstance.getString(DefaultProperties.TEMP_CONTENT_VIEW).contains(expectedContent));
	}

	private EmfInstance withInstance(String id, boolean locked) {
		EmfInstance instance = spy(EmfInstance.class);
		instance.setId(id);
		instance.setProperties(new HashMap<>());

		InstanceReference mockReference = mock(InstanceReference.class);
		doReturn(mockReference).when(instance).toReference();

		when(domainInstanceService.loadInstance(eq(id))).thenReturn(instance);

		LockInfo lockInfo = new LockInfo(null, locked ? "user1" : null, null, null, null);
		when(lockService.lockStatus(eq(instance.toReference()))).thenReturn(lockInfo);

		return instance;
	}

}
