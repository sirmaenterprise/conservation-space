package com.sirma.itt.seip.instance.util;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

import java.io.Serializable;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.ShortUri;
import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;

/**
 * Test for {@link BookmarkUtil}
 *
 * @author BBonev
 */
public class BookmarkUtilTest {

	@InjectMocks
	private BookmarkUtil bookmarkUtil;

	@Mock
	private TypeConverter typeConverter;

	@Before
	public void beforeMethod() {
		MockitoAnnotations.initMocks(this);
		when(typeConverter.convert(eq(ShortUri.class), anyString()))
				.then(a -> new ShortUri(a.getArgumentAt(1, String.class)));
	}

	@Test
	public void testBuildLink_id() throws Exception {
		assertEquals("/", bookmarkUtil.buildLink((Serializable) null));
		assertEquals("#/idoc/emf:instanceId", bookmarkUtil.buildLink("emf:instanceId"));
	}

	@Test
	public void testBuildLink_Instance() throws Exception {
		EmfInstance instance = new EmfInstance();
		instance.setId("emf:instanceId");
		assertEquals("/", bookmarkUtil.buildLink((Instance) null));
		assertEquals("#/idoc/emf:instanceId", bookmarkUtil.buildLink(instance));
	}
}
