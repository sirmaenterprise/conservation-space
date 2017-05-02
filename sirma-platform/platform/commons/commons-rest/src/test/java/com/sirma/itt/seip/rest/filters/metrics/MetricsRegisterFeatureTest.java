package com.sirma.itt.seip.rest.filters.metrics;

import static com.sirma.itt.seip.util.EqualsHelper.nullSafeEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.ws.rs.Path;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.FeatureContext;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.monitor.Statistics;
import com.sirma.itt.seip.security.context.SecurityContext;

/**
 * Test for {@link MetricsRegisterFeature}
 *
 * @author BBonev
 */
public class MetricsRegisterFeatureTest {

	@InjectMocks
	private MetricsRegisterFeature metricsFeature;
	@Mock
	private Statistics statistics;
	@Mock
	private SecurityContext securityContext;
	@Mock
	private ResourceInfo resourceInfo;
	@Mock
	private FeatureContext context;

	@Before
	public void beforeMethod() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void should_registerFilter_missingMethodAnnotation() throws Exception {
		mockRestEndpoint("method1");

		metricsFeature.configure(resourceInfo, context);
		verify(context).register(argThat(matches("classLevel")));
	}

	@Test
	public void should_registerFilter_presentMethodAnnotation() throws Exception {
		mockRestEndpoint("method2");

		metricsFeature.configure(resourceInfo, context);
		verify(context).register(argThat(matches("classLevel_method")));
	}

	@Test
	public void should_registerFilter_withNonLeadingSlash() throws Exception {
		mockRestEndpoint("method3");

		metricsFeature.configure(resourceInfo, context);
		verify(context).register(argThat(matches("classLevel_method")));
	}

	@Test
	public void should_registerFilter_withFileExtension() throws Exception {
		mockRestEndpoint("method4");

		metricsFeature.configure(resourceInfo, context);
		verify(context).register(argThat(matches("classLevel_method_json")));
	}

	@Test
	public void should_registerFilter_pathParam() throws Exception {
		mockRestEndpoint("method5");

		metricsFeature.configure(resourceInfo, context);
		verify(context).register(argThat(matches("classLevel_method__pathParam_")));
	}

	@Test
	public void should_registerFilter_doublePathParam() throws Exception {
		mockRestEndpoint("method6");

		metricsFeature.configure(resourceInfo, context);
		verify(context).register(argThat(matches("classLevel__pathParam1___pathParam2_")));
	}

	@Test
	public void should_registerFilter_customPathVariable() throws Exception {
		mockRestEndpoint("method7");

		metricsFeature.configure(resourceInfo, context);
		verify(context).register(argThat(matches("classLevel__pathParam1___username___a_zA_Z__a_zA_Z_0_9__")));
	}

	@Test
	public void should_registerFilter_endWithSlash() throws Exception {
		mockRestEndpoint("method8");

		metricsFeature.configure(resourceInfo, context);
		verify(context).register(argThat(matches("classLevel_method")));
	}

	@Test
	public void shouldNot_registerFilter_whenMissingClassAnnotation() throws Exception {
		when(resourceInfo.getResourceClass()).then(a -> MetricsRegisterFeatureTest.class);
		when(resourceInfo.getResourceMethod()).then(a -> ValidPath1.class.getMethod("method1"));

		metricsFeature.configure(resourceInfo, context);
		verify(context, never()).register(any(Object.class));
	}

	private static Matcher<Object> matches(String path) {
		return new CustomMatcher(path);
	}

	private void mockRestEndpoint(String method) {
		when(resourceInfo.getResourceClass()).then(a -> ValidPath1.class);
		when(resourceInfo.getResourceMethod()).then(a -> ValidPath1.class.getMethod(method));
	}

	private static final class CustomMatcher extends BaseMatcher<Object> {

		private final String path;

		private CustomMatcher(String path) {
			this.path = path;
		}

		@Override
		public boolean matches(Object item) {
			return nullSafeEquals(((MetricsRequestFilter) item).getPath(), path);
		}

		@Override
		public void describeTo(Description description) {
			description.appendText("Expected path " + path + " but didn't get it");
		}
	}

	@Path("/classLevel")
	private static class ValidPath1 {
		public void method1() { // nothing
		}

		@Path("/method")
		public void method2() { // nothing
		}

		@Path("method")
		public void method3() { // nothing
		}

		@Path("//method.json")
		public void method4() { // nothing
		}

		@Path("method/{pathParam}")
		public void method5() { // nothing
		}

		@Path("{pathParam1}/{pathParam2}")
		public void method6() { // nothing
		}

		@Path("{pathParam1}/{username: [a-zA-Z][a-zA-Z_0-9]}")
		public void method7() { // nothing
		}

		@Path("method/")
		public void method8() { // nothing
		}
	}
}
