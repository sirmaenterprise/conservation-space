/**
 * Copyright (c) 2013 22.07.2013 , Sirma ITT. /* /**
 */
package com.sirma.itt.idoc.web.document;

import static org.testng.Assert.assertEquals;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.mockito.Mockito;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.sirma.itt.cmf.beans.model.DocumentInstance;
import com.sirma.itt.cmf.services.DraftService;
import com.sirma.itt.commons.utils.reflection.ReflectionUtils;
import com.sirma.itt.emf.converter.TypeConverter;
import com.sirma.itt.emf.definition.DictionaryService;
import com.sirma.itt.emf.definition.model.DataType;
import com.sirma.itt.emf.domain.model.DefinitionModel;
import com.sirma.itt.emf.entity.LinkSourceId;
import com.sirma.itt.emf.instance.dao.InstanceService;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.instance.model.InstanceReference;
import com.sirma.itt.emf.rest.EmfApplicationException;
import com.sirma.itt.emf.rest.model.RestInstance;
import com.sirma.itt.emf.security.AuthenticationService;
import com.sirma.itt.emf.security.AuthorityService;
import com.sirma.itt.emf.security.AuthorityServiceImpl;
import com.sirma.itt.emf.security.model.EmfUser;
import com.sirma.itt.emf.state.operation.Operation;
import com.sirma.itt.emf.util.sanitize.ContentSanitizer;

/**
 * Tests for {@link IntelligentDocumentRestService}.
 * 
 * @author Adrian Mitev
 */
@Test
public class IntelligentDocumentRestSerivceTest {

	private static IntelligentDocumentRestService idocService;
	private static AuthorityService authorityService;

	private DocumentInstance createDocumentInstance;

	private static TypeConverter typeConverter;
	private AuthenticationService authenticationService;
	private DictionaryService dictionaryService;
	private ContentSanitizer idocSanitizer;
	private InstanceService<Instance, DefinitionModel> instanceService;
	private DraftService draftService;

	/**
	 * Init CUT.
	 */
	@BeforeClass
	public void init() {
		idocService = new IntelligentDocumentRestService();
		authorityService = Mockito.mock(AuthorityServiceImpl.class);
		typeConverter = Mockito.mock(TypeConverter.class);
		authenticationService = Mockito.mock(AuthenticationService.class);
		dictionaryService = Mockito.mock(DictionaryService.class);
		idocSanitizer = Mockito.mock(ContentSanitizer.class);
		instanceService = Mockito.mock(InstanceService.class);
		draftService = Mockito.mock(DraftService.class);

		ReflectionUtils.setField(idocService, "typeConverter", typeConverter);
		ReflectionUtils.setField(idocService, "authorityService", authorityService);
		ReflectionUtils.setField(idocService, "authenticationService", authenticationService);
		ReflectionUtils.setField(idocService, "dictionaryService", dictionaryService);
		ReflectionUtils.setField(idocService, "idocSanitizer", idocSanitizer);
		ReflectionUtils.setField(idocService, "instanceService", instanceService);
		ReflectionUtils.setField(idocService, "draftService", draftService);
	}

	/**
	 * Test for
	 * {@link IntelligentDocumentRestSerivce#getAllowedActions(String,String)}.
	 */
	public void testLoadActions() {
		createDocumentInstance = createDocumentInstance(Long.valueOf(1));
		idocService.getAllowedActions(createDocumentInstance.getIdentifier(), "documentinstance");
	}

	/**
	 * Creates the document instance.
	 * 
	 * @param id
	 *            the id
	 * @return the document instance
	 */
	public DocumentInstance createDocumentInstance(Long id) {
		DocumentInstance documentInstance = new DocumentInstance();
		documentInstance.setId(id);
		addReference(documentInstance, id, DocumentInstance.class);
		return documentInstance;
	}

	/**
	 * Adds the reference.
	 * 
	 * @param instance
	 *            the instance
	 * @param id
	 *            the id
	 * @param type
	 *            the type
	 */
	private void addReference(Instance instance, Serializable id, Class<?> type) {
		com.sirma.itt.commons.utils.reflection.ReflectionUtils.setField(instance, "reference",
				createInstanceReference(id.toString(), type));
	}

	/**
	 * Creates the instance reference.
	 * 
	 * @param id
	 *            the id
	 * @param type
	 *            the type
	 * @return the instance reference
	 */
	public InstanceReference createInstanceReference(String id, Class<?> type) {
		DataType sourceType = new DataType();
		sourceType.setName(type.getSimpleName().toLowerCase());
		sourceType.setJavaClassName(type.getCanonicalName());
		sourceType.setJavaClass(type);
		InstanceReference ref = new LinkSourceId(id.toString(), sourceType);
		return ref;
	}

