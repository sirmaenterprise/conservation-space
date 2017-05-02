package com.sirma.itt.seip.annotations;

import static org.mockito.Matchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.ejb.EJBException;

import org.apache.commons.io.IOUtils;
import org.mockito.Mockito;
import org.openrdf.model.Literal;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.vocabulary.XMLSchema;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;
import org.openrdf.rio.RDFParserRegistry;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.sirma.itt.commons.utils.reflection.ReflectionUtils;
import com.sirma.itt.emf.GeneralSemanticTest;
import com.sirma.itt.emf.mocks.NamespaceRegistryMock;
import com.sirma.itt.emf.mocks.TransactionalRepositoryConnectionMock;
import com.sirma.itt.emf.mocks.search.SearchServiceMock;
import com.sirma.itt.emf.mocks.search.SemanticDaoMock;
import com.sirma.itt.seip.annotations.model.Annotation;
import com.sirma.itt.seip.annotations.model.AnnotationProperties;
import com.sirma.itt.seip.annotations.parser.AnnotationParser;
import com.sirma.itt.seip.annotations.parser.ContextBuilder;
import com.sirma.itt.seip.annotations.parser.DefaultSeipContextExtension;
import com.sirma.itt.seip.annotations.parser.JsonLdParserFactory;
import com.sirma.itt.seip.annotations.rest.AnnotationWriter;
import com.sirma.itt.seip.domain.definition.label.LabelProvider;
import com.sirma.itt.seip.instance.state.StateTransitionManager;
import com.sirma.itt.seip.monitor.NoOpStatistics;
import com.sirma.itt.seip.permissions.action.AuthorityService;
import com.sirma.itt.seip.resources.EmfUser;
import com.sirma.itt.seip.resources.ResourceService;
import com.sirma.itt.seip.security.context.SecurityContext;
import com.sirma.itt.seip.testutil.mocks.InstanceProxyMock;
import com.sirma.itt.semantic.TransactionalRepositoryConnection;
import com.sirma.itt.semantic.model.vocabulary.EMF;
import com.sirma.itt.semantic.model.vocabulary.OA;

/**
 * Integration test for {@link SemanticAnnotationService}
 *
 * @author kirq4e
 */
public class SemanticAnnotationServiceTest extends GeneralSemanticTest<SemanticAnnotationService> {

	private TransactionalRepositoryConnection transactionalRepositoryConnection;
	private NamespaceRegistryMock namespaceRegistry;
	private SemanticDaoMock dbDao;
	private AnnotationWriter annotationWriter = new AnnotationWriter();
	private ResourceService resourceService;
	private AuthorityService authorityService;
	private StateTransitionManager stateTransitionManager;
	private URI actionsUri = ValueFactoryImpl
			.getInstance()
			.createURI("http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#", "actions");

	private LabelProvider labelProvider;
	private ContextBuilder contextProvider;


	@BeforeClass
	public void init() {
		RDFParserRegistry.getInstance().add(new JsonLdParserFactory());




		service = new SemanticAnnotationService();
		context.put("dictionaryService", new AnnotationDictionaryServiceMock());
		namespaceRegistry = new NamespaceRegistryMock(context);
		ReflectionUtils.setField(service, "namespaceRegistryService", namespaceRegistry);

		contextProvider.getInstance().registerProvider(new DefaultSeipContextExtension());

		authorityService = mock(AuthorityService.class);
		ReflectionUtils.setField(service, "authorityService", authorityService);

		stateTransitionManager = mock(StateTransitionManager.class);
		ReflectionUtils.setField(service, "stateTransitionManager", stateTransitionManager);
		when(stateTransitionManager.getNextState(Mockito.any(Annotation.class), Mockito.eq("OPEN"),
				Mockito.eq("suspend"))).thenReturn("ON_HOLD");


		SecurityContext securityContextMock = mock(SecurityContext.class);
		EmfUser user = new EmfUser();
		user.setId("emf:admin");
		user.setName("admin");
		user.setDisplayName("Admin");

		when(securityContextMock.getAuthenticated()).thenReturn(user);

		ReflectionUtils.setField(service, "securityContext", securityContextMock);
		ReflectionUtils.setField(service, "idManager", idManager);
		ReflectionUtils.setField(service, "statistics", new NoOpStatistics());
		ReflectionUtils.setField(service, "searchService", new SearchServiceMock(context));
		ReflectionUtils.setField(service, "queryBuilder", new AnnotationQueryBuilderMock(context));

		dbDao = new SemanticDaoMock(context);
		ReflectionUtils.setField(service, "dbDao", dbDao);

		resourceService = mock(ResourceService.class);
		when(resourceService.loadByDbId(anyList())).thenReturn(Collections.<Serializable>singletonList(user));
		ReflectionUtils.setField(service, "resourceService", resourceService);

		ReflectionUtils.setField(annotationWriter, "typeConverter", context.get("typeConverter"));
		ReflectionUtils.setField(annotationWriter, "namespaceRegistryService", namespaceRegistry);

		labelProvider = mock(LabelProvider.class);
		when(labelProvider.getValue(Mockito.any())).thenReturn("noPermissions");
		ReflectionUtils.setField(service, "labelProvider", labelProvider);
	}

