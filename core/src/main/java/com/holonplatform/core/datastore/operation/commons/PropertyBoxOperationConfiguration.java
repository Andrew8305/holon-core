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

import java.util.Map;

import com.holonplatform.core.Path;
import com.holonplatform.core.TypedExpression;
import com.holonplatform.core.property.PropertyBox;

/**
 * A {@link DatastoreOperationConfiguration} with {@link PropertyBox} support.
 * 
 * @since 5.1.0
 */
public interface PropertyBoxOperationConfiguration extends DatastoreOperationConfiguration {

	/**
	 * Get the {@link PropertyBox} value.
	 * @return the {@link PropertyBox} value
	 */
	PropertyBox getValue();

	/**
	 * Get the {@link PropertyBox} value as a {@link Path} - {@link TypedExpression} map.
	 * @param includeNullValues Whether to include <code>null</code> property box values
	 * @return Map of the {@link PropertyBox} properties which can be represented as a {@link Path} and their values as
	 *         {@link TypedExpression}s
	 */
	Map<Path<?>, TypedExpression<?>> getValueExpressions(boolean includeNullValues);

	/**
	 * {@link PropertyBoxOperationConfiguration} builder.
	 *
	 * @param <B> Concrete builder type
	 */
	public interface Builder<B extends Builder<B>> extends DatastoreOperationConfiguration.Builder<B> {

		/**
		 * Set the operation {@link PropertyBox} value.
		 * @param value The value to set
		 * @return this
		 */
		B value(PropertyBox value);

	}

}
