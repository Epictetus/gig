package org.eiichiro.gig;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.testing.ServletTester;
import org.eiichiro.jazzmaster.Jazzmaster;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class GigListenerTest {

	private ServletTester tester = new ServletTester();
	
	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testContextInitializedServletContextEvent() throws Exception {
		tester.setContextPath("/gig");
		tester.addEventListener(new GigListener());
		tester.addServlet(DefaultServlet.class, "/");
		tester.start();
		assertThat(tester.getContext().getServletContext().getAttribute(GigConfiguration.class.getName()), instanceOf(DefaultGigConfiguration.class));
		tester.stop();
		
		Map<String, String> initParams = new HashMap<String, String>();
		initParams.put(GigListener.SETTINGS, GigConfigurationTestConfiguration.class.getName());
		tester.getContext().setInitParams(initParams);
		tester.start();
		assertThat(tester.getContext().getServletContext().getAttribute(GigConfiguration.class.getName()), instanceOf(GigConfigurationTestConfiguration.class));
		tester.stop();
		
		initParams = new HashMap<String, String>();
		initParams.put(GigListener.SETTINGS, GigListenerTest.class.getName());
		tester.getContext().setInitParams(initParams);
		tester.start();
		assertTrue(tester.getContext().isFailed());
		
		try {
			tester.stop();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			Jazzmaster.shutdown();
		}
		
		initParams = new HashMap<String, String>();
		initParams.put(GigListener.SETTINGS, "org.eiichiro.gig.NotFound");
		tester.getContext().setInitParams(initParams);
		tester.start();
		assertTrue(tester.getContext().isFailed());
		
		try {
			tester.stop();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			Jazzmaster.shutdown();
		}
	}

	@Test
	public void testDeploymentServletContext() throws Exception {
		GigListener listener = new GigListener();
		tester.setContextPath("/gig");
		tester.addEventListener(listener);
		tester.addServlet(DefaultServlet.class, "/");
		Map<String, String> initParams = new HashMap<String, String>();
		initParams.put(GigListener.SETTINGS, GigConfigurationTestConfiguration.class.getName());
		tester.getContext().setInitParams(initParams);
		tester.start();
		assertThat(listener.deployment(tester.getContext().getServletContext()), is((Object) Deployment1.class));
		tester.stop();
	}

	@Test
	public void testInstallServletContext() throws Exception {
		tester.setContextPath("/gig");
		tester.addEventListener(new GigListener());
		tester.addServlet(DefaultServlet.class, "/");
		Map<String, String> initParams = new HashMap<String, String>();
		initParams.put(GigListener.SETTINGS, GigConfigurationTestConfiguration.class.getName());
		tester.getContext().setInitParams(initParams);
		tester.start();
		assertFalse(Jazzmaster.installed(Endpoint1.class));
		assertFalse(Jazzmaster.installed(Endpoint2.class));
		assertTrue(Jazzmaster.installed(Endpoint3.class));
		assertFalse(Jazzmaster.installed(Endpoint4.class));
		assertFalse(Jazzmaster.installed(Object1.class));
		assertFalse(Jazzmaster.installed(Object2.class));
		assertTrue(Jazzmaster.installed(Object3.class));
		assertFalse(Jazzmaster.installed(Object4.class));
		assertTrue(Jazzmaster.installed(Object5.class));
		assertTrue(Jazzmaster.installed(Object6.class));
		assertTrue(Jazzmaster.installed(Object7.class));
		assertFalse(Jazzmaster.installed(Object8.class));
		assertFalse(Jazzmaster.installed(Object9.class));
		assertFalse(Jazzmaster.installed(Component1.class));
		assertTrue(Jazzmaster.installed(Component2.class));
		assertFalse(Jazzmaster.installed(Component3.class));
		tester.stop();
	}

}
