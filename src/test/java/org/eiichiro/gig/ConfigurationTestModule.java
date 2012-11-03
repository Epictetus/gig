package org.eiichiro.gig;

import java.util.Set;

import org.eiichiro.jaguar.Module;

public class ConfigurationTestModule implements Module {

	private Configuration configuration = new DefaultConfiguration();
	
	@Override
	public Set<Class<?>> components() {
		return configuration.module().components();
	}
	
}
