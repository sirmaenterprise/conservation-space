package com.sirma.itt.seip.template.db;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.db.DbDao;
import com.sirma.itt.seip.instance.dao.InstanceDao;
import com.sirma.itt.seip.resources.User;
import com.sirma.itt.seip.security.context.SecurityContext;
import com.sirma.itt.seip.template.Template;
import com.sirma.itt.seip.template.TemplateProperties;
import com.sirma.itt.seip.template.exceptions.MissingTemplateException;

/**
 * Tests the functionality of {@link TemplateDao}.
 *
 * @author Vilizar Tsonev
 */
public class TemplateDaoTest {

	@InjectMocks
	private TemplateDao templateDao;

	@Mock
	private InstanceDao instanceDao;

	@Mock
	private DbDao dbDao;

	@Mock
	private SecurityContext securityContext;

	@Test
	public void should_Filter_Found_Templates_By_Rule() {
		TemplateEntity template1 = new TemplateEntity();
		template1.setTemplateId("template1");
		template1.setRule("(department == \"BA\" || department == \"ENG\")");

		TemplateEntity template2 = new TemplateEntity();
		template2.setTemplateId("template2");
		template2.setRule("(department == \"BA\" || department == \"QA\")");

		TemplateEntity template3 = new TemplateEntity();
		template3.setTemplateId("template3");

		TemplateEntity template4 = new TemplateEntity();
		template4.setTemplateId("template4");
		template4.setRule("functional == \"MDG\" && (department == \"CEG\" || department == \"ENG\")");

		withExistingTemplatesInRDB(Arrays.asList(template1, template2, template3, template4));

		Optional<Template> result = templateDao.findExistingPrimaryTemplate("sampleType", "creatable",
				"(department == \"ENG\" || department == \"BA\")", null);

		assertTrue(result.isPresent());
		assertEquals(template1.getTemplateId(), result.get().getId());
	}

	@Test
	public void should_Not_Return_Templates_With_Rules_When_No_Rule_Requested() {
		TemplateEntity template1 = new TemplateEntity();
		template1.setTemplateId("template1");
		template1.setRule("(department == \"BA\" || department == \"ENG\")");

		TemplateEntity template2 = new TemplateEntity();
		template2.setTemplateId("template2");
		template2.setRule("(department == \"BA\" || department == \"QA\")");

		TemplateEntity template3 = new TemplateEntity();
		template3.setTemplateId("template3");
		template3.setPrimary(Boolean.TRUE);


		withExistingTemplatesInRDB(Arrays.asList(template1, template2, template3));

		Optional<Template> result = templateDao
				.findExistingPrimaryTemplate("sampleType", "creatable", null, null);

		assertTrue(result.isPresent());
		assertEquals(template3.getTemplateId(), result.get().getId());
	}

	@Test
	public void should_Return_Empty_Optional_When_No_Template_Matches_The_Rule() {
		TemplateEntity template1 = new TemplateEntity();
		template1.setTemplateId("template1");
		template1.setRule("(department == \"BA\" || department == \"ENG\")");

		TemplateEntity template2 = new TemplateEntity();
		template2.setTemplateId("template2");
		template2.setRule("(department == \"BA\" || department == \"QA\")");

		TemplateEntity template3 = new TemplateEntity();
		template3.setTemplateId("template3");

		withExistingTemplatesInRDB(Arrays.asList(template1, template2, template3));

		Optional<Template> result = templateDao.findExistingPrimaryTemplate("sampleType", "creatable",
				"functional == \"MDG\" && (department == \"CEG\" || department == \"ENG\")", null);

		assertFalse(result.isPresent());
	}

