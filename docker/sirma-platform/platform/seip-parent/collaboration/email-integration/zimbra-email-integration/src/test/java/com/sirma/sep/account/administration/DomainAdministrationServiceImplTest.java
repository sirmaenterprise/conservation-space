package com.sirma.sep.account.administration;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFactory;
import javax.xml.ws.soap.SOAPFaultException;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.sirma.email.ZimbraEmailIntegrationConstants;
import com.sirma.itt.seip.testutil.mocks.ConfigurationPropertyMock;
import com.sirma.sep.email.configuration.EmailIntegrationConfiguration;
import com.sirma.sep.email.exception.EmailIntegrationException;
import com.sirma.sep.email.model.account.GenericAttribute;
import com.sirma.sep.email.model.domain.ClassOfServiceInformation;
import com.sirma.sep.email.model.domain.DomainInformation;
import com.zimbra.wsdl.zimbraservice.ZcsAdminPortType;

import zimbraadmin.Attr;
import zimbraadmin.CosInfo;
import zimbraadmin.CosSelector;
import zimbraadmin.CreateCosRequest;
import zimbraadmin.CreateCosResponse;
import zimbraadmin.CreateDomainRequest;
import zimbraadmin.DeleteCosRequest;
import zimbraadmin.DeleteDomainRequest;
import zimbraadmin.DomainInfo;
import zimbraadmin.GetCosRequest;
import zimbraadmin.GetCosResponse;
import zimbraadmin.GetDomainInfoResponse;
import zimbraadmin.GetDomainRequest;
import zimbraadmin.GetDomainResponse;
import zimbraadmin.ModifyCosRequest;
import zimbraadmin.ModifyDomainRequest;

/**
 * Test case for the {@link DomainAdministrationServiceImpl}
 *
 * @author g.tsankov
 */
public class DomainAdministrationServiceImplTest {

	private static final String TEST_DOMAIN = "test-domain.com";

	@Mock
	private AccountAuthenticationService authenticationService;

	@Mock
	private EmailIntegrationConfiguration emailIntegrationConfiguration;

	@InjectMocks
	private DomainAdministrationServiceImpl service;

	private ZcsAdminPortType stubbedAdminPort;

	@Before
	public void setup() throws JAXBException, ParserConfigurationException {
		stubbedAdminPort = mock(ZcsAdminPortType.class);
		service = mock(DomainAdministrationServiceImpl.class);
		MockitoAnnotations.initMocks(this);

		initCreateCosConfig();
		when(authenticationService.getAdminPort()).thenReturn(stubbedAdminPort);
	}

	@Test
	public void createDomainTest()
			throws EmailIntegrationException, NoSuchAlgorithmException, UnsupportedEncodingException {
		doCallRealMethod().when(service).createDomain(anyString());
		ArgumentCaptor<CreateDomainRequest> captor = ArgumentCaptor.forClass(CreateDomainRequest.class);
		service.createDomain(TEST_DOMAIN);
		verify(stubbedAdminPort).createDomainRequest(captor.capture());
		CreateDomainRequest response = captor.getValue();
		assertEquals(TEST_DOMAIN, response.getName());
		assertEquals("zimbraPreAuthKey", response.getA().get(0).getN());
		// hash is generated using a random online utility.
		assertEquals("D3C99D986262EDD8681AC2E8A535A9F9A7E8E6DDA9B84E2B11BC8D0D9BE66E42",
				response.getA().get(0).getValue());
	}

	@Test(expected = EmailIntegrationException.class)
	public void failCreateDomainTest() throws EmailIntegrationException {
		doThrow(EmailIntegrationException.class).when(stubbedAdminPort)
				.createDomainRequest(any(CreateDomainRequest.class));
		stubbedAdminPort.createDomainRequest(any(CreateDomainRequest.class));
		service.createDomain(TEST_DOMAIN);
		fail();
	}

