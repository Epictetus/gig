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

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.eiichiro.jaguar.WebFilter;
import org.eiichiro.bootleg.BootlegFilter;
import org.eiichiro.bootleg.Configuration;

/**
 * {@code GigFilter} is a {@code BootlegFilter} extension for Gig to setup 
 * Web contexts and Web endpoints from {@code Configuration}.
 * 
 * @author <a href="mailto:eiichiro@eiichiro.org">Eiichiro Uchiumi</a>
 */
public class GigFilter extends BootlegFilter {

	/**
	 * Sets up Web context with {@code WebFilter} and runs HTTP request 
	 * processing pipeline.
	 * 
	 * @param request HTTP request.
	 * @param response HTTP response.
	 * @throws IOException If any I/O error has occurred.
	 * @throws ServletException If any exception has occurred in processing the 
	 * request.
	 */
	@Override
	public void doFilter(ServletRequest request, ServletResponse response, 
			FilterChain chain) throws IOException, ServletException {
		HttpServletRequest req = WebFilter.request();
		
		try {
			WebFilter.request((HttpServletRequest) request);
			super.doFilter(request, response, chain);
		} finally {
			WebFilter.request(req);
		}
	}
	
	/**
	 * Returns the {@link Configuration} specified by the deployment descriptor.
	 * If no {@code Configuration} is specified, this method returns 
	 * {@link DefaultConfiguration}.
	 * 
	 * @param config Servlet filter configuration.
	 * @return The {@link Configuration} specified by the deployment descriptor.
	 * @throws ServletException If the {@code Configuration} has not been set on 
	 * the Servlet context.
	 */
	@Override
	protected Configuration configuration(FilterConfig config) throws ServletException {
		Configuration configuration = (Configuration) config.getServletContext().getAttribute(GigListener.CONFIGURATION);
		
		if (configuration == null) {
			throw new ServletException("Configuration has not been set on the Servlet context");
		}
		
		return configuration;
	}
	
}