	@Test
	public void should_Not_Find_The_Template_Passed_As_New_Primary() {
		TemplateEntity template1 = new TemplateEntity();
		template1.setTemplateId("template1");
		template1.setRule("(department == \"BA\" || department == \"ENG\")");
		template1.setCorrespondingInstance("instance1");

		TemplateEntity template2 = new TemplateEntity();
		template2.setTemplateId("template2");
		template2.setRule("(department == \"BA\" || department == \"QA\")");
		template2.setCorrespondingInstance("instance2");

		TemplateEntity template3 = new TemplateEntity();
		template3.setTemplateId("template3");
		template3.setRule("functional == \"MDG\" && (department == \"CEG\" || department == \"ENG\")");
		template3.setCorrespondingInstance("instance3");

		withExistingTemplatesInRDB(Arrays.asList(template1, template2, template3));

		Optional<Template> result = templateDao.findExistingPrimaryTemplate("sampleType", "creatable",
				"functional == \"MDG\" && (department == \"CEG\" || department == \"ENG\")", "instance3");

		assertFalse(result.isPresent());
	}

	@Test
	public void should_Correctly_Convert_Retrieved_Entity_To_Template() {
		TemplateEntity entity = new TemplateEntity();
		entity.setTemplateId("testTemplate");
		entity.setTitle("Test Template");
		entity.setPrimary(Boolean.TRUE);
		entity.setGroupId("sampleForType");
		entity.setPurpose("creatable");
		entity.setRule("department == sampleRule");
		entity.setPublishedInstanceVersion("1.11");
		entity.setCorrespondingInstance("emf:Instance");

		withExistingTemplatesInRDB(Collections.singletonList(entity));

		Template expected = new Template();
		expected.setId("testTemplate");
		expected.setTitle("Test Template");
		expected.setPrimary(true);
		expected.setForType("sampleForType");
		expected.setPurpose("creatable");
		expected.setRule("department == sampleRule");
		expected.setPublishedInstanceVersion("1.11");
		expected.setCorrespondingInstance("emf:Instance");

		Template actual = templateDao.getTemplate("test");
		assertEquals(expected, actual);
	}

	@Test(expected = IllegalArgumentException.class)
	public void should_Throw_Exception_If_Identifier_Not_Provided_When_Getting_Template() {
		templateDao.getTemplate("");
	}

	@Test
	public void should_Return_Null_When_Template_Not_found() {
		withExistingTemplatesInRDB(Collections.emptyList());
		assertNull(templateDao.getTemplate("test"));
	}

	@Test
	public void should_Save_Correct_Template_Data() {
		Template input = new Template();
		input.setId("testTemplate");
		input.setTitle("Test Template");
		input.setPrimary(true);
		input.setForType("sampleForType");
		input.setPurpose("creatable");
		input.setRule("department == sampleRule");
		input.setPublishedInstanceVersion("1.11");
		input.setCorrespondingInstance("emf:Instance");

		TemplateEntity expected = new TemplateEntity();
		expected.setId(Long.valueOf(7777));
		expected.setTemplateId("testTemplate");
		expected.setTitle("Test Template");
		expected.setPrimary(Boolean.TRUE);
		expected.setGroupId("sampleForType");
		expected.setPurpose("creatable");
		expected.setRule("department == sampleRule");
		expected.setPublishedInstanceVersion("1.11");
		expected.setCorrespondingInstance("emf:Instance");
		expected.setModifiedBy("adminUsername");

		withExistingTemplatesInRDB(Collections.singletonList(expected));

		withCurrentlyLoggedUser("adminUsername");

		templateDao.saveOrUpdate(input);

		ArgumentCaptor<TemplateEntity> captor = ArgumentCaptor.forClass(TemplateEntity.class);
		verify(dbDao).saveOrUpdate(captor.capture());

		assertEquals(expected, captor.getValue());
		assertNotNull(captor.getValue().getModifiedOn());
	}

	@Test
	public void should_Get_All_Templates_When_Requested() {
		TemplateEntity entity1 = new TemplateEntity();
		entity1.setId(Long.valueOf(1));
		TemplateEntity entity2 = new TemplateEntity();
		entity2.setId(Long.valueOf(2));
		TemplateEntity entity3 = new TemplateEntity();
		entity3.setId(Long.valueOf(3));
		TemplateEntity entity4 = new TemplateEntity();
		entity4.setId(Long.valueOf(4));
		withExistingTemplatesInRDB(Arrays.asList(entity1, entity2, entity3, entity4));

		List<Template> allTemplates = templateDao.getAllTemplates();

		assertEquals(4, allTemplates.size());
	}

