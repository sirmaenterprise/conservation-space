package com.sirma.itt.seip.instance.content.share;

import com.sirma.itt.seip.Pair;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Test for {@link ShareContentEventRegistrator}.
 *
 * @author <a href="mailto:ivo.rusev@sirma.bg">Ivo Rusev</a>
 * @since 12/09/2017
 */
public class ShareContentEventRegistratorTest {

	@Test
	public void test_getClassesToRegister() throws Exception {
		ShareContentEventRegistrator cut = new ShareContentEventRegistrator();
		List<Pair<Class<?>, Integer>> classesToRegister = cut.getClassesToRegister();
		assertEquals(classesToRegister.size(), 2);
		assertEquals(classesToRegister.get(0).getFirst(), ShareInstanceContentEvent.class);
		assertEquals(new Integer(103), classesToRegister.get(0).getSecond());
		assertEquals(classesToRegister.get(1).getFirst(), ContentShareData.class);
		assertEquals(new Integer(104), classesToRegister.get(1).getSecond());
	}
}