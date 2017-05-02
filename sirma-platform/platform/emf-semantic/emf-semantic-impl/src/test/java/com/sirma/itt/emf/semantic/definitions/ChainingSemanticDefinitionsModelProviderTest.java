package com.sirma.itt.emf.semantic.definitions;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openrdf.model.Model;
import org.openrdf.model.impl.LinkedHashModel;

import com.sirma.itt.cmf.beans.definitions.impl.GenericDefinitionImpl;
import com.sirma.itt.commons.utils.reflection.ReflectionUtils;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.testutil.mocks.InstanceProxyMock;

/**
 * Test for ChainingSemanticDefinitionsModelProvider.
 *
 * @author A. Kunchev
 */
public class ChainingSemanticDefinitionsModelProviderTest {

	@InjectMocks
	private ChainingSemanticDefinitionsModelProvider provider = new ChainingSemanticDefinitionsModelProvider();

	@Mock
	private SemanticPropertyRegister semanticPropertyRegister;

	@Mock
	private SemanticDefinitionsURIBuilder definitionsURIStatementsBuilder;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
	}

	// ------------------------------------- provideModelStatements ----------------------------------------

	@Test
	public void provideModelStatements_twoExtensions_extensionsMethodsCalled() {
		ReflectionUtils.setField(provider, "providers",
				new InstanceProxyMock<>(semanticPropertyRegister, definitionsURIStatementsBuilder));
		provider.provideModelStatements(new GenericDefinitionImpl(), new LinkedHashModel());
		verify(semanticPropertyRegister).provideModelStatements(any(DefinitionModel.class), any(Model.class));
		verify(definitionsURIStatementsBuilder).provideModelStatements(any(DefinitionModel.class), any(Model.class));
	}

}
