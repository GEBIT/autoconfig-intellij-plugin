package de.gebit.plugins.autoconfig.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import de.gebit.plugins.autoconfig.AutoconfigStartup;
import de.gebit.plugins.autoconfig.ConfigurationLoaderService;
import org.jetbrains.annotations.NotNull;

/**
 * Action to apply the projects Autoconfig-Configurations manually
 */
public class RunAutoconfigAction extends DumbAwareAction {
	@Override
	public void actionPerformed(@NotNull AnActionEvent e) {
		Project project = e.getProject();
		if (project == null) {
			return;
		}

		ConfigurationLoaderService configurationLoaderService = project.getService(ConfigurationLoaderService.class);
		if (configurationLoaderService != null) {
			configurationLoaderService.resetConfigurationCache();
		}

		new AutoconfigStartup().runAutoconfig(project);
	}
}
