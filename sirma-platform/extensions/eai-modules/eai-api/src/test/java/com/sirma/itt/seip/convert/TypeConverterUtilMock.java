package com.sirma.itt.seip.convert;

import org.mockito.ArgumentMatcher;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.sirma.itt.seip.ShortUri;
import com.sirma.itt.seip.Uri;

/**
 * Access mock to {@link TypeConverterUtil}
 *
 * @author bbanchev
 */
public class TypeConverterUtilMock {

	/**
	 * Setter method for typeConverter.
	 *
	 * @param typeConverter
	 *            the typeConverter to set
	 */
	public static void setUpTypeConverter() {

		TypeConverter converter = Mockito.mock(TypeConverter.class);
		Mockito.when(converter.convert(Mockito.argThat(new IsUri()), Matchers.any(String.class))).thenAnswer(
				new Answer<Uri>() {
					public Uri answer(InvocationOnMock invocation) {
						String uri = (String) invocation.getArguments()[1];
						if (uri.indexOf(":") > 0) {
							return new ShortUri(uri);
						} else if (uri.indexOf("#") > 0) {
							String[] uriParts = uri.split("#");
							return new Uri() {
								private static final long serialVersionUID = 1L;

								@Override
								public String getNamespace() {
									return uriParts[0];
								}

								@Override
								public String getLocalName() {
									return uriParts[1];
								}
							};
						}
						return null;
					}
				});
		TypeConverterUtil.setTypeConverter(converter);
	}

	static class IsUri extends ArgumentMatcher<Class<?>> {
		public boolean matches(Object clz) {
			if (clz instanceof Class) {
				return ((Class<?>) clz).getName().equals(Uri.class.getName());
			}
			return false;
		}

	}
}
