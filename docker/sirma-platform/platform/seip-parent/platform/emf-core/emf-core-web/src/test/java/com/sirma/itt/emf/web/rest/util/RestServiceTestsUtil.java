/**
 *
 */
package com.sirma.itt.emf.web.rest.util;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.mockito.Matchers;

import com.sirma.itt.emf.rest.EmfRestService;
import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.domain.definition.DataTypeDefinition;
import com.sirma.itt.seip.domain.definition.label.LabelProvider;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.resources.EmfUser;

/**
 * Will be used to contain some common and reusable logic for rest service testing.
 *
 * @author A. Kunchev
 */
public class RestServiceTestsUtil {

	/**
	 * Prepares instance reference.
	 *
	 * @param typeConverter
	 *            the type converter, which conver method will be mocked.
	 * @return mocked instance reference
	 */
	public static InstanceReference prepareLinkInstance(TypeConverter typeConverter) {
		InstanceReference instanceReference = mock(InstanceReference.class);
		instanceReference.setId("123");
		DataTypeDefinition type = mock(DataTypeDefinition.class);
		when(type.getJavaClass()).then(a -> String.class);
		when(type.getJavaClassName()).then(a -> String.class.getName());
		when(type.getName()).thenReturn(DataTypeDefinition.TEXT);
		doReturn(type).when(instanceReference).getReferenceType();
		when(typeConverter.convert(Matchers.eq(InstanceReference.class), anyString())).thenReturn(instanceReference);
		return instanceReference;
	}

	/**
	 * Prepares mock for the user and user display name extraction.
	 *
	 * @param service
	 *            the service, which method getCurrentLoggedUser will be mocked
	 * @param labelProvider
	 *            the label provider, which method getValue will be mocked
	 * @param authenticationService
	 *            the AuthenticationService, which methods will be mocked
	 */
	public static void prepareUser(EmfRestService service, LabelProvider labelProvider) {
		when(labelProvider.getValue(anyString())).thenReturn("");
		EmfUser user = mock(EmfUser.class);
		user.setId("user");
		user.setDisplayName("user");
		when(service.getCurrentLoggedUser()).thenReturn(user);
	}

}
