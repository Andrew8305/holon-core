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
package com.holonplatform.core.internal;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.WeakHashMap;

import com.holonplatform.core.Context;
import com.holonplatform.core.ContextScope;
import com.holonplatform.core.internal.utils.ClassUtils;
import com.holonplatform.core.internal.utils.ObjectUtils;

/**
 * {@link Context} manager to register and retrieve {@link ContextScope}s.
 * 
 * @since 5.0.0
 */
public final class ContextManager {

	/**
	 * Logger
	 */
	private static final Logger LOGGER = CoreLogger.create();

	/**
	 * ContextScopes registry
	 */
	private static final ScopeRegistry SCOPES = new ScopeRegistry();

	private ContextManager() {
	}

	/**
	 * Get whether the ClassLoader hierarchy (i.e. current ClassLoader and all it's parents) must be scanned when
	 * looking for available context scopes.
	 * @return <code>true</code> if the ClassLoader hierarchy must be scanned when looking for available context scopes,
	 *         <code>false</code> if only the current ClassLoader must be taken into account
	 */
	public static boolean isUseClassLoaderHierarchy() {
		return SCOPES.isUseClassLoaderHierarchy();
	}

	/**
	 * Set whether the ClassLoader hierarchy (i.e. current ClassLoader and all it's parents) must be scanned when
	 * looking for available context scopes.
	 * @param useClassLoaderHierarchy <code>true</code> to scan the ClassLoader hierarchy when looking for available
	 *        context scopes, <code>false</code> if only the current ClassLoader must be taken into account
	 */
	public static void setUseClassLoaderHierarchy(boolean useClassLoaderHierarchy) {
		SCOPES.setUseClassLoaderHierarchy(useClassLoaderHierarchy);
	}

	/**
	 * Gets the default {@link ClassLoader}.
	 * @return the default {@link ClassLoader}. If <code>null</code>, {@link ClassUtils#getDefaultClassLoader()} will be
	 *         used
	 */
	public static ClassLoader getDefaultClassLoader() {
		return SCOPES.getDefaultClassLoader();
	}

	/**
	 * Sets the default {@link ClassLoader} to use when a ClassLoader is not directly specified.
	 * @param classLoader the default {@link ClassLoader}
	 */
	public static void setDefaultClassLoader(ClassLoader classLoader) {
		SCOPES.setDefaultClassLoader(classLoader);
	}

	/**
	 * Gets whether the given scope name is registered for given <code>classLoader</code>.
	 * @param classLoader ClassLoader. If <code>null</code>, {@link #getDefaultClassLoader()} will be used
	 * @param scopeName Scope name (not null)
	 * @return <code>true</code> if scope is registered
	 */
	public static boolean isScopeRegistered(ClassLoader classLoader, String scopeName) {
		return SCOPES.isScopeRegistered(classLoader, scopeName);
	}

	/**
	 * Register a {@link ContextScope} bound to given <code>classLoader</code>.
	 * @param classLoader ClassLoader. If <code>null</code>, {@link #getDefaultClassLoader()} will be used
	 * @param scope Scope to register (not null)
	 */
	public static void registerScope(ClassLoader classLoader, ContextScope scope) {
		SCOPES.registerScope(classLoader, scope);
	}

	/**
	 * Unregister the {@link ContextScope} bound to given <code>classLoader</code>, if any.
	 * @param classLoader ClassLoader. If <code>null</code>, {@link #getDefaultClassLoader()} will be used
	 * @param name Scope name (not null)
	 * @return <code>true</code> if the scope was unregistered
	 */
	public static boolean unregisterScope(ClassLoader classLoader, String name) {
		return SCOPES.unregisterScope(classLoader, name);
	}

	/**
	 * Gets all available {@link ContextScope}s for default ClassLoader
	 * @return ContextScopes iterator, preserving the order defined using {@link ContextScope#getOrder()}
	 * @see ContextManager#getDefaultClassLoader()
	 */
	public static Iterable<ContextScope> getScopes() {
		return SCOPES.getScopes();
	}

