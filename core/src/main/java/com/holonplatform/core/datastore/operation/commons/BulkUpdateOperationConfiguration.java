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
import java.util.Optional;

import com.holonplatform.core.Path;
import com.holonplatform.core.TypedExpression;
import com.holonplatform.core.query.QueryFilter;

/**
 * Bulk <code>UPDATE</code> operation configuration.
 *
 * @since 5.1.0
 */
public interface BulkUpdateOperationConfiguration extends DatastoreOperationConfiguration {

	/**
	 * Get the optional operation restrictions, expressed as a {@link QueryFilter}.
	 * @return Optional operation filter
	 */
	Optional<QueryFilter> getFilter();

	/**
	 * Get the operation values, expressed as a {@link Path} - {@link TypedExpression} map.
	 * @return The path-value expression map, empty if none
	 */
	Map<Path<?>, TypedExpression<?>> getValues();

}
