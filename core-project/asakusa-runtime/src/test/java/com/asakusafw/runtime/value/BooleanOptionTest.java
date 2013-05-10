/**
 * Copyright 2011-2013 Asakusa Framework Team.
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
package com.asakusafw.runtime.value;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import org.junit.Test;

/**
 * Test for {@link BooleanOption}.
 */
@SuppressWarnings("deprecation")
public class BooleanOptionTest extends ValueOptionTestRoot {

    /**
     * 初期状態のテスト。
     */
    @Test
    public void init() {
        BooleanOption option = new BooleanOption();
        assertThat(option.isNull(), is(true));
    }

    /**
     * 値の取得。
     */
    @Test
    public void get() {
        BooleanOption option = new BooleanOption();
        option.modify(false);
        assertThat(option.get(), is(false));
        assertThat(option.isNull(), is(false));
    }

    /**
     * nullに対するor。
     */
    @Test
    public void or() {
        BooleanOption option = new BooleanOption();
        assertThat(option.or(true), is(true));
        assertThat(option.isNull(), is(true));
    }

    /**
     * すでに値が設定された状態のor。
     */
    @Test
    public void orNotNull() {
        BooleanOption option = new BooleanOption();
        option.modify(true);
        assertThat(option.or(false), is(true));
    }

    /**
     * copyFromのテスト。
     */
    @Test
    public void copy() {
        BooleanOption option = new BooleanOption();
        BooleanOption other = new BooleanOption();
        other.modify(true);
        option.copyFrom(other);
        assertThat(option.get(), is(true));

        option.modify(false);
        assertThat(other.get(), is(true));
    }

    /**
     * copyFromにnullを指定するテスト。
     */
    @Test
    public void copyNull() {
        BooleanOption option = new BooleanOption();
        option.modify(true);

        BooleanOption other = new BooleanOption();
        option.copyFrom(other);
        assertThat(option.isNull(), is(true));
        option.modify(true);

        option.copyFrom(null);
        assertThat(option.isNull(), is(true));
    }

    /**
     * 比較のテスト。
     */
    @Test
    public void compareTo() {
        BooleanOption a = new BooleanOption();
        BooleanOption b = new BooleanOption();
        BooleanOption c = new BooleanOption();

        a.modify(false);
        b.modify(true);
        c.modify(false);

        assertThat(compare(a, b), lessThan(0));
        assertThat(compare(b, a), greaterThan(0));
        assertThat(compare(a, c), is(0));
    }

    /**
     * nullに関する順序付けのテスト。
     */
    @Test
    public void compareNull() {
        BooleanOption a = new BooleanOption();
        BooleanOption b = new BooleanOption();
        BooleanOption c = new BooleanOption();

        a.modify(false);

        assertThat(compare(a, b), greaterThan(0));
        assertThat(compare(b, a), lessThan(0));
        assertThat(compare(b, c), is(0));
    }

    /**
     * Writableのテスト。
     */
    @Test
    public void writeTrue() {
        BooleanOption option = new BooleanOption();
        option.modify(true);
        BooleanOption restored = restore(option);
        assertThat(restored.get(), is(option.get()));
    }

    /**
     * Writableのテスト。
     */
    @Test
    public void writeFalse() {
        BooleanOption option = new BooleanOption();
        option.modify(true);
        BooleanOption restored = restore(option);
        assertThat(restored.get(), is(option.get()));
    }

    /**
     * null-Writableのテスト。
     */
    @Test
    public void writeNull() {
        BooleanOption option = new BooleanOption();
        BooleanOption restored = restore(option);
        assertThat(restored.isNull(), is(true));
    }
}
