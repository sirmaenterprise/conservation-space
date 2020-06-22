package com.sirmaenterprise.sep.eai.spreadsheet.model.transform;

import static com.sirmaenterprise.sep.eai.spreadsheet.model.EAISpreadsheetConstants.LOCALE_BG;
import static java.util.Locale.ENGLISH;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.sirma.itt.seip.configuration.SystemConfiguration;
import com.sirma.itt.seip.domain.codelist.CodelistService;
import com.sirma.itt.seip.domain.codelist.model.CodeValue;

@RunWith(MockitoJUnitRunner.class)
public class SpreadsheetModelConverterTest {
	@Mock
	private CodelistService codelistService;
	@Mock
	private SystemConfiguration systemConfiguration;
	@InjectMocks
	private SpreadsheetModelConverter spreadsheetModelConverter;

	@Test
	public void testConvertInternalToExternalValueByCodelist() throws Exception {
		CodeValue value = mock(CodeValue.class);
		when(codelistService.getCodeValue(eq(1), eq("val"))).thenReturn(value).thenReturn(value);
		when(codelistService.getDescription(eq(value))).thenReturn("description");
		Serializable clValue = spreadsheetModelConverter.convertInternalToExternalValueByCodelist(1, "val");
		Mockito.verify(codelistService).getDescription(eq(value));
		assertEquals("description", clValue);
	}

	@Test
	public void testConvertExternalToInternalValueByCodelistMissingCL() throws Exception {
		mockCodelist();
		Serializable converted = spreadsheetModelConverter.convertExternalToInternalValueByCodelist(1, "val");
		assertEquals("val", converted);
	}

	@Test
	public void testConvertExternalToInternalValueByCodelistValidCL() throws Exception {
		mockCodelist();
		Serializable converted = spreadsheetModelConverter.convertExternalToInternalValueByCodelist(1, "ст-ст2");
		assertEquals("val2", converted);
		converted = spreadsheetModelConverter.convertExternalToInternalValueByCodelist(1, "ст-ст3");
		assertEquals("val3", converted);
		converted = spreadsheetModelConverter.convertExternalToInternalValueByCodelist(1, "ст-ст1");
		assertEquals("val1", converted);
		converted = spreadsheetModelConverter.convertExternalToInternalValueByCodelist(1, "value1");
		assertEquals("val1", converted);
	}

	private void mockCodelist() {
		CodeValue value1 = mock(CodeValue.class);
		when(value1.getDescription(ENGLISH)).thenReturn("value1");
		when(value1.getDescription(LOCALE_BG)).thenReturn("ст-ст1");
		when(value1.getValue()).thenReturn("val1");
		CodeValue value2 = mock(CodeValue.class);
		when(value2.getDescription(ENGLISH)).thenReturn("value2");
		when(value2.getDescription(LOCALE_BG)).thenReturn("ст-ст2");
		when(value2.getValue()).thenReturn("val2");
		CodeValue value3 = mock(CodeValue.class);
		when(value3.getDescription(ENGLISH)).thenReturn("value3");
		when(value3.getDescription(LOCALE_BG)).thenReturn("ст-ст3");
		when(value3.getValue()).thenReturn("val3");

		Map<String, CodeValue> map = new HashMap<>();
		map.put("val1", value1);
		map.put("val2", value2);
		map.put("val3", value3);

		when(codelistService.getCodeValues(eq(1))).thenReturn(map);
	}

}
