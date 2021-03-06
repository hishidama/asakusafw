/**
 * Copyright 2011-2017 Asakusa Framework Team.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.asakusafw.testdriver.core;

import java.io.IOException;
import java.net.URI;

/**
 * Service provider interface of {@link VerifyRule} drivers.
 * <p>
 * Adding {@link VerifyRule} formats, clients can implement this
 * and put the class name in
 * {@code META-INF/services/com.asakusafw.testdriver.core.VerifyRuleProvider}.
 * </p>
 * @since 0.2.0
 */
@FunctionalInterface
public interface VerifyRuleProvider {

    /**
     * Creates a {@link VerifyRule} object from the specified source.
     * <p>
     * If this object does not support the {@link URI},
     * this method will return {@code null}.
     * </p>
     * @param <T> type of data model
     * @param definition the data model definition
     * @param context current verification context
     * @param source the target identifier
     * @return the created {@link VerifyRule},
     *     or {@code null} if the specified source is not valid for this object
     * @throws IOException if failed to load a {@link VerifyRule} from the source
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    <T> VerifyRule get(
            DataModelDefinition<T> definition,
            VerifyContext context,
            URI source) throws IOException;
}
