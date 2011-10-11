/**
 * Copyright 2011 Asakusa Framework Team.
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
package com.asakusafw.yaess.jsch;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.junit.Test;

import com.asakusafw.yaess.core.ExecutionContext;
import com.asakusafw.yaess.core.ExecutionMonitor;
import com.asakusafw.yaess.core.ExecutionPhase;
import com.asakusafw.yaess.core.HadoopScript;
import com.asakusafw.yaess.core.HadoopScriptHandler;
import com.asakusafw.yaess.core.ServiceProfile;
import com.asakusafw.yaess.core.VariableResolver;

/**
 * Test for {@link SshHadoopScriptHandler}.
 */
public class SshHadoopScriptHandlerTest extends SshScriptHandlerTestRoot {

    /**
     * Simple testing.
     * @throws Exception if failed
     */
    @Test
    public void simple() throws Exception {
        String target = new File(getAsakusaHome(), "bin/exec.sh").getAbsolutePath();
        File shell = putScript("arguments.sh", new File(target));

        HadoopScript script = new HadoopScript(
                "testing",
                set(),
                "com.example.Client",
                map(),
                map());

        HadoopScriptHandler handler = handler("command.0", target);
        ExecutionContext context = new ExecutionContext(
                "tbatch", "tflow", "texec", ExecutionPhase.MAIN, map("hello", "world", "key", "value"));
        execute(context, script, handler);

        List<String> results = getOutput(shell);
        assertThat(results.subList(0, 5), is(Arrays.asList(
                "com.example.Client",
                "tbatch",
                "tflow",
                "texec",
                context.getArgumentsAsString())));
    }

    /**
     * With properties.
     * @throws Exception if failed
     */
    @Test
    public void properties() throws Exception {
        String target = new File(getAsakusaHome(), "bin/exec.sh").getAbsolutePath();
        File shell = putScript("arguments.sh", new File(target));

        HadoopScript script = new HadoopScript(
                "testing",
                set(),
                "com.example.Client",
                map("hello", "world", "hoge", "foo"),
                map());

        HadoopScriptHandler handler = handler("command.0", target);
        ExecutionContext context = new ExecutionContext(
                "tbatch", "tflow", "texec", ExecutionPhase.MAIN, map());
        execute(context, script, handler);

        List<String> results = getOutput(shell);
        assertThat(results.subList(0, 5), is(Arrays.asList(
                "com.example.Client",
                "tbatch",
                "tflow",
                "texec",
                context.getArgumentsAsString())));

        List<String> rest = results.subList(5, results.size());
        int hello = rest.indexOf("hello=world");
        assertThat(hello, greaterThanOrEqualTo(1));
        assertThat(rest.get(hello - 1), is("-D"));

        int hoge = rest.indexOf("hoge=foo");
        assertThat(hoge, greaterThanOrEqualTo(1));
        assertThat(rest.get(hoge - 1), is("-D"));
    }

    /**
     * Using complex prefix.
     * @throws Exception if failed
     */
    @Test
    public void complex_prefix() throws Exception {
        String target = new File(getAsakusaHome(), "bin/exec.sh").getAbsolutePath();
        File shell = putScript("arguments.sh", new File(target));

        HadoopScript script = new HadoopScript(
                "testing",
                set(),
                "com.example.Client",
                map(),
                map());

        HadoopScriptHandler handler = handler(
                "command.0", target,
                "command.1", "@[1]-@[2]-@[3]");
        ExecutionContext context = new ExecutionContext(
                "tbatch", "tflow", "texec", ExecutionPhase.MAIN, map("hello", "world", "key", "value"));
        execute(context, script, handler);

        List<String> results = getOutput(shell);
        assertThat(results.subList(0, 6), is(Arrays.asList(
                "tbatch-tflow-texec",
                "com.example.Client",
                "tbatch",
                "tflow",
                "texec",
                context.getArgumentsAsString())));
    }

    /**
     * Show environment variables.
     * @throws Exception if failed
     */
    @Test
    public void environment() throws Exception {
        String target = new File(getAsakusaHome(), "bin/exec.sh").getAbsolutePath();
        File shell = putScript("environment.sh", new File(target));

        HadoopScript script = new HadoopScript(
                "testing",
                set(),
                "com.example.Client",
                map(),
                map("script", "SCRIPT", "override", "SCRIPT"));

        HadoopScriptHandler handler = handler("command.0", target,
                "env.handler", "HANDLER", "env.override", "HANDLER");
        execute(script, handler);

        List<String> results = getOutput(shell);
        assertThat(results, hasItem(equalToIgnoringWhiteSpace("script=SCRIPT")));
        assertThat(results, hasItem(equalToIgnoringWhiteSpace("handler=HANDLER")));
        assertThat(results, hasItem(equalToIgnoringWhiteSpace("override=SCRIPT")));
    }

    /**
     * Mandatory configuration about SSH is missing.
     * @throws Exception if failed
     */
    @Test(expected = IOException.class)
    public void missing_config() throws Exception {
        String target = new File(getAsakusaHome(), "bin/exec.sh").getAbsolutePath();
        putScript("arguments.sh", new File(target));

        Map<String, String> conf = map();
        conf.put("command.0", target);
        ServiceProfile<HadoopScriptHandler> profile = new ServiceProfile<HadoopScriptHandler>(
                "hadoop", SshHadoopScriptHandler.class, conf, getClass().getClassLoader());
        profile.newInstance(VariableResolver.system());
    }

    /**
     * Private key is invalid.
     * @throws Exception if failed
     */
    @Test(expected = IOException.class)
    public void invalid_id() throws Exception {
        String target = new File(getAsakusaHome(), "bin/exec.sh").getAbsolutePath();
        putScript("arguments.sh", new File(target));

        Map<String, String> conf = map();
        conf.put("command.0", target);
        conf.put(JschProcessExecutor.KEY_USER, "${USER}");
        conf.put(JschProcessExecutor.KEY_HOST, "localhost");
        conf.put(JschProcessExecutor.KEY_PRIVATE_KEY, privateKey.getAbsolutePath() + "__INVALID__");
        ServiceProfile<HadoopScriptHandler> profile = new ServiceProfile<HadoopScriptHandler>(
                "hadoop", SshHadoopScriptHandler.class, conf, getClass().getClassLoader());
        profile.newInstance(VariableResolver.system());
    }

    /**
     * Executable file is missing.
     * @throws Exception if failed
     */
    @Test(expected = IOException.class)
    public void command_missing() throws Exception {
        Map<String, String> conf = map();
        conf.put(JschProcessExecutor.KEY_USER, "${USER}");
        conf.put(JschProcessExecutor.KEY_HOST, "localhost");
        conf.put(JschProcessExecutor.KEY_PRIVATE_KEY, privateKey.getAbsolutePath());
        ServiceProfile<HadoopScriptHandler> profile = new ServiceProfile<HadoopScriptHandler>(
                "hadoop", SshHadoopScriptHandler.class, conf, getClass().getClassLoader());
        profile.newInstance(VariableResolver.system());
    }

    /**
     * Exit abnormally.
     * @throws Exception if failed
     */
    @Test(expected = IOException.class)
    public void abnormal_exit() throws Exception {
        String target = new File(getAsakusaHome(), "bin/exec.sh").getAbsolutePath();
        putScript("abnormal.sh", new File(target));

        HadoopScript script = new HadoopScript(
                "testing",
                set(),
                "com.example.Client",
                map(),
                map());

        HadoopScriptHandler handler = handler("command.0", target);
        ExecutionContext context = new ExecutionContext(
                "tbatch", "tflow", "texec", ExecutionPhase.MAIN, map());
        handler.execute(ExecutionMonitor.NULL, context, script);
    }

    /**
     * Script is missing.
     * @throws Exception if failed
     */
    @Test(expected = IOException.class)
    public void script_missing() throws Exception {
        HadoopScript script = new HadoopScript(
                "testing",
                set(),
                "com.example.Client",
                map(),
                map());

        HadoopScriptHandler handler = handler("command.0", "__INVALID__");
        ExecutionContext context = new ExecutionContext(
                "tbatch", "tflow", "texec", ExecutionPhase.MAIN, map());
        handler.execute(ExecutionMonitor.NULL, context, script);
    }

    /**
     * Using invalid prefix.
     * @throws Exception if failed
     */
    @Test(expected = IOException.class)
    public void invalid_prefix() throws Exception {
        String target = new File(getAsakusaHome(), "bin/exec.sh").getAbsolutePath();
        putScript("arguments.sh", new File(target));

        HadoopScript script = new HadoopScript(
                "testing",
                set(),
                "com.example.Client",
                map(),
                map());

        HadoopScriptHandler handler = handler(
                "command.0", target,
                "command.1", "@[999]");
        ExecutionContext context = new ExecutionContext(
                "tbatch", "tflow", "texec", ExecutionPhase.MAIN, map());
        handler.execute(ExecutionMonitor.NULL, context, script);
    }

    private HadoopScriptHandler handler(String... keyValuePairs) {
        Map<String, String> conf = map(keyValuePairs);
        conf.put(JschProcessExecutor.KEY_USER, "${USER}");
        conf.put(JschProcessExecutor.KEY_HOST, "localhost");
        conf.put(JschProcessExecutor.KEY_PRIVATE_KEY, privateKey.getAbsolutePath());
        ServiceProfile<HadoopScriptHandler> profile = new ServiceProfile<HadoopScriptHandler>(
                "hadoop", SshHadoopScriptHandler.class, conf, getClass().getClassLoader());
        try {
            return profile.newInstance(VariableResolver.system());
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }
}