	@BeforeMethod
	public void startTransaction() {
		transactionalRepositoryConnection = new TransactionalRepositoryConnectionMock(context);
		ReflectionUtils.setField(dbDao, "repositoryConnection",
				new InstanceProxyMock<>(transactionalRepositoryConnection));
		ReflectionUtils.setField(service, "repositoryConnection",
				new InstanceProxyMock<>(transactionalRepositoryConnection));

		try {
			transactionalRepositoryConnection.afterBegin();
		} catch (EJBException | RemoteException e) {
			fail("Unable to initate transaction", e);
		}
	}

	@AfterMethod
	public void commitTransaction() {
		try {
			transactionalRepositoryConnection.beforeCompletion();
			transactionalRepositoryConnection.afterCompletion(true);
		} catch (EJBException | RemoteException e) {
			fail("Unable to commit transaction", e);
		}
	}

	/**
	 * Tests persistence of annotation data
	 *
	 * @throws RepositoryException
	 *             If an error occurs while retrieving the statements from the semantic repository
	 * @throws IOException
	 *             If an error occurs when reading the test file
	 */
	@Test
	public void testPersistingOfAnnotation() throws IOException, RepositoryException {
		Collection<Annotation> annotations = AnnotationParser.parse(readTestData());

		Collection<Annotation> saveAnnoation = service.saveAnnotation(annotations);
		Assert.assertEquals(saveAnnoation.size(), 1);

		Serializable serializable = saveAnnoation.iterator().next().getId();
		URI annotationId = namespaceRegistry.buildUri(serializable.toString());

		RepositoryResult<Statement> statements = transactionalRepositoryConnection.getStatements(annotationId, null,
				null, true, EMF.ANNOTATIONS_CONTEXT);

		Map<URI, Value> predicates = new HashMap<>();
		while (statements.hasNext()) {
			Statement st = statements.next();
			predicates.put(st.getPredicate(), st.getObject());
		}
		statements.close();

		Assert.assertFalse(predicates.isEmpty());
		Assert.assertTrue(predicates.containsKey(EMF.CONTENT));
		Assert.assertTrue(predicates.containsKey(EMF.CREATED_BY));
		Assert.assertTrue(predicates.get(EMF.CREATED_BY) instanceof URI);
		Assert.assertTrue(predicates.containsKey(EMF.CREATED_ON));
		Assert.assertTrue(((Literal) predicates.get(EMF.CREATED_ON)).getDatatype().equals(XMLSchema.DATETIME));
		Assert.assertTrue(predicates.containsKey(EMF.MODIFIED_BY));
		Assert.assertTrue(predicates.get(EMF.MODIFIED_BY) instanceof URI);
		Assert.assertTrue(predicates.containsKey(EMF.MODIFIED_ON));
		Assert.assertTrue(((Literal) predicates.get(EMF.MODIFIED_ON)).getDatatype().equals(XMLSchema.DATETIME));
		Assert.assertTrue(predicates.containsKey(EMF.IS_DELETED));
		Assert.assertTrue(((Literal) predicates.get(EMF.IS_DELETED)).getDatatype().equals(XMLSchema.BOOLEAN));
		Assert.assertTrue(predicates.containsKey(OA.HAS_TARGET));
		Assert.assertTrue(predicates.containsKey(OA.HAS_BODY));
		Assert.assertTrue(predicates.containsKey(OA.MOTIVATED_BY));
		Assert.assertTrue(predicates.containsKey(EMF.COMMENTS_ON));
		Assert.assertEquals(predicates.get(OA.MOTIVATED_BY), OA.COMMENTING);
		Assert.assertTrue(predicates.containsKey(EMF.STATUS));
		Assert.assertTrue(predicates.containsKey(EMF.MENTIONED_USERS));

	}

