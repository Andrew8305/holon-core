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
package com.holonplatform.core.internal.datastore.operation.common;

import com.holonplatform.core.datastore.operation.commons.PropertyBoxOperationConfiguration;
import com.holonplatform.core.property.PropertyBox;

/**
 * {@link PropertyBoxOperationConfiguration} definition with configuration setters.
 *
 * @since 5.1.0
 */
public interface PropertyBoxOperationDefinition
		extends DatastoreOperationDefinition, PropertyBoxOperationConfiguration {

	/**
	 * Set the operation {@link PropertyBox} value.
	 * @param value The value to set
	 */
	void setValue(PropertyBox value);

}
