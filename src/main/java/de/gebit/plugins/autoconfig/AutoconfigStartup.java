//
//  AutoconfigStartup.java
//
//  Copyright (C) 2024
//  GEBIT Solutions GmbH,
//  Berlin, Duesseldorf, Stuttgart, Leipzig (Germany)
//  All rights reserved.

package de.gebit.plugins.autoconfig;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.ProjectActivity;
import de.gebit.plugins.autoconfig.service.ConfigurationUpdaterService;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Entry point for the opening of a project. The yaml configuration file is read here, the resulting configuration
 * options object is passed to the CommonConfigurationHandler. At the end, a message is composed, displaying a list of
 * all updated configuration options.
 */
public class AutoconfigStartup implements ProjectActivity {

	@Nullable
	@Override
	public Object execute(@NotNull Project project, @NotNull Continuation<? super Unit> continuation) {
		return project.getService(ConfigurationUpdaterService.class).runAutoconfig();
	}
}
