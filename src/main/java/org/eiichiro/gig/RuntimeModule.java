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

import java.lang.annotation.Annotation;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

import javassist.CtClass;

import org.eiichiro.jazzmaster.Builtin;
import org.eiichiro.jazzmaster.Component;
import org.eiichiro.jazzmaster.Module;
import org.eiichiro.jazzmaster.Stereotype;
import org.eiichiro.jazzmaster.deployment.Deployment;
import org.eiichiro.jazzmaster.inject.Binding;
import org.eiichiro.jazzmaster.scope.Scope;
import org.eiichiro.monophony.CtClassClassResolver;
import org.eiichiro.monophony.annotation.Endpoint;
import org.eiichiro.reverb.lang.ClassResolver;
import org.eiichiro.reverb.lang.UncheckedException;
import org.eiichiro.reverb.lang.ClassResolver.Matcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@code RuntimeModule} is the built-in implementation of {@code Module} which 
 * loads the components from the runtime classpath.
 * 
 * @author <a href="mailto:eiichiro@eiichiro.org">Eiichiro Uchiumi</a>
 */
public class RuntimeModule implements Module {

	private static Logger logger = LoggerFactory.getLogger(RuntimeModule.class);
	
	private Set<Class<?>> components = new HashSet<Class<?>>();
	
	/**
	 * Constructs a new {@code RuntimeModule} by scanning components from 
	 * runtime classpath.
	 */
	public RuntimeModule() {
		try {
			ClassResolver<CtClass> resolver = new CtClassClassResolver();
			logger.info("Scanning components from runtime classpath [" + resolver.urls() + "]");
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
					components.add(Class.forName(ctClass.getName(), true, Thread.currentThread().getContextClassLoader()));
				} catch (Exception e) {
					logger.error("Failed to load component class", e);
				}
			}
			
		} catch (Exception e) {
			logger.error("Failed to load component classes", e);
			throw new UncheckedException(e);
		}
	}
	
	/**
	 * Returns the components loaded from the runtime classpath.
	 * The components consist of Web endpoint interfaces, Web endpoint abstract 
	 * classes, Web endpoint concrete classes and Jazzmaster component classes.
	 * 
	 * @return The components loaded from the runtime classpath.
	 */
	@Override
	public Set<Class<?>> components() {
		return components;
	}

}
