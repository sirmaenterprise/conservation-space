package com.sirma.itt.seip.annotations.model;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.util.Set;

import javax.enterprise.inject.Instance;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.cache.lookup.EntityLookupCache;
import com.sirma.itt.seip.cache.lookup.EntityLookupCacheContext;
import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.itt.seip.db.DbDao;
import com.sirma.itt.seip.definition.DefinitionService;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.definition.label.LabelProvider;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.event.EventService;
import com.sirma.itt.seip.testutil.mocks.ConfigurationPropertyMock;

/**
 * Test for {@link AnnotationDefinitionAccessor}.
 *
 * @author BBonev
 */
public class AnnotationDefinitionAccessorTest {

	@InjectMocks
	private AnnotationDefinitionAccessor definitionAccessor;
	@Mock
	private EventService eventService;
	@Spy
	private ConfigurationProperty<String> defaultAnnotationDefinition = new ConfigurationPropertyMock<>(
			"annotationDefinition");
	@Mock
	private DbDao dbDao;
	@Spy
	private ConfigurationProperty<Set<String>> excludeDefinitions = new ConfigurationPropertyMock<>();
	@Mock
	private LabelProvider labelProvider;
	@Mock
	private EntityLookupCacheContext cacheContext;
	@Mock
	private Instance<DefinitionService> definitionServiceInstance;
	@Mock
	private EntityLookupCache<Serializable, Object, Serializable> cache;

	@Before
	public void beforeMethod() {
		MockitoAnnotations.initMocks(this);
		defaultAnnotationDefinition.isInitialized();
		when(cacheContext.getCache(anyString())).thenReturn(cache);
	}

	@Test
	public void getDefinitionForInstance() throws Exception {
		DefinitionModel definitionModel = mock(DefinitionModel.class);
		when(cache.getByKey("annotationDefinition")).thenReturn(new Pair<>("annotationDefinition", definitionModel));
		Annotation annotation = new Annotation();
		DefinitionModel model = definitionAccessor.getDefinition(annotation);
		assertNotNull(model);
	}

	@Test
	public void getDefinitionForInstance_NotSupported() throws Exception {
		DefinitionModel model = definitionAccessor.getDefinition(new EmfInstance());
		assertNull(model);
	}

	@Test
	public void getDefinitionForInstance_noDefinition() throws Exception {
		when(cache.getByKey("annotationDefinition")).thenReturn(null);
		Annotation annotation = new Annotation();
		DefinitionModel model = definitionAccessor.getDefinition(annotation);
		assertNull(model);
	}

	@Test
	public void getDefinitionForInstance_WithRevision() throws Exception {
		DefinitionModel definitionModel = mock(DefinitionModel.class);
		when(cache.getByKey("annotationDefinition")).thenReturn(new Pair<>("annotationDefinition", definitionModel));
		Annotation annotation = new Annotation();
		DefinitionModel model = definitionAccessor.getDefinition(annotation);
		assertNotNull(model);
	}
}
