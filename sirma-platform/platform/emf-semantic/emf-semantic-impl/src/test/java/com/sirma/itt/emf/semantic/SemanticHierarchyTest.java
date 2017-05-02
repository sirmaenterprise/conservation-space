package com.sirma.itt.emf.semantic;

import java.util.Iterator;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.domain.instance.ClassInstance;

public class SemanticHierarchyTest {
	
	@Mock
	private SemanticDefinitionServiceImpl service;
	
	@Before
	public void init() {
		MockitoAnnotations.initMocks(this);
		
		ClassInstance sub = new ClassInstance();
		sub.setId("sub");
		
		ClassInstance root = new ClassInstance();
		root.setId("root");
		root.getSubClasses().put("sub", sub);
		
		sub.getSubClasses().put("root", root);
		
		Mockito.when(service.getClassInstance("root")).thenReturn(root);
		Mockito.when(service.getClassInstance("sub")).thenReturn(sub);
		
		Mockito.when(service.collectSubclasses(Mockito.anyString())).thenCallRealMethod();
	}
	
	@Test
	public void testGetSubclasses() {
		Assert.assertTrue(service.collectSubclasses(null).isEmpty());
		Assert.assertTrue(service.collectSubclasses("").isEmpty());
		
		Set<ClassInstance> subclasses = service.collectSubclasses("root");
		Assert.assertTrue(subclasses.size() == 2);
		
		Iterator<ClassInstance> iterator = subclasses.iterator();
		Assert.assertEquals("root", iterator.next().getId().toString());
		Assert.assertEquals("sub", iterator.next().getId().toString());
	}
}
