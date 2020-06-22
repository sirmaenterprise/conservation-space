package com.sirma.itt.seip.instance.version;

import static java.util.stream.Collectors.toMap;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyCollection;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.function.Function;

import org.junit.Test;

/**
 * Test for {@link VersionIdsCache}.
 *
 * @author A. Kunchev
 */
public class VersionedCacheTest {

	@Test
	public void getVersioned() {
		VersionIdsRetriever retriever = spy(new VersionIdsRetriever());
		VersionIdsCache cache = new VersionIdsCache(new Date(), retriever::toVersions);

		Map<Serializable, Serializable> versioned = cache.getVersioned(Arrays.asList("instance-id-1", "instance-id-2"));

		assertTrue(versioned.values().containsAll(Arrays.asList("instance-id-1-v1.0", "instance-id-2-v1.0")));
		verify(retriever).toVersions(anyCollection(), any(Date.class));

		// verify retrieval from the cache
		versioned = cache.getVersioned(Collections.singleton("instance-id-2"));
		assertTrue(versioned.values().contains("instance-id-2-v1.0"));
		verifyNoMoreInteractions(retriever);
	}

	private class VersionIdsRetriever {

		public VersionIdsRetriever() {
		}

		public Map<Serializable, Serializable> toVersions(Collection<Serializable> ids,
				@SuppressWarnings("unused") Date versionDate) {
			return ids.stream().collect(toMap(Function.identity(), id -> id + "-v1.0"));
		}
	}
}
