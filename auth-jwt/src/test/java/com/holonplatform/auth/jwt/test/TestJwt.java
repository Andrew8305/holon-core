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
package com.holonplatform.auth.jwt.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;

import javax.crypto.SecretKey;

import org.junit.Test;

import com.holonplatform.auth.AuthContext;
import com.holonplatform.auth.Authentication;
import com.holonplatform.auth.AuthenticationToken;
import com.holonplatform.auth.Permission;
import com.holonplatform.auth.Realm;
import com.holonplatform.auth.exceptions.ExpiredCredentialsException;
import com.holonplatform.auth.exceptions.InvalidTokenException;
import com.holonplatform.auth.exceptions.UnexpectedAuthenticationException;
import com.holonplatform.auth.jwt.AuthenticationClaims;
import com.holonplatform.auth.jwt.JwtAuthenticator;
import com.holonplatform.auth.jwt.JwtConfigProperties;
import com.holonplatform.auth.jwt.JwtConfiguration;
import com.holonplatform.auth.jwt.JwtSignatureAlgorithm;
import com.holonplatform.auth.jwt.JwtTokenBuilder;
import com.holonplatform.auth.jwt.JwtTokenParser;
import com.holonplatform.auth.jwt.internal.AuthenticationClaimsImpl;
import com.holonplatform.core.internal.utils.TestUtils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.impl.crypto.MacProvider;

public class TestJwt {

	@Test
	public void testConfig() {

		Properties props = new Properties();
		props.put(JwtConfigProperties.NAME + "." + JwtConfigProperties.ISSUER.getKey(), "TestIssuer");

		JwtConfigProperties cfg = JwtConfigProperties.builder().withPropertySource(props).build();

		assertEquals(JwtConfigProperties.NAME, cfg.getName());
		assertEquals("TestIssuer", cfg.getConfigPropertyValue(JwtConfigProperties.ISSUER, null));

		final JwtAuthenticator jwtAuthenticator = JwtAuthenticator.builder()
				.configuration(JwtConfiguration.builder().build()).issuer("TestUnit").build();

		final Realm realm = Realm.builder().authenticator(jwtAuthenticator).build();

		final Authentication authc = Authentication.builder("testuser").build();

		String jwt = JwtTokenBuilder.get().buildJwt(JwtConfiguration.builder().issuer("TestUnit").build(), authc);

		Authentication authenticated = realm.authenticate(AuthenticationToken.bearer(jwt));
		assertNotNull(authenticated);

		TestUtils.expectedException(InvalidTokenException.class, new Callable<Authentication>() {

			@Override
			public Authentication call() throws Exception {
				String jwt = JwtTokenBuilder.get().buildJwt(JwtConfiguration.builder().issuer("InvalidIssuer").build(),
						authc);
				return realm.authenticate(AuthenticationToken.bearer(jwt));
			}
		});

		final JwtAuthenticator jwtAuthenticator2 = JwtAuthenticator.builder()
				.configuration(JwtConfiguration.builder().build()).issuer("TestUnit").requiredClaim("testReq").build();

		TestUtils.expectedException(InvalidTokenException.class, new Callable<Authentication>() {

			@Override
			public Authentication call() throws Exception {
				String jwt = JwtTokenBuilder.get().buildJwt(JwtConfiguration.builder().issuer("TestUnit").build(),
						authc);
				return Realm.builder().authenticator(jwtAuthenticator2).build()
						.authenticate(AuthenticationToken.bearer(jwt));
			}
		});

		final Authentication authc2 = Authentication.builder("testuser").parameter("testReq", "VAL").build();
		jwt = JwtTokenBuilder.get().buildJwt(JwtConfiguration.builder().issuer("TestUnit").includeDetails(true).build(),
				authc2);

		authenticated = realm.authenticate(AuthenticationToken.bearer(jwt));
		assertNotNull(authenticated);

		TestUtils.expectedException(UnexpectedAuthenticationException.class, new Callable<Authentication>() {

			@Override
			public Authentication call() throws Exception {
				JwtAuthenticator.builder().configuration(JwtConfiguration.builder().build()).build().authenticate(null);
				return null;
			}
		});

		AuthenticationClaims cs = new AuthenticationClaimsImpl(null);
		assertNull(cs.get("test", String.class));

		TestUtils.expectedException(ExpiredCredentialsException.class, new Callable<Authentication>() {

			@Override
			public Authentication call() throws Exception {
				String jwt = JwtTokenBuilder.get().buildJwt(
						JwtConfiguration.builder().issuer("TestUnit").includeDetails(true).expireTime(1L).build(),
						authc2);
				Thread.sleep(2L);
				realm.authenticate(AuthenticationToken.bearer(jwt));
				return null;
			}
		});

		TestUtils.expectedException(InvalidTokenException.class, new Callable<Authentication>() {

			@Override
			public Authentication call() throws Exception {
				realm.authenticate(AuthenticationToken.bearer("x"));
				return null;
			}
		});

	}

