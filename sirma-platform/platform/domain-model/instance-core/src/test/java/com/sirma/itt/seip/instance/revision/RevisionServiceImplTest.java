package com.sirma.itt.seip.instance.revision;

import static com.sirma.itt.seip.domain.instance.DefaultProperties.REVISION_TYPE;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.util.HashMap;

import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.AssertJUnit;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.sirma.itt.seip.Uri;
import com.sirma.itt.seip.collections.ContextualMap;
import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.convert.UriConverterProvider;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.security.ActionTypeConstants;
import com.sirma.itt.seip.event.EventService;
import com.sirma.itt.seip.instance.DomainInstanceService;
import com.sirma.itt.seip.instance.InstanceSaveContext;
import com.sirma.itt.seip.instance.dao.InstanceLoadDecorator;
import com.sirma.itt.seip.instance.dao.InstanceService;
import com.sirma.itt.seip.instance.relation.LinkConstants;
import com.sirma.itt.seip.instance.relation.LinkService;
import com.sirma.itt.seip.instance.revision.steps.PublishStepRunner;
import com.sirma.itt.seip.instance.revision.steps.PublishStepRunner.StepRunner;
import com.sirma.itt.seip.instance.state.Operation;
import com.sirma.itt.seip.instance.state.StateService;
import com.sirma.itt.seip.instance.version.InstanceVersionService;
import com.sirma.itt.seip.search.SearchService;
import com.sirma.itt.seip.security.User;
import com.sirma.itt.seip.security.context.SecurityContextManager;
import com.sirma.itt.seip.testutil.EmfTest;

/**
 * @author BBonev
 */
@Test
public class RevisionServiceImplTest extends EmfTest {

	@InjectMocks
	private RevisionServiceImpl service;

	@Mock
	private DomainInstanceService domainInstanceService;

	@Mock
	EventService eventService;

	@Mock
	LinkService linkService;

	@Mock
	LinkService chainingLinkService;

	@Mock
	StateService stateService;

	@Mock
	SearchService searchService;

	@Mock
	User authenticated;

	@Mock
	private InstanceLoadDecorator instanceLoadDecorator;

	@Mock
	private PublishStepRunner stepRunner;

	@Mock
	private InstanceService objectService;

	@Mock
	private InstanceVersionService instanceVersionService;

	private TypeConverter typeConverter;
	EmfInstance instanceToPublish;

	/**
	 * Setup.
	 */
	@BeforeMethod
	public void setup() {
		typeConverter = spy(createTypeConverter());

		MockitoAnnotations.initMocks(this);


		LinkConstants.init(mock(SecurityContextManager.class), ContextualMap.create());

		instanceToPublish = (EmfInstance) createInstance("emf:id");
		instanceToPublish.getProperties().put(DefaultProperties.STATUS, "APPROVED");

		when(objectService.clone(any(), any())).then(a -> {
			Instance instance = a.getArgumentAt(0, Instance.class);
			Instance clone = new EmfInstance();
			clone.addAllProperties(instance.getOrCreateProperties());
			clone.setId(instance.getId());
			return clone;
		});

		when(objectService.loadByDbId(Matchers.any(Serializable.class))).thenReturn(new EmfInstance());

		when(securityContext.getAuthenticated()).thenReturn(authenticated);

		when(idManager.getRevisionId(any(), anyString())).thenCallRealMethod();

		when(stepRunner.getRunner(any(String[].class))).thenReturn(mock(StepRunner.class));
	}

	/**
	 * Creates the instance.
	 *
	 * @param id
	 *            the id
	 * @return the instance
	 */
	private static Instance createInstance(Serializable id) {
		EmfInstance revision = new EmfInstance();
		revision.setId(id);
		revision.setProperties(new HashMap<String, Serializable>());
		return revision;
	}

	/**
	 * Test publish approved.
	 */
	@Test
	public void testPublishApproved() {
		Operation operation = new Operation(ActionTypeConstants.APPROVE_AND_PUBLISH);
		Instance revision = service.publish(instanceToPublish, operation);
		AssertJUnit.assertNotNull(revision);
		AssertJUnit.assertEquals(revision.getId(), "emf:id-r1.0");
		// this is without children
		verify(domainInstanceService, times(3)).save(any(InstanceSaveContext.class));
	}

	/**
	 * Test publish rejected.
	 */
	@Test
	public void testPublishRejected() {
		Operation operation = new Operation(ActionTypeConstants.REJECT_AND_PUBLISH);
		Instance revision = service.publish(instanceToPublish, operation);
		AssertJUnit.assertNotNull(revision);
		AssertJUnit.assertEquals(revision.getId(), "emf:id-r1.0");
		// this is without children
		verify(domainInstanceService, times(2)).save(any(InstanceSaveContext.class));
	}

	@Test
	public void isRevision_FalseWhenNotSpecifiedType() throws Exception {
		assertFalse(service.isRevision(new EmfInstance()));
	}

	@Test
	public void isRevision_FalseWhenCurrent() throws Exception {
		EmfInstance instance = new EmfInstance();
		instance.add(REVISION_TYPE, "emf:current");
		assertFalse(service.isRevision(instance));
	}

	@Test
	public void isRevision_TrueWhenEmfRevision() throws Exception {
		EmfInstance instance = new EmfInstance();
		instance.add(REVISION_TYPE, "emf:revision");
		assertTrue(service.isRevision(instance));
	}

	@Test
	public void isRevision_TrueWhenEmfLatestRevision() throws Exception {
		EmfInstance instance = new EmfInstance();
		instance.add(REVISION_TYPE, "emf:latestRevision");
		assertTrue(service.isRevision(instance));
	}

	@Test
	public void isRevision_WhenNotStringConvertToTheSameType() throws Exception {
		EmfInstance instance = new EmfInstance();
		instance.add(REVISION_TYPE, new UriConverterProvider.StringUriProxy(
				"http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#current"));
		when(typeConverter.convert(eq(Uri.class), any())).then(a -> a.getArgumentAt(1, Uri.class));
		assertFalse(service.isRevision(instance));
	}
}
