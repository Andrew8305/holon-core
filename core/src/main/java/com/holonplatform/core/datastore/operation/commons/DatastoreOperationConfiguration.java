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
package com.holonplatform.core.datastore.operation.commons;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.holonplatform.core.Expression;
import com.holonplatform.core.ExpressionResolver;
import com.holonplatform.core.ExpressionResolver.ExpressionResolverBuilder;
import com.holonplatform.core.ExpressionResolver.ExpressionResolverHandler;
import com.holonplatform.core.ExpressionResolver.ExpressionResolverProvider;
import com.holonplatform.core.ParameterSet;
import com.holonplatform.core.config.ConfigProperty;
import com.holonplatform.core.datastore.DataTarget;
import com.holonplatform.core.datastore.DatastoreOperations.WriteOption;
import com.holonplatform.core.internal.utils.ObjectUtils;

/**
 * Represents a {@link DatastoreOperation} configuration.
 * <p>
 * Extends {@link ExpressionResolverHandler} to support {@link ExpressionResolver} handling.
 * </p>
 *
 * @since 5.1.0
 */
public interface DatastoreOperationConfiguration extends Expression, ExpressionResolverProvider {

	/**
	 * Get the data target.
	 * @return The operation {@link DataTarget}
	 */
	DataTarget<?> getTarget();

	/**
	 * Get the operation parameters.
	 * @return the operation parameters set (not null)
	 */
	ParameterSet getParameters();

	/**
	 * Get the {@link WriteOption}s associated to this operation.
	 * @return The {@link WriteOption}s set, empty if none
	 */
	Set<WriteOption> getWriteOptions();

	/**
	 * Checks whether given {@link WriteOption} is present in this configuration.
	 * @param writeOption The write option to look for (not null)
	 * @return <code>true</code> if the write option is present in this configuration, <code>false</code> otherwise
	 */
	default boolean hasWriteOption(WriteOption writeOption) {
		ObjectUtils.argumentNotNull(writeOption, "Write option must be not null");
		return getWriteOptions().contains(writeOption);
	}

	/**
	 * Get the {@link WriteOption} of given type available in this configuration.
	 * @param <WO> WriteOption type
	 * @param type WriteOption type to look for (not null)
	 * @return A set of write options of given type, empty if none
	 */
	@SuppressWarnings("unchecked")
	default <WO extends WriteOption> Set<WO> getWriteOptions(Class<WO> type) {
		ObjectUtils.argumentNotNull(type, "Write option type be not null");
		return getWriteOptions().stream().filter(wo -> type.isAssignableFrom(wo.getClass())).map(wo -> (WO) wo)
				.collect(Collectors.toSet());
	}

	/**
	 * Get the {@link WriteOption} of given type, if avaible.
	 * <p>
	 * When more than one {@link WriteOption} of given type is available, the first available one is returned.
	 * </p>
	 * @param <WO> WriteOption type
	 * @param type WriteOption type to look for (not null)
	 * @return Optional write option of given type
	 */
	@SuppressWarnings("unchecked")
	default <WO extends WriteOption> Optional<WO> getWriteOption(Class<WO> type) {
		ObjectUtils.argumentNotNull(type, "Write option type be not null");
		return getWriteOptions().stream().filter(wo -> type.isAssignableFrom(wo.getClass())).map(wo -> (WO) wo)
				.findFirst();
	}

	/**
	 * Base {@link DatastoreOperationConfiguration} builder.
	 *
	 * @param <B> Concrete builder type
	 */
	public interface Builder<B extends Builder<B>> extends ExpressionResolverBuilder<B> {

		/**
		 * Set the operation {@link DataTarget}.
		 * @param target the operation data target to set
		 * @return this
		 */
		B target(DataTarget<?> target);

		/**
		 * Add an operation parameter.
		 * @param name Parameter name (not null)
		 * @param value Parameter value
		 * @return this
		 */
		B parameter(String name, Object value);

		/**
		 * Add an operation parameter using a {@link ConfigProperty} and {@link ConfigProperty#getKey()} as parameter
		 * name.
		 * @param <T> Property type
		 * @param property ConfigProperty (not null)
		 * @param value Property value
		 * @return this
		 */
		<T> B parameter(ConfigProperty<T> property, T value);

		/**
		 * Add a {@link WriteOption} to this operation.
		 * @param writeOption The write option to add (not null)
		 * @return this
		 */
		B withWriteOption(WriteOption writeOption);

		/**
		 * Add a set of {@link WriteOption}s to this operation.
		 * @param writeOptions The write options to add (not null)
		 * @return this
		 */
		B withWriteOptions(WriteOption... writeOptions);

		/**
		 * Add a set of {@link WriteOption}s to this operation.
		 * @param writeOptions The write options to add (not null)
		 * @return this
		 */
		B withWriteOptions(Set<WriteOption> writeOptions);

		/**
		 * Add all the expression resolvers provided by given {@link Iterable}.
		 * @param expressionResolvers Expression resolvers to add (not null)
		 * @return this
		 */
		@SuppressWarnings("rawtypes")
		B withExpressionResolvers(Iterable<? extends ExpressionResolver> expressionResolvers);

	}

}
