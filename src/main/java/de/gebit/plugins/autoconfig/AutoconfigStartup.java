//
//  AutoconfigStartup.java
//
//  Copyright (C) 2024
//  GEBIT Solutions GmbH,
//  Berlin, Duesseldorf, Stuttgart, Leipzig (Germany)
//  All rights reserved.

package de.gebit.plugins.autoconfig;

import com.intellij.ide.impl.TrustedProjects;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.extensions.ExtensionPointName;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.ProjectActivity;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static de.gebit.plugins.autoconfig.util.Notifications.showInfo;

/**
 * Entry point for the opening of a project. The yaml configuration file is read here, the resulting configuration options object is passed to the
 * CommonConfigurationHandler. At the end, a message is composed, displaying a list of all updated configuration options.
 */
public class AutoconfigStartup implements ProjectActivity {
	private static final com.intellij.openapi.diagnostic.Logger LOG = Logger.getInstance(AutoconfigStartup.class);

	public static final ExtensionPointName<UpdateHandler<?>>
			EP_NAME = ExtensionPointName.create("de.gebit.plugins.autoconfig.configurationUpdater");

	@Nullable
	@Override
	public Object execute(@NotNull Project project, @NotNull Continuation<? super Unit> continuation) {
		return runAutoconfig(project);
	}

	@SuppressWarnings("UnstableApiUsage")
	public @Nullable List<String> runAutoconfig(@NotNull Project project) {
		ConfigurationLoaderService projectService = project.getService(ConfigurationLoaderService.class);
		if (projectService == null || !projectService.hasAutoconfigDir()) {
			return null;
		}

		if (!TrustedProjects.isTrusted(project)) {
			showInfo("Project configuration has not been updated, because project was opened in safe mode.", project);
			return null;
		}

		List<String> changedConfigs = new ArrayList<>();

		for (UpdateHandler<?> updateHandler : EP_NAME.getExtensionList()) {
			changedConfigs.addAll(processUpdateHandler(project, updateHandler, projectService));
		}

		if (!changedConfigs.isEmpty()) {
			String notification = String.join(", ", changedConfigs);
			showInfo("New project configurations applied: " + notification, project);
		}

		return changedConfigs;
	}

	private <T> List<String> processUpdateHandler(@NotNull Project project, UpdateHandler<T> updateHandler, ConfigurationLoaderService projectService) {
		Optional<T> extensionConfiguration = projectService.getConfiguration(updateHandler.getConfigurationClass(), updateHandler.getFileName());
		if (extensionConfiguration.isPresent()) {
			return updateHandler.updateConfiguration(extensionConfiguration.get(), project);
		} else {
			LOG.info("No configuration for " + updateHandler.getUpdaterName() + " found.");
			return Collections.emptyList();
		}
	}
}