	/**
	 * Gets all available {@link ContextScope}s for given <code>classLoader</code>
	 * @param classLoader ClassLoader. If <code>null</code>, {@link #getDefaultClassLoader()} will be used
	 * @return ContextScopes iterator, preserving the order defined using {@link ContextScope#getOrder()}
	 */
	public static Iterable<ContextScope> getScopes(ClassLoader classLoader) {
		return SCOPES.getScopes(classLoader);
	}

	/**
	 * Gets the {@link ContextScope} with given <code>name</code> bound to default ClassLoader
	 * @param name Scope name (not null)
	 * @return ContextScope with given name, or <code>null</code> if not registered
	 */
	public static ContextScope getScope(String name) {
		return SCOPES.getScope(name);
	}

	/**
	 * Gets the {@link ContextScope} with given <code>name</code> bound to given <code>classLoader</code>
	 * @param name Scope name (not null)
	 * @param classLoader ClassLoader. If <code>null</code>, {@link #getDefaultClassLoader()} will be used
	 * @return ContextScope with given name, or <code>null</code> if not registered
	 */
	public static ContextScope getScope(String name, ClassLoader classLoader) {
		return SCOPES.getScope(name, classLoader);
	}

	/**
	 * {@link ContextScope}s registry by {@link ClassLoader}.
	 */
	private static class ScopeRegistry {

		/**
		 * Context scopes organized by {@link ClassLoader} and mapped by name.
		 */
		private final WeakHashMap<ClassLoader, LinkedHashMap<String, ContextScope>> scopes;

		private boolean useClassLoaderHierarchy = true;

		/**
		 * The default {@link ClassLoader}. When <code>null</code>, the {@link Thread#getContextClassLoader()} will be
		 * used.
		 */
		private volatile ClassLoader classLoader;

		public ScopeRegistry() {
			super();
			this.scopes = new WeakHashMap<>(4);
			this.classLoader = null;
		}

		/**
		 * Get whether the ClassLoader hierarchy must be scanned when looking for available context scopes.
		 * @return <code>true</code> if the ClassLoader hierarchy must be scanned when looking for available context
		 *         scopes, <code>false</code> if only the current ClassLoader must be taken into account
		 */
		public boolean isUseClassLoaderHierarchy() {
			return useClassLoaderHierarchy;
		}

		/**
		 * Set whether the ClassLoader hierarchy must be scanned when looking for available context scopes.
		 * @param useClassLoaderHierarchy <code>true</code> to scan the ClassLoader hierarchy when looking for available
		 *        context scopes, <code>false</code> if only the current ClassLoader must be taken into account
		 */
		public void setUseClassLoaderHierarchy(boolean useClassLoaderHierarchy) {
			this.useClassLoaderHierarchy = useClassLoaderHierarchy;
		}

		/**
		 * Obtains the default {@link ClassLoader} to use.
		 * <p>
		 * By default {@link ClassUtils#getDefaultClassLoader()} is used.
		 * </p>
		 * @return the default {@link ClassLoader}
		 */
		public ClassLoader getDefaultClassLoader() {
			return classLoader == null ? ClassUtils.getDefaultClassLoader() : classLoader;
		}

		/**
		 * Set the default {@link ClassLoader} to use.
		 * @param classLoader Default {@link ClassLoader}
		 */
		public void setDefaultClassLoader(ClassLoader classLoader) {
			this.classLoader = classLoader;
		}

		/**
		 * Gets whether the given scope name is registred for <code>classLoader</code>.
		 * @param classLoader ClassLoader
		 * @param scopeName Scope name
		 * @return <code>true</code> if scope is registered
		 */
		public synchronized boolean isScopeRegistered(ClassLoader classLoader, String scopeName) {
			ObjectUtils.argumentNotNull(scopeName, "Scope name must be not null");

			final ClassLoader cl = classLoader == null ? getDefaultClassLoader() : classLoader;
			return ensureInited(cl).containsKey(scopeName);
		}

