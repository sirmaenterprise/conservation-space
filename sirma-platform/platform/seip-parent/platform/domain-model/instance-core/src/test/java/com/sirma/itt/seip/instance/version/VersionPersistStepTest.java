package com.sirma.itt.seip.instance.version;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.domain.instance.ArchivedInstance;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.exception.EmfRuntimeException;

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

	@Test(expected = EmfRuntimeException.class)
	public void execute_missingVersionInstance_archveServiceNotCalled() {
		step.execute(VersionContext.create(new EmfInstance()));
		verify(versionDao, never()).persistVersion(any(Instance.class));
	}

	@Test
	public void execute_archveServiceCalled() {
		Instance instance = new EmfInstance();
		ArchivedInstance version = new ArchivedInstance();
		when(versionDao.persistVersion(instance)).thenReturn(version);
		VersionContext versionContext = VersionContext.create(instance).setVersionInstance(version);
		step.execute(versionContext);

		verify(versionDao).persistVersion(version);
	}
}