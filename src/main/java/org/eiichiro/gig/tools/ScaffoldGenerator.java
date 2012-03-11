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

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eiichiro.acidhouse.Entities;
import org.eiichiro.acidhouse.Entity;
import org.eiichiro.gig.Main;

/**
 * {@code ScaffoldGenerator} is a experimental tool to generate a CRUD 
 * application for the specified entity class.
 * <pre>
 * usage: ScaffoldGenerator<br>
 *  -e,--entity &lt;class&gt;				entity class that the CRUD scaffold is <br>
 *  										generating from<br>
 *  -d,--destination &lt;directory&gt;		directory path that the scaffold <br>
 *  										classes are stored<br>
 *  -w,--war &lt;directory&gt;				WAR directory path that the scaffold <br>
 *  										Web pages and scripts are stored
 * </pre>
 * 
 * @author <a href="mailto:eiichiro@eiichiro.org">Eiichiro Uchiumi</a>
 */
public class ScaffoldGenerator {

	private static final String ENDPOINT_TEMPLATE 
			= "org/eiichiro/gig/tools/Endpoint.java.template";

	private static final String SERVICE_TEMPLATE 
			= "org/eiichiro/gig/tools/Service.java.template";
	
	private static final String INDEX_TEMPLATE 
			= "org/eiichiro/gig/tools/index.html";
	
	private static final String LIST_TEMPLATE 
			= "org/eiichiro/gig/tools/list.html";
	
	private static final String CREATE_TEMPLATE 
			= "org/eiichiro/gig/tools/create.html";
	
	private static final String SHOW_TEMPLATE 
			= "org/eiichiro/gig/tools/show.html";
	
	private static final String EDIT_TEMPLATE 
			= "org/eiichiro/gig/tools/edit.html";
	
	private static final String CSS_TEMPLATE 
			= "org/eiichiro/gig/tools/crud.css";
	
	private static final String CSS_DIRECTORY = "css";
	
	private static final String ENDPOINT_FILENAME = "Endpoint.java";
	
	private static final String SERVICE_FILENAME = "Service.java";
	
	private static final String INDEX_FILENAME = "index.html";
	
	private static final String LIST_FILENAME = "list.html";
	
	private static final String CREATE_FILENAME = "create.html";
	
	private static final String SHOW_FILENAME = "show.html";
	
	private static final String EDIT_FILENAME = "edit.html";
	
	private static final String CSS_FILENAME = ".css";
	
	private static final String CONTENT_SNIPPET = "<div class=\"content-main-item\">${field}</div>\n" 
			+ "<input id=\"${field}\" type=\"text\" class=\"content-main-item-input\"";
	
	private static final String POST_SNIPPET = "${field}: $('#${field}').val()";
	
	private static final String DATA_SNIPPET = "$('#${field}').val(data.${field});";
	
	private Class<?> entity;
	
	private File destination;
	
	private File war;
	
	/**
	 * Runs [scaffold] tool.
	 * 
	 * @param args The command line arguments.
	 */
	public static void main(String[] args) {
		Main.main(args);
		System.out.println("Starting [scaffold]");
		
		Class<?> entity = null;
		File destination = null;
		File war = null;
		
		for (int i = 0; i < args.length; i++) {
			if (args[i].equals("-e") || args[i].equals("--entity")) {
				if (args.length > i) {
					try {
						entity = Class.forName(args[i + 1]);
					} catch (ClassNotFoundException e) {
						System.err.println("Failed to run [scaffold]: Entity class [" 
								+ args[i + 1] + "] not found");
						fail();
					}
				}
				
			} else if (args[i].equals("-d") || args[i].equals("--destination")) {
				if (args.length > i) {
					destination = new File(args[i + 1]);
				}
				
			} else if (args[i].equals("-w") || args[i].equals("--war")) {
				if (args.length > i) {
					war = new File(args[i + 1]);
				}
			}
		}
		
		if (entity == null) {
			System.err.println("Failed to run [scaffold]: Entity class (-e <class>) must be specified");
			fail();
		}
		
		if (entity.getAnnotation(Entity.class) == null) {
			System.err.println("Failed to run [scaffold]: Entity class must be annotated with @Entity");
			fail();
		}
		
		if (destination == null) {
			System.err.println("Failed to run [scaffold]: Destination (-d <directory>) must be specified");
			fail();
		}
		
		if (!destination.exists()) {
			System.err.println("Failed to run [scaffold]: Destination [" + destination + "] must exist");
			fail();
		}
		
		if (war == null) {
			System.err.println("Failed to run [scaffold]: WAR directory (-w <directory>) must be specified");
			fail();
		}
		
		if (!war.exists()) {
			System.err.println("Failed to run [scaffold]: WAR directory [" + war + "] must exist");
			fail();
		}
		
		ScaffoldGenerator generator = new ScaffoldGenerator(entity, destination, war);
		generator.generate();
	}
	