	@Test
	public void testPersistingOfAnnotationWithReplies() throws IOException, RepositoryException {
		Collection<Annotation> annotations = AnnotationParser.parse(readTestData("annotations/createWithReplies.json"));

		Collection<Annotation> saveAnnoation = service.saveAnnotation(annotations);
		Assert.assertEquals(saveAnnoation.size(), 1);
		Annotation annotation = saveAnnoation.iterator().next();
		Assert.assertEquals(annotation.getReplies().size(), 2);
		Serializable serializable = annotation.getReplies().iterator().next().getId();
		URI annotationId = namespaceRegistry.buildUri(serializable.toString());

		RepositoryResult<Statement> statements = transactionalRepositoryConnection.getStatements(annotationId, null,
				null, true, EMF.ANNOTATIONS_CONTEXT);

		Map<URI, Value> predicates = new HashMap<>();
		while (statements.hasNext()) {
			Statement st = statements.next();
			predicates.put(st.getPredicate(), st.getObject());
		}
		statements.close();

		Assert.assertFalse(predicates.isEmpty());
		Assert.assertTrue(predicates.containsKey(EMF.CONTENT));
		Assert.assertTrue(predicates.containsKey(EMF.CREATED_BY));
		Assert.assertTrue(predicates.get(EMF.CREATED_BY) instanceof URI);
		Assert.assertTrue(predicates.containsKey(EMF.CREATED_ON));
		Assert.assertTrue(((Literal) predicates.get(EMF.CREATED_ON)).getDatatype().equals(XMLSchema.DATETIME));
		Assert.assertTrue(predicates.containsKey(EMF.MODIFIED_BY));
		Assert.assertTrue(predicates.get(EMF.MODIFIED_BY) instanceof URI);
		Assert.assertTrue(predicates.containsKey(EMF.MODIFIED_ON));
		Assert.assertTrue(((Literal) predicates.get(EMF.MODIFIED_ON)).getDatatype().equals(XMLSchema.DATETIME));
		Assert.assertTrue(predicates.containsKey(EMF.IS_DELETED));
		Assert.assertTrue(((Literal) predicates.get(EMF.IS_DELETED)).getDatatype().equals(XMLSchema.BOOLEAN));
		Assert.assertTrue(predicates.containsKey(OA.HAS_TARGET));
		Assert.assertTrue(predicates.containsKey(OA.HAS_BODY));
		Assert.assertTrue(predicates.containsKey(OA.MOTIVATED_BY));
		Assert.assertTrue(predicates.containsKey(EMF.REPLY_TO));
		Assert.assertEquals(predicates.get(OA.MOTIVATED_BY), OA.COMMENTING);
		Assert.assertTrue(predicates.containsKey(EMF.STATUS));
		Assert.assertTrue(predicates.containsKey(EMF.MENTIONED_USERS));

		statements = transactionalRepositoryConnection.getStatements(null, EMF.REPLY_TO,
				namespaceRegistry.buildUri(annotation.getId().toString()), true, EMF.ANNOTATIONS_CONTEXT);

		predicates = new HashMap<>();
		while (statements.hasNext()) {
			Statement st = statements.next();
			predicates.put((URI) st.getSubject(), st.getObject());
		}
		statements.close();

		Assert.assertFalse(predicates.isEmpty());
		Assert.assertEquals(predicates.size(), 2);
	}

