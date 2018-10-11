package com.sirma.sep.content.rendition;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
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
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.plugin.Plugins;
import com.sirma.itt.seip.testutil.mocks.InstanceReferenceMock;
import com.sirma.sep.content.rendition.ThumbnailDao;
import com.sirma.sep.content.rendition.ThumbnailMappingEntity;
import com.sirma.sep.content.rendition.ThumbnailProvider;
import com.sirma.sep.content.rendition.ThumbnailServiceImpl;

/**
 * ThumbnailService test.
 *
 * @author A. Kunchev
 */
@Test
public class ThumbnailServiceImplTest {

	private static final String DEFAULT_PURPOSE = "default";

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

	// ----------------- copyThumbnailFromSource(Instance, Instance) --------------------

	/**
	 * Null data
	 */
	public void copyThumbnailFromSource_nullArgs() {
		service.copyThumbnailFromSource(null, null);
		service.copyThumbnailFromSource(mock(InstanceReference.class), null);
		service.copyThumbnailFromSource(null, mock(InstanceReference.class));
		verify(thumbnailDao, never()).persist(any());
	}

	/**
	 * No entity id.
	 */
	public void copyThumbnailFromSource_notNullParams_noThumbnailRelations() {
		InstanceReference instance = mock(InstanceReference.class);
		InstanceReference instanceSource = mock(InstanceReference.class);
		ThumbnailMappingEntity entity = new ThumbnailMappingEntity();
		when(thumbnailDao.getOrCreateThumbnailMappingEntity(instanceSource, DEFAULT_PURPOSE)).thenReturn(entity);
		service.copyThumbnailFromSource(instance, instanceSource);
		verify(thumbnailDao, never()).persist(any());
	}

	/**
	 * Successful.
	 */
	public void copyThumbnailFromSource_notNullParams_saved_new_thumbnailMappingEntity() {
		InstanceReference instance = mock(InstanceReference.class);
		InstanceReference instanceSource = mock(InstanceReference.class);
		ThumbnailMappingEntity entity = new ThumbnailMappingEntity();
		entity.setId(5L);
		when(thumbnailDao.getOrCreateThumbnailMappingEntity(instanceSource, DEFAULT_PURPOSE)).thenReturn(entity);
		service.copyThumbnailFromSource(instance, instanceSource);
		verify(thumbnailDao).persist(any());
	}

	public void register_notValidData() {
		EmfInstance target = new EmfInstance();
		EmfInstance source = new EmfInstance();

		service.register(null, null);
		service.register(target, null);
		service.register(null, source);

		target.setReference(new InstanceReferenceMock());

		service.register(target, source);

		verify(thumbnailDao, never()).getOrCreateThumbnailMappingEntity(any(), any());
	}

	public void register_validData() {
		EmfInstance target = new EmfInstance();
		target.setId("emf:target");
		EmfInstance source = new EmfInstance();
		source.setId("emf:source");
		target.setReference(new InstanceReferenceMock());

		when(provider.createThumbnailEndPoint(source)).thenReturn("thumbnailSourceId");
		when(provider.getName()).thenReturn("provider");
		when(thumbnailDao.getOrCreateThumbnailMappingEntity(any(), any())).thenReturn(new ThumbnailMappingEntity());

		service.register(target, source);
		verify(thumbnailDao).saveThumbnail("emf:source", null, "thumbnailSourceId", "provider");
		verify(thumbnailDao).persist(any());
	}

}
