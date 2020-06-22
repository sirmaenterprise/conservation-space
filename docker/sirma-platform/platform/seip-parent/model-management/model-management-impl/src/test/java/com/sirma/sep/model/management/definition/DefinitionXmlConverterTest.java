package com.sirma.sep.model.management.definition;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.sirma.itt.seip.definition.jaxb.Definition;
import com.sirma.itt.seip.definition.model.GenericDefinitionImpl;
import com.sirma.itt.seip.mapping.ObjectMapper;
import com.sirma.itt.seip.mapping.ObjectMappingException;

/**
 * Tests corner cases in {@link DefinitionXmlConverterTest} that cannot be covered by component testing or simply they cannot occur
 * theoretically.
 *
 * @author Mihail Radkov
 */
@RunWith(MockitoJUnitRunner.class)
public class DefinitionXmlConverterTest {

	@Mock
	private ObjectMapper objectMapper;

	@InjectMocks
	private DefinitionXmlConverter xmlConverter;

	@Test(expected = IllegalArgumentException.class)
	public void shouldNotHandleDefinitionsThatCannotBeMappedToXml() {
		when(objectMapper.map(any(), Matchers.eq(Definition.class))).thenThrow(new ObjectMappingException());
		xmlConverter.convertToXMLs(Collections.singleton(new GenericDefinitionImpl()));
	}
}
