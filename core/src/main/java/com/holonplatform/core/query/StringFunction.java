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
package com.holonplatform.core.query;

import com.holonplatform.core.TypedExpression;
import com.holonplatform.core.internal.query.function.LowerFunction;
import com.holonplatform.core.internal.query.function.UpperFunction;
import com.holonplatform.core.query.QueryFunction.PropertyQueryFunction;

/**
 * Represents a {@link QueryFunction} on a String data type.
 * 
 * @since 5.1.0
 */
public interface StringFunction extends PropertyQueryFunction<String, String>, StringQueryExpression {

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.query.QueryExpression#getType()
	 */
	@Override
	default Class<? extends String> getType() {
		return String.class;
	}

	/**
	 * Function to convert a string to lowercase.
	 */
	public interface Lower extends StringFunction {

		/**
		 * Create a new {@link Lower} function instance.
		 * @param argument Function argument (not null)
		 * @return New {@link Lower} function instance
		 */
		static Lower create(TypedExpression<String> argument) {
			return new LowerFunction(argument);
		}

	}

	/**
	 * Function to convert a string to uppercase.
	 */
	public interface Upper extends StringFunction {

		/**
		 * Create a new {@link Lower} function instance.
		 * @param argument Function argument (not null)
		 * @return New {@link Lower} function instance
		 */
		static Upper create(TypedExpression<String> argument) {
			return new UpperFunction(argument);
		}

	}

}
