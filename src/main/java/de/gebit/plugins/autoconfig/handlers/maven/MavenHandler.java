//
//  MavenHandler.java
//
//  Copyright (C) 2024
//  GEBIT Solutions GmbH,
//  Berlin, Duesseldorf, Stuttgart, Leipzig (Germany)
//  All rights reserved.

package de.gebit.plugins.autoconfig.handlers.maven;

import com.intellij.conversion.impl.ConversionContextImpl;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.ProjectJdkTable;
import com.intellij.openapi.projectRoots.Sdk;
import de.gebit.plugins.autoconfig.UpdateHandler;
import de.gebit.plugins.autoconfig.handlers.AbstractHandler;
import de.gebit.plugins.autoconfig.model.MavenConfiguration;
import de.gebit.plugins.autoconfig.util.Notifications;
import org.jetbrains.idea.maven.execution.MavenRunner;
import org.jetbrains.idea.maven.project.MavenProjectBundle;
import org.jetbrains.idea.maven.project.MavenProjectsManager;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Maven configuration update handler.
 */
public class MavenHandler extends AbstractHandler implements UpdateHandler<MavenConfiguration> {
	@Override
	public String getFileName() {
		return "autoconfigMaven.yaml";
	}

	@Override
	public String getJsonSchema() {
		return "/schema/maven.schema.json";
	}

	@Override
	public String getUpdaterName() {
		return "Maven settings updater";
	}

	@Override
	public Class<MavenConfiguration> getConfigurationClass() {
		return MavenConfiguration.class;
	}

	@Override
	public List<String> updateConfiguration(MavenConfiguration maven, Project project) {
		List<String> changedConfigs = new ArrayList<>();
		var mavenProject = MavenProjectsManager.getInstance(project);
		var mavenProjectSettings = mavenProject.getGeneralSettings();
		applySetting(maven.getThreads(), mavenProjectSettings.getThreads(), mavenProjectSettings::setThreads, changedConfigs, "Maven thread parameter");

		var settingsFilePath = maven.getSettingsFile();
		if (settingsFilePath != null && project.getBasePath() != null) {
			var projectBasePath = Paths.get(project.getBasePath());
			var conversionContextImpl = new ConversionContextImpl(projectBasePath);
			var expandPath = conversionContextImpl.expandPath(settingsFilePath);
			applySetting(expandPath, mavenProjectSettings.getUserSettingsFile(), mavenProjectSettings::setUserSettingsFile, changedConfigs, "Maven user settings file");
		}

		// https://youtrack.jetbrains.com/issue/IDEA-338870 setMavenHome und getMavenHome wird vsl. mit 2025.1 entfernt
		applySetting(getMavenHome(maven.getUseMavenWrapper()), mavenProjectSettings.getMavenHome(), mavenProjectSettings::setMavenHome, changedConfigs, "Maven home");
		applySetting(maven.getUseMavenConfig(), mavenProjectSettings.isUseMavenConfig(), mavenProjectSettings::setUseMavenConfig, changedConfigs, "Use maven config");

		var mavenImportingConfig = maven.getImporting();
		if (mavenImportingConfig != null) {
			var mavenImportingProjectSettings = mavenProject.getImportingSettings();
			applySetting(mavenImportingConfig.getDetectCompiler(), mavenImportingProjectSettings.isAutoDetectCompiler(), mavenImportingProjectSettings::setAutoDetectCompiler, changedConfigs, "Autodetect compiler");
			applySetting(mavenImportingConfig.getDownloadSources(), mavenImportingProjectSettings.isDownloadSourcesAutomatically(), mavenImportingProjectSettings::setDownloadSourcesAutomatically, changedConfigs, "Download sources");
			applySetting(mavenImportingConfig.getDownloadDocumentation(), mavenImportingProjectSettings.isDownloadDocsAutomatically(), mavenImportingProjectSettings::setDownloadDocsAutomatically, changedConfigs, "Download documentation");
			applySetting(mavenImportingConfig.getVmOptions(), mavenImportingProjectSettings.getVmOptionsForImporter(), mavenImportingProjectSettings::setVmOptionsForImporter, changedConfigs, "VM options for maven importer");
			applySetting(findProjectSdk(mavenImportingConfig.getJdk(), project), mavenImportingProjectSettings.getJdkForImporter(), mavenImportingProjectSettings::setJdkForImporter, changedConfigs, "JDK for importer");
		}

		var mavenRunnerConfig = maven.getRunner();
		if (mavenRunnerConfig != null) {
			var mavenRunnerProjectSettings = MavenRunner.getInstance(project).getState();
			applySetting(mavenRunnerConfig.getVmOptions(), mavenRunnerProjectSettings.getVmOptions(), mavenRunnerProjectSettings::setVmOptions, changedConfigs, "VM options for maven runner");
			applySetting(findProjectSdk(mavenRunnerConfig.getJre(), project), mavenRunnerProjectSettings.getJreName(), mavenRunnerProjectSettings::setJreName, changedConfigs, "JRE for runner");
		}

		return changedConfigs;
	}

	private String getMavenHome(Boolean useMavenWrapperConfig) {
		if (useMavenWrapperConfig == null) {
			return null;
		} else {
			if (useMavenWrapperConfig) {
				return MavenProjectBundle.message("maven.wrapper.version.title");
			} else {
				return MavenProjectBundle.message("maven.bundled.version.3.title");
			}
		}
	}

	private String findProjectSdk(String sdkName, Project project) {
		if (sdkName == null) {
			return null;
		}
		for (Sdk jdk : ProjectJdkTable.getInstance().getAllJdks()) {
			if (jdk.getName().equals(sdkName)) {
				return jdk.getName();
			}
		}
		Notifications.showError("SDK \"" + sdkName + " not found, please install it and add it in your SDK list.", project);
		return null;
	}
}