	/**
	 * Tests persistence of annotation data
	 *
	 * @throws RepositoryException
	 *             If an error occurs while retrieving the statements from the semantic repository
	 * @throws IOException
	 *             If an error occurs when reading the test file
	 */
	@Test
	public void testUpdateOfAnnotation() throws IOException, RepositoryException {
		Collection<Annotation> annotations = AnnotationParser.parse(readTestData());

		Collection<Annotation> saveAnnoation = service.saveAnnotation(annotations);
		Assert.assertEquals(saveAnnoation.size(), 1);
		Annotation updated = saveAnnoation.iterator().next();
		ByteArrayOutputStream entityStream = new ByteArrayOutputStream();
		annotationWriter.writeTo(updated, entityStream, true);

		String data = new String(entityStream.toByteArray());
		data = data.replace("oa:commenting", "oa:editing").replace("asdf", "updatedContent");
		annotations = AnnotationParser.parse(new ByteArrayInputStream(data.getBytes(StandardCharsets.UTF_8)));

		saveAnnoation = service.saveAnnotation(annotations);

		commitTransaction();
		startTransaction();

		Assert.assertEquals(saveAnnoation.size(), 1);
		updated = saveAnnoation.iterator().next();

		Serializable serializable = updated.getId();
		URI annotationId = namespaceRegistry.buildUri(serializable.toString());

		RepositoryResult<Statement> statements = transactionalRepositoryConnection.getStatements(annotationId, null,
				null, true, EMF.ANNOTATIONS_CONTEXT);

		Map<URI, Value> predicates = new HashMap<>();
		while (statements.hasNext()) {
			Statement st = statements.next();
			predicates.put(st.getPredicate(), st.getObject());
		}
		statements.close();

		Assert.assertFalse(predicates.isEmpty());
		Assert.assertTrue(predicates.containsKey(EMF.CONTENT));
		Assert.assertTrue(predicates.containsKey(EMF.CREATED_BY));
		Assert.assertTrue(predicates.get(EMF.CREATED_BY) instanceof URI);
		Assert.assertTrue(predicates.containsKey(EMF.CREATED_ON));
		Assert.assertTrue(((Literal) predicates.get(EMF.CREATED_ON)).getDatatype().equals(XMLSchema.DATETIME));
		Assert.assertTrue(predicates.containsKey(EMF.MODIFIED_BY));
		Assert.assertTrue(predicates.get(EMF.MODIFIED_BY) instanceof URI);
		Assert.assertTrue(predicates.containsKey(EMF.MODIFIED_ON));
		Assert.assertTrue(((Literal) predicates.get(EMF.MODIFIED_ON)).getDatatype().equals(XMLSchema.DATETIME));
		Assert.assertTrue(predicates.containsKey(EMF.IS_DELETED));
		Assert.assertTrue(((Literal) predicates.get(EMF.IS_DELETED)).getDatatype().equals(XMLSchema.BOOLEAN));
		Assert.assertTrue(predicates.containsKey(OA.HAS_TARGET));
		Assert.assertTrue(predicates.containsKey(OA.HAS_BODY));
		Assert.assertTrue(predicates.get(OA.HAS_BODY).toString().contains("updatedContent"));
		Assert.assertTrue(predicates.containsKey(OA.MOTIVATED_BY));
		Assert.assertEquals(predicates.get(OA.MOTIVATED_BY), OA.EDITING);
		Assert.assertTrue(predicates.containsKey(EMF.MENTIONED_USERS));

	}

