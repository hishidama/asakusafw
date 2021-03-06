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
package com.asakusafw.directio.hive.orc;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hive.ql.io.orc.CompressionKind;
import org.apache.hadoop.hive.ql.io.orc.OrcFile;
import org.apache.hadoop.hive.ql.io.orc.OrcFile.OrcTableProperties;
import org.apache.hadoop.hive.ql.io.orc.OrcFile.Version;
import org.apache.hadoop.hive.ql.io.orc.OrcFile.WriterOptions;
import org.apache.hadoop.hive.ql.io.orc.Reader;
import org.apache.hadoop.hive.ql.io.orc.StripeInformation;

import com.asakusafw.directio.hive.info.BuiltinStorageFormatInfo;
import com.asakusafw.directio.hive.info.StorageFormatInfo;
import com.asakusafw.directio.hive.info.TableInfo;
import com.asakusafw.directio.hive.serde.DataModelDescriptor;
import com.asakusafw.directio.hive.serde.DataModelInspector;
import com.asakusafw.directio.hive.serde.DataModelMapping;
import com.asakusafw.directio.hive.serde.PropertyDescriptor;
import com.asakusafw.runtime.directio.Counter;
import com.asakusafw.runtime.directio.DirectInputFragment;
import com.asakusafw.runtime.directio.hadoop.BlockMap;
import com.asakusafw.runtime.directio.hadoop.HadoopFileFormat;
import com.asakusafw.runtime.directio.hadoop.StripedDataFormat;
import com.asakusafw.runtime.io.ModelInput;
import com.asakusafw.runtime.io.ModelOutput;

/**
 * An abstract implementation of {@link HadoopFileFormat} for ORCFile.
 * @param <T> the data model type
 * @since 0.7.0
 */