	@Test
	public void testConfigNbf() {

		Properties props = new Properties();
		props.put(JwtConfigProperties.NAME + "." + JwtConfigProperties.ISSUER.getKey(), "TestIssuer");
		props.put(JwtConfigProperties.NAME + "." + JwtConfigProperties.NOT_BEFORE_NOW.getKey(), "true");

		JwtConfigProperties cfg = JwtConfigProperties.builder().withPropertySource(props).build();

		assertEquals(JwtConfigProperties.NAME, cfg.getName());
		assertEquals("TestIssuer", cfg.getConfigPropertyValue(JwtConfigProperties.ISSUER, null));
		assertTrue(cfg.getConfigPropertyValue(JwtConfigProperties.NOT_BEFORE_NOW, Boolean.FALSE));

		Authentication authc = Authentication.builder("testuser").build();

		String jwt = JwtTokenBuilder.get().buildJwt(JwtConfiguration.build(cfg), authc);

		JwtParser parser = Jwts.parser();
		Claims claims = parser.parseClaimsJwt(jwt).getBody();
		assertNotNull(claims.get("nbf", Date.class));

		authc = JwtTokenParser.get()
				.parseJwt(JwtConfiguration.builder().issuer("TestIssuer").includeDetails(true).build(), jwt).build();
		assertNotNull(authc);
		assertEquals("testuser", authc.getName());
	}

	@Test
	public void testTokenParser() {

		final JwtConfiguration cfg = JwtConfiguration.builder().includeDetails(true).includePermissions(true).build();

		Authentication authc = Authentication.builder("testuser").parameter("test", "value").permission("rolex")
				.build();

		String jwt = JwtTokenBuilder.get().buildJwt(cfg, authc);
		assertNotNull(jwt);

		Authentication authc2 = JwtTokenParser.get().parseJwt(cfg, jwt).build();
		assertNotNull(authc2);

		assertEquals("testuser", authc.getName());
		assertTrue(authc2.hasParameter("test"));
		assertEquals("value", authc2.getParameter("test").orElse(null));

		Collection<Permission> ps = authc2.getPermissions();
		assertEquals(1, ps.size());

		Permission p = ps.iterator().next();
		assertNotNull(p);
		assertEquals("rolex", p.getPermission().orElse(null));
	}

	@Test
	public void testJWTAuthentication_unsigned() throws Exception {

		final Realm realm = Realm.builder()
				.authenticator(JwtAuthenticator.builder().configuration(JwtConfiguration.builder().build()).build())
				.build();

		Authentication authc = Authentication.builder("testuser").build();

		String jwt = JwtTokenBuilder.get().buildJwt(JwtConfiguration.builder().issuer("TestUnit").build(), authc);
		assertNotNull(jwt);

		Authentication authenticated = realm.authenticate(AuthenticationToken.bearer(jwt));
		assertNotNull(authenticated);
		assertEquals(authc, authenticated);
		assertEquals("TestUnit", authenticated.getParameter(Claims.ISSUER, String.class).get());

		// with id
		String id = UUID.randomUUID().toString();

		jwt = JwtTokenBuilder.get().buildJwt(JwtConfiguration.builder().issuer("TestUnit").build(), authc, id);
		assertNotNull(jwt);

		authenticated = realm.authenticate(AuthenticationToken.bearer(jwt));
		assertNotNull(authenticated);
		assertEquals(authc, authenticated);
		assertEquals("TestUnit", authenticated.getParameter(Claims.ISSUER, String.class).get());
		assertEquals(id, authenticated.getParameter(Claims.ID, String.class).get());

		// with expiration (10s)

		jwt = JwtTokenBuilder.get().buildJwt(JwtConfiguration.builder().issuer("TestUnit").expireTime(10000L).build(),
				authc, id);
		assertNotNull(jwt);

		authenticated = realm.authenticate(AuthenticationToken.bearer(jwt));
		assertNotNull(authenticated);
		assertEquals(authc, authenticated);
		assertEquals("TestUnit", authenticated.getParameter(Claims.ISSUER, String.class).get());
		assertEquals(id, authenticated.getParameter(Claims.ID, String.class).get());
		assertNotNull(authenticated.getParameter(Claims.EXPIRATION, Integer.class).get());

		// with permissions

		Permission p1 = Permission.create("p1");
		Permission p2 = Permission.create("p2");
		Permission p3 = Permission.create("p3");

		Set<Permission> permissions = new HashSet<>(3);
		permissions.add(p1);
		permissions.add(p2);
		permissions.add(p3);

		authc = Authentication.builder("testuser").permission(p1).permission(p2).permission(p3).build();

		jwt = JwtTokenBuilder.get().buildJwt(
				JwtConfiguration.builder().issuer("TestUnit").expireTime(10000L).includePermissions(true).build(),
				authc, id);
		assertNotNull(jwt);

		authenticated = realm.authenticate(AuthenticationToken.bearer(jwt));
		assertNotNull(authenticated);
		assertEquals(authc, authenticated);
		assertEquals("TestUnit", authenticated.getParameter(Claims.ISSUER, String.class).get());
		assertEquals(id, authenticated.getParameter(Claims.ID, String.class).get());
		assertNotNull(authenticated.getParameter(Claims.EXPIRATION, Integer.class).get());
		assertFalse(authenticated.getParameter(AuthenticationClaims.CLAIM_NAME_ROOT, boolean.class).get());

		// with details

		authc = Authentication.builder("testuser").parameter("testd", 1).permission(p1).permission(p2).permission(p3)
				.build();

		jwt = JwtTokenBuilder.get().buildJwt(JwtConfiguration.builder().issuer("TestUnit").expireTime(10000L)
				.includeDetails(true).includePermissions(true).build(), authc, id);
		assertNotNull(jwt);

		authenticated = realm.authenticate(AuthenticationToken.bearer(jwt));
		assertNotNull(authenticated);
		assertEquals(authc, authenticated);
		assertEquals("TestUnit", authenticated.getParameter(Claims.ISSUER, String.class).get());
		assertEquals(id, authenticated.getParameter(Claims.ID, String.class).get());
		assertNotNull(authenticated.getParameter(Claims.EXPIRATION, Integer.class).get());
		assertFalse(authenticated.getParameter(AuthenticationClaims.CLAIM_NAME_ROOT, boolean.class).get());

		assertEquals(new Integer(1), authenticated.getParameter("testd", Integer.class).get());

	}

