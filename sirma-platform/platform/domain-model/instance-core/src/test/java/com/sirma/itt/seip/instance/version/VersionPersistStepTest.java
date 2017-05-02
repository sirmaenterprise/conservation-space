package com.sirma.itt.seip.instance.version;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.domain.instance.ArchivedInstance;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;

/**
 * Test for {@link VersionPersistStep}.
 *
 * @author A. Kunchev
 */
public class VersionPersistStepTest {

	@InjectMocks
	private VersionPersistStep step;

	@Mock
	private VersionDao versionDao;

	@Before
	public void setup() {
		step = new VersionPersistStep();
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void getName() {
		assertEquals("versionPersist", step.getName());
	}

	@Test(expected = NullPointerException.class)
	public void execute_nullInstance_archveServiceNotCalled() {
		step.execute(VersionContext.create(null));
		verify(versionDao, never()).persistVersion(any(Instance.class));
	}

	@Test
	public void execute_archveServiceCalled() {
		EmfInstance instance = new EmfInstance();
		Date createdOn = new Date();

		when(versionDao.persistVersion(instance)).thenReturn(new ArchivedInstance());
		VersionContext versionContext = VersionContext.create(instance, createdOn);
		step.execute(versionContext);

		verify(versionDao).persistVersion(instance);
		assertEquals(createdOn, instance.get(VersionProperties.VERSION_CREATED_ON, Date.class));
	}

}
