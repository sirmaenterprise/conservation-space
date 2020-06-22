package com.sirma.sep.content.upload;

import static org.junit.Assert.*;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;

import org.apache.commons.fileupload.FileItem;
import org.junit.Test;

/**
 * Test for {@link UploadRequest}
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 27/06/2018
 */
public class UploadRequestTest {

	@Test
	public void resolveFormField() throws Exception {
		FileItem item1 = createFileItem("field1", "value1");
		FileItem item2 = createFileItem("field2", "value2");
		FileItem item3 = mock(FileItem.class);
		UploadRequest request = new UploadRequest(Arrays.asList(item1, item2, item3), null);
		assertEquals("value2", request.resolveFormField("field2", "someDefaultValue"));
		assertEquals("missingValue", request.resolveFormField("field4", "missingValue"));
	}

	private FileItem createFileItem(String name, String value) throws UnsupportedEncodingException {
		FileItem item = mock(FileItem.class);
		when(item.isFormField()).thenReturn(Boolean.TRUE);
		when(item.getFieldName()).thenReturn(name);
		when(item.getString(anyString())).thenThrow(UnsupportedEncodingException.class);
		when(item.getString()).thenReturn(value);
		return item;
	}

}
