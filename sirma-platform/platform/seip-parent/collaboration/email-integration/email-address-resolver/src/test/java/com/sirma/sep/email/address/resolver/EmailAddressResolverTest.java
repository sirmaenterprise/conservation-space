package com.sirma.sep.email.address.resolver;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.LinkedList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.beust.jcommander.internal.Lists;
import com.sirma.itt.emf.sequence.SequenceGeneratorService;
import com.sirma.itt.seip.db.DbDao;

/**
 * Test for {@link EmailAddressResolver}.
 *
 * @author S.Djulgerova
 */
public class EmailAddressResolverTest {

	@InjectMocks
	private EmailAddressResolver emailAddressResolver;

	@Mock
	private DbDao dbDao;

	@Mock
	protected SequenceGeneratorService generatorService;

	@Before
	public void setup() {
		emailAddressResolver = new EmailAddressResolver();
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void testIsMailRegisteredByEmailNotExist() {
		assertNull(emailAddressResolver.getEmailAddress("sequence-135@sirma.bg"));
	}

	@Test
	public void testIsMailRegisteredByEmail() {
		List<EmailAddress> result = new LinkedList<>();
		result.add(new EmailAddress("tenant.com", "emf:123456", "project-123-tenant.com@sirma.bg", "sirma.bg"));
		when(dbDao.fetchWithNamed(any(String.class), any(List.class))).thenReturn(result);
		assertEquals(emailAddressResolver.getEmailAddress("project-123-tenant.com@sirma.bg").getEmailAddress(),
				"project-123-tenant.com@sirma.bg");
	}

	@Test
	public void testIsMailRegisteredByInstanceIdNotExist() {
		assertNull(emailAddressResolver.getEmailAddress("emf:123456", "stella.com"));
	}

	@Test
	public void testIsMailRegisteredByInstanceId() {
		List<EmailAddress> result = new LinkedList<>();
		result.add(new EmailAddress("tenant.com", "emf:123456", "project-123-tenant.com@sirma.bg", "sirma.bg"));
		when(dbDao.fetchWithNamed(any(String.class), any(List.class))).thenReturn(result);
		assertEquals(emailAddressResolver.getEmailAddress("emf:123456", "stella.com").getEmailAddress(),
				"project-123-tenant.com@sirma.bg");
	}

	@Test
	public void testInsertEmailAddress() {
		emailAddressResolver.insertEmailAddress("emf:123456", "tenant.com", "project-123-tenant.com@sirma.bg",
				"sirma.bg");
		verify(dbDao, times(1)).saveOrUpdate(
				new EmailAddress("tenant.com", "emf:123456", "project-123-tenant.com@sirma.bg", "sirma.bg"));
	}

	@Test
	public void testDeleteEmailAddress() {
		emailAddressResolver.deleteEmailAddress("project-123-tenant.com@sirma.bg");
		verify(dbDao, times(1)).executeUpdate(any(String.class), any(List.class));
	}

	@Test
	public void testGetAllTenantsInDomain() {
		List<String> result = new LinkedList<>();
		result.add("tenant.com");
		when(dbDao.fetchWithNamed(any(String.class), any(List.class))).thenReturn(result);
		assertEquals(emailAddressResolver.getAllTenantsInDomain("tenant.com").get(0),
				"tenant.com");
	}

	@Test
	public void testGetAllEmailsByTenant() {
		List<EmailAddress> result = new LinkedList<>();
		result.add(new EmailAddress("tenant.com", "emf:123456", "project-123-tenant.com@sirma.bg", "sirma.bg"));
		when(dbDao.fetchWithNamed(any(String.class), any(List.class))).thenReturn(result);
		assertEquals(emailAddressResolver.getAllEmailsByTenant("tenant.com").get(0).getEmailAddress(),"project-123-tenant.com@sirma.bg");
	}

	@Test
	public void testEmptyGetAllEmailByTenant(){
		when(dbDao.fetch(any(String.class), any(List.class))).thenReturn(Lists.newLinkedList());
		assertEquals(emailAddressResolver.getAllEmailsByTenant("tenant.com").size(),0);
	}
	
}
