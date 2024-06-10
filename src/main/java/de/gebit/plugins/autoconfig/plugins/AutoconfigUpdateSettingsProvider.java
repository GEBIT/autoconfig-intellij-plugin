package de.gebit.plugins.autoconfig.plugins;

import com.intellij.ide.impl.ProjectUtilCore;
import com.intellij.ide.impl.TrustedProjects;
import com.intellij.openapi.updateSettings.impl.UpdateSettingsProvider;
import de.gebit.plugins.autoconfig.ConfigurationLoaderService;
import de.gebit.plugins.autoconfig.handlers.CommonConfigurationHandler;
import de.gebit.plugins.autoconfig.model.GeneralConfiguration;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;


/**
 * "Safer" Plugin-Repository Provider which does not persist the configured plugin repository. This provider is used when checking for required
 * plugins and when checking for plugin updates.
 */
public class AutoconfigUpdateSettingsProvider implements UpdateSettingsProvider {

	@SuppressWarnings("UnstableApiUsage")
	@NotNull
	@Override
	public List<String> getPluginRepositories() {
		List<String> pluginRepositories = new ArrayList<>();
		Arrays.stream(ProjectUtilCore.getOpenProjects()).filter(TrustedProjects::isTrusted).forEach(project -> {
			ConfigurationLoaderService projectService = project.getService(ConfigurationLoaderService.class);
			final Optional<GeneralConfiguration> configuration = projectService.getConfiguration(CommonConfigurationHandler.CONFIGURATION_CLASS, CommonConfigurationHandler.CONFIG_FILE_NAME);
			configuration.map(GeneralConfiguration::getPluginRepositories).ifPresent(pluginRepositories::addAll);
		});
		return pluginRepositories;
	}
}