	@Test
	public void testJWTAuthentication_signed_symmetric() throws Exception {

		SecretKey key = MacProvider.generateKey(SignatureAlgorithm.HS256);

		JwtConfiguration cfg = JwtConfiguration.builder().signatureAlgorithm(JwtSignatureAlgorithm.HS256)
				.sharedKey(key.getEncoded()).build();

		Realm realm = Realm.builder().authenticator(JwtAuthenticator.builder().configuration(cfg).build()).build();

		final Authentication authc = Authentication.builder("testuser").root(true).build();

		String jwt = JwtTokenBuilder.get().buildJwt(
				JwtConfiguration.builder().issuer("TestUnit").expireTime(20000L)
						.signatureAlgorithm(JwtSignatureAlgorithm.HS256).sharedKey(key.getEncoded()).build(),
				authc, "TestId");
		assertNotNull(jwt);

		Authentication authenticated = realm.authenticate(AuthenticationToken.bearer(jwt));
		assertNotNull(authenticated);
		assertEquals(authc, authenticated);
		assertEquals("TestUnit", authenticated.getParameter(Claims.ISSUER, String.class).get());
		assertEquals("TestId", authenticated.getParameter(Claims.ID, String.class).get());

		jwt = JwtTokenBuilder.get().buildJwt(
				JwtConfiguration.builder().issuer("TestUnit").expireTime(20000L).includePermissions(true)
						.signatureAlgorithm(JwtSignatureAlgorithm.HS256).sharedKey(key.getEncoded()).build(),
				authc, "TestId");
		assertNotNull(jwt);

		authenticated = realm.authenticate(AuthenticationToken.bearer(jwt));
		assertNotNull(authenticated);
		assertEquals(authc, authenticated);
		assertEquals("TestUnit", authenticated.getParameter(Claims.ISSUER, String.class).get());
		assertEquals("TestId", authenticated.getParameter(Claims.ID, String.class).get());
		assertTrue(authenticated.getParameter(AuthenticationClaims.CLAIM_NAME_ROOT, boolean.class).get());

		jwt = JwtTokenBuilder.get().buildJwt(JwtConfiguration.builder().issuer("TestUnit").expireTime(20000L).build(),
				authc, "TestId");
		assertNotNull(jwt);

		cfg = JwtConfiguration.builder().signatureAlgorithm(JwtSignatureAlgorithm.HS256)
				.sharedKeyBase64(Base64.getEncoder().encodeToString(key.getEncoded())).build();

		realm = Realm.builder().authenticator(JwtAuthenticator.builder().configuration(cfg).build()).build();

		jwt = JwtTokenBuilder.get().buildJwt(
				JwtConfiguration.builder().issuer("TestUnit").expireTime(20000L)
						.signatureAlgorithm(JwtSignatureAlgorithm.HS256).sharedKey(key.getEncoded()).build(),
				authc, "TestId");
		assertNotNull(jwt);

		authenticated = realm.authenticate(AuthenticationToken.bearer(jwt));
		assertNotNull(authenticated);
		assertEquals(authc, authenticated);
		assertEquals("TestUnit", authenticated.getParameter(Claims.ISSUER, String.class).get());
		assertEquals("TestId", authenticated.getParameter(Claims.ID, String.class).get());

		Properties props = new Properties();
		props.put(JwtConfigProperties.NAME + "." + JwtConfigProperties.SIGNATURE_ALGORITHM.getKey(),
				SignatureAlgorithm.HS256.getValue());
		props.put(JwtConfigProperties.NAME + "." + JwtConfigProperties.SHARED_KEY.getKey(),
				Base64.getEncoder().encodeToString(key.getEncoded()));

		JwtConfigProperties jcfg = JwtConfigProperties.builder().withPropertySource(props).build();

		cfg = JwtConfiguration.build(jcfg);

		realm = Realm.builder().authenticator(JwtAuthenticator.builder().configuration(cfg).build()).build();

		jwt = JwtTokenBuilder.get().buildJwt(
				JwtConfiguration.builder().issuer("TestUnit").expireTime(20000L).includePermissions(true)
						.signatureAlgorithm(JwtSignatureAlgorithm.HS256).sharedKey(key.getEncoded()).build(),
				authc, "TestId");
		assertNotNull(jwt);

		authenticated = realm.authenticate(AuthenticationToken.bearer(jwt));
		assertNotNull(authenticated);
		assertEquals(authc, authenticated);
		assertEquals("TestUnit", authenticated.getParameter(Claims.ISSUER, String.class).get());
		assertEquals("TestId", authenticated.getParameter(Claims.ID, String.class).get());

	}

