package com.sirma.itt.seip.adapters.iiif.iip;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;

import com.sirma.itt.seip.adapters.iiif.Dimension;
import com.sirma.itt.seip.adapters.iip.IIPServerImageProvider;
import com.sirma.itt.seip.adapters.remote.DMSClientException;
import com.sirma.itt.seip.adapters.remote.RESTClient;
import com.srima.itt.seip.adapters.mock.HttpMethodMock;
import com.srima.itt.seip.adapters.mock.ImageServerConfigurationsMock;
import org.apache.commons.httpclient.methods.GetMethod;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

/**
 * Tests for {@link IIPServerImageProvider}
 */
public class IIPServerImagerProviderTest {
	@InjectMocks
	private IIPServerImageProvider iipServerImageProvider;

	@Mock
	private RESTClient restClient;

	@Spy
	private ImageServerConfigurationsMock imageServerConfigurations;

	@Before
	public void init() {
		MockitoAnnotations.initMocks(this);
		imageServerConfigurations.setIiifServerAddress("localhost/iip-server?IIIF=");
	}

	@Test
	public void testGetImageFull() throws DMSClientException {
		HttpMethodMock mockedResponse = new HttpMethodMock();

		when(restClient.rawRequest(any(GetMethod.class), Mockito.anyString())).thenReturn(mockedResponse);
		iipServerImageProvider.getImage("emf:dummy");
		Mockito.verify(restClient, times(1))
				.rawRequest(any(GetMethod.class), eq("localhost/iip-server?IIIF=emf:dummy/full/full/0/default.jpg"));
	}

	@Test
	public void testGetImageWithDimension() throws DMSClientException {
		HttpMethodMock mockedResponse = new HttpMethodMock();

		when(restClient.rawRequest(any(GetMethod.class), Mockito.anyString())).thenReturn(mockedResponse);
		Dimension<Integer> size = new Dimension<>(100, 100);
		iipServerImageProvider.getImage("emf:dummy", size);
		Mockito.verify(restClient, times(1))
				.rawRequest(any(GetMethod.class), eq("localhost/iip-server?IIIF=emf:dummy/full/100,100/0/default.jpg"));
	}

	@Test
	public void testGetImageWithDimensionAndNullHeight() throws DMSClientException {
		HttpMethodMock mockedResponse = new HttpMethodMock();

		when(restClient.rawRequest(any(GetMethod.class), Mockito.anyString())).thenReturn(mockedResponse);
		Dimension<Integer> size = new Dimension<>(100, null);
		iipServerImageProvider.getImage("emf:dummy", size);
		Mockito.verify(restClient, times(1))
				.rawRequest(any(GetMethod.class), eq("localhost/iip-server?IIIF=emf:dummy/full/100,/0/default.jpg"));

	}

	@Test
	public void testGetImageWithDimensionAndNullWidth() throws DMSClientException {
		HttpMethodMock mockedResponse = new HttpMethodMock();

		when(restClient.rawRequest(any(GetMethod.class), Mockito.anyString())).thenReturn(mockedResponse);
		Dimension<Integer> size = new Dimension<>(null, 100);
		iipServerImageProvider.getImage("emf:dummy", size);
		Mockito.verify(restClient, times(1))
				.rawRequest(any(GetMethod.class), eq("localhost/iip-server?IIIF=emf:dummy/full/,100/0/default.jpg"));
	}
}
