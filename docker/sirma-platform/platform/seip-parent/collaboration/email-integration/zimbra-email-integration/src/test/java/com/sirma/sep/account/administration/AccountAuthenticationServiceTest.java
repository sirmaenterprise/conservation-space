package com.sirma.sep.account.administration;

import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import java.util.Map;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.sirma.sep.account.administration.AccountAuthenticationService.PortCache;
import com.sirma.sep.account.administration.AccountAuthenticationService.PortWrapper;
import com.sirma.sep.email.exception.EmailIntegrationException;
import com.sun.xml.ws.developer.WSBindingProvider;
import com.zimbra.wsdl.zimbraservice.ZcsPortType;

import zimbraaccount.AuthResponse;

/**
 * Test case for the {@link AccountAuthenticationService}
 *
 * @author S.Djulgerova
 */
public class AccountAuthenticationServiceTest {

	@InjectMocks
	private AccountAuthenticationService accountAuthenticationService;

	@Mock
	private ZcsPortType clientPort;

	@Mock
	private Map<String, PortCache> clientPortsMap;

	@Before
	public void setup() throws JAXBException, ParserConfigurationException {
		accountAuthenticationService = mock(AccountAuthenticationService.class);
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void createDomainTest() throws EmailIntegrationException {
		ZcsPortType mock = Mockito.mock(ZcsPortType.class, withSettings().extraInterfaces(WSBindingProvider.class));
		when(mock.authRequest(anyObject())).thenReturn(Mockito.mock(AuthResponse.class));
		when(clientPortsMap.get("test-user@sirmaplatform.com")).thenReturn(new PortCache(new PortWrapper(mock)));

		doCallRealMethod().when(accountAuthenticationService).getClientPort("test-user@sirmaplatform.com");
		accountAuthenticationService.getClientPort("test-user@sirmaplatform.com");
		verify(accountAuthenticationService).addSoapAcctAuthHeader(anyObject(), anyObject());
	}

}
