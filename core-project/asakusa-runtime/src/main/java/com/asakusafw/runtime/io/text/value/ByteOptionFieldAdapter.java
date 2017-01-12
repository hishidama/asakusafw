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
package com.asakusafw.runtime.io.text.value;

import java.util.Collection;

import com.asakusafw.runtime.io.text.TextUtil;
import com.asakusafw.runtime.io.text.driver.FieldAdapter;
import com.asakusafw.runtime.io.text.driver.FieldOutput;
import com.asakusafw.runtime.value.ByteOption;

/**
 * An implementation of {@link FieldAdapter} which accepts {@link ByteOption}.
 * @since 0.9.1
 */
public final class ByteOptionFieldAdapter extends ValueOptionFieldAdapter<ByteOption> {

    ByteOptionFieldAdapter(String nullFormat, Collection<? extends FieldOutput.Option> outputOptions) {
        super(nullFormat, outputOptions);
    }

    /**
     * Returns a new builder.
     * @return the created builder
     */
    public static Builder builder() {
        return new Builder();
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void doParse(CharSequence contents, ByteOption property) {
        property.modify(TextUtil.parseByte(contents, 0, contents.length()));
    }

    @Override
    protected void doEmit(ByteOption property, StringBuilder output) {
        output.append(property.get());
    }

    /**
     * A builder of {@link ByteOptionFieldAdapter}.
     * @since 0.9.1
     */
    public static class Builder extends BuilderBase<Builder, ByteOptionFieldAdapter> {
        @Override
        public ByteOptionFieldAdapter build() {
            return new ByteOptionFieldAdapter(getNullFormat(), getOutputOptions());
        }
    }
}