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
package com.asakusafw.windgate.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.windgate.core.process.ProcessProfile;
import com.asakusafw.windgate.core.process.ProcessProvider;
import com.asakusafw.windgate.core.resource.ResourceProfile;
import com.asakusafw.windgate.core.session.SessionProfile;

/**
 * A total profile for WindGate execution.
 * @since 0.2.3
 */
public class GateProfile {

    static final Logger LOG = LoggerFactory.getLogger(GateProfile.class);

    private final CoreProfile core;

    private final SessionProfile session;

    private final List<ProcessProfile> processes;

    private final List<ResourceProfile> resources;

    /**
     * Creates a new instance.
     * @param core the core segment
     * @param session the session segment
     * @param processes the process segment
     * @param resources the resource segment
     * @throws IllegalArgumentException if any parameter is {@code null}
     */
    public GateProfile(
            CoreProfile core,
            SessionProfile session,
            Collection<? extends ProcessProfile> processes,
            Collection<? extends ResourceProfile> resources) {
        if (core == null) {
            throw new IllegalArgumentException("core must not be null"); //$NON-NLS-1$
        }
        if (session == null) {
            throw new IllegalArgumentException("session must not be null"); //$NON-NLS-1$
        }
        if (processes == null) {
            throw new IllegalArgumentException("processes must not be null"); //$NON-NLS-1$
        }
        if (resources == null) {
            throw new IllegalArgumentException("resources must not be null"); //$NON-NLS-1$
        }
        this.core = core;
        this.session = session;
        this.processes = Collections.unmodifiableList(new ArrayList<ProcessProfile>(processes));
        this.resources = Collections.unmodifiableList(new ArrayList<ResourceProfile>(resources));
    }

    /**
     * Returns the core segment of this profile.
     * @return the core segment
     */
    public CoreProfile getCore() {
        return core;
    }

    /**
     * Returns the session segment of this profile.
     * @return the session segment
     */
    public SessionProfile getSession() {
        return session;
    }

    /**
     * Returns the process segment of this profile.
     * @return the process segment
     */
    public List<ProcessProfile> getProcesses() {
        return processes;
    }

    /**
     * Returns the resource segment of this profile.
     * @return the resource segment
     */
    public List<ResourceProfile> getResources() {
        return resources;
    }

    /**
     * Loads a total profile from the properties.
     * @param properties source properties
     * @param loader class loader to load the {@link ProcessProvider}
     * @return the loaded profile
     * @throws IllegalArgumentException if properties are invalid, or if any parameter is {@code null}
     */
    public static GateProfile loadFrom(Properties properties, ClassLoader loader) {
        if (properties == null) {
            throw new IllegalArgumentException("properties must not be null"); //$NON-NLS-1$
        }
        if (loader == null) {
            throw new IllegalArgumentException("loader must not be null"); //$NON-NLS-1$
        }
        LOG.debug("Restoring WindGate profile");
        Properties copy = (Properties) properties.clone();
        CoreProfile core = CoreProfile.loadFrom(copy, loader);
        CoreProfile.removeCorrespondingKeys(copy);
        SessionProfile session = SessionProfile.loadFrom(copy, loader);
        SessionProfile.removeCorrespondingKeys(copy);
        Collection<? extends ProcessProfile> processes = ProcessProfile.loadFrom(copy, loader);
        ProcessProfile.removeCorrespondingKeys(copy);
        Collection<? extends ResourceProfile> resources = ResourceProfile.loadFrom(copy, loader);
        ResourceProfile.removeCorrespondingKeys(copy);
        if (copy.isEmpty() == false) {
            // TODO logging WARN
        }
        return new GateProfile(core, session, processes, resources);
    }
}