		/**
		 * Register the given {@link ContextScope} bound to given <code>classLoader</code>
		 * @param classLoader ClassLoader
		 * @param scope Scope to register
		 */
		public synchronized void registerScope(ClassLoader classLoader, ContextScope scope) {
			ObjectUtils.argumentNotNull(scope, "Scope to register must be not null");
			ObjectUtils.argumentNotNull(scope.getName(), "Scope name must be not null");

			final ClassLoader cl = classLoader == null ? getDefaultClassLoader() : classLoader;

			LinkedHashMap<String, ContextScope> contextScopes = ensureInited(cl);
			contextScopes.put(scope.getName(), scope);
			sortScopes(contextScopes);

			LOGGER.debug(() -> "Registered scope [" + scope + "] with name [" + scope.getName() + "] for classloader ["
					+ cl + "]");
		}

		/**
		 * Unregister the scope with given <code>name</code>
		 * @param classLoader ClassLoader
		 * @param name Scope name
		 * @return <code>true</code> if unregistered
		 */
		public synchronized boolean unregisterScope(ClassLoader classLoader, String name) {
			ObjectUtils.argumentNotNull(name, "Scope name must be not null");

			final ClassLoader cl = classLoader == null ? getDefaultClassLoader() : classLoader;

			final boolean removed;
			LinkedHashMap<String, ContextScope> contextScopes = ensureInited(cl);
			if (contextScopes.containsKey(name)) {
				contextScopes.remove(name);
				removed = true;
			} else {
				removed = false;
			}

			LOGGER.debug(() -> "Unregistered scope with name [" + name + "] for classloader [" + cl
					+ "] - was registered: " + removed);

			return removed;
		}

		/**
		 * Gets all the scopes bound to default ClassLoader
		 * @return ContextScopes iterator, preserving the order defined using {@link ContextScope#getOrder()}
		 */
		public Iterable<ContextScope> getScopes() {
			return getScopes(getDefaultClassLoader());
		}

		/**
		 * Gets all the scopes bound to given <code>classLoader</code>
		 * @param classLoader ClassLoader
		 * @return ContextScopes iterator, preserving the order defined using {@link ContextScope#getOrder()}
		 */
		public synchronized Iterable<ContextScope> getScopes(ClassLoader classLoader) {
			ClassLoader cl = classLoader == null ? getDefaultClassLoader() : classLoader;

			if (isUseClassLoaderHierarchy()) {

				final Set<String> scopeNames = new HashSet<>();
				final List<ContextScope> scopes = new LinkedList<>();

				while (cl != null) {
					getScopesForClassLoader(cl).forEach(scope -> {
						if (!scopeNames.contains(scope.getName())) {
							scopes.add(scope);
						}
					});

					// get parent ClassLoader
					try {
						final ClassLoader currentClassLoader = cl;
						cl = AccessController.doPrivileged(new PrivilegedAction<ClassLoader>() {

							@Override
							public ClassLoader run() {
								return currentClassLoader.getParent();
							}

						});
					} catch (Exception e) {
						LOGGER.debug(() -> "Failed to obtain parent ClassLoader", e);
					}
				}

				return scopes;

			}

			return getScopesForClassLoader(cl);
		}

		/**
		 * Gets the scope with given <code>name</code> bound to default ClassLoader
		 * @param name Scope name
		 * @return ContextScope, or <code>null</code> if not registered
		 */
		public ContextScope getScope(String name) {
			return getScope(name, getDefaultClassLoader());
		}

