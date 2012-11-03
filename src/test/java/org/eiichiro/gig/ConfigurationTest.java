package org.eiichiro.gig;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ConfigurationTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testDeployment() {
		Configuration configuration = new ConfigurationTestConfiguration();
		assertThat(configuration.deployment(), is((Object) Deployment1.class));
	}

	@Test
	public void testModule() {
		Configuration configuration = new ConfigurationTestConfiguration();
		assertThat(configuration.module(), instanceOf(ConfigurationTestModule.class));
	}

}