	private ScaffoldGenerator(Class<?> entity, File destination, File war) {
		this.entity = entity;
		this.destination = destination;
		this.war = war;
	}
	
	private void generate() {
		System.out.println("Tool [scaffold] started: Entity class [" + entity.getName() + "]");
		System.out.println("Tool [scaffold] started: Destination [" + destination + "]");
		System.out.println("Tool [scaffold] started: Web application directory [" + war + "]");
		
		System.out.println("Constructing parameters...");
		String entityPackage = entity.getPackage().getName();
		String entityClass = entity.getSimpleName();
		String entityName = entityClass.toLowerCase();
		Field key = Entities.keyField(entity);
		Package keyPackage = key.getType().getPackage();
		String keyClass = key.getType().getSimpleName();
		String keyName = key.getName();
		String keyImport = "";
		
		if (keyPackage != null && !keyPackage.getName().equals("java.lang")) {
			keyImport = "\nimport " + keyPackage + ";\n";
		}
		
		List<Field> fields = new ArrayList<Field>();
		// Key is anytime in the lead.
		fields.add(key);
		
		for (Field field : entity.getDeclaredFields()) {
			if (!field.equals(key)) {
				fields.add(field);
			}
		}
		
		int i = 0;
		final int size = fields.size();
		StringBuilder createHTMLContent = new StringBuilder();
		StringBuilder showHTMLContent = new StringBuilder();
		StringBuilder editHTMLContent = new StringBuilder();
		
		for (Field field : fields) {
			i++;
			String snippet = CONTENT_SNIPPET.replace("${field}", field.getName());
			
			if (i == 1) {
				editHTMLContent.append(snippet + " readonly>");
			} else {
				editHTMLContent.append(snippet + ">");
			}
			
			createHTMLContent.append(snippet + ">");
			showHTMLContent.append(snippet + " readonly>");
			
			if (i < size) {
				createHTMLContent.append("\n");
				showHTMLContent.append("\n");
				editHTMLContent.append("\n");
			}
		}
		
		i = 0;
		StringBuilder createHTMLPost = new StringBuilder();
		StringBuilder editHTMLPost = new StringBuilder();
		
		for (Field field : fields) {
			i++;
			String snippet = POST_SNIPPET.replace("${field}", field.getName());
			createHTMLPost.append(snippet);
			editHTMLPost.append(snippet);
			
			if (i < size) {
				createHTMLPost.append(", \n");
				editHTMLPost.append(", \n");
			}
		}
		
		i = 0;
		StringBuilder showHTMLData = new StringBuilder();
		StringBuilder editHTMLData = new StringBuilder();
		
		for (Field field : fields) {
			i++;
			String snippet = DATA_SNIPPET.replace("${field}", field.getName());
			showHTMLData.append(snippet);
			editHTMLData.append(snippet);
			
			if (i < size) {
				showHTMLData.append("\n");
				editHTMLData.append("\n");
			}
		}
		
		if (size > 1) {
			editHTMLData.append("\n$('#${field}').focus();".replace("${field}", fields.get(1).getName()));
		}
		
		System.out.println("Generating scaffold...");
		ScaffoldFile service = null;
		ScaffoldFile endpoint = null;
		ScaffoldFile index = null;
		ScaffoldFile list = null;
		ScaffoldFile create = null;
		ScaffoldFile show = null;
		ScaffoldFile edit = null;
		ScaffoldFile css = null;
		Map<String, String> parameters = new HashMap<String, String>();
		parameters.put("${entity.package}", entityPackage);
		parameters.put("${entity.class}", entityClass);
		parameters.put("${entity.name}", entityName);
		parameters.put("${key.name}", keyName);
		parameters.put("${key.class}", keyClass);
		parameters.put("${key.import}", keyImport);
		parameters.put("${create.html.content}", createHTMLContent.toString());
		parameters.put("${show.html.content}", showHTMLContent.toString());
		parameters.put("${edit.html.content}", editHTMLContent.toString());
		parameters.put("${create.html.post}", createHTMLPost.toString());
		parameters.put("${edit.html.post}", editHTMLPost.toString());
		parameters.put("${show.html.data}", showHTMLData.toString());
		parameters.put("${edit.html.data}", editHTMLData.toString());
		
		try {
			service = new ScaffoldFile(SERVICE_TEMPLATE,
					new File(destination, entityPackage.replace('.', File.separatorChar)).toString(), 
					entityClass + SERVICE_FILENAME, parameters);
			endpoint = new ScaffoldFile(ENDPOINT_TEMPLATE,
					new File(destination, entityPackage.replace('.', File.separatorChar)).toString(), 
					entityClass + ENDPOINT_FILENAME, parameters);
			index = new ScaffoldFile(INDEX_TEMPLATE,
					new File(war, entityName).toString(), INDEX_FILENAME, parameters);
			list = new ScaffoldFile(LIST_TEMPLATE,
					new File(war, entityName).toString(), LIST_FILENAME, parameters);
			create = new ScaffoldFile(CREATE_TEMPLATE,
					new File(war, entityName).toString(), CREATE_FILENAME, parameters);
			show = new ScaffoldFile(SHOW_TEMPLATE,
					new File(war, entityName).toString(), SHOW_FILENAME, parameters);
			edit = new ScaffoldFile(EDIT_TEMPLATE,
					new File(war, entityName).toString(), EDIT_FILENAME, parameters);
			css = new ScaffoldFile(CSS_TEMPLATE, 
					new File(war, entityName + File.separatorChar + CSS_DIRECTORY).toString(),
					entityName + CSS_FILENAME, parameters);
		} catch (IOException e) {
			System.err.println("Tool [scaffold] failed: ");
			e.printStackTrace();
			fail();
		}
		
		if (service.exists()) {
			if (!service.delete()) {
				System.err.println("Tool [scaffold] failed: Cannot delete existing file ["
						+ service.path() + "]");
				fail();
			}
		}
		
		if (endpoint.exists()) {
			if (!endpoint.delete()) {
				System.err.println("Tool [scaffold] failed: Cannot delete existing file ["
						+ endpoint.path() + "]");
				fail();
			}
		}
		
		if (index.exists()) {
			if (!index.delete()) {
				System.err.println("Tool [scaffold] failed: Cannot delete existing file ["
						+ index.path() + "]");
				System.err.println("Bye!");
				System.exit(1);
			}
		}
		
		if (list.exists()) {
			if (!list.delete()) {
				System.err.println("Tool [scaffold] failed: Cannot delete existing file ["
						+ list.path() + "]");
				fail();
			}
		}
		
		if (create.exists()) {
			if (!create.delete()) {
				System.err.println("Tool [scaffold] failed: Cannot delete existing file ["
						+ create.path() + "]");
				fail();
			}
		}
		
		if (show.exists()) {
			if (!show.delete()) {
				System.err.println("Tool [scaffold] failed: Cannot delete existing file ["
						+ show.path() + "]");
				System.err.println("Bye!");
				System.exit(1);
			}
		}
		
		if (edit.exists()) {
			if (!edit.delete()) {
				System.err.println("Tool [scaffold] failed: Cannot delete existing file ["
						+ edit.path() + "]");
				fail();
			}
		}
		
		if (css.exists()) {
			if (!css.delete()) {
				System.err.println("Tool [scaffold] failed: Cannot delete existing file ["
						+ css.path() + "]");
				fail();
			}
		}
		
		try {
			service.save();
			System.out.println("CRUD Service component [" + service.path() + "] generated");
			endpoint.save();
			System.out.println("CRUD Web endpoint [" + endpoint.path() + "] generated");
			index.save();
			System.out.println("HTML [" + index.path() + "] generated");
			list.save();
			System.out.println("HTML [" + list.path() + "] generated");
			create.save();
			System.out.println("HTML [" + create.path() + "] generated");
			show.save();
			System.out.println("HTML [" + show.path() + "] generated");
			edit.save();
			System.out.println("HTML [" + edit.path() + "] generated");
			css.save();
			System.out.println("CSS [" + css.path() + "] generated");
		} catch (IOException e) {
			System.err.println("Tool [scaffold] failed: ");
			e.printStackTrace();
			fail();
		}
		
		System.out.println("Tool [scaffold] completed!");
	}

	private static void fail() {
		System.err.println("Bye!");
		System.exit(1);
	}
	
}
