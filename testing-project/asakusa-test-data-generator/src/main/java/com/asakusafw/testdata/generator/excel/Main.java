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
package com.asakusafw.testdata.generator.excel;

import static com.asakusafw.dmdl.util.CommandLineUtils.*;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.dmdl.source.DmdlSourceRepository;
import com.asakusafw.testdata.generator.GenerateTask;
import com.asakusafw.testdata.generator.TemplateGenerator;

/**
 * Excel test template generator Command Line Interface.
 * @since 0.2.0
 * @version 0.5.3
 */
public final class Main {

    static final Logger LOG = LoggerFactory.getLogger(Main.class);

    private static final Option OPT_OUTPUT;
    private static final Option OPT_FORMAT;
    private static final Option OPT_ENCODING;
    private static final Option OPT_SOURCE_PATH;
    private static final Option OPT_PLUGIN;

    private static final Options OPTIONS;
    static {
        OPT_OUTPUT = new Option("output", true, //$NON-NLS-1$
                Messages.getString("Main.optOutput")); //$NON-NLS-1$
        OPT_OUTPUT.setArgName("/path/to/output"); //$NON-NLS-1$
        OPT_OUTPUT.setRequired(true);

        OPT_SOURCE_PATH = new Option("source", true, //$NON-NLS-1$
                Messages.getString("Main.optSource")); //$NON-NLS-1$
        OPT_SOURCE_PATH.setArgName(
                "source-file.dmdl" + File.pathSeparatorChar + "/path/to/source"); //$NON-NLS-1$ //$NON-NLS-2$
        OPT_SOURCE_PATH.setRequired(true);

        OPT_FORMAT = new Option("format", true, //$NON-NLS-1$
                Messages.getString("Main.optFormat")); //$NON-NLS-1$
        OPT_FORMAT.setArgName(MessageFormat.format(
                "one-of-{0}", //$NON-NLS-1$
                Arrays.toString(WorkbookFormat.values())));
        OPT_FORMAT.setRequired(true);

        OPT_ENCODING = new Option("encoding", true, //$NON-NLS-1$
                Messages.getString("Main.optEncoding")); //$NON-NLS-1$
        OPT_ENCODING.setArgName("source-encoding"); //$NON-NLS-1$
        OPT_ENCODING.setRequired(false);

        OPT_PLUGIN = new Option("plugin", true, //$NON-NLS-1$
                Messages.getString("Main.optPlugin")); //$NON-NLS-1$
        OPT_PLUGIN.setArgName("plugin-1.jar" + File.pathSeparatorChar + "plugin-2.jar"); //$NON-NLS-1$ //$NON-NLS-2$
        OPT_PLUGIN.setValueSeparator(File.pathSeparatorChar);
        OPT_PLUGIN.setRequired(false);

        OPTIONS = new Options();
        OPTIONS.addOption(OPT_OUTPUT);
        OPTIONS.addOption(OPT_FORMAT);
        OPTIONS.addOption(OPT_ENCODING);
        OPTIONS.addOption(OPT_SOURCE_PATH);
        OPTIONS.addOption(OPT_PLUGIN);
    }

    private Main() {
        return;
    }

    /**
     * Program entry.
     * @param args program arguments
     */
    public static void main(String... args) {
        System.exit(start(args));
    }

    static int start(String... args) {
        assert args != null;
        GenerateTask task;
        try {
            CommandLineParser parser = new BasicParser();
            CommandLine cmd = parser.parse(OPTIONS, args);
            TemplateGenerator generator = getGenerator(cmd);
            DmdlSourceRepository repository = getRepository(cmd);
            ClassLoader classLoader = getClassLoader(cmd);
            task = new GenerateTask(generator, repository, classLoader);
        } catch (Exception e) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.setWidth(Integer.MAX_VALUE);
            formatter.printHelp(
                    MessageFormat.format(
                            "java -classpath ... {0}", //$NON-NLS-1$
                            Main.class.getName()),
                    OPTIONS,
                    true);
            System.out.printf(Messages.getString("Main.helpFormatHead"), OPT_FORMAT.getOpt()); //$NON-NLS-1$
            System.out.printf(" %8s - %s%n", WorkbookFormat.DATA, //$NON-NLS-1$
                    Messages.getString("Main.helpFormatData")); //$NON-NLS-1$
            System.out.printf(" %8s - %s%n", WorkbookFormat.RULE, //$NON-NLS-1$
                    Messages.getString("Main.helpFormatRule")); //$NON-NLS-1$
            System.out.printf(" %8s - %s%n", WorkbookFormat.INOUT, //$NON-NLS-1$
                    Messages.getString("Main.helpFormatInout")); //$NON-NLS-1$
            System.out.printf(" %8s - %s%n", WorkbookFormat.INSPECT, //$NON-NLS-1$
                    Messages.getString("Main.helpFormatInspect")); //$NON-NLS-1$
            System.out.printf(" %8s - %s%n", WorkbookFormat.ALL, //$NON-NLS-1$
                    Messages.getString("Main.helpFormatAll")); //$NON-NLS-1$
            System.out.printf(" %8s - %s%n", WorkbookFormat.DATAX, //$NON-NLS-1$
                    Messages.getString("Main.helpFormatDataX")); //$NON-NLS-1$
            System.out.printf(" %8s - %s%n", WorkbookFormat.RULEX, //$NON-NLS-1$
                    Messages.getString("Main.helpFormatRuleX")); //$NON-NLS-1$
            System.out.printf(" %8s - %s%n", WorkbookFormat.INOUTX, //$NON-NLS-1$
                    Messages.getString("Main.helpFormatInoutX")); //$NON-NLS-1$
            System.out.printf(" %8s - %s%n", WorkbookFormat.INSPECTX, //$NON-NLS-1$
                    Messages.getString("Main.helpFormatInspectX")); //$NON-NLS-1$
            System.out.printf(" %8s - %s%n", WorkbookFormat.ALLX, //$NON-NLS-1$
                    Messages.getString("Main.helpFormatAllX")); //$NON-NLS-1$
            e.printStackTrace(System.out);
            return 1;
        }
        try {
            task.process();
        } catch (IOException e) {
            e.printStackTrace(System.out);
            return 1;
        }
        return 0;
    }

    private static TemplateGenerator getGenerator(CommandLine cmd) {
        assert cmd != null;
        String outputCmd = cmd.getOptionValue(OPT_OUTPUT.getOpt());
        String formatCmd = cmd.getOptionValue(OPT_FORMAT.getOpt());

        File output = new File(outputCmd);
        WorkbookFormat format = WorkbookFormat.findByName(formatCmd);
        if (format == null) {
            throw new IllegalArgumentException(MessageFormat.format(
                    Messages.getString("Main.errorUnknownFormat"), //$NON-NLS-1$
                    OPT_FORMAT.getOpt(),
                    formatCmd,
                    Arrays.toString(WorkbookFormat.values())));
        }
        return new WorkbookGenerator(output, format);
    }

    private static DmdlSourceRepository getRepository(CommandLine cmd) {
        assert cmd != null;
        Charset encoding = parseCharset(cmd.getOptionValue(OPT_ENCODING.getOpt()));
        String sourceCmd = cmd.getOptionValue(OPT_SOURCE_PATH.getOpt());
        DmdlSourceRepository source = buildRepository(parseFileList(sourceCmd), encoding);
        return source;
    }

    private static ClassLoader getClassLoader(CommandLine cmd) {
        assert cmd != null;
        String pluginCmd = cmd.getOptionValue(OPT_PLUGIN.getOpt());
        List<File> plugins = parseFileList(pluginCmd);
        ClassLoader serviceLoader = buildPluginLoader(Main.class.getClassLoader(), plugins);
        return serviceLoader;
    }
}
