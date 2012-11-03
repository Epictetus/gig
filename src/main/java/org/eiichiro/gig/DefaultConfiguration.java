/*
 * Copyright (C) 2011-2012 Eiichiro Uchiumi. All Rights Reserved.
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

import java.lang.annotation.Annotation;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

import javassist.CtClass;

import org.eiichiro.jaguar.Builtin;
import org.eiichiro.jaguar.Component;
import org.eiichiro.jaguar.Module;
import org.eiichiro.jaguar.Stereotype;
import org.eiichiro.jaguar.deployment.Deployment;
import org.eiichiro.jaguar.deployment.Production;
import org.eiichiro.jaguar.inject.Binding;
import org.eiichiro.jaguar.scope.Scope;
import org.eiichiro.monophony.CtClassClassResolver;
import org.eiichiro.monophony.Instantiator;
import org.eiichiro.monophony.Loader;
import org.eiichiro.monophony.annotation.Endpoint;
import org.eiichiro.reverb.lang.ClassResolver;
import org.eiichiro.reverb.lang.UncheckedException;
import org.eiichiro.reverb.lang.ClassResolver.Matcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Predicate;
import com.google.common.collect.Sets;

/**
 * {@code DefaultConfiguration} is the default implementation of {@link Configuration}.
 * You can extend this class to declare your own {@code Configuration}.
 * 
 * @author <a href="mailto:eiichiro@eiichiro.org">Eiichiro Uchiumi</a>
 */
public class DefaultConfiguration extends
		org.eiichiro.monophony.DefaultConfiguration implements Configuration {

	private Logger logger = LoggerFactory.getLogger(getClass());
	
	private Module module;
	
	private Instantiator instantiator = new Instantiator() {

		@Override
		public <T> T instantiate(Class<T> clazz) {
			return component(clazz);
		}
		
	};
	
	private Loader loader = new Loader() {

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
	
	/**
	 * Constructs a new {@code DefaultConfiguration} instance.
	 * Instantiates {@code org.eiichiro.gig.Module_} (the {@code Module} 
	 * implementation that the 'modgen' tool generates) if it could be found on 
	 * the classpath. If it could not be found, this method instantiates an 
	 * anonymous {@code Module} instance which scans components from runtime 
	 * classpath.
	 */
	public DefaultConfiguration() {
	}
	
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
	 * Returns the {@code Module} instance.
	 * Returns {@code org.eiichiro.gig.Module_} (the {@code Module} 
	 * implementation that the 'modgen' tool generates) if it could be found on 
	 * the classpath. If it could not be found, this method returns an anonymous 
	 * {@code Module} instance which scans components from runtime classpath.
	 * 
	 * @return The {@code Module} instance.
	 */
	@Override
	public synchronized Module module() {
		if (module != null) {
			return module;
		}
		
		try {
			module = (Module) Class.forName("org.eiichiro.gig.Module_").newInstance();
			return module;
		} catch (ClassNotFoundException e) {
			// Go through.
		} catch (Exception e) {
			logger.error("Failed to instantiate module", e);
			throw new UncheckedException(e);
		}
		
		logger.debug("Slow? Generate module class on ahead with 'modgen' CLI tool to make the spinup faster");
		final Set<Class<?>> components = new HashSet<Class<?>>();
		
		try {
			ClassResolver<CtClass> resolver = new CtClassClassResolver();
			logger.debug("Scanning components from runtime classpath [" + resolver.urls() + "]");
			Set<CtClass> ctClasses = resolver.resolve(new Matcher<CtClass>() {
				
				@Override
				public boolean matches(CtClass ctClass) {
					try {
						int modifiers = ctClass.getModifiers();
						
						if (!Modifier.isPublic(modifiers)) {
							ctClass.detach();
							return false;
						}
						
						if (ctClass.getAnnotation(Endpoint.class) != null) {
							return true;
						}
						
						if (ctClass.isInterface() || Modifier.isAbstract(modifiers)
								|| ctClass.hasAnnotation(Builtin.class)) {
							ctClass.detach();
							return false;
						}
						
						CtClass superclass = ctClass.getSuperclass();
						
						while (superclass != null) {
							if (superclass.getName().equals(Component.class.getName())) {
								return true;
							}
							
							superclass = superclass.getSuperclass();
						}
						
						for (Object object : ctClass.getAnnotations()) {
							Class<? extends Annotation> annotationType = ((Annotation) object).annotationType();
							
							if (annotationType.isAnnotationPresent(Stereotype.class) 
									|| annotationType.isAnnotationPresent(Deployment.class) 
									|| annotationType.isAnnotationPresent(Binding.class) 
									|| annotationType.isAnnotationPresent(Scope.class)) {
								return true;
							}
						}
						
					} catch (Exception e) {
//						e.printStackTrace();
					}
					
					ctClass.detach();
					return false;
				}
			});
			
			for (CtClass ctClass : ctClasses) {
				try {
					components.add(Class.forName(ctClass.getName(), true, 
							Thread.currentThread().getContextClassLoader()));
				} catch (Exception e) {
					logger.error("Failed to load component class", e);
				}
			}
			
		} catch (Exception e) {
			logger.error("Failed to load component classes", e);
			throw new UncheckedException(e);
		}
		
		module = new Module() {

			@Override
			public Set<Class<?>> components() {
				return components;
			}
			
		};
		return module;
	}
	
	/**
	 * Returns the {@code Instantiator} which instantiates an Web endpoint 
	 * class with Jaguar.
	 * 
	 * @return The {@code Instantiator} which instantiates an Web endpoint 
	 * class with Jaguar.
	 */
	@Override
	public Instantiator instantiator() {
		return instantiator;
	}

	/**
	 * Returns the {@code Loader} which loads Web endpoint classes from the 
	 * {@code Module} which {@code #module()} method returns.
	 * 
	 * @return The {@code Loader} which loads Web endpoint classes from the 
	 * {@code Module} which {@code #module()} method returns.
	 */
	@Override
	public Loader loader() {
		return loader;
	}
	
}