	@Test
	public void getDomainInfoTest() throws EmailIntegrationException {
		when(service.getDomain(anyString())).thenCallRealMethod();
		GetDomainResponse response = new GetDomainResponse();
		DomainInfo info = new DomainInfo();
		info.setId("test.id");
		info.setName("test-domain.com");
		response.setDomain(info);
		when(stubbedAdminPort.getDomainRequest(any(GetDomainRequest.class))).thenReturn(response);

		GetDomainInfoResponse domainInfoResponse = Mockito.mock(GetDomainInfoResponse.class);
		domainInfoResponse.setDomain(info);
		when(stubbedAdminPort.getDomainInfoRequest(anyObject())).thenReturn(domainInfoResponse);
		when(domainInfoResponse.getDomain()).thenReturn(info);

		ArgumentCaptor<GetDomainRequest> captor = ArgumentCaptor.forClass(GetDomainRequest.class);
		DomainInformation actualReturn = service.getDomain("test-domain.com").get();
		verify(stubbedAdminPort).getDomainRequest(captor.capture());
		GetDomainRequest actualReq = captor.getValue();
		assertEquals("test-domain.com", actualReq.getDomain().getValue());
		assertEquals("test.id", actualReturn.getDomainId());
		assertEquals("test-domain.com", actualReturn.getDomainName());
	}

	@Test
	public void invalidDomainTest() throws EmailIntegrationException {
		when(service.getDomain(anyString())).thenCallRealMethod();
		GetDomainResponse response = new GetDomainResponse();
		DomainInfo info = new DomainInfo();
		info.setId("globalconfig-dummy-id");
		response.setDomain(info);

		GetDomainInfoResponse domainInfoResponse = Mockito.mock(GetDomainInfoResponse.class);
		domainInfoResponse.setDomain(info);
		when(stubbedAdminPort.getDomainInfoRequest(anyObject())).thenReturn(domainInfoResponse);
		when(domainInfoResponse.getDomain()).thenReturn(info);

		when(stubbedAdminPort.getDomainRequest(any(GetDomainRequest.class)))
				.thenReturn(Mockito.mock(GetDomainResponse.class));

		Optional<DomainInformation> actualReturn = service.getDomain("test-domain.com");
		assertFalse(actualReturn.isPresent());
	}

	@Test(expected = IllegalArgumentException.class)
	public void createWithNoDomainTest() throws EmailIntegrationException {
		doCallRealMethod().when(service).createDomain(anyString());
		service.createDomain("");
	}

	@Test
	public void modifyDomainTest() throws EmailIntegrationException {
		doCallRealMethod().when(service).modifyDomain(anyString(), anyString(), anyString());
		ArgumentCaptor<ModifyDomainRequest> captor = ArgumentCaptor.forClass(ModifyDomainRequest.class);
		when(service.getDomain(anyString()))
				.thenReturn(Optional.of(new DomainInformation("test-id", "test-name", Collections.emptyList())));
		service.modifyDomain("test-id", "testAttr", "testVal");
		verify(stubbedAdminPort).modifyDomainRequest(captor.capture());
		ModifyDomainRequest actual = captor.getValue();
		assertEquals(actual.getId(), "test-id");
		assertEquals(actual.getA().get(0).getN(), "testAttr");
		assertEquals(actual.getA().get(0).getValue(), "testVal");
	}

	@Test
	public void modifyUnexistingDomain() throws EmailIntegrationException {
		doCallRealMethod().when(service).modifyDomain(anyString(), anyString(), anyString());
		when(service.getDomain(anyString())).thenReturn(Optional.empty());
		service.modifyDomain("test-id", "testAttr", "testVal");
		verify(stubbedAdminPort, times(0)).modifyDomainRequest(any(ModifyDomainRequest.class));
	}

	@Test
	public void deleteDomainTest() throws EmailIntegrationException {
		doCallRealMethod().when(service).deleteDomain(any(DomainInformation.class));
		ArgumentCaptor<DeleteDomainRequest> captor = ArgumentCaptor.forClass(DeleteDomainRequest.class);
		service.deleteDomain(new DomainInformation("test.id", "test-name", Collections.EMPTY_LIST));
		verify(stubbedAdminPort).deleteDomainRequest(captor.capture());
		DeleteDomainRequest actual = captor.getValue();
		assertEquals("test.id", actual.getId());
	}

	@Test
	public void createCosTest() throws EmailIntegrationException {
		when(service.createCoS(anyString())).thenCallRealMethod();
		ArgumentCaptor<CreateCosRequest> captor = ArgumentCaptor.forClass(CreateCosRequest.class);
		CreateCosResponse response = new CreateCosResponse();
		response.setCos(new CosInfo());
		when(stubbedAdminPort.createCosRequest(any(CreateCosRequest.class))).thenReturn(response);
		service.createCoS("test-cos");
		verify(stubbedAdminPort).createCosRequest(captor.capture());
		CreateCosRequest actual = captor.getValue();
		assertEquals("test-cos", actual.getName());
		int index = 0;
		List<Attr> attributes = actual.getA();
		assertEquals(attributes.size(), 12);
		for (GenericAttribute attribute : buildInitList()) {
			assertEquals(attribute.getAttributeName(), attributes.get(index).getN());
			index++;
		}

	}