	/**
	 * Save method test
	 */
	public void testSave() {
		EmfUser user = new EmfUser("admin");
		user.setId("emf:" + user.getIdentifier());

		Mockito.when(authenticationService.getCurrentUserId()).thenReturn(user.getIdentifier());

		RestInstance restInstance = new RestInstance();
		restInstance.setType("documentinstance");

		Map<String, Object> properties = new HashMap<String, Object>();
		properties.put("title", "test title");
		restInstance.setProperties(properties);

		DataType dataType = new DataType();
		dataType.setName("documentinstance");
		dataType.setJavaClass(DocumentInstance.class);
		dataType.setJavaClassName(DocumentInstance.class.getName());

		Mockito.when(dictionaryService.getDataTypeDefinition("documentinstance")).thenReturn(
				dataType);

		Mockito.when(idocSanitizer.sanitize("test content", "")).thenReturn("test content");

		DocumentInstance documentInstance = new DocumentInstance();
		documentInstance.setId(Long.valueOf(1));

		Mockito.when(typeConverter.convert(DocumentInstance.class, restInstance)).thenReturn(
				documentInstance);

		Mockito.when(instanceService.save(documentInstance, new Operation(""))).thenReturn(
				documentInstance);

		Mockito.when(typeConverter.convert(RestInstance.class, documentInstance)).thenReturn(
				restInstance);

		RestInstance rest = idocService.save(null, restInstance);
		assertEquals(rest, restInstance);
	}

	/**
	 * Save method test if user is null
	 */
	public void testSaveUserNull() {

		Mockito.when(authenticationService.getCurrentUserId()).thenReturn(null);

		RestInstance restInstance = new RestInstance();
		restInstance.setType(null);

		Map<String, Object> properties = new HashMap<String, Object>();
		properties.put("title", "test title");
		restInstance.setProperties(properties);

		Mockito.when(dictionaryService.getDataTypeDefinition("documentinstance")).thenReturn(null);

		RestInstance rest = idocService.save(null, restInstance);
		assertEquals(rest, null);
	}

	/**
	 * Save method test if type class is null
	 */
	@Test(expectedExceptions = EmfApplicationException.class)
	public void testSaveTypeClassNull() {
		EmfUser user = new EmfUser("admin");
		user.setId("emf:" + user.getIdentifier());

		Mockito.when(authenticationService.getCurrentUserId()).thenReturn(user.getIdentifier());

		RestInstance restInstance = new RestInstance();
		restInstance.setType(null);

		Map<String, Object> properties = new HashMap<String, Object>();
		properties.put("title", "test title");
		restInstance.setProperties(properties);

		Mockito.when(dictionaryService.getDataTypeDefinition("documentinstance")).thenReturn(null);

		RestInstance rest = idocService.save(null, restInstance);
	}

	/**
	 * Save method test if document title is empty
	 */
	@Test(expectedExceptions = EmfApplicationException.class)
	public void testSaveTitleEmpty() {
		EmfUser user = new EmfUser("admin");
		user.setId("emf:" + user.getIdentifier());

		Mockito.when(authenticationService.getCurrentUserId()).thenReturn(user.getIdentifier());

		RestInstance restInstance = new RestInstance();
		restInstance.setType("documentinstance");

		Map<String, Object> properties = new HashMap<String, Object>();
		properties.put("title", "");
		restInstance.setProperties(properties);

		DataType dataType = new DataType();
		dataType.setName("documentinstance");
		dataType.setJavaClass(DocumentInstance.class);
		dataType.setJavaClassName(DocumentInstance.class.getName());

		Mockito.when(dictionaryService.getDataTypeDefinition("documentinstance")).thenReturn(
				dataType);

		idocService.save(null, restInstance);
	}

	/**
	 * Save method test
	 */
	public void testSaveInstanceNull() {
		EmfUser user = new EmfUser("admin");
		user.setId("emf:" + user.getIdentifier());

		Mockito.when(authenticationService.getCurrentUserId()).thenReturn(user.getIdentifier());

		RestInstance restInstance = new RestInstance();
		restInstance.setType("documentinstance");

		Map<String, Object> properties = new HashMap<String, Object>();
		properties.put("title", "test title");
		restInstance.setProperties(properties);

		DataType dataType = new DataType();
		dataType.setName("documentinstance");
		dataType.setJavaClass(DocumentInstance.class);
		dataType.setJavaClassName(DocumentInstance.class.getName());

		Mockito.when(dictionaryService.getDataTypeDefinition("documentinstance")).thenReturn(
				dataType);

		Mockito.when(idocSanitizer.sanitize("test content", "")).thenReturn("test content");

		DocumentInstance documentInstance = new DocumentInstance();
		documentInstance.setId(Long.valueOf(1));

		Mockito.when(typeConverter.convert(DocumentInstance.class, restInstance)).thenReturn(null);

		Mockito.when(instanceService.save(documentInstance, new Operation(""))).thenReturn(
				documentInstance);

		Mockito.when(typeConverter.convert(RestInstance.class, documentInstance)).thenReturn(
				restInstance);

		RestInstance rest = idocService.save(null, restInstance);
		assertEquals(rest, rest);
	}
}