		/**
		 * Gets the scope with given <code>name</code> bound to given <code>classLoader</code>
		 * @param name Scope name
		 * @param classLoader ClassLoader
		 * @return ContextScope, or <code>null</code> if not registered
		 */
		public synchronized ContextScope getScope(String name, ClassLoader classLoader) {
			ObjectUtils.argumentNotNull(name, "Scope name must be not null");

			ClassLoader cl = classLoader == null ? getDefaultClassLoader() : classLoader;

			ContextScope scope = null;

			while (cl != null) {
				scope = getScopeForClassLoader(name, cl);
				if (scope != null) {
					break;
				}
				if (isUseClassLoaderHierarchy()) {
					// get parent ClassLoader
					try {
						final ClassLoader currentClassLoader = cl;
						cl = AccessController.doPrivileged(new PrivilegedAction<ClassLoader>() {

							@Override
							public ClassLoader run() {
								return currentClassLoader.getParent();
							}

						});
					} catch (Exception e) {
						LOGGER.debug(() -> "Failed to obtain parent ClassLoader", e);
					}
				} else {
					cl = null;
				}
			}

			return scope;
		}

		/**
		 * Get the the context scopes registered with given ClassLoader, ensuring scopes are inited using service
		 * loaders.
		 * @param cl The ClassLoader
		 * @return The context scopes bound to given ClassLoader, an empty collection if none
		 */
		private Collection<ContextScope> getScopesForClassLoader(ClassLoader cl) {
			if (cl != null) {
				Collection<ContextScope> classLoaderScopes = ensureInited(cl).values();
				if (classLoaderScopes != null) {
					return classLoaderScopes;
				}
			}
			return Collections.emptyList();
		}

		/**
		 * Get the context scope with given <code>name</code> registered with given ClassLoader, ensuring scopes are
		 * inited using service loaders.
		 * @param name Scope name
		 * @param cl The ClassLoader
		 * @return The {@link ContextScope} with given name, or <code>null</code> if not found
		 */
		private ContextScope getScopeForClassLoader(String name, ClassLoader cl) {
			if (cl != null) {
				LinkedHashMap<String, ContextScope> contextScopes = ensureInited(cl);
				return contextScopes.get(name);
			}
			return null;
		}

		/**
		 * Ensure scopes registry inited using {@link ServiceLoader}
		 * @param classLoader ClassLoader
		 * @return The {LinkedHashMap} associated to ClassLoader ensuring it's never null
		 */
		private LinkedHashMap<String, ContextScope> ensureInited(final ClassLoader classLoader) {

			LinkedHashMap<String, ContextScope> contextScopes = scopes.get(classLoader);

			if (contextScopes == null) {
				// load using ServiceLoader
				contextScopes = AccessController
						.doPrivileged(new PrivilegedAction<LinkedHashMap<String, ContextScope>>() {
							@Override
							public LinkedHashMap<String, ContextScope> run() {
								LinkedHashMap<String, ContextScope> result = new LinkedHashMap<>();
								LOGGER.debug(() -> "Load scopes for classloader [" + classLoader
										+ "] using ServiceLoader with service name: " + ContextScope.class.getName());
								ServiceLoader<ContextScope> serviceLoader = ServiceLoader.load(ContextScope.class,
										classLoader);
								for (final ContextScope provider : serviceLoader) {
									if (provider.getName() == null) {
										throw new IllegalStateException("Invalid ContextScope, missing scope name: "
												+ provider.getClass().getName());
									}
									result.put(provider.getName(), provider);
									LOGGER.debug(() -> "Loaded and registered scope with name [" + provider.getName()
											+ "] for classloader [" + classLoader + "]");
								}
								sortScopes(result);
								return result;
							}
						});
				scopes.put(classLoader, contextScopes);
			}

			return contextScopes;
		}

		/**
		 * Sorts the scopes map using {@link ContextScope#getOrder()}
		 * @param scopes Scopes map to sort
		 */
		private static void sortScopes(LinkedHashMap<String, ContextScope> scopes) {
			List<Map.Entry<String, ContextScope>> entries = new ArrayList<>(scopes.entrySet());
			scopes.clear();
			entries.stream()
					.sorted(Comparator.comparing(Map.Entry::getValue, Comparator.comparingInt((v) -> v.getOrder())))
					.forEachOrdered(e -> scopes.put(e.getKey(), e.getValue()));
		}

	}

}
