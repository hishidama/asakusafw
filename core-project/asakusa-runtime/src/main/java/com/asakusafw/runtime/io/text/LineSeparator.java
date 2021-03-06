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
package com.asakusafw.runtime.io.text;

/**
 * Represents line separator sequence kind.
 * @since 0.9.1
 */
public enum LineSeparator {

    /**
     * Unix-style line separator ({@code LF}).
     */
    UNIX("\n"),

    /**
     * Windows-style line separator ({@code CR+LF}).
     */
    WINDOWS("\r\n"),
    ;

    private final String sequence;

    LineSeparator(String sequence) {
        this.sequence = sequence;
    }

    /**
     * Returns the line separator sequence.
     * @return the sequence
     */
    public String getSequence() {
        return sequence;
    }
}
