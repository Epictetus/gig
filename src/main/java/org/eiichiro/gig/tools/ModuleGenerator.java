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
package org.eiichiro.gig.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.annotation.Annotation;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

import javassist.CtClass;

import org.eiichiro.gig.Main;
import org.eiichiro.jazzmaster.Builtin;
import org.eiichiro.jazzmaster.Component;
import org.eiichiro.jazzmaster.Stereotype;
import org.eiichiro.jazzmaster.deployment.Deployment;
import org.eiichiro.jazzmaster.inject.Binding;
import org.eiichiro.jazzmaster.scope.Scope;
import org.eiichiro.monophony.CtClassClassResolver;
import org.eiichiro.monophony.annotation.Endpoint;
import org.eiichiro.reverb.lang.ClassResolver;
import org.eiichiro.reverb.lang.ClassResolver.Matcher;

/**
 * {@code ModuleGenerator} is a tool to generate {@code Module} implementation 
 * class by scanning components to be deployed (Web endpoint classes and service 
 * component classes) from the current classpath.
 * <pre>
 * usage: ModuleGenerator
 *  -d,--destination &lt;directory&gt;		directory path that the Module file 
 *  										being stored
 * </pre>
 * 
 * @see Module
 * @author <a href="mailto:eiichiro@eiichiro.org">Eiichiro Uchiumi</a>
 */
public class ModuleGenerator {

	private static final String TEMPLATE = "org/eiichiro/gig/tools/BuildtimeModule_.java.template";

	private static final String PACKAGE = "org.eiichiro.gig";
	
	private static final String FILENAME = "BuildtimeModule_.java";
	
	private static final String INDENT = "\t\t";
	
	private final File destination;
	
	/**
	 * Runs [modgen] tool.
	 * 
	 * @param args The command line arguments.
	 */
	public static void main(String[] args) {
		Main.main(args);
		System.out.println("Starting [modgen]");
		File destination = null;
		
		for (int i = 0; i < args.length; i++) {
			if (args[i].equals("-d") || args[i].equals("--destination")) {
				if (args.length > i) {
					destination = new File(args[i + 1]);
				}
			}
		}
		
		if (destination == null) {
			System.err.println("Failed to run [modgen]: Destination (-d <directory>) must be specified");
			fail();
		}
		
		if (!destination.exists()) {
			System.err.println("Failed to run [modgen]: Destination [" + destination + "] must exist");
			fail();
		}
		
		ModuleGenerator generator = new ModuleGenerator(destination);
		generator.generate();
	}
	
	private ModuleGenerator(File destination) {
		this.destination = destination;
	}
	
	private void generate() {
		System.out.println("Tool [modgen] started: Destination [" + destination + "]");
		GigMatcher matcher = new GigMatcher();
		
		try {
			ClassResolver<CtClass> resolver = new CtClassClassResolver();
			System.out.println("Resolving classpath...");
			resolver.resolve(matcher);
		} catch (Exception e) {
			System.err.println("Tool [modgen] failed: ");
			e.printStackTrace();
			fail();
		}
		
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				classLoader.getResourceAsStream(TEMPLATE)));
		StringBuilder template = new StringBuilder();
		String line = null;
		
		try {
			while ((line = reader.readLine()) != null) {
				template.append(line + '\n');
			}
			
		} catch (Exception e) {
			System.err.println("Tool [modgen] failed: ");
			e.printStackTrace();
			fail();
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {}
			}
		}
		
		File directory = new File(destination, PACKAGE.replace('.', File.separatorChar));
		
		if (!directory.exists()) {
			if (!directory.mkdirs()) {
				System.err.println("Tool [modgen] failed: Cannot create directory [" + directory + "]");
				fail();
			}
		}
		
		File file = new File(directory, FILENAME);
		
		if (file.exists()) {
			if (!file.delete()) {
				System.err.println("Tool [modgen] failed: Cannot delete existing file [" + file + "]");
				fail();
			}
		}
		
		System.out.println("Packaging components...");
		String source = replace(template.toString(), matcher);
		
		FileWriter writer = null;
		
		try {
			System.out.println("Saving module...");
			writer = new FileWriter(file);
			writer.write(source);
		} catch (IOException e) {
			System.err.println("Tool [modgen] failed: ");
			e.printStackTrace();
			fail();
		} finally {
			if (writer != null) {
				try {
					writer.close();
				} catch (Exception e) {}
			}
		}
		
		System.out.println("Tool [modgen] completed!");
	}
	
	private static void fail() {
		System.err.println("Bye!");
		System.exit(1);
	}
	
	private String replace(String source, GigMatcher matcher) {
		StringBuilder components = new StringBuilder();
		int i = 0;
		int size = matcher.components.size();
		
		for (CtClass ctClass : matcher.components) {
			components.append(INDENT + "components.add(" + ctClass.getName() + ".class);");
			i++;
			
			if (i < size) {
				components.append("\n");
			}
			
			System.out.println("Added [" + ctClass.getName() + "] to components");
		}
		
		return source.replace("${components}", components);
	}
	
	private static class GigMatcher implements Matcher<CtClass> {

		private Set<CtClass> components = new HashSet<CtClass>();
		
		@Override
		public boolean matches(CtClass ctClass) {
			try {
				int modifiers = ctClass.getModifiers();
				
				if (!Modifier.isPublic(modifiers)) {
					ctClass.detach();
					return false;
				}
				
				if (ctClass.getAnnotation(Endpoint.class) != null) {
					components.add(ctClass);
					return false;
				}
				
				if (ctClass.isInterface() || Modifier.isAbstract(modifiers)
						|| ctClass.hasAnnotation(Builtin.class)) {
					ctClass.detach();
					return false;
				}
				
				CtClass superclass = ctClass.getSuperclass();
				
				while (superclass != null) {
					if (superclass.getName().equals(Component.class.getName())) {
						components.add(ctClass);
						return false;
					}
					
					superclass = superclass.getSuperclass();
				}
				
				for (Object object : ctClass.getAnnotations()) {
					Class<? extends Annotation> annotationType = ((Annotation) object).annotationType();
					
					if (annotationType.isAnnotationPresent(Stereotype.class) 
							|| annotationType.isAnnotationPresent(Deployment.class) 
							|| annotationType.isAnnotationPresent(Binding.class) 
							|| annotationType.isAnnotationPresent(Scope.class)) {
						components.add(ctClass);
						return false;
					}
				}
				
			} catch (Exception e) {
//				e.printStackTrace();
			}
			
			ctClass.detach();
			return false;
		}
		
	}
	
}