	@Test
	public void testJWTAuthentication_signed_asymmetric() throws Exception {

		KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
		keyGen.initialize(512);
		KeyPair keyPair = keyGen.genKeyPair();
		PrivateKey privateKey = keyPair.getPrivate();
		PublicKey publicKey = keyPair.getPublic();

		final JwtConfiguration cfg = JwtConfiguration.builder().signatureAlgorithm(JwtSignatureAlgorithm.RS256)
				.privateKey(privateKey).publicKey(publicKey).build();

		final Realm realm = Realm.builder().authenticator(JwtAuthenticator.builder().configuration(cfg).build())
				.build();

		Authentication authc = Authentication.builder("testuser").root(true).build();

		String jwt = JwtTokenBuilder.get().buildJwt(
				JwtConfiguration.builder().issuer("TestUnit").expireTime(20000L)
						.signatureAlgorithm(JwtSignatureAlgorithm.RS256).privateKey(privateKey).build(),
				authc, "TestId");
		assertNotNull(jwt);

		Authentication authenticated = realm.authenticate(AuthenticationToken.bearer(jwt));
		assertNotNull(authenticated);
		assertEquals(authc, authenticated);
		assertEquals("TestUnit", authenticated.getParameter(Claims.ISSUER, String.class).get());
		assertEquals("TestId", authenticated.getParameter(Claims.ID, String.class).get());

	}

	@Test
	public void testJWTAuthentication_permissions() throws Exception {

		byte[] sharedKey = generateKey();

		final JwtConfiguration cfg = JwtConfiguration.builder().signatureAlgorithm(JwtSignatureAlgorithm.HS256)
				.sharedKey(sharedKey).build();

		final Realm realm = Realm.builder().authenticator(JwtAuthenticator.builder().configuration(cfg).build())
				.withDefaultAuthorizer().build();

		Authentication authc = Authentication.builder("testuser").root(true).parameter("language", "it")
				.permission(Permission.create("RoleX")).build();

		String jwt = JwtTokenBuilder.get()
				.buildJwt(JwtConfiguration.builder().issuer("TestUnit").expireTime(2000000L).includeDetails(true)
						.includePermissions(true).signatureAlgorithm(JwtSignatureAlgorithm.HS256).sharedKey(sharedKey)
						.build(), authc, "TestId");
		assertNotNull(jwt);

		AuthContext ctx = AuthContext.create(realm);

		ctx.authenticate(AuthenticationToken.bearer(jwt));
		assertTrue(ctx.getAuthentication().isPresent());
		assertEquals(authc, ctx.getAuthentication().get());
		assertEquals("it", ctx.getAuthentication().get().getParameter("language", String.class, null));
		assertTrue(ctx.isPermitted("RoleX"));
		assertFalse(ctx.isPermitted("xxx"));

	}

	private static byte[] generateKey() throws Exception {
		String random = UUID.randomUUID().toString();
		MessageDigest algorithm = MessageDigest.getInstance("MD5");
		algorithm.reset();
		algorithm.update(random.getBytes());
		byte[] messageDigest = algorithm.digest();
		return Base64.getEncoder().encode(messageDigest);
	}

}
