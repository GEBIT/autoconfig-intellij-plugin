//
//  CommonConfigurationHandler.java
//
//  Copyright (C) 2024
//  GEBIT Solutions GmbH,
//  Berlin, Duesseldorf, Stuttgart, Leipzig (Germany)
//  All rights reserved.

package de.gebit.plugins.autoconfig.handlers;

import com.intellij.codeInsight.actions.onSave.FormatOnSaveOptions;
import com.intellij.codeInsight.actions.onSave.FormatOnSaveOptionsBase;
import com.intellij.codeInsight.actions.onSave.OptimizeImportsOnSaveOptions;
import com.intellij.externalDependencies.ExternalDependenciesManager;
import com.intellij.externalDependencies.impl.ExternalDependenciesManagerImpl;
import com.intellij.openapi.externalSystem.autoimport.ExternalSystemProjectTrackerSettings;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.updateSettings.impl.UpdateSettings;
import com.intellij.openapi.vcs.IssueNavigationConfiguration;
import com.intellij.openapi.vcs.IssueNavigationLink;
import de.gebit.plugins.autoconfig.UpdateHandler;
import de.gebit.plugins.autoconfig.model.*;
import de.gebit.plugins.autoconfig.state.TransientPluginStateService;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The main class used to update the IntelliJ configuration. All supported options are checked, updated and logged in a list of changed configs.
 */
public class CommonConfigurationHandler extends AbstractHandler implements UpdateHandler<GeneralConfiguration> {
	private static final @NonNls String CONFIG_SCHEMA_JSON = "/schema/config.schema.json";

	public static final @NonNls String CONFIG_FILE_NAME = "autoconfig.yaml";

	public static final @NonNls Class<GeneralConfiguration> CONFIGURATION_CLASS = GeneralConfiguration.class;

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
		return "Common configuration updater";
	}

	@Override
	public Class<GeneralConfiguration> getConfigurationClass() {
		return CONFIGURATION_CLASS;
	}

	@Override
	public List<String> updateConfiguration(GeneralConfiguration options, Project project) {
		List<String> updatedConfigs = new ArrayList<>();
		applyIssueNavigationConfiguration(options.getIssueNavigation(), project, updatedConfigs);
		applyPluginHosts(options.getGlobalPluginRepositories(), project, updatedConfigs);
		applyOnSaveOptions(options.getOnSave(), project, updatedConfigs);
		applyReloadConfiguration(options.getReloadProjectAutomatically(), project, updatedConfigs);
		return updatedConfigs;
	}

	private void applyReloadConfiguration(ReloadProjectAutomatically reloadProjectAutomatically, Project project, List<String> updatedConfigs) {
		if (reloadProjectAutomatically != null) {
			final ExternalSystemProjectTrackerSettings instance = ExternalSystemProjectTrackerSettings.getInstance(project);
			if (Boolean.FALSE.equals(reloadProjectAutomatically.getEnabled())) {
				applySetting(ExternalSystemProjectTrackerSettings.AutoReloadType.NONE, instance.getAutoReloadType(), instance::setAutoReloadType, updatedConfigs, "Automatic project reload deactivated");
				instance.setAutoReloadType(ExternalSystemProjectTrackerSettings.AutoReloadType.NONE);
			} else if (reloadProjectAutomatically.getMode() != null) {
				applySetting(switch (reloadProjectAutomatically.getMode()) {
					case ANY_CHANGES -> ExternalSystemProjectTrackerSettings.AutoReloadType.ALL;
					case EXTERNAL_CHANGES -> ExternalSystemProjectTrackerSettings.AutoReloadType.SELECTIVE;
				}, instance.getAutoReloadType(), instance::setAutoReloadType, updatedConfigs, "Automatic project reload activated");
			}
		}
	}

	private void applyIssueNavigationConfiguration(List<IssueNavigation> issueNavigationConfig, Project aProject, List<String> updatedConfigs) {
		if (issueNavigationConfig != null && !issueNavigationConfig.isEmpty()) {
			var issueNavigationSettings = IssueNavigationConfiguration.getInstance(aProject);
			var newSettingsList = new ArrayList<IssueNavigationLink>();

			for (IssueNavigation navigationConfig : issueNavigationConfig) {
				newSettingsList.add(new IssueNavigationLink(navigationConfig.getExpression(), navigationConfig.getUrl()));
			}
			applySetting(newSettingsList, issueNavigationSettings.getLinks(), issueNavigationSettings::setLinks, updatedConfigs, "Issue navigation links");
		}
	}

	private void applyOnSaveOptions(OnSave options, Project project, List<String> updatedConfigs) {
		if (options != null) {
			// first, initialise the application state service used for code formatter options
			Formatting optionsFormat = options.getFormat();
			Formatting optionsOptimizeImports = options.getOptimizeImports();
			TransientPluginStateService.getInstance().initFormatterSettings(getFileTypes(optionsFormat), getFileTypes(optionsOptimizeImports));

			setFormattingOptions(updatedConfigs, optionsFormat, "Code format on save", FormatOnSaveOptions.getInstance(project));
			setFormattingOptions(updatedConfigs, optionsOptimizeImports, "Optimize imports on save", OptimizeImportsOnSaveOptions.getInstance(project));
		}
	}

	private void setFormattingOptions(List<String> updatedConfigs, Formatting optionsFormat, String description, FormatOnSaveOptionsBase<?> format) {
		applySetting(optionsFormat != null, format.isRunOnSaveEnabled(), format::setRunOnSaveEnabled, updatedConfigs, description);
	}

	private List<String> getFileTypes(@Nullable Formatting formattingOptions) {
		if (formattingOptions == null) {
			return Collections.emptyList();
		}
		return formattingOptions.getTypes();
	}

	private void applyPluginHosts(List<String> pluginHosts, Project aProject, List<String> changedConfigs) {
		// Maybe ask the user before applying new Plugin-Hosts? This could be a possible attack vector otherwise.
		if (pluginHosts != null) {
			var updateSettings = UpdateSettings.getInstance();
			var storedPluginHosts = updateSettings.getStoredPluginHosts();
			var newHostAdded = false;
			for (String pluginHost : pluginHosts) {
				if (!storedPluginHosts.contains(pluginHost)) {
					storedPluginHosts.add(pluginHost);
					newHostAdded = true;
				}
			}
			if (newHostAdded) {
				changedConfigs.add("Plugin repositories");
			}
			if (newHostAdded) {
				var dependenciesManager = ExternalDependenciesManager.getInstance(aProject);
				var dependenciesManagerImpl = (ExternalDependenciesManagerImpl) dependenciesManager;
				// very sketchy but works for now. rechecks for required plugins.
				var state = dependenciesManagerImpl.getState();
				dependenciesManagerImpl.loadState(new ExternalDependenciesManagerImpl.ExternalDependenciesState());
				dependenciesManagerImpl.loadState(state);
			}
		}
	}
}
