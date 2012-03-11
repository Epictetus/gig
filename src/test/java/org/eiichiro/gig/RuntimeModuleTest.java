package org.eiichiro.gig;

import static org.junit.Assert.*;

import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class RuntimeModuleTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testComponents() {
		RuntimeModule module = new RuntimeModule();
		Set<Class<?>> components = module.components();
		assertSame(components, module.components());
		assertTrue(components.contains(Endpoint1.class));
		assertTrue(components.contains(Endpoint2.class));
		assertTrue(components.contains(Endpoint3.class));
		assertFalse(components.contains(Endpoint4.class));
		assertFalse(components.contains(Object1.class));
		assertFalse(components.contains(Object2.class));
		assertTrue(components.contains(Object3.class));
		assertFalse(components.contains(Object4.class));
		assertTrue(components.contains(Object5.class));
		assertTrue(components.contains(Object6.class));
		assertTrue(components.contains(Object7.class));
		assertFalse(components.contains(Object8.class));
		assertFalse(components.contains(Object9.class));
		assertFalse(components.contains(Component1.class));
		assertTrue(components.contains(Component2.class));
		assertFalse(components.contains(Component3.class));
	}

}