	/**
	 * Tests persistence of annotation data
	 *
	 * @throws RepositoryException
	 *             If an error occurs while retrieving the statements from the semantic repository
	 * @throws IOException
	 *             If an error occurs when reading the test file
	 */
	@Test
	public void testNewActionOfAnnotation() throws IOException, RepositoryException {
		Collection<Annotation> annotations = AnnotationParser.parse(readTestData("annotations/createWithReplies.json"));

		Collection<Annotation> saveAnnoation = service.saveAnnotation(annotations);
		Assert.assertEquals(saveAnnoation.size(), 1);
		Annotation updated = saveAnnoation.iterator().next();
		ByteArrayOutputStream entityStream = new ByteArrayOutputStream();
		annotationWriter.writeTo(updated, entityStream, true);

		String data = new String(entityStream.toByteArray());
		data = data.substring(0, data.length() - 1).concat(", \"action\":\"suspend\"}");
		annotations = AnnotationParser.parse(new ByteArrayInputStream(data.getBytes(StandardCharsets.UTF_8)));

		saveAnnoation = service.saveAnnotation(annotations);

		commitTransaction();
		startTransaction();

		Assert.assertEquals(saveAnnoation.size(), 1);
		updated = saveAnnoation.iterator().next();

		Serializable serializable = updated.getId();
		URI annotationId = namespaceRegistry.buildUri(serializable.toString());

		RepositoryResult<Statement> statements = transactionalRepositoryConnection.getStatements(annotationId, null,
				null, true, EMF.ANNOTATIONS_CONTEXT);

		Map<URI, Value> predicates = new HashMap<>();
		while (statements.hasNext()) {
			Statement st = statements.next();
			predicates.put(st.getPredicate(), st.getObject());
		}
		statements.close();

		Assert.assertFalse(predicates.isEmpty());
		Assert.assertTrue(predicates.containsKey(EMF.CONTENT));
		Assert.assertTrue(predicates.containsKey(EMF.CREATED_BY));
		Assert.assertTrue(predicates.get(EMF.CREATED_BY) instanceof URI);
		Assert.assertTrue(predicates.containsKey(EMF.CREATED_ON));
		Assert.assertTrue(((Literal) predicates.get(EMF.CREATED_ON)).getDatatype().equals(XMLSchema.DATETIME));
		Assert.assertTrue(predicates.containsKey(EMF.MODIFIED_BY));
		Assert.assertTrue(predicates.get(EMF.MODIFIED_BY) instanceof URI);
		Assert.assertTrue(predicates.containsKey(EMF.MODIFIED_ON));
		Assert.assertTrue(((Literal) predicates.get(EMF.MODIFIED_ON)).getDatatype().equals(XMLSchema.DATETIME));
		Assert.assertTrue(predicates.containsKey(EMF.IS_DELETED));
		Assert.assertTrue(((Literal) predicates.get(EMF.IS_DELETED)).getDatatype().equals(XMLSchema.BOOLEAN));
		Assert.assertTrue(predicates.containsKey(OA.HAS_TARGET));
		Assert.assertTrue(predicates.containsKey(OA.HAS_BODY));
		Assert.assertTrue(predicates.containsKey(OA.MOTIVATED_BY));
		Assert.assertTrue(predicates.containsKey(EMF.STATUS));
		Assert.assertTrue(predicates.get(EMF.STATUS).stringValue().equals("ON_HOLD"));
		Assert.assertTrue(predicates.containsKey(EMF.MENTIONED_USERS));

	}

	@Test
	public void testDeleteAnnotation() throws IOException, RepositoryException {
		Annotation annotation = AnnotationParser.parseSingle(readTestData());
		service.saveAnnotation(annotation);

		commitTransaction();
		startTransaction();

		RepositoryResult<Statement> statements = null;
		try {
			// for invalid data
			service.deleteAnnotation(null);

			service.deleteAnnotation(namespaceRegistry.getShortUri(annotation.getId().toString()));
			commitTransaction();
			startTransaction();

			statements = transactionalRepositoryConnection.getStatements(
					namespaceRegistry.buildUri(annotation.getId().toString()), null, null, true,
					EMF.ANNOTATIONS_CONTEXT);
			Assert.assertFalse(statements.hasNext());
		} finally {
			if (statements != null) {
				statements.close();
			}
		}
	}

