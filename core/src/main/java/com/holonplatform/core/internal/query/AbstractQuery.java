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
package com.holonplatform.core.internal.query;

import com.holonplatform.core.query.Query;

/**
 * Base {@link Query} implementation providing support for {@link QueryDefinition} management.
 * 
 * @param <D> Query definition type
 * 
 * @since 5.0.0
 */
public abstract class AbstractQuery<D extends QueryDefinition> extends AbstractQueryBuilder<Query, D> implements Query {

	private static final long serialVersionUID = -3424581990790271462L;

	/**
	 * Constructor
	 * @param queryDefinition Query definition. Must be not <code>null</code>.
	 */
	public AbstractQuery(D queryDefinition) {
		super(queryDefinition);
	}

}
