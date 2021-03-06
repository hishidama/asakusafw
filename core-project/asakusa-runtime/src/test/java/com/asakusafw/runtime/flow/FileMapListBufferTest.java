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
package com.asakusafw.runtime.flow;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.text.MessageFormat;

import org.apache.hadoop.io.Writable;
import org.junit.Test;

import com.asakusafw.runtime.model.DataModel;
import com.asakusafw.runtime.value.IntOption;

/**
 * Test for {@link FileMapListBuffer}.
 */
public class FileMapListBufferTest {

    /**
     * creates an empty buffer.
     */
    @Test
    public void createEmpty() {
        FileMapListBuffer<Holder> buf = new FileMapListBuffer<>();
        buf.begin();
        buf.end();
        assertThat(buf.size(), is(0));

        buf.shrink();
    }

    /**
     * creates a buffer with one element.
     */
    @Test
    public void createSingle() {
        FileMapListBuffer<Holder> buf = new FileMapListBuffer<>();
        buf.begin();
        assertThat(buf.isExpandRequired(), is(true));
        buf.expand(new Holder(""));
        assertThat(buf.isExpandRequired(), is(false));
        buf.advance().value = "Hello";
        buf.end();
        assertThat(buf.size(), is(1));
        assertThat(buf.get(0), is(new Holder("Hello")));

        buf.shrink();
    }

    /**
     * reuses element objects.
     */
    @Test
    public void reuse() {
        FileMapListBuffer<Holder> buf = new FileMapListBuffer<>();
        buf.begin();
        assertThat(buf.isExpandRequired(), is(true));
        buf.expand(new Holder(""));
        buf.advance().value = "Hello";
        buf.end();
        buf.shrink();

        buf.begin();
        buf.advance().value = "World";
        buf.end();

        assertThat(buf.size(), is(1));
        assertThat(buf.get(0), is(new Holder("World")));
        buf.shrink();
    }

    /**
     * creates a big buffer.
     */
    @Test
    public void createBigList() {
        int size = 10000000;
        long begin = System.currentTimeMillis();
        ListBuffer<Holder> buf = new FileMapListBuffer<>();
        buf.begin();
        for (int i = 0; i < size; i++) {
            if (buf.isExpandRequired()) {
                buf.expand(new Holder(""));
            }
            buf.advance().value = String.valueOf(i);
        }
        buf.end();
        long written = System.currentTimeMillis();
        System.out.println(MessageFormat.format(
                "Elapsed for write: {0}ms",
                written - begin));

        for (int i = 0; i < size; i++) {
            buf.get(i);
        }
        long read = System.currentTimeMillis();
        System.out.println(MessageFormat.format(
                "Elapsed for read: {0}ms",
                read - written));

        assertThat(buf.size(), is(size));

        for (int i = 0; i < size; i++) {
            assertThat(buf.get(i).value, is(String.valueOf(i)));
        }

        buf.shrink();
    }

    /**
     * w/ multiple records.
     * @throws Exception if failed
     */
    @Test
    public void huge() throws Exception {
        long t0 = System.currentTimeMillis();
        ListBuffer<IntOption> list = new FileMapListBuffer<>();
        try {
            int begin = 0;
            int end = 100_000_000;
            int size = range(list, begin, end);
            assertThat(list.size(), is(size));
            long t1 = System.currentTimeMillis();
            for (int i = 0, n = end - begin; i < n; i++) {
                IntOption value = list.get(i);
                assertEquals(i + begin, value.get());
            }
            long t2 = System.currentTimeMillis();
            System.out.printf("SpLB - write: %,dms, read: %,dms%n", t1 - t0, t2 - t1);
        } finally {
            list.shrink();
        }
    }

    @SuppressWarnings("deprecation")
    private static int range(ListBuffer<IntOption> buffer, int begin, int end) {
        buffer.begin();
        for (int i = begin; i < end; i++) {
            if (buffer.isExpandRequired()) {
                buffer.expand(new IntOption());
            }
            buffer.advance().modify(i);
        }
        buffer.end();
        return end - begin;
    }

    /**
     * over expand.
     */
    @Test(expected = IndexOutOfBoundsException.class)
    public void over_expand() {
        FileMapListBuffer<Holder> buf = new FileMapListBuffer<>();
        try {
            buf.begin();
            while (true) {
                buf.expand(new Holder(""));
            }
        } finally {
            buf.shrink();
        }
    }

    /**
     * missing advance.
     */
    @Test(expected = IndexOutOfBoundsException.class)
    public void get_UpperOutOfBounds() {
        FileMapListBuffer<Holder> buf = new FileMapListBuffer<>();
        try {
            buf.begin();
            buf.end();
            buf.get(0);
        } finally {
            buf.shrink();
        }
    }

    /**
     * negative index.
     */
    @Test(expected = IndexOutOfBoundsException.class)
    public void get_LowerOutOfBounds() {
        FileMapListBuffer<Holder> buf = new FileMapListBuffer<>();
        try {
            buf.begin();
            buf.end();
            buf.get(-1);
        } finally {
            buf.shrink();
        }
    }

    static class Holder implements DataModel<Holder>, Writable {

        String value;

        Holder(String value) {
            this.value = value;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((value == null) ? 0 : value.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            Holder other = (Holder) obj;
            if (value == null) {
                if (other.value != null) {
                    return false;
                }
            } else if (!value.equals(other.value)) {
                return false;
            }
            return true;
        }

        @Override
        public String toString() {
            return String.valueOf(value);
        }

        @Override
        public void write(DataOutput out) throws IOException {
            if (value != null) {
                out.writeBoolean(true);
                out.writeUTF(value);
            } else {
                out.writeBoolean(false);
            }
        }

        @Override
        public void readFields(DataInput in) throws IOException {
            if (in.readBoolean()) {
                value = in.readUTF();
            } else {
                value = null;
            }
        }

        @Override
        public void reset() {
            value = null;
        }

        @Override
        public void copyFrom(Holder other) {
            value = other.value;
        }
    }
}
