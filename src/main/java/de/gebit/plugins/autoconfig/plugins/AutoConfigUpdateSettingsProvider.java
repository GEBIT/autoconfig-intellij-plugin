package de.gebit.plugins.autoconfig.plugins;

import com.intellij.openapi.updateSettings.impl.UpdateSettingsProvider;
import de.gebit.plugins.autoconfig.ConfigurationLoaderService;
import de.gebit.plugins.autoconfig.handlers.CommonConfigurationHandler;
import de.gebit.plugins.autoconfig.model.GeneralConfiguration;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * "Safer" Plugin-Repository Provider which does not persist the configured plugin repository. This provider is used when checking for required
 * plugins and when checking for plugin updates.
 */
public class AutoConfigUpdateSettingsProvider implements UpdateSettingsProvider {

	@NotNull
	@Override
	public List<String> getPluginRepositories() {
		List<String> pluginRepositories = new ArrayList<>();
		com.intellij.openapi.project.ProjectUtil.getOpenedProjects().iterator().forEachRemaining(project -> {
			ConfigurationLoaderService projectService = project.getService(ConfigurationLoaderService.class);
			final Optional<GeneralConfiguration> configuration = projectService.getConfiguration(CommonConfigurationHandler.CONFIGURATION_CLASS, CommonConfigurationHandler.CONFIG_FILE_NAME);
			configuration.map(GeneralConfiguration::getPluginRepositories).ifPresent(pluginRepositories::addAll);
		});
		return pluginRepositories;
	}
}
