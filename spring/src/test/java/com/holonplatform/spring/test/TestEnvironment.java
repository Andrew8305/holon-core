/*
 * Copyright 2016-2017 Axioma srl.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.holonplatform.spring.test;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.holonplatform.core.config.ConfigPropertyProvider;
import com.holonplatform.spring.EnvironmentConfigPropertyProvider;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestEnvironment.Config.class)
@DirtiesContext
public class TestEnvironment {

	@Configuration
	@PropertySource("env.properties")
	protected static class Config {

		@Autowired
		private Environment env;

		@Bean
		public ConfigPropertyProvider configPropertyProvider() {
			return EnvironmentConfigPropertyProvider.create(env);
		}

	}

	@Autowired
	private ConfigPropertyProvider provider;

	@Test
	public void testEnv() {
		Assert.assertEquals("Test", provider.getProperty("test.env.str", String.class));
		Assert.assertTrue(provider.containsProperty("test.env.str"));
		Assert.assertFalse(provider.containsProperty("xxx"));
		final List<String> pns = provider.getPropertyNames().collect(Collectors.toList());
		Assert.assertTrue(pns.size() > 0);
		Assert.assertTrue(pns.contains("test.env.str"));
	}

}
