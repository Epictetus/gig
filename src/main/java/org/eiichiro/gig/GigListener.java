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

import static org.eiichiro.gig.Version.*;
import java.lang.reflect.Modifier;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;

import org.eiichiro.jazzmaster.Jazzmaster;
import org.eiichiro.jazzmaster.WebListener;
import org.eiichiro.reverb.lang.UncheckedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Predicate;
import com.google.common.collect.Sets;

/**
 * {@code GigListener} is a {@code WebListener} extension to bootstrap/shutdown 
 * Gig application.
 * 
 * @author <a href="mailto:eiichiro@eiichiro.org">Eiichiro Uchiumi</a>
 */
public class GigListener extends WebListener {

	public static final String SETTINGS = "org.eiichiro.gig.configuration";
	
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	private GigConfiguration configuration;
	
	static {
		Logger logger = LoggerFactory.getLogger(Version.class);
		logger.info("Gig " + MAJOR + "." + MINER + "." + BUILD);
//		logger.info("Copyright (C) 2011 Eiichiro Uchiumi. All Rights Reserved.");
	}
	
	/**
	 * Loads the {@link GigConfiguration} specified by the deployment descriptor and 
	 * sets it to the {@code ServletContext}.
	 * If no {@code GigConfiguration} is specified, this method uses 
	 * {@link DefaultGigConfiguration}.
	 * 
	 * @param sce {@code ServletContextEvent}.
	 */
	@Override
	public void contextInitialized(ServletContextEvent sce) {
		String clazz = sce.getServletContext().getInitParameter(SETTINGS);
		GigConfiguration configuration = null;
		
		if (clazz == null) {
			configuration = new DefaultGigConfiguration();
		} else {
			try {
				configuration = (GigConfiguration) Class.forName(clazz).newInstance();
			} catch (Exception e) {
				logger.error("Failed to load configuration", e);
				throw new UncheckedException(e);
			}
		}
		
		sce.getServletContext().setAttribute(GigConfiguration.class.getName(), configuration);
		this.configuration = configuration;
		super.contextInitialized(sce);
	}
	
	/**
	 * Returns the deployment qualifier from the {@code GigConfiguration} loaded.
	 * 
	 * @param context {@code ServletContext}.
	 * @return The deployment qualifier from the {@code GigConfiguration} loaded.
	 */
	@Override
	protected Class<?> deployment(ServletContext context) {
		return configuration.deployment();
	}
	
	/**
	 * Installs service component classes to the Jazzmaster container from the 
	 * {@link Module} which the {@code GigConfiguration#module()} returns.
	 * 
	 * @param context {@code ServletContext}.
	 */
	@Override
	protected void install(ServletContext context) {
		Jazzmaster.install(Sets.filter(configuration.module().components(), new Predicate<Class<?>>() {

			@Override
			public boolean apply(Class<?> clazz) {
				return (!clazz.isInterface() && !Modifier.isAbstract(clazz.getModifiers()));
			}
			
		}));
	}
	
}
