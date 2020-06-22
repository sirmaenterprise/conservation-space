package com.sirma.itt.seip.content.expressions;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mockito.Mockito;
import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import com.sirma.itt.seip.util.ReflectionUtils;
import com.sirma.itt.seip.ShortUri;
import com.sirma.itt.seip.content.ContentResourceManagerService;
import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.InstanceType;
import com.sirma.itt.seip.expressions.BaseEvaluatorTest;
import com.sirma.itt.seip.expressions.ExpressionContext;
import com.sirma.itt.seip.expressions.ExpressionEvaluator;
import com.sirma.itt.seip.expressions.ExpressionsManager;
import com.sirma.itt.seip.security.context.SecurityContext;
import com.sirma.sep.content.ContentInfo;

/**
 * Test for the thumbnail evaluator class.
 *
 * @author Nikolay Ch
 */
public class ThumbnailEvaluatorTest extends BaseEvaluatorTest {

	private ContentResourceManagerService contentService;

	private TypeConverter typeConverter;

	private SecurityContext securityContext;

	@Override
	protected List<ExpressionEvaluator> initializeEvaluators(ExpressionsManager manager, TypeConverter converter) {
		List<ExpressionEvaluator> list = super.initializeEvaluators(manager, converter);
		ThumbnailEvaluator evaluator = new ThumbnailEvaluator();
		typeConverter = Mockito.mock(TypeConverter.class);
		ReflectionUtils.setFieldValue(evaluator, "typeConverter", typeConverter);
		contentService = Mockito.mock(ContentResourceManagerService.class);
		ReflectionUtils.setFieldValue(evaluator, "contentService", contentService);
		securityContext = Mockito.mock(SecurityContext.class);
		ReflectionUtils.setFieldValue(evaluator, "securityContext", securityContext);
		list.add(initEvaluator(evaluator, manager, createTypeConverter()));
		return list;
	}

	@Test
	public void testGettingThumbnailWithoutInstanceType() {
		ExpressionsManager manager = createManager();
		EmfInstance target = new EmfInstance();
		target.setId("emf:id");
		InstanceType type = new InstanceType.DefaultInstanceType("emf:Test");
		target.setType(type);
		Mockito.when(typeConverter.convert(ShortUri.class, "emf:Test")).thenReturn(new ShortUri("test"));
		Mockito.when(contentService.getContent(Mockito.any(Serializable.class), Mockito.any(String.class)))
				.thenReturn(new ContentInfo.NonExistingContent());
		ExpressionContext context = manager.createDefaultContext(target, null, null);
		AssertJUnit.assertEquals("/images/instance-icons/documentinstance-icon-64.png",
				manager.evaluateRule("${thumbnailUri(64)}", String.class, context));
	}

	/**
	 * Test if the evaluator return the thumbnail if it provided.
	 */
	@Test
	public void testGettingThumbnailForClassInstance() {
		ExpressionsManager manager = createManager();
		EmfInstance target = new EmfInstance();
		target.setId("emf:id");
		Map<String, Serializable> properties = new HashMap<String, Serializable>();
		properties.put("thumbnailImage", "thumbnail");
		target.setProperties(properties);
		InstanceType type = new InstanceType.DefaultInstanceType("emf:Test");
		target.setType(type);
		ExpressionContext context = manager.createDefaultContext(target, null, null);

		AssertJUnit.assertEquals("thumbnail", manager.evaluateRule("${thumbnailUri(64)}", String.class, context));
	}

	/**
	 * Test if the evaluator handles null instance type.
	 */
	@Test
	public void testHandleNullInstanceType() {
		ExpressionsManager manager = createManager();
		EmfInstance target = new EmfInstance();
		target.setId("emf:id");
		ExpressionContext context = manager.createDefaultContext(target, null, null);

		AssertJUnit.assertNull(manager.evaluateRule("${thumbnailUri(64)}", String.class, context));
	}

	/**
	 * Tests if the evaluator returns correct path to the default instance image.
	 */
	@Test
	public void testGettingDefaultThumbnailForClassInstance() {
		ExpressionsManager manager = createManager();
		EmfInstance target = new EmfInstance();
		target.setId("emf:id");
		InstanceType type = new InstanceType.DefaultInstanceType("emf:Test");
		target.setType(type);
		ExpressionContext context = manager.createDefaultContext(target, null, null);

		Mockito.when(typeConverter.convert(ShortUri.class, "emf:Test")).thenReturn(new ShortUri("test"));
		Mockito.when(contentService.getContent(Mockito.any(Serializable.class), Mockito.any(String.class)))
				.thenReturn(new ContentInfo.NonExistingContent());

		AssertJUnit.assertEquals("/images/instance-icons/documentinstance-icon-64.png",
				manager.evaluateRule("${thumbnailUri(64)}", String.class, context));
	}

	/**
	 * Test if the evaluator returns correct uri to the rest service when the class icon is provided.
	 */
	@Test
	public void testGettingClassThumbnailForInstance() {
		ExpressionsManager manager = createManager();
		EmfInstance target = new EmfInstance();
		target.setId("emf:id");
		InstanceType type = new InstanceType.DefaultInstanceType("emf:Test");
		target.setType(type);
		ExpressionContext context = manager.createDefaultContext(target, null, null);
		ContentInfo mockInfo = Mockito.mock(ContentInfo.class);
		Mockito.when(typeConverter.convert(ShortUri.class, "emf:Test")).thenReturn(new ShortUri("test"));
		Mockito.when(contentService.getContent(Mockito.any(Serializable.class), Mockito.any(String.class)))
				.thenReturn(mockInfo);
		Mockito.when(securityContext.getCurrentTenantId()).thenReturn("tenantId");
		Mockito.when(mockInfo.getContentId()).thenReturn("contentId");
		Mockito.when(mockInfo.exists()).thenReturn(true);
		AssertJUnit.assertEquals("/remote/api/content/resource/tenantId/contentId",
				manager.evaluateRule("${thumbnailUri(64)}", String.class, context));
	}

}
