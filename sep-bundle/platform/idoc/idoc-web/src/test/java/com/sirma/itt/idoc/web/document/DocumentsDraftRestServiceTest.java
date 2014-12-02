package com.sirma.itt.idoc.web.document;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import javax.enterprise.inject.Instance;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.sirma.itt.cmf.beans.model.DocumentInstance;
import com.sirma.itt.cmf.beans.model.DraftInstance;
import com.sirma.itt.cmf.constants.DocumentProperties;
import com.sirma.itt.cmf.services.DraftService;
import com.sirma.itt.commons.utils.reflection.ReflectionUtils;
import com.sirma.itt.emf.converter.TypeConverter;
import com.sirma.itt.emf.definition.DictionaryService;
import com.sirma.itt.emf.definition.model.DataType;
import com.sirma.itt.emf.entity.LinkSourceId;
import com.sirma.itt.emf.exceptions.EmfRuntimeException;
import com.sirma.itt.emf.instance.model.InitializedInstance;
import com.sirma.itt.emf.instance.model.InstanceReference;
import com.sirma.itt.emf.resources.ResourceService;
import com.sirma.itt.emf.resources.ResourceType;
import com.sirma.itt.emf.rest.model.RestInstance;
import com.sirma.itt.emf.security.AuthenticationService;
import com.sirma.itt.emf.security.model.EmfUser;
import com.sirma.itt.emf.security.model.User;
import com.sirma.itt.emf.util.sanitize.ContentSanitizer;

/**
 * Unit test for {@link DocumentsDraftRestService}
 * 
 * @author yasko
 * 
 */
public class DocumentsDraftRestServiceTest {

	private DocumentsDraftRestService restService = new DocumentsDraftRestService();

	@Mock
	private ResourceService resourceService;

	@Mock
	private DictionaryService dictionaryService;

	@Mock
	private AuthenticationService authenticationService;

	@Mock
	private Instance<AuthenticationService> authenticationServiceInstance;

	@Mock
	private ContentSanitizer contentSanitizer;

	@Mock
	private TypeConverter typeConverter;

	@Mock
	private DraftService draftService;

	@Mock
	private DataType dataType;

	private User currentUser = new EmfUser("admin");

	/**
	 * Initialize instance mock before tests.
	 */
	@BeforeMethod
	public void init() {
		MockitoAnnotations.initMocks(this);

		currentUser.setId("admin");
		Mockito.when(dataType.getJavaClass()).thenAnswer(new Answer<Class<?>>() {

			@Override
			public Class<?> answer(InvocationOnMock invocation) throws Throwable {
				return DocumentInstance.class;
			}
		});

		Mockito.when(dictionaryService.getDataTypeDefinition("documentinstance")).thenReturn(
				dataType);
		ReflectionUtils.setField(restService, "dictionaryService", dictionaryService);

		Mockito.when(authenticationServiceInstance.get()).thenReturn(authenticationService);
		ReflectionUtils.setField(restService, "authenticationService",
				authenticationServiceInstance);

		Mockito.when(contentSanitizer.sanitize("dirty")).thenReturn("clean");

		ReflectionUtils.setField(restService, "resourceService", resourceService);
		ReflectionUtils.setField(restService, "typeConverter", typeConverter);
		ReflectionUtils.setField(restService, "draftService", draftService);
		ReflectionUtils.setField(restService, "contentSanitizer", contentSanitizer);
	}

	/**
	 * Test draft creation w/o user.
	 */
	@Test(expectedExceptions = EmfRuntimeException.class)
	public void testCreateDraftWithoutUser() {
		Mockito.when(authenticationService.getCurrentUser()).thenReturn(null);
		restService.create(createRestInstance("title", "content"));
	}

	/**
	 * Test draft creation w/o a converted instance.
	 */
	@Test(expectedExceptions = EmfRuntimeException.class)
	public void testCreateDraftWithoutConvertedInstance() {
		Mockito.when(authenticationService.getCurrentUser()).thenReturn(currentUser);
		Mockito.when(resourceService.loadByDbId("whatever")).thenReturn(currentUser);
		restService.create(createRestInstance("title", "content"));
	}