	@Test(expected = EmailIntegrationException.class)
	public void createCosFailTest() throws EmailIntegrationException {
		when(service.createCoS(anyString())).thenCallRealMethod();
		when(stubbedAdminPort.createCosRequest(any(CreateCosRequest.class))).thenThrow(SOAPFaultException.class);
		service.createCoS("test");
	}

	@Test
	public void getCosByNameTest() throws EmailIntegrationException {
		when(service.getCosByName(anyString())).thenCallRealMethod();
		ArgumentCaptor<GetCosRequest> captor = ArgumentCaptor.forClass(GetCosRequest.class);
		GetCosResponse response = new GetCosResponse();
		CosInfo info = new CosInfo();
		response.setCos(info);
		when(stubbedAdminPort.getCosRequest(any(GetCosRequest.class))).thenReturn(response);
		service.getCosByName("test-cos");
		verify(stubbedAdminPort).getCosRequest(captor.capture());
		GetCosRequest actual = captor.getValue();
		CosSelector actualSelector = actual.getCos();
		assertEquals(actualSelector.getValue(), "test-cos");
	}

	@Test
	public void getCosByIdTest() throws EmailIntegrationException {
		when(service.getCosById(anyString())).thenCallRealMethod();
		ArgumentCaptor<GetCosRequest> captor = ArgumentCaptor.forClass(GetCosRequest.class);
		GetCosResponse response = new GetCosResponse();
		CosInfo info = new CosInfo();
		response.setCos(info);
		when(stubbedAdminPort.getCosRequest(any(GetCosRequest.class))).thenReturn(response);
		service.getCosById("123456");
		verify(stubbedAdminPort).getCosRequest(captor.capture());
		GetCosRequest actual = captor.getValue();
		CosSelector actualSelector = actual.getCos();
		assertEquals(actualSelector.getValue(), "123456");
	}

	@Test
	public void extractCosFromDomainAddressTest() throws EmailIntegrationException {
		when(service.extractCosFromDomainAddress(anyObject())).thenCallRealMethod();
		List<GenericAttribute> attributes = new LinkedList<>();
		GenericAttribute attr = new GenericAttribute();
		attr.setAttributeName(ZimbraEmailIntegrationConstants.CLASS_OF_SERVICE_CONFIG);
		attr.setValue("123456");
		attributes.add(attr);

		ClassOfServiceInformation cos = new ClassOfServiceInformation();
		cos.setCosName("default_cos");
		when(service.getCosById("123456")).thenReturn(cos);

		String cosName = service
				.extractCosFromDomainAddress(Optional.of(new DomainInformation("test-id", "test-name", attributes)));
		assertEquals(cosName, "default_cos");
	}

	@Test
	public void getNonExistentCosTest() throws EmailIntegrationException, SOAPException {
		when(service.getCosByName(anyString())).thenCallRealMethod();

		SOAPFaultException exception = new SOAPFaultException(SOAPFactory.newInstance()
				.createFault("ERROR: account.NO_SUCH_COS (no such cos: test)", QName.valueOf("test-excepiton")));
		when(stubbedAdminPort.getCosRequest(any(GetCosRequest.class))).thenThrow(exception);
		ClassOfServiceInformation returned = service.getCosByName("test");
		assertNull(returned.getCosId());
	}

	@Test
	public void modifyCos() throws EmailIntegrationException {
		doCallRealMethod().when(service).modifyCos(anyString(), anyString(), anyString());
		ArgumentCaptor<ModifyCosRequest> captor = ArgumentCaptor.forClass(ModifyCosRequest.class);
		service.modifyCos("test-id", "test-attr-name", "test-attr-value");
		verify(stubbedAdminPort).modifyCosRequest(captor.capture());
		ModifyCosRequest request = captor.getValue();
		assertEquals(request.getId(), "test-id");
		assertEquals(request.getA().get(0).getN(), "test-attr-name");
		assertEquals(request.getA().get(0).getValue(), "test-attr-value");
	}

	@Test(expected = EmailIntegrationException.class)
	public void modifiyCosFailTest() throws EmailIntegrationException {
		doCallRealMethod().when(service).modifyCos(anyString(), anyString(), anyString());
		when(stubbedAdminPort.modifyCosRequest(any(ModifyCosRequest.class))).thenThrow(SOAPFaultException.class);
		service.modifyCos("test", "testName", "testAttr");
	}