	@Test
	public void testDeleteAnnotationWithReplies() throws IOException, RepositoryException {
		Annotation annotation = AnnotationParser.parseSingle(readTestData("annotations/createWithReplies.json"));
		service.saveAnnotation(annotation);
		List<Serializable> ids = annotation.stream().map(Annotation::getId).collect(Collectors.toList());

		commitTransaction();
		startTransaction();

		RepositoryResult<Statement> statements = null;
		try {
			// for invalid data
			service.deleteAnnotation(null);

			service.deleteAnnotation(namespaceRegistry.getShortUri(annotation.getId().toString()));
			commitTransaction();
			startTransaction();

			Assert.assertEquals(ids.size(), 3);
			for (Serializable id : ids) {
				statements = transactionalRepositoryConnection.getStatements(namespaceRegistry.buildUri(id.toString()),
						null, null, true, EMF.ANNOTATIONS_CONTEXT);
				Assert.assertFalse(statements.hasNext());
			}
		} finally {
			if (statements != null) {
				statements.close();
			}
		}
	}

	@Test
	public void testDeleteReply() throws IOException, RepositoryException {
		Annotation annotation = AnnotationParser.parseSingle(readTestData("annotations/createWithReplies.json"));
		service.saveAnnotation(annotation);

		commitTransaction();
		startTransaction();

		// for invalid data
		service.deleteAnnotation(null);
		Annotation reply = annotation.getReplies().iterator().next();
		service.deleteAnnotation(namespaceRegistry.getShortUri(reply.getId().toString()));
		commitTransaction();
		startTransaction();

		Optional<Annotation> optional = service.loadAnnotation(annotation.getId().toString());
		Assert.assertTrue(optional.isPresent());
		Annotation found = optional.get();

		Assert.assertEquals(found.getReplies().size(), 1);
	}

	@Test
	public void testDeleteAnnotationForTarget() throws IOException, RepositoryException {
		Annotation annotation = AnnotationParser.parseSingle(readTestData());
		service.saveAnnotation(annotation);

		commitTransaction();
		startTransaction();

		RepositoryResult<Statement> statements = null;
		try {
			// for invalid data
			service.deleteAllAnnotations(null);

			service.deleteAllAnnotations(namespaceRegistry.getShortUri(annotation.getTargetId().toString()));
			commitTransaction();
			startTransaction();

			statements = transactionalRepositoryConnection.getStatements(
					namespaceRegistry.buildUri(annotation.getId().toString()), null, null, true,
					EMF.ANNOTATIONS_CONTEXT);
			Assert.assertFalse(statements.hasNext());
		} finally {
			if (statements != null) {
				statements.close();
			}
		}
	}

	@Test
	public void testSearchForAnnotation() throws IOException, RepositoryException {
		Collection<Annotation> annotations = AnnotationParser.parse(readTestData());

		Collection<Annotation> saveAnnotation = service.saveAnnotation(annotations);
		commitTransaction();
		startTransaction();

		Serializable targetId = saveAnnotation.iterator().next().getTargetId();
		Serializable commentsOn = saveAnnotation.iterator().next().getCommentsOn();

		Collection<Annotation> result = service.searchAnnotation(null, null, null);
		Assert.assertNotNull(result);
		Assert.assertTrue(result.isEmpty());

		result = service.searchAnnotation(namespaceRegistry.getShortUri(targetId.toString()), namespaceRegistry.getShortUri(commentsOn.toString()), 10);
		Assert.assertNotNull(result);
		Assert.assertFalse(result.isEmpty());
		Assert.assertNotNull(result.iterator().next().getContent());
	}
	
	@Test
	public void testSearchForAnnotationNoTabId() throws IOException, RepositoryException {
		Collection<Annotation> annotations = AnnotationParser.parse(readTestData());

		Collection<Annotation> saveAnnotation = service.saveAnnotation(annotations);
		commitTransaction();
		startTransaction();

		Serializable targetId = saveAnnotation.iterator().next().getTargetId();
		Serializable commentsOn = saveAnnotation.iterator().next().getCommentsOn();

		Collection<Annotation> result = service.searchAnnotation(null, null, null);
		Assert.assertNotNull(result);
		Assert.assertTrue(result.isEmpty());

		result = service.searchAnnotation(namespaceRegistry.getShortUri(targetId.toString()), null, 10);
		Assert.assertNotNull(result);
		Assert.assertFalse(result.isEmpty());
		Assert.assertNotNull(result.iterator().next().getContent());
	}

