package com.sirma.itt.seip.expressions;

import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.expressions.ExpressionContext;
import com.sirma.itt.seip.expressions.ExpressionEvaluator;
import com.sirma.itt.seip.resources.EmfUser;
import com.sirma.itt.seip.resources.Resource;
import com.sirma.itt.seip.resources.ResourceProperties;
import com.sirma.itt.seip.resources.ResourceService;

/**
 * Test for {@link UserPropertiesEvaluator}
 *
 * @author BBonev
 */
public class UserPropertiesEvaluatorTest extends BaseEvaluatorTest {

	@InjectMocks
	private UserPropertiesEvaluator userPropertiesEvaluator;
	@Mock
	private ResourceService resourceService;

	@Override
	protected List<ExpressionEvaluator> initializeEvaluators(ExpressionsManager manager,
			TypeConverter converter) {
		List<ExpressionEvaluator> evaluators = super.initializeEvaluators(manager, converter);
		evaluators.add(initEvaluator(userPropertiesEvaluator, manager, converter));
		return evaluators;
	}

	@Test
	public void getUserNameResolveFromInstance() {

		Resource user = new EmfUser();
		user.setDisplayName("User Name");
		when(resourceService.findResource("emf:user")).thenReturn(user);

		ExpressionsManager manager = createManager();
		Instance instance = new EmfInstance();
		instance.add("instanceProperty", "emf:user");
		ExpressionContext context = manager.createDefaultContext(instance, null, null);
		String result = manager.evaluateRule("${user([instanceProperty])}", String.class, context);

		Assert.assertEquals(result, "User Name");
	}

	@Test
	public void getUserNameResolveFromInstance_noValue() {

		Resource user = new EmfUser();
		user.setDisplayName("User Name");
		when(resourceService.findResource("emf:user")).thenReturn(user);

		ExpressionsManager manager = createManager();
		Instance instance = new EmfInstance();
		instance.getOrCreateProperties();

		ExpressionContext context = manager.createDefaultContext(instance, null, null);
		String result = manager.evaluateRule("${user([instanceProperty])}", String.class, context);
		Assert.assertNull(result);
	}

	@Test
	public void getUserName() {

		Resource user = new EmfUser();
		user.setDisplayName("User Name");
		when(resourceService.findResource("emf:user")).thenReturn(user);

		ExpressionsManager manager = createManager();

		ExpressionContext context = manager.createDefaultContext(null, null, null);
		String result = manager.evaluateRule("${user(emf:user)}", String.class, context);
		Assert.assertEquals(result, "User Name");
	}

	@Test
	public void getMultivalueUserUserName() {

		Resource user1 = new EmfUser();
		user1.setDisplayName("User Name 1");
		when(resourceService.findResource("emf:user")).thenReturn(user1);
		Resource user2 = new EmfUser();
		user2.setDisplayName("User Name 2");
		when(resourceService.findResource("emf:user@tenant.com")).thenReturn(user2);

		ExpressionsManager manager = createManager();

		ExpressionContext context = manager.createDefaultContext(null, null, null);
		String result = manager.evaluateRule("${user(emf:user,emf:user@tenant.com)}", String.class, context);
		Assert.assertEquals(result, "User Name 1, User Name 2");
	}

	@Test
	public void getMultivalueUserProperty() {

		Resource user1 = new EmfUser();
		user1.setDisplayName("User Name 1");
		when(resourceService.findResource("emf:user")).thenReturn(user1);
		Resource user2 = new EmfUser();
		user2.setDisplayName("User Name 2");
		when(resourceService.findResource("emf:user@tenant.com")).thenReturn(user2);

		ExpressionsManager manager = createManager();

		Instance instance = new EmfInstance();
		instance.add("someProperty", new LinkedList<>(Arrays.asList("emf:user", "emf:user@tenant.com")));

		ExpressionContext context = manager.createDefaultContext(instance, null, null);
		String result = manager.evaluateRule("${user([someProperty])}", String.class, context);
		Assert.assertEquals(result, "User Name 1, User Name 2");
	}

	@Test
	public void getUserId() {

		EmfUser user = new EmfUser();
		user.setDisplayName("User Name");
		user.setTenantId("tenant.com");
		user.setId("emf:user");
		when(resourceService.findResource("emf:user")).thenReturn(user);

		ExpressionsManager manager = createManager();

		ExpressionContext context = manager.createDefaultContext(null, null, null);
		String result = manager.evaluateRule("${user(emf:user).id}", String.class, context);
		Assert.assertEquals(result, "emf:user");
	}

	@Test
	public void getUserNameProperty() {

		EmfUser user = new EmfUser();
		user.setDisplayName("User Name");
		user.setTenantId("tenant.com");
		user.setName("user");
		when(resourceService.findResource("emf:user")).thenReturn(user);

		ExpressionsManager manager = createManager();

		ExpressionContext context = manager.createDefaultContext(null, null, null);
		String result = manager.evaluateRule("${user(emf:user).name}", String.class, context);
		Assert.assertEquals(result, "user");
	}

	@Test
	public void getUserProperty() {

		EmfUser user = new EmfUser();
		user.setDisplayName("User Name");
		user.setTenantId("tenant.com");
		user.add(ResourceProperties.EMAIL, "user@tenant.com");
		user.setName("user");
		when(resourceService.findResource("emf:user")).thenReturn(user);

		ExpressionsManager manager = createManager();

		ExpressionContext context = manager.createDefaultContext(null, null, null);
		String result = manager.evaluateRule("${user(emf:user)." + ResourceProperties.EMAIL + "}", String.class,
				context);
		Assert.assertEquals(result, "user@tenant.com");
	}
}