	@Test(expected = EmailIntegrationException.class)
	public void deleteCosFailTest() throws EmailIntegrationException {
		doCallRealMethod().when(service).deleteCos(anyString());
		when(service.getCosByName(anyString()))
				.thenReturn(new ClassOfServiceInformation("test-id", "", Collections.EMPTY_LIST));
		when(stubbedAdminPort.deleteCosRequest(any(DeleteCosRequest.class))).thenThrow(SOAPFaultException.class);
		service.deleteCos("test");
	}

	@Test
	public void deleteCosTest() throws EmailIntegrationException {
		doCallRealMethod().when(service).deleteCos(anyString());
		when(service.getCosByName(anyString()))
				.thenReturn(new ClassOfServiceInformation("test-id", "", Collections.EMPTY_LIST));
		ArgumentCaptor<DeleteCosRequest> captor = ArgumentCaptor.forClass(DeleteCosRequest.class);
		service.deleteCos("test-cos-name");
		verify(stubbedAdminPort).deleteCosRequest(captor.capture());
		DeleteCosRequest actual = captor.getValue();
		assertEquals(actual.getId(), "test-id");
	}

	@Test
	public void shouldInitializeTenantClassOfServiceConfigurationListener() throws EmailIntegrationException {
		when(emailIntegrationConfiguration.getTenantClassOfService())
				.thenReturn(new ConfigurationPropertyMock("test-1"));
		when(service.getCosByName("test-1"))
				.thenReturn(new ClassOfServiceInformation("test-id", "test-1", Collections.emptyList()));

		doCallRealMethod().when(service).initialize();
		service.initialize();
		emailIntegrationConfiguration.getTenantClassOfService().valueUpdated();
		verify(service, times(1)).modifyDomain("test-domain", ZimbraEmailIntegrationConstants.CLASS_OF_SERVICE_CONFIG,
				"test-id");
	}

	@Test
	public void shouldCreateCosIfItsRenamed() throws EmailIntegrationException {
		when(emailIntegrationConfiguration.getTenantClassOfService())
				.thenReturn(new ConfigurationPropertyMock("test-1"));
		when(service.createCoS("test-1")).thenReturn("test-id");
		when(service.getCosByName("test-1")).thenReturn(new ClassOfServiceInformation());

		doCallRealMethod().when(service).initialize();
		service.initialize();
		emailIntegrationConfiguration.getTenantClassOfService().valueUpdated();
		verify(service, times(1)).modifyDomain("test-domain", ZimbraEmailIntegrationConstants.CLASS_OF_SERVICE_CONFIG,
				"test-id");
	}

	@Test
	public void shouldInitializePostConstructListeners() throws EmailIntegrationException {
		doCallRealMethod().when(service).initialize();
		when(service.getCosByName(anyString())).thenReturn(new ClassOfServiceInformation("test-cos", null, null));

		service.initialize();
		emailIntegrationConfiguration.getCalendarEnabled().valueUpdated();
		verify(service, times(1)).modifyCos("test-cos", ZimbraEmailIntegrationConstants.CALENDAR_ENABLED, "FALSE");

		emailIntegrationConfiguration.getContactsEnabled().valueUpdated();
		verify(service, times(1)).modifyCos("test-cos", ZimbraEmailIntegrationConstants.CONTACTS_ENABLED, "FALSE");

		emailIntegrationConfiguration.getFeatureOptionsEnabled().valueUpdated();
		verify(service, times(1)).modifyCos("test-cos", ZimbraEmailIntegrationConstants.OPTIONS_ENABLED, "FALSE");

		emailIntegrationConfiguration.getFeatureTaskEnabled().valueUpdated();
		verify(service, times(1)).modifyCos("test-cos", ZimbraEmailIntegrationConstants.TASKS_ENABLED, "FALSE");

		emailIntegrationConfiguration.getGroupCalendarEnabled().valueUpdated();
		verify(service, times(1)).modifyCos("test-cos", ZimbraEmailIntegrationConstants.GROUP_CALENDAR_ENABLED,
				"FALSE");

		emailIntegrationConfiguration.getBriefcasesEnabled().valueUpdated();
		verify(service, times(1)).modifyCos("test-cos", ZimbraEmailIntegrationConstants.BRIEFCASE_ENABLED, "FALSE");

		emailIntegrationConfiguration.getTaggingEnabled().valueUpdated();
		verify(service, times(1)).modifyCos("test-cos", ZimbraEmailIntegrationConstants.TAGGING_ENABLED, "FALSE");

		emailIntegrationConfiguration.getSavedSearchesEnabled().valueUpdated();
		verify(service, times(1)).modifyCos("test-cos", ZimbraEmailIntegrationConstants.SAVED_SEARCHES_ENABLED,
				"FALSE");

		emailIntegrationConfiguration.getSkin().valueUpdated();
		verify(service, times(1)).modifyCos("test-cos", ZimbraEmailIntegrationConstants.PREF_SKIN, "sep");

		emailIntegrationConfiguration.getMailViewPreference().valueUpdated();
		verify(service, times(1)).modifyCos("test-cos", ZimbraEmailIntegrationConstants.MAIL_VIEW_PREFERENCE,
				"message");
	}

