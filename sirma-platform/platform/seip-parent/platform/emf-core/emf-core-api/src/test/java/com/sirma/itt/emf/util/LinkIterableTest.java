/**
 *
 */
package com.sirma.itt.emf.util;

import static org.junit.Assert.assertArrayEquals;

import java.util.Arrays;

import com.sirma.itt.seip.collections.LinkIterable;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.instance.relation.LinkInstance;

/**
 * @author BBonev
 *
 */
public class LinkIterableTest {

	@org.junit.Test
	public void test_iterateFrom() throws Exception {
		LinkInstance linkInstance = new LinkInstance();
		EmfInstance instance = new EmfInstance();
		instance.setId("emf:instance");
		linkInstance.setFrom(instance);

		assertArrayEquals(new Instance[] { instance }, LinkIterable.iterateFrom(Arrays.asList(linkInstance)).toArray());
	}

	@org.junit.Test
	public void test_iterateTo() throws Exception {
		LinkInstance linkInstance = new LinkInstance();
		EmfInstance instance = new EmfInstance();
		instance.setId("emf:instance");
		linkInstance.setTo(instance);

		assertArrayEquals(new Instance[] { instance }, LinkIterable.iterateTo(Arrays.asList(linkInstance)).toArray());
	}
}