	@Test
	public void testSearchForAllAnnotation() throws IOException, RepositoryException {
		Collection<Annotation> annotations = service.loadAnnotations(null, 10);
		Assert.assertEquals(annotations.size(),0);
	}

	@Test
	public void testSearchForAnnotationSetActions() throws IOException, RepositoryException {
		Collection<Annotation> annotations = AnnotationParser.parse(readTestData());

		Collection<Annotation> saveAnnotation = service.saveAnnotation(annotations);
		commitTransaction();
		startTransaction();

		Serializable targetId = saveAnnotation.iterator().next().getTargetId();
		Serializable commentsOn = saveAnnotation.iterator().next().getCommentsOn();

		Collection<Annotation> result = service.searchAnnotation(null, null, null);
		Assert.assertNotNull(result);
		Assert.assertTrue(result.isEmpty());

		result = service.searchAnnotation(namespaceRegistry.getShortUri(targetId.toString()), namespaceRegistry.getShortUri(commentsOn.toString()), 10);

		Assert.assertFalse(result.iterator().next().getProperties().containsKey(actionsUri.getLocalName()));

	}

	@Test
	public void testSearchForAnnotationsByCriteria() throws IOException {
		Collection<Annotation> annotations = AnnotationParser.parse(readTestData());
		Collection<Annotation> saveAnnotation = service.saveAnnotation(annotations);
		commitTransaction();
		startTransaction();

		String expectedHasTarget = namespaceRegistry
				.getShortUri(saveAnnotation.iterator().next().getTargetId().toString());
		EmfUser expectedModifiedBy = (EmfUser) saveAnnotation.iterator().next().getModifiedBy();

		AnnotationSearchRequest request = new AnnotationSearchRequest()
				.setInstanceIds(Collections.singletonList(expectedHasTarget))
				.setUserIds(Collections.singletonList(expectedModifiedBy.getId().toString()));

		Collection<Annotation> result = service.searchAnnotations(request);

		Assert.assertNotNull(result);
		Assert.assertFalse(result.isEmpty());
		Annotation returnedAnnotation = result.iterator().next();
		Assert.assertEquals(returnedAnnotation.getString(AnnotationProperties.HAS_TARGET), expectedHasTarget);
		Assert.assertEquals(returnedAnnotation.getModifiedBy(), expectedModifiedBy);
	}

	@Test
	public void testSearchForAnnotationsCountOnly() throws IOException {
		Collection<Annotation> annotations = AnnotationParser.parse(readTestData("annotations/create-multiple.json"));
		Collection<Annotation> savedAnnotation = service.saveAnnotation(annotations);

		int expectedTotalCount = savedAnnotation.size();
		commitTransaction();
		startTransaction();

		// all the saved annotations have the same target instance ID
		String hasTarget = namespaceRegistry
				.getShortUri(savedAnnotation.iterator().next().getTargetId().toString());

		AnnotationSearchRequest request = new AnnotationSearchRequest()
				.setInstanceIds(Collections.singletonList(hasTarget))
				.setLimit(1);

		int returnedTotalCount = service.searchAnnotationsCountOnly(request);
		Assert.assertEquals(returnedTotalCount, expectedTotalCount);
	}

	@Test
	public void testLoadAnnotationSetActions() throws IOException, RepositoryException {
		Collection<Annotation> annotations = AnnotationParser.parse(readTestData("annotations/createWithReplies.json"));

		Collection<Annotation> saveAnnotation = service.saveAnnotation(annotations);
		commitTransaction();
		startTransaction();

		Annotation annotation = saveAnnotation.iterator().next();
		Optional<Annotation> result = service
				.loadAnnotation(namespaceRegistry.getShortUri(annotation.getId().toString()));
		Assert.assertTrue(result.get().getProperties().containsKey(actionsUri.getLocalName()));
	}

	@Test
	public void testCountAnnotation() throws IOException, RepositoryException {
		Collection<Annotation> annotations = AnnotationParser.parse(readTestData());

		Collection<Annotation> saveAnnotation = service.saveAnnotation(annotations);
		commitTransaction();
		startTransaction();

		Serializable targetId = saveAnnotation.iterator().next().getTargetId();
		Serializable commentsOn = saveAnnotation.iterator().next().getCommentsOn();

		// not valid id
		Assert.assertEquals(service.countAnnotations(null, null), -1);

		int count = service.countAnnotations(namespaceRegistry.getShortUri(targetId.toString()), namespaceRegistry.getShortUri(commentsOn.toString()));
		Assert.assertEquals(count, 1);
	}

	@Test
	public void testCountAnnotationReplies_noReplies() throws IOException, RepositoryException {
		Collection<Annotation> annotations = AnnotationParser.parse(readTestData());

		Collection<Annotation> saveAnnotation = service.saveAnnotation(annotations);
		commitTransaction();
		startTransaction();

		Annotation annotation = saveAnnotation.iterator().next();
		Serializable targetId = annotation.getTargetId();

		// not valid id
		Map<String, Integer> replies = service.countAnnotationReplies(null);
		Assert.assertNotNull(replies);
		Assert.assertTrue(replies.isEmpty());

		replies = service.countAnnotationReplies(namespaceRegistry.getShortUri(targetId.toString()));
		Assert.assertNotNull(replies);
		Assert.assertEquals(replies.size(), 1);
		Assert.assertEquals(replies.get(annotation.getId()), Integer.valueOf(0));
	}

	@Test
	public void testCountAnnotationReplies() throws IOException, RepositoryException {
		Collection<Annotation> annotations = AnnotationParser.parse(readTestData("annotations/createWithReplies.json"));

		Collection<Annotation> saveAnnotation = service.saveAnnotation(annotations);
		commitTransaction();
		startTransaction();

		Annotation annotation = saveAnnotation.iterator().next();
		Serializable targetId = annotation.getTargetId();

		// not valid id
		Map<String, Integer> replies = service.countAnnotationReplies(null);
		Assert.assertNotNull(replies);
		Assert.assertTrue(replies.isEmpty());

		replies = service.countAnnotationReplies(namespaceRegistry.getShortUri(targetId.toString()));
		Assert.assertNotNull(replies);
		Assert.assertEquals(replies.size(), 1);
		Assert.assertEquals(replies.get(annotation.getId()), Integer.valueOf(2));
	}

	@Test
	public void testLoadAnnotationReplies() throws IOException, RepositoryException {
		Collection<Annotation> annotations = AnnotationParser.parse(readTestData("annotations/createWithReplies.json"));

		Collection<Annotation> saveAnnotation = service.saveAnnotation(annotations);
		commitTransaction();
		startTransaction();

		Annotation annotation = saveAnnotation.iterator().next();

		// not valid id
		Optional<Annotation> result = service.loadAnnotation(null);
		Assert.assertNotNull(result);
		Assert.assertFalse(result.isPresent());

		result = service.loadAnnotation(namespaceRegistry.getShortUri(annotation.getId().toString()));
		Assert.assertNotNull(result);
		Assert.assertTrue(result.isPresent());
		Annotation loaded = result.get();
		Assert.assertEquals(loaded.getId(), annotation.getId());
		Assert.assertEquals(loaded.getReplies().size(), 2);
	}



	private InputStream readTestData() throws IOException {
		return readTestData("annotations/create.json");
	}

	private InputStream readTestData(String path) throws IOException {
		String string = IOUtils
				.toString(SemanticAnnotationService.class.getClassLoader().getResourceAsStream(path),
						StandardCharsets.UTF_8)
				.replaceAll("http://canvas/uri1", idManager.generateId().toString());
		return new ByteArrayInputStream(string.getBytes(StandardCharsets.UTF_8));
	}

	@Override
	protected String getTestDataFile() {
		return null;
	}
}