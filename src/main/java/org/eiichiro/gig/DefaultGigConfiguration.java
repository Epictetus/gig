/*
 * Copyright (C) 2011 Eiichiro Uchiumi. All Rights Reserved.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 *     
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.eiichiro.gig;

import static org.eiichiro.jaguar.Jaguar.*;

import java.util.Set;

import org.eiichiro.jaguar.Module;
import org.eiichiro.jaguar.deployment.Production;
import org.eiichiro.monophony.DefaultConfiguration;
import org.eiichiro.monophony.Instantiator;
import org.eiichiro.monophony.Loader;
import org.eiichiro.monophony.annotation.Endpoint;
import org.eiichiro.reverb.lang.UncheckedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Predicate;
import com.google.common.collect.Sets;

/**
 * {@code DefaultGigConfiguration} is the default implementation of {@link GigConfiguration}.
 * You can extend this class to declare your own custom {@code GigConfiguration}.
 * 
 * @author <a href="mailto:eiichiro@eiichiro.org">Eiichiro Uchiumi</a>
 */
public class DefaultGigConfiguration extends DefaultConfiguration implements GigConfiguration {

	private Logger logger = LoggerFactory.getLogger(getClass());
	
	private Module module;
	
	private Instantiator instantiator;
	
	private Loader loader;
	
	/**
	 * Returns {@code Production}.
	 * 
	 * @return {@code Production}.
	 */
	@Override
	public Class<?> deployment() {
		return Production.class;
	}

	/**
	 * Returns {@code org.eiichiro.gig.BuildtimeModule_} (the {@code Module} 
	 * implementation that the [make-module] tool generates) if it could be 
	 * found on the classpath. If it could not be found, this method returns 
	 * {@link RuntimeModule} instance.
	 * 
	 * @return {@code org.eiichiro.gig.BuildtimeModule_} or {@code RuntimeModule}.
	 */
	@SuppressWarnings("unchecked")
	@Override
	public synchronized Module module() {
		if (module == null) {
			try {
				Class<? extends Module> clazz = (Class<? extends Module>) Class.forName("org.eiichiro.gig.BuildtimeModule_");
				module = clazz.newInstance();
			} catch (ClassNotFoundException e) {
				logger.info("Module [org.eiichiro.gig.BuildtimeModule_] could not be found; " +
						"Generate module with [modgen] tool to make the spinup faster");
				module = new RuntimeModule();
			} catch (Exception e) {
				logger.error("Failed to load module", e);
				throw new UncheckedException(e);
			}
		}
		
		return module;
	}
	
	/**
	 * Returns the {@code Instantiator} which instantiates an Web endpoint 
	 * class with Jaguar.
	 * 
	 * @return The {@code Instantiator} which instantiates an Web endpoint 
	 * class with Jaguar.
	 */
	public synchronized Instantiator instantiator() {
		if (instantiator == null) {
			instantiator = new Instantiator() {

				@Override
				public <T> T instantiate(Class<T> clazz) {
					return component(clazz);
				}
				
			};
		}
		
		return instantiator;
	}

	/**
	 * Returns the {@code Loader} which loads Web endpoint classes from the 
	 * {@code Module} which {@code #module()} method returns.
	 * 
	 * @return The {@code Loader} which loads Web endpoint classes from the 
	 * {@code Module} which {@code #module()} method returns.
	 */
	public synchronized Loader loader() {
		if (loader == null) {
			loader = new Loader() {

				@Override
				public Set<Class<?>> load() {
					return Sets.filter(module().components(), new Predicate<Class<?>>() {

						@Override
						public boolean apply(Class<?> clazz) {
							return clazz.isAnnotationPresent(Endpoint.class);
						}
						
					});
				}
				
			};
		}
		
		return loader;
	}
	
}
