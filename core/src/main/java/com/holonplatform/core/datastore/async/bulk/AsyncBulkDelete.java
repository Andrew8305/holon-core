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
package com.holonplatform.core.datastore.async.bulk;

import java.util.concurrent.CompletableFuture;

import com.holonplatform.core.datastore.Datastore.OperationResult;
import com.holonplatform.core.datastore.DatastoreCommodity;
import com.holonplatform.core.datastore.bulk.BulkDeleteOperation;
import com.holonplatform.core.datastore.operation.ExecutableOperation;

/**
 * TODO
 */
public interface AsyncBulkDelete extends BulkDeleteOperation<AsyncBulkDelete>,
		ExecutableOperation<CompletableFuture<OperationResult>>, DatastoreCommodity {

}
