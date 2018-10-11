package com.sirma.itt.seip.definition;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.function.Function;
import java.util.function.Supplier;

import javax.enterprise.context.ContextNotActiveException;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import com.sirma.itt.seip.definition.model.BaseDefinition;
import com.sirma.itt.seip.definition.model.FieldDefinitionImpl;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;

/**
 * Test for {@link InstancePropertyNameResolverImpl}
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 20/07/2018
 */
public class InstancePropertyNameResolverImplTest {
	@InjectMocks
	private InstancePropertyNameResolverImpl resolver;

	@Mock
	private DefinitionService definitionService;

	@Spy
	private InstancePropertyNameResolverCache cacheInstance;

	@Before
	public void setUp() throws Exception {
		cacheInstance = new InstancePropertyNameResolverCache();
		MockitoAnnotations.initMocks(this);
		BaseDefinition baseDefinition = new BaseDefinition();
		baseDefinition.getFields().add(createField("references", "emf:references"));
		baseDefinition.getFields().add(createField("hasParent", "emf:hasParent"));
		baseDefinition.getFields().add(createField("description", "dcterms:description"));
		when(definitionService.getInstanceDefinition(any(Instance.class))).thenReturn(baseDefinition);
	}

	private static PropertyDefinition createField(String name, String uri) {
		FieldDefinitionImpl field = new FieldDefinitionImpl();
		field.setIdentifier(name);
		field.setUri(uri);
		return field;
	}

	@Test
	public void resolve_shouldNotResolveDefinitionForEachCall() throws Exception {
		assertEquals("references", resolver.resolve(new EmfInstance("emf:instanceId"), "emf:references"));
		assertEquals("hasParent", resolver.resolve(new EmfInstance("emf:instanceId"), "emf:hasParent"));
		assertEquals("description", resolver.resolve(new EmfInstance("emf:instanceId"), "dcterms:description"));

		verify(definitionService).getInstanceDefinition(any());
	}

	@Test
	public void resolve_shouldResolveDefinitionForEachCall_whenContextNotActive() throws Exception {
		when(cacheInstance.getOrResolveModel(any(), any(Supplier.class))).thenThrow(ContextNotActiveException.class);

		assertEquals("references", resolver.resolve(new EmfInstance("emf:instanceId"), "emf:references"));
		assertEquals("hasParent", resolver.resolve(new EmfInstance("emf:instanceId"), "emf:hasParent"));
		assertEquals("description", resolver.resolve(new EmfInstance("emf:instanceId"), "dcterms:description"));

		verify(definitionService, times(3)).getInstanceDefinition(any());
	}

	@Test
	public void resolverFor_shouldProvideCacheableResolver() throws Exception {
		Function<String, String> resolverFunction = this.resolver.resolverFor(new EmfInstance("emf:instanceId"));
		assertEquals("references", resolverFunction.apply("emf:references"));
		assertEquals("hasParent", resolverFunction.apply("emf:hasParent"));
		assertEquals("description", resolverFunction.apply("dcterms:description"));

		verify(definitionService).getInstanceDefinition(any());
	}

}
