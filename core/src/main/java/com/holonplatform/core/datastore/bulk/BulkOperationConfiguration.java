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
package com.holonplatform.core.datastore.bulk;

import java.util.Set;

import com.holonplatform.core.Expression;
import com.holonplatform.core.ExpressionResolver;
import com.holonplatform.core.ExpressionResolver.ExpressionResolverHandler;
import com.holonplatform.core.datastore.DataTarget;
import com.holonplatform.core.datastore.DatastoreOperations.WriteOption;
import com.holonplatform.core.internal.utils.ObjectUtils;

/**
 * Represents a {@link BulkOperation} configuration.
 * <p>
 * Extends {@link ExpressionResolverHandler} to support {@link ExpressionResolver} handling.
 * </p>
 *
 * @since 5.1.0
 */
public interface BulkOperationConfiguration extends Expression, ExpressionResolverHandler {

	/**
	 * Get the data target.
	 * @return The operation {@link DataTarget}
	 */
	DataTarget<?> getTarget();

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

}
