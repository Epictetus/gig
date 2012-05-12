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

import org.eiichiro.jaguar.Module;
import org.eiichiro.monophony.Configuration;

/**
 * {@code GigConfiguration} is the {@code Configuration} extension to specify the configuration 
 * of Gig application.
 * You can specify your own custom {@code GigConfiguration} as the 
 * {@code ServletContext}'s init parameter <code>'org.eiichiro.gig.configuration'</code> 
 * like this: 
 * <pre>
 * &lt;context-param&gt;
 *     &lt;param-name&gt;org.eiichiro.gig.configuration&lt;/param-name&gt;
 *     &lt;param-value&gt;type.of.your.own.CustomGigConfiguration&lt;/param-value&gt;
 * &lt;/context-param&gt;
 * </pre>
 * 
 * @author <a href="mailto:eiichiro@eiichiro.org">Eiichiro Uchiumi</a>
 */
public interface GigConfiguration extends Configuration {

	/**
	 * Returns the deployment qualifier.
	 * 
	 * @return The deployment qualifier.
	 */
	public Class<?> deployment();
	
	/**
	 * Returns the {@code Module} packages the components to be deployed.
	 * 
	 * @return The {@code Module} packages the components to be deployed.
	 */
	public Module module();
	
}
