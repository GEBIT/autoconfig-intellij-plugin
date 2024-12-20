package de.gebit.plugins.autoconfig.service;


import com.intellij.ide.impl.TrustedProjects;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.extensions.ExtensionPointName;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import de.gebit.plugins.autoconfig.AutoconfigStartup;
import de.gebit.plugins.autoconfig.UpdateHandler;
import de.gebit.plugins.autoconfig.UpdateModuleHandler;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static de.gebit.plugins.autoconfig.util.Notifications.showInfo;

/**
 * Service used to update project/module configurations.
 */
@RequiredArgsConstructor
@Service(Service.Level.PROJECT)
public final class ConfigurationUpdaterService {
	private static final com.intellij.openapi.diagnostic.Logger LOG = Logger.getInstance(AutoconfigStartup.class);

	public static final ExtensionPointName<UpdateHandler<?>> PROJECT_EP_NAME = ExtensionPointName.create(
			"de.gebit.plugins.autoconfig.configurationUpdater");

	public static final ExtensionPointName<UpdateModuleHandler<?>> MODULE_EP_NAME = ExtensionPointName.create(
			"de.gebit.plugins.autoconfig.moduleConfigurationUpdater");

	private final Project project;

	@SuppressWarnings("UnstableApiUsage")
	public @Nullable List<String> runAutoconfig() {
		ConfigurationLoaderService projectService = project.getService(ConfigurationLoaderService.class);
		if (projectService == null) {
			return null;
		}

		if (!TrustedProjects.isTrusted(project)) {
			showInfo("Project configuration has not been updated, because project was opened in safe mode.", project);
			return null;
		}

		List<String> changedConfigs = new ArrayList<>();

		ConfigurationDirectoryService configurationDirectoryService = project.getService(
				ConfigurationDirectoryService.class);
		configurationDirectoryService.getProjectAutoconfigDirectory().ifPresent(dir -> {
			for (UpdateHandler<?> updateHandler : PROJECT_EP_NAME.getExtensionList()) {
				changedConfigs.addAll(processUpdateHandler(project, updateHandler, projectService, dir));
			}
		});

		List<ModuleDirectory> moduleConfigs = new ArrayList<>();
		List<Module> modules = new ArrayList<>();
		for (Module module : ModuleManager.getInstance(project).getModules()) {
			Optional<VirtualFile> moduleAutoconfigDirectory = configurationDirectoryService.getModuleAutoconfigDirectory(
					module);
			moduleAutoconfigDirectory.ifPresent(d -> moduleConfigs.add(new ModuleDirectory(module, d)));
			modules.add(module);
		}


		for (UpdateModuleHandler<?> updateHandler : MODULE_EP_NAME.getExtensionList()) {
			changedConfigs.addAll(processModuleUpdateHandler(moduleConfigs, modules, updateHandler, projectService));
		}

		if (!changedConfigs.isEmpty()) {
			String notification = String.join(", ", changedConfigs);
			showInfo("New project configurations applied: " + notification, project);
		}

		return changedConfigs;
	}

	private <T> List<String> processModuleUpdateHandler(@NotNull List<ModuleDirectory> moduleConfigs, List<Module> modules, UpdateModuleHandler<T> updateHandler, ConfigurationLoaderService projectService) {
		Set<String> modulesUpdates = new HashSet<>();
		for (ModuleDirectory moduleDirectory : moduleConfigs) {
			Optional<T> extensionConfiguration = projectService.getConfiguration(updateHandler.getConfigurationClass(),
					updateHandler.getFileName(), moduleDirectory.directory());
			extensionConfiguration.ifPresent(config -> modules.stream()
					.filter(module -> updateHandler.acceptModule(config, module))
					.forEach(module -> modulesUpdates.addAll(updateHandler.updateConfiguration(config, module))));
		}

		return modulesUpdates.stream().toList();
	}

	private <T> List<String> processUpdateHandler(@NotNull Project project, UpdateHandler<T> updateHandler, ConfigurationLoaderService projectService, VirtualFile configDirectory) {
		Optional<T> extensionConfiguration = projectService.getConfiguration(updateHandler.getConfigurationClass(),
				updateHandler.getFileName(), configDirectory);
		return extensionConfiguration.map(c -> updateHandler.updateConfiguration(c, project)).orElseGet(() -> {
			LOG.info("No configuration for " + updateHandler.getUpdaterName() + " found.");
			return Collections.emptyList();
		});
	}

	private record ModuleDirectory(Module module, VirtualFile directory) {

	}
}