	private void initCreateCosConfig() {
		when(emailIntegrationConfiguration.getBriefcasesEnabled()).thenReturn(new ConfigurationPropertyMock<>("FALSE"));
		when(emailIntegrationConfiguration.getCalendarEnabled()).thenReturn(new ConfigurationPropertyMock<>("FALSE"));
		when(emailIntegrationConfiguration.getContactsEnabled()).thenReturn(new ConfigurationPropertyMock<>("FALSE"));
		when(emailIntegrationConfiguration.getTenantDomainAddress())
				.thenReturn(new ConfigurationPropertyMock<>("FALSE"));
		when(emailIntegrationConfiguration.getGroupCalendarEnabled())
				.thenReturn(new ConfigurationPropertyMock<>("FALSE"));
		when(emailIntegrationConfiguration.getSkin()).thenReturn(new ConfigurationPropertyMock<>("sep"));
		when(emailIntegrationConfiguration.getFeatureTaskEnabled())
				.thenReturn(new ConfigurationPropertyMock<>("FALSE"));
		when(emailIntegrationConfiguration.getFeatureOptionsEnabled())
				.thenReturn(new ConfigurationPropertyMock<>("FALSE"));
		when(emailIntegrationConfiguration.getTaggingEnabled()).thenReturn(new ConfigurationPropertyMock<>("FALSE"));
		when(emailIntegrationConfiguration.getSavedSearchesEnabled())
				.thenReturn(new ConfigurationPropertyMock<>("FALSE"));
		when(emailIntegrationConfiguration.getMailViewPreference())
				.thenReturn(new ConfigurationPropertyMock<>("message"));
		when(emailIntegrationConfiguration.getTenantClassOfService())
				.thenReturn(new ConfigurationPropertyMock<>("test-cos"));
		when(emailIntegrationConfiguration.getTenantDomainAddress())
				.thenReturn(new ConfigurationPropertyMock<>("test-domain"));
	}

	private List<GenericAttribute> buildInitList() {
		List<GenericAttribute> initialAttributes = new ArrayList<GenericAttribute>() {
			{
				String FALSE = "FALSE";
				add(new GenericAttribute(ZimbraEmailIntegrationConstants.CALENDAR_ENABLED, FALSE));
				add(new GenericAttribute(ZimbraEmailIntegrationConstants.CONTACTS_ENABLED, FALSE));
				add(new GenericAttribute(ZimbraEmailIntegrationConstants.OPTIONS_ENABLED, FALSE));
				add(new GenericAttribute(ZimbraEmailIntegrationConstants.TASKS_ENABLED, FALSE));
				add(new GenericAttribute(ZimbraEmailIntegrationConstants.GROUP_CALENDAR_ENABLED, FALSE));
				add(new GenericAttribute(ZimbraEmailIntegrationConstants.BRIEFCASE_ENABLED, FALSE));
				add(new GenericAttribute(ZimbraEmailIntegrationConstants.TAGGING_ENABLED, FALSE));
				add(new GenericAttribute(ZimbraEmailIntegrationConstants.SAVED_SEARCHES_ENABLED, FALSE));
				add(new GenericAttribute(ZimbraEmailIntegrationConstants.COMPOSE_IN_NEW_WINDOW_ENABLED, FALSE));
				add(new GenericAttribute(ZimbraEmailIntegrationConstants.MAIL_VIEW_PREFERENCE, "message"));
				add(new GenericAttribute(ZimbraEmailIntegrationConstants.PREF_SKIN, "sep"));
			}
		};
		return initialAttributes;
	}
}
