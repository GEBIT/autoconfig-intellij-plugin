//
//  JavaHandler.java
//
//  Copyright (C) 2024
//  GEBIT Solutions GmbH,
//  Berlin, Duesseldorf, Stuttgart, Leipzig (Germany)
//  All rights reserved.

package de.gebit.plugins.autoconfig.handlers.java;

import com.intellij.compiler.CompilerConfiguration;
import com.intellij.compiler.CompilerConfigurationImpl;
import com.intellij.compiler.impl.javaCompiler.BackendCompiler;
import com.intellij.openapi.project.Project;
import de.gebit.plugins.autoconfig.UpdateHandler;
import de.gebit.plugins.autoconfig.handlers.AbstractHandler;
import de.gebit.plugins.autoconfig.model.AnnotationProcessor;
import de.gebit.plugins.autoconfig.model.Compiler;
import de.gebit.plugins.autoconfig.model.JavaConfiguration;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.jps.model.java.compiler.ProcessorConfigProfile;

import java.util.ArrayList;
import java.util.List;

/**
 * Handler used to update general Java settings in IntelliJ configuration.
 */
public class JavaHandler extends AbstractHandler implements UpdateHandler<JavaConfiguration> {
	private static final @NonNls String CONFIG_SCHEMA_JSON = "/schema/java.schema.json";

	public static final @NonNls String CONFIG_FILE_NAME = "autoconfigJava.yaml";

	@Override
	public String getFileName() {
		return CONFIG_FILE_NAME;
	}

	@Override
	public String getJsonSchema() {
		return CONFIG_SCHEMA_JSON;
	}

	@Override
	public String getUpdaterName() {
		return "Java configuration updater";
	}

	@Override
	public Class<JavaConfiguration> getConfigurationClass() {
		return JavaConfiguration.class;
	}

	@Override
	public List<String> updateConfiguration(JavaConfiguration configuration, Project project) {
		List<String> changedConfigs = new ArrayList<>();
		Compiler compiler = configuration.getCompiler();
		if (compiler != null && CompilerConfiguration.getInstance(project) instanceof CompilerConfigurationImpl compilerConfiguration) {
			Compiler.JavaCompiler javaCompiler = compiler.getJavaCompiler();
			if (javaCompiler != null && !compilerConfiguration.getDefaultCompiler().getId().equals(javaCompiler.value())) {
				for (BackendCompiler registeredCompiler : compilerConfiguration.getRegisteredJavaCompilers()) {
					if (registeredCompiler.getId().equals(javaCompiler.value())) {
						applySetting(registeredCompiler, compilerConfiguration.getDefaultCompiler(), compilerConfiguration::setDefaultCompiler, changedConfigs, "Java compiler");
						break;
					}
				}
			}
			AnnotationProcessor annotationProcessor = compiler.getAnnotationProcessor();
			if (annotationProcessor != null) {
				ProcessorConfigProfile defaultProcessorProfile = compilerConfiguration.getDefaultProcessorProfile();
				applySetting(annotationProcessor.getEnable(), defaultProcessorProfile.isEnabled(), defaultProcessorProfile::setEnabled, changedConfigs, "Enable annotation processor");
			}

			Boolean parallelCompilation = compiler.getParallelCompilation();
			if (parallelCompilation != null) {
				applySetting(parallelCompilation, compilerConfiguration.isParallelCompilationEnabled(), compilerConfiguration::setParallelCompilationEnabled, changedConfigs, "Enable parallel compilation");
			}

			Integer buildProcessHeapSize = compiler.getBuildProcessHeapSize();
			if (buildProcessHeapSize != null) {
				applySetting(buildProcessHeapSize, compilerConfiguration.getBuildProcessHeapSize(0), compilerConfiguration::setBuildProcessHeapSize, changedConfigs, "Build process heap size");
			}
		}
		return changedConfigs;
	}
}
