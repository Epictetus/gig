package org.eiichiro.gig;

import org.eiichiro.jaguar.Module;

public class GigConfigurationTestConfiguration extends DefaultGigConfiguration {

	@Override
	public Class<?> deployment() {
		return Deployment1.class;
	}

	@Override
	public Module module() {
		return new GigConfigurationTestModule();
	}

}
