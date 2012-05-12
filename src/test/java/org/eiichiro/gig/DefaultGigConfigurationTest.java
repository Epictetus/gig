package org.eiichiro.gig;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

import java.util.Set;

import org.eiichiro.jaguar.Jaguar;
import org.eiichiro.jaguar.Module;
import org.eiichiro.jaguar.deployment.Production;
import org.eiichiro.monophony.Instantiator;
import org.eiichiro.monophony.Loader;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class DefaultGigConfigurationTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testDeployment() {
		DefaultGigConfiguration configuration = new DefaultGigConfiguration();
		assertThat(configuration.deployment(), is((Object) Production.class));
	}

	@Test
	public void testModule() {
		DefaultGigConfiguration configuration = new DefaultGigConfiguration();
		Module module = configuration.module();
		assertThat(module, instanceOf(RuntimeModule.class));
		assertSame(module, configuration.module());
	}

	@Test
	public void testInstantiator() {
		Jaguar.bootstrap();
		Jaguar.install(Endpoint3.class);
		DefaultGigConfiguration configuration = new DefaultGigConfiguration();
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
		DefaultGigConfiguration configuration = new DefaultGigConfiguration();
		Loader loader = configuration.loader();
		Set<Class<?>> endpoints = loader.load();
		assertThat(endpoints.size(), is(3));
		assertTrue(endpoints.contains(Endpoint1.class));
		assertTrue(endpoints.contains(Endpoint2.class));
		assertTrue(endpoints.contains(Endpoint3.class));
		assertSame(loader, configuration.loader());
	}

}