	/**
	 * Test draft creation w/o providing a content property.
	 */
	@Test
	public void testCreateDraftWithoutContentProperty() {
		RestInstance rest = createRestInstance("title", "content");
		DocumentInstance documentInstance = new DocumentInstance();
		HashMap<String, Serializable> properties = new HashMap<String, Serializable>();
		properties.put(DocumentProperties.TITLE,
				(Serializable) rest.getProperties().get(DocumentProperties.TITLE));
		documentInstance.setProperties(properties);

		Mockito.when(authenticationService.getCurrentUser()).thenReturn(currentUser);
		Mockito.when(resourceService.loadByDbId("admin")).thenReturn(currentUser);
		Mockito.when(typeConverter.convert(DocumentInstance.class, rest)).thenReturn(
				documentInstance);

		DraftInstance draft = new DraftInstance();
		Mockito.when(draftService.create(documentInstance, currentUser)).thenReturn(draft);

		restService.create(rest);
		Assert.assertFalse(properties.containsKey(DocumentProperties.CONTENT));
	}

	/**
	 * Test find draft w/o specifying instance id and type.
	 */
	@Test(expectedExceptions = EmfRuntimeException.class)
	public void testFindDraftNoInstance() {
		restService.find(null, null, "admin");
	}

	/**
	 * Test find draft w/o specifying a user.
	 */
	@Test(expectedExceptions = EmfRuntimeException.class)
	public void testFindDraftNoUser() {
		LinkSourceId linkSourceId = new LinkSourceId();
		Mockito.when(typeConverter.convert(InstanceReference.class, "documentinstance"))
				.thenReturn(linkSourceId);

		InitializedInstance initializedInstance = new InitializedInstance(new DocumentInstance());
		Mockito.when(typeConverter.convert(InitializedInstance.class, linkSourceId)).thenReturn(
				initializedInstance);

		Mockito.when(resourceService.getResource("admin", ResourceType.USER)).thenReturn(null);
		restService.find("1-2-3-4", "documentinstance", null);

		restService.find("1-2-3-4", "documentinstance", "admin");
	}

	/**
	 * Test find draft when the user cannot be loaded.
	 */
	@Test(expectedExceptions = EmfRuntimeException.class)
	public void testFindDraftNotLoadedUser() {
		LinkSourceId linkSourceId = new LinkSourceId();
		Mockito.when(typeConverter.convert(InstanceReference.class, "documentinstance"))
				.thenReturn(linkSourceId);

		InitializedInstance initializedInstance = new InitializedInstance(new DocumentInstance());
		Mockito.when(typeConverter.convert(InitializedInstance.class, linkSourceId)).thenReturn(
				initializedInstance);

		Mockito.when(resourceService.getResource("admin", ResourceType.USER)).thenReturn(null);

		restService.find("1-2-3-4", "documentinstance", "admin");
	}

	/**
	 * Test find draft, but no matching results.
	 */
	@Test
	public void testCantFindDrafts() {
		LinkSourceId linkSourceId = new LinkSourceId();
		Mockito.when(typeConverter.convert(InstanceReference.class, "documentinstance"))
				.thenReturn(linkSourceId);

		DocumentInstance instance = new DocumentInstance();
		InitializedInstance initializedInstance = new InitializedInstance(instance);
		Mockito.when(typeConverter.convert(InitializedInstance.class, linkSourceId)).thenReturn(
				initializedInstance);

		Mockito.when(resourceService.getResource("admin", ResourceType.USER)).thenReturn(
				currentUser);
		Mockito.when(draftService.getDraft(instance, currentUser)).thenReturn(null);
		List<DraftInstance> find = restService.find("1-2-3-4", "documentinstance", "admin");
		Assert.assertEquals(find, Collections.emptyList());
	}

	/**
	 * Test find draft w/ one available draft.
	 */
	@Test
	public void testFindDrafts() {
		LinkSourceId linkSourceId = new LinkSourceId();
		Mockito.when(typeConverter.convert(InstanceReference.class, "documentinstance"))
				.thenReturn(linkSourceId);

		DocumentInstance instance = new DocumentInstance();
		InitializedInstance initializedInstance = new InitializedInstance(instance);
		Mockito.when(typeConverter.convert(InitializedInstance.class, linkSourceId)).thenReturn(
				initializedInstance);

		Mockito.when(resourceService.getResource("admin", ResourceType.USER)).thenReturn(
				currentUser);
		Mockito.when(draftService.getDraft(instance, currentUser)).thenReturn(new DraftInstance());
		List<DraftInstance> find = restService.find("1-2-3-4", "documentinstance", "admin");
		Assert.assertTrue(find != null && !find.isEmpty());
	}

	/**
	 * Utility method for creating a {@link RestInstance}
	 * 
	 * @param title
	 *            Title for the instance.
	 * @param content
	 *            Content for the instance.
	 * @return A new {@link RestInstance} containing the specified properties.
	 */
	private RestInstance createRestInstance(String title, String content) {
		RestInstance instance = new RestInstance();
		instance.setType("documentinstance");
		instance.getProperties().put(DocumentProperties.TITLE, title);
		instance.setContent(content);
		return instance;
	}
}
