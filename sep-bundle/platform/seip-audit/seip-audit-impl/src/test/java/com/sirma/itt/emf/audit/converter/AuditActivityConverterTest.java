package com.sirma.itt.emf.audit.converter;

import org.easymock.EasyMock;
import org.easymock.EasyMockRunner;
import org.easymock.Mock;
import org.easymock.TestSubject;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.sirma.itt.commons.utils.reflection.ReflectionUtils;
import com.sirma.itt.emf.audit.activity.AuditActivity;
import com.sirma.itt.emf.definition.DictionaryService;
import com.sirma.itt.emf.definition.model.DataType;
import com.sirma.itt.emf.label.retrieve.FieldValueRetrieverService;
import com.sirma.itt.semantic.NamespaceRegistryService;

/**
 * Testing the {@link AuditActivityConverter}.
 * 
 * @author nvelkov
 */
@RunWith(EasyMockRunner.class)
public class AuditActivityConverterTest {

	@Mock
	private FieldValueRetrieverService fieldValueRetrieverService;

	@Mock
	private DictionaryService dictionaryService;

	@Mock
	private NamespaceRegistryService namespaceRegistryService;

	@TestSubject
	private AuditActivityConverter auditActivityConverter = new AuditActivityConverterImpl();

	/**
	 * Test the convertActivity method. The method just calls upon the
	 * {@link FieldValueRetrieverService} to get the appropriate labels, so the only thing that
	 * needs to be tested here is if the {@link AuditActivity} values are being set.
	 */
	@Test
	public void testConvertActivity() {
		DataType dataType = new DataType();
		EasyMock.expect(
				fieldValueRetrieverService.getLabel(EasyMock.anyString(), EasyMock.anyString()))
				.andReturn("mockedResult").anyTimes();
		EasyMock.expect(
				fieldValueRetrieverService.getLabel(EasyMock.anyString(), EasyMock.anyString(),
						EasyMock.anyString())).andReturn("mockedResult").anyTimes();
		EasyMock.expect(dictionaryService.getDataTypeDefinition(EasyMock.anyString()))
				.andReturn(dataType).anyTimes();
		EasyMock.expect(namespaceRegistryService.buildFullUri(EasyMock.anyString()))
				.andReturn("fullUri").anyTimes();
		ReflectionUtils.setField(dataType, "name", "mockedName");
		EasyMock.replay(fieldValueRetrieverService, dictionaryService, namespaceRegistryService);
		AuditActivity activity = new AuditActivity();
		activity.setObjectTitle("title");
		activity.setObjectType("emf:Project");
		activity.setObjectPreviousState("APPROVED");
		auditActivityConverter.convertActivity(activity);
		Assert.assertEquals("mockedResult", activity.getObjectTypeLabel());
		Assert.assertEquals("mockedResult", activity.getObjectSubTypeLabel());
		Assert.assertEquals("mockedResult", activity.getUserDisplayName());
		Assert.assertEquals("mockedName", activity.getObjectInstanceType());
	}
}
