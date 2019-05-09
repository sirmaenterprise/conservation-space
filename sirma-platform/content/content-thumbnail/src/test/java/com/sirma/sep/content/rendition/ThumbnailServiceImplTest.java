package com.sirma.sep.content.rendition;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.convert.TypeConverterUtil;
import com.sirma.itt.seip.plugin.Plugins;

/**
 * ThumbnailService test.
 *
 * @author A. Kunchev
 */
@Test
public class ThumbnailServiceImplTest {

	@InjectMocks
	private ThumbnailServiceImpl service = new ThumbnailServiceImpl();

	private List<ThumbnailProvider> providersList = new ArrayList<>();

	@Spy
	private Plugins<ThumbnailProvider> providers = new Plugins<>(null, providersList);

	@Mock
	private ThumbnailDao thumbnailDao;
	@Mock
	private ThumbnailProvider provider;
	@Mock
	private TypeConverter typeConverter;

	/**
	 * Setups the mocks.
	 */
	@BeforeMethod
	public void setup() {
		MockitoAnnotations.initMocks(this);
		providersList.clear();
		providersList.add(provider);
		TypeConverterUtil.setTypeConverter(typeConverter);
	}

	public void register_validData() {
		when(provider.createThumbnailEndPoint("emf:source")).thenReturn("thumbnailSourceId");
		when(provider.getName()).thenReturn("provider");
		when(thumbnailDao.getOrCreateThumbnailMappingEntity(any(), any())).thenReturn(new ThumbnailMappingEntity());

		service.register("emf:target", "emf:source");
		verify(thumbnailDao).saveThumbnail("emf:source", null, "thumbnailSourceId", "provider");
		verify(thumbnailDao).persist(any());
	}

}