	@Test
	public void should_Delete_Template() {
		TemplateEntity existing = new TemplateEntity();
		existing.setId(Long.valueOf(7777));
		withExistingTemplatesInRDB(Collections.singletonList(existing));

		templateDao.delete("test");
		verify(dbDao).delete(any(), eq(Long.valueOf(7777)));
	}

	@Test(expected = MissingTemplateException.class)
	public void should_Throw_Exception_If_Template_For_Deleting_Des_Not_Exist() {
		withExistingTemplatesInRDB(Collections.emptyList());
		templateDao.delete("test");
	}

	@Test
	public void should_Request_Templates_Only_By_ForType_When_No_Purpose_Provided() {
		TemplateEntity template1 = new TemplateEntity();
		template1.setTemplateId("1");
		template1.setTitle("First Template");
		withExistingTemplatesInRDB(Arrays.asList(template1));

		templateDao.getTemplates("someForType", null);

		ArgumentCaptor<List<Pair<String, Object>>> captor = ArgumentCaptor.forClass(List.class);
		verify(dbDao).fetchWithNamed(eq(TemplateEntity.QUERY_TEMPLATES_FOR_GROUP_ID_KEY), captor.capture());

		assertEquals(1, captor.getValue().size());
		assertEquals(TemplateProperties.GROUP_ID, captor.getValue().get(0).getFirst());
		assertEquals("someForType", captor.getValue().get(0).getSecond());
	}

	@Test
	public void should_Retrieve_Templates_By_Purpose_And_ForType_When_Both_Provided() {
		TemplateEntity template1 = new TemplateEntity();
		template1.setTemplateId("1");
		template1.setTitle("First Template");
		TemplateEntity template2 = new TemplateEntity();
		template2.setTemplateId("2");
		template2.setTitle("Second Template");

		withExistingTemplatesInRDB(Arrays.asList(template1, template2));

		List<Template> templates = templateDao.getTemplates("someForType", "creatable");

		ArgumentCaptor<List<Pair<String, Object>>> captor = ArgumentCaptor.forClass(List.class);
		verify(dbDao).fetchWithNamed(eq(TemplateEntity.QUERY_TEMPLATES_FOR_GROUP_ID_PURPOSE_KEY), captor.capture());

		// verify the search parameters are correctly constructed
		assertEquals(TemplateProperties.GROUP_ID, captor.getValue().get(0).getFirst());
		assertEquals("someForType", captor.getValue().get(0).getSecond());
		assertEquals(TemplateProperties.PURPOSE, captor.getValue().get(1).getFirst());
		assertEquals("creatable", captor.getValue().get(1).getSecond());

		assertEquals(2, templates.size());
		assertEquals("1", templates.get(0).getId());
		assertEquals("First Template", templates.get(0).getTitle());
		assertEquals("2", templates.get(1).getId());
		assertEquals("Second Template", templates.get(1).getTitle());
	}

	@Test(expected = IllegalArgumentException.class)
	public void should_Throw_Exception_If_ForType_Not_Provided_When_Getting_Templates() {
		templateDao.getTemplates(null, "creatable");
	}

	@Before
	public void init() {
		MockitoAnnotations.initMocks(this);
	}

	private void withCurrentlyLoggedUser(String userName) {
		when(securityContext.isActive()).thenReturn(Boolean.TRUE);
		User user = mock(User.class);
		when(user.getIdentityId()).thenReturn("user");
		when(securityContext.getEffectiveAuthentication()).thenReturn(user);
	}

	private void withExistingTemplatesInRDB(List<TemplateEntity> templates) {
		// if boolean values are missing, set them some default values to avoid NPEs
		templates.forEach(template -> {
			if (template.getPrimary() == null) {
				template.setPrimary(Boolean.FALSE);
			}
		});
		when(dbDao.fetchWithNamed(anyString(), any(List.class))).thenReturn(templates);
	}
}
