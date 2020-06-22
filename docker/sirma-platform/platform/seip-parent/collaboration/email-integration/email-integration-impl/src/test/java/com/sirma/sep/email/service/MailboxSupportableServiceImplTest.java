package com.sirma.sep.email.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.db.DbDao;
import com.sirma.itt.semantic.model.vocabulary.EMF;
import com.sirma.sep.email.entity.MailboxSupportable;

/**
 * Tests for MailboxSupportableServiceImpl service.
 * 
 * @author svelikov
 */
public class MailboxSupportableServiceImplTest {

	@InjectMocks
	public MailboxSupportableServiceImpl mailboxSupportableServiceImpl;

	@Mock
	private DbDao dbDao;

	@Before
	public void setup() {
		mailboxSupportableServiceImpl = new MailboxSupportableServiceImpl();
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void test_isMailboxSupportable_class_supports_mailboxes() {
		List<Pair<String, Object>> args = new ArrayList<>(1);
		args.add(new Pair<String, Object>("className", EMF.USER.toString()));
		Mockito.when(dbDao.fetchWithNamed(MailboxSupportable.QUERY_BY_CLASSNAME_KEY, args))
				.thenReturn(Arrays.asList("some-user@email.addr"));
		boolean actual = mailboxSupportableServiceImpl.isMailboxSupportable(EMF.USER.toString());
		Assert.assertTrue("Class should support mailboxes", actual);
	}

	@Test
	public void test_isMailboxSupportable_class_doesnt_supports_mailboxes() {
		List<Pair<String, Object>> args = new ArrayList<>(1);
		Mockito.when(dbDao.fetchWithNamed(MailboxSupportable.QUERY_BY_CLASSNAME_KEY, args))
				.thenReturn(new ArrayList<>());
		boolean actual = mailboxSupportableServiceImpl.isMailboxSupportable(EMF.USER.toString());
		Assert.assertFalse("Class should not support mailboxes", actual);
	}

}