public abstract class AbstractOrcFileFormat<T> extends HadoopFileFormat<T>
        implements StripedDataFormat<T>, TableInfo.Provider {

    static final Log LOG = LogFactory.getLog(AbstractOrcFileFormat.class);

    /**
     * Returns the format configuration.
     * @return the format configuration
     */
    public abstract OrcFormatConfiguration getFormatConfiguration();

    /**
     * Returns the target data model descriptor.
     * @return the target data model descriptor
     */
    public abstract DataModelDescriptor getDataModelDescriptor();

    /**
     * Returns the table name.
     * @return the table name
     */
    public abstract String getTableName();

    @Override
    public TableInfo getSchema() {
        DataModelDescriptor desc = getDataModelDescriptor();
        TableInfo.Builder builder = new TableInfo.Builder(getTableName());
        for (PropertyDescriptor property : desc.getPropertyDescriptors()) {
            builder.withColumn(property.getSchema());
        }
        builder.withComment(desc.getDataModelComment());
        builder.withStorageFormat(BuiltinStorageFormatInfo.of(StorageFormatInfo.FormatKind.ORC));
        OrcFormatConfiguration conf = getFormatConfiguration();
        Map<String, String> properties = new HashMap<>();
        putTableProperty(properties, OrcTableProperties.COMPRESSION, conf.getCompressionKind());
        putTableProperty(properties, OrcTableProperties.STRIPE_SIZE, conf.getStripeSize());
        builder.withProperties(properties);

        return builder.build();
    }

    private void putTableProperty(Map<String, String> results, OrcTableProperties property, Object value) {
        if (value == null) {
            return;
        }
        results.put(property.getPropName(), value.toString());
    }

    @SuppressWarnings("unchecked")
    @Override
    public Class<T> getSupportedType() {
        return (Class<T>) getDataModelDescriptor().getDataModelClass();
    }

    @Override
    public List<DirectInputFragment> computeInputFragments(
            InputContext context) throws IOException, InterruptedException {
        // TODO parallel?
        List<DirectInputFragment> results = new ArrayList<>();
        for (FileStatus status : context.getInputFiles()) {
            if (LOG.isInfoEnabled()) {
                LOG.info(MessageFormat.format(
                        Messages.getString("AbstractOrcFileFormat.infoLoadMetadata"), //$NON-NLS-1$
                        context.getDataType().getSimpleName(),
                        status.getPath()));
            }
            Reader orc = OrcFile.createReader(context.getFileSystem(), status.getPath());
            if (LOG.isInfoEnabled()) {
                LOG.info(MessageFormat.format(
                        Messages.getString("AbstractOrcFileFormat.infoAnalyzeMetadata"), //$NON-NLS-1$
                        context.getDataType().getSimpleName(),
                        status.getPath(),
                        orc.getNumberOfRows(),
                        orc.getRawDataSize()));
            }
            BlockMap blockMap = BlockMap.create(
                    status.getPath().toString(),
                    status.getLen(),
                    BlockMap.computeBlocks(context.getFileSystem(), status),
                    false);
            // TODO configurable split
            for (StripeInformation stripe : orc.getStripes()) {
                long begin = stripe.getOffset();
                long end = begin + stripe.getLength();
                DirectInputFragment fragment = blockMap.get(begin, end);
                if (LOG.isDebugEnabled()) {
                    LOG.debug(MessageFormat.format(
                            "Detect ORCFile stripe: path={0}, rows={1}, range={2}+{3}, allocation={4}", //$NON-NLS-1$
                            fragment.getPath(),
                            stripe.getNumberOfRows(),
                            fragment.getOffset(),
                            fragment.getSize(),
                            fragment.getOwnerNodeNames()));
                }
                results.add(fragment);
            }
        }
        return results;
    }

    @Override
    public ModelInput<T> createInput(
            Class<? extends T> dataType,
            FileSystem fileSystem, Path path,
            long offset, long fragmentSize,
            Counter counter) throws IOException, InterruptedException {
        DataModelMapping driverConf = new DataModelMapping();
        OrcFormatConfiguration conf = getFormatConfiguration();
        if (LOG.isDebugEnabled()) {
            LOG.debug(MessageFormat.format(
                    "ORCFile input ({0}): {1}", //$NON-NLS-1$
                    path,
                    conf));
        }
        if (conf.getFieldMappingStrategy() != null) {
            driverConf.setFieldMappingStrategy(conf.getFieldMappingStrategy());
        }
        if (conf.getOnMissingSource() != null) {
            driverConf.setOnMissingSource(conf.getOnMissingSource());
        }
        if (conf.getOnMissingTarget() != null) {
            driverConf.setOnMissingTarget(conf.getOnMissingTarget());
        }
        if (conf.getOnIncompatibleType() != null) {
            driverConf.setOnIncompatibleType(conf.getOnIncompatibleType());
        }
        long size = fragmentSize;
        if (size < 0L) {
            FileStatus stat = fileSystem.getFileStatus(path);
            size = stat.getLen();
        }
        return new OrcFileInput<>(
                getDataModelDescriptor(), driverConf,
                fileSystem, path,
                offset, size, counter);
    }

    @Override
    public ModelOutput<T> createOutput(
            Class<? extends T> dataType,
            FileSystem fileSystem, Path path,
            Counter counter) throws IOException, InterruptedException {
        WriterOptions options = OrcFile.writerOptions(getConf());
        options.fileSystem(fileSystem);
        options.inspector(new DataModelInspector(getDataModelDescriptor()));

        OrcFormatConfiguration conf = getFormatConfiguration();
        if (LOG.isDebugEnabled()) {
            LOG.debug(MessageFormat.format(
                    "ORCFile output ({0}): {1}", //$NON-NLS-1$
                    path,
                    conf));
        }
        Version formatVersion = conf.getFormatVersion();
        if (formatVersion != null) {
            options.version(formatVersion);
        }
        CompressionKind compressionKind = conf.getCompressionKind();
        if (compressionKind != null) {
            options.compress(compressionKind);
        }
        Long stripeSize = conf.getStripeSize();
        if (stripeSize != null) {
            options.stripeSize(stripeSize);
        }

        return new OrcFileOutput<>(getDataModelDescriptor(), path, options, counter);
    }
}
