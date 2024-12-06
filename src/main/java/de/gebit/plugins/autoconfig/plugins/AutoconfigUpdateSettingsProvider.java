package de.gebit.plugins.autoconfig.plugins;

import com.intellij.ide.impl.ProjectUtilCore;
import com.intellij.ide.impl.TrustedProjects;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.updateSettings.impl.UpdateSettingsProvider;
import com.intellij.openapi.vfs.VirtualFile;
import de.gebit.plugins.autoconfig.handlers.common.CommonConfigurationHandler;
import de.gebit.plugins.autoconfig.model.GeneralConfiguration;
import de.gebit.plugins.autoconfig.service.ConfigurationDirectoryService;
import de.gebit.plugins.autoconfig.service.ConfigurationLoaderService;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;


/**
 * "Safer" Plugin-Repository Provider which does not persist the configured plugin repository. This provider is used
 * when checking for required plugins and when checking for plugin updates.
 */
public class AutoconfigUpdateSettingsProvider implements UpdateSettingsProvider {

	@SuppressWarnings("UnstableApiUsage")
	@NotNull
	@Override
	public List<String> getPluginRepositories() {
		List<String> pluginRepositories = new ArrayList<>();
		Arrays.stream(ProjectUtilCore.getOpenProjects()).filter(TrustedProjects::isTrusted).forEach(project -> {
			ConfigurationDirectoryService configDirectoryService = project.getService(
					ConfigurationDirectoryService.class);
			pluginRepositories.addAll(configDirectoryService.getProjectAutoconfigDirectory()
					.map(dir -> setupPluginRepositories(project, dir))
					.orElse(Collections.emptyList()));
		});
		return pluginRepositories;
	}

	private List<String> setupPluginRepositories(Project project, VirtualFile configDirectory) {
		ConfigurationLoaderService projectService = project.getService(ConfigurationLoaderService.class);
		final Optional<GeneralConfiguration> configuration = projectService.getConfiguration(
				CommonConfigurationHandler.CONFIGURATION_CLASS, CommonConfigurationHandler.CONFIG_FILE_NAME,
				configDirectory);
		return configuration.map(GeneralConfiguration::getPluginRepositories).orElse(Collections.emptyList());
	}
}
