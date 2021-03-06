/*
 * Copyright 2015-present wequick.net
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain
 * a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package net.wequick.gradle

import org.gradle.api.Project
import org.gradle.api.Plugin
import static org.fusesource.jansi.Ansi.*;
import static org.fusesource.jansi.Ansi.Color.*;

/**
 *
 */
public abstract class BasePlugin implements Plugin<Project> {

    public static final String SMALL_AAR_PREFIX = "net.wequick.small:small:"
    public static final String SMALL_JAR_PATTERN = "net.wequick.small-small-*.jar"
    public static final String SMALL_LIBS = 'smallLibs'

    protected boolean isBuildingBundle
    protected boolean isBuildingLib

    protected Project project

    void apply(Project project) {
        this.project = project

        def sp = project.gradle.startParameter
        def p = sp.projectDir
        def t = sp.taskNames[0]
        if (p == null || p == project.rootProject.projectDir) {
            // gradlew buildLib | buildBundle
            if (t == 'buildLib') isBuildingLib = true
            else if (t == 'buildBundle') isBuildingBundle = true
        } else if (t == 'assembleRelease' || t == 'aR') {
            // gradlew -p [project.name] assembleRelease
            if (pluginType == PluginType.Library) isBuildingLib = true
            else isBuildingBundle = true
        }

        createExtension()

        configureProject()

        createTask()
    }

    protected void createExtension() {
        // Add the 'small' extension object
        project.extensions.create('small', getExtensionClass(), project)
        small.type = getPluginType()
    }

    protected void configureProject() {
        // Tidy up while gradle build finished
        project.gradle.buildFinished { result ->
            if (result.failure == null) return
            tidyUp()
        }
    }

    protected void createTask() {}

    protected <T extends BaseExtension> T getSmall() {
        return (T) project.small
    }

    protected PluginType getPluginType() { return PluginType.Unknown }

    /** Restore state for DEBUG mode */
    protected void tidyUp() { }

    protected abstract Class<? extends BaseExtension> getExtensionClass()

    /**
     * This class consists exclusively of static methods for printing colourful text
     */
    public final class Log {

        public static void header(String text) {
            println(ansi().fg(YELLOW).a("[Small] ")
                    .fg(WHITE).a(text).reset());
        }

        public static void success(String text) {
            print(String.format('\t%-64s', text))
            println(ansi().fg(GREEN).a('[  OK  ]').reset())
        }

        public static void warn(String text) {
            println(ansi().fg(RED).a(String.format('\t%s', text)).reset());
        }

        public static void footer(String text) {
            println(ansi().fg(WHITE).a(String.format('\t%s', text)).reset());
        }
    }
}
