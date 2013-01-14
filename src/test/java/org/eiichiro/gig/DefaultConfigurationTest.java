package org.eiichiro.gig;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

import java.util.Set;

import org.eiichiro.jaguar.Jaguar;
import org.eiichiro.jaguar.Module;
import org.eiichiro.jaguar.deployment.Production;
import org.eiichiro.bootleg.Instantiator;
import org.eiichiro.bootleg.Loader;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class DefaultConfigurationTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testDeployment() {
		DefaultConfiguration configuration = new DefaultConfiguration();
		assertThat(configuration.deployment(), is((Object) Production.class));
	}

	@Test
	public void testModule() {
		DefaultConfiguration configuration = new DefaultConfiguration();
		Module module = configuration.module();
		assertSame(module, configuration.module());
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

	@Test
	public void testInstantiator() {
		Jaguar.bootstrap();
		Jaguar.install(Endpoint3.class);
		DefaultConfiguration configuration = new DefaultConfiguration();
		Instantiator instantiator = configuration.instantiator();
		assertNotNull(instantiator.instantiate(Endpoint3.class));
		assertNotNull(instantiator.instantiate(Endpoint1.class));
		assertNotNull(instantiator.instantiate(Endpoint2.class));
		assertNull(instantiator.instantiate(Object3.class));
		assertSame(instantiator, configuration.instantiator());
		Jaguar.shutdown();
	}

	@Test
	public void testLoader() {
		DefaultConfiguration configuration = new DefaultConfiguration();
		Loader loader = configuration.loader();
		Set<Class<?>> endpoints = loader.load();
		assertThat(endpoints.size(), is(3));
		assertTrue(endpoints.contains(Endpoint1.class));
		assertTrue(endpoints.contains(Endpoint2.class));
		assertTrue(endpoints.contains(Endpoint3.class));
		assertSame(loader, configuration.loader());
	}

}
