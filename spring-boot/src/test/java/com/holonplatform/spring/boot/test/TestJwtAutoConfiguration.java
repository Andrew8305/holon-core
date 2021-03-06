/*
 * Copyright 2000-2017 Holon TDCN.
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
package com.holonplatform.spring.boot.test;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.holonplatform.auth.jwt.JwtConfiguration;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
@ActiveProfiles("jwt")
public class TestJwtAutoConfiguration {

	@Configuration
	@EnableAutoConfiguration
	protected static class Config {

	}

	@Autowired
	private JwtConfiguration jwtConfiguration;

	@Test
	public void testConfig() {
		Assert.assertNotNull(jwtConfiguration);

		Assert.assertTrue("TestIssuer", jwtConfiguration.getIssuer().isPresent());
		Assert.assertEquals("TestIssuer", jwtConfiguration.getIssuer().get());
	}

}
