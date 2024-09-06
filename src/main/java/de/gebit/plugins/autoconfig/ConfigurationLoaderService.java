//
//  ConfigurationLoaderService.java
//
//  Copyright (C) 2024
//  GEBIT Solutions GmbH,
//  Berlin, Duesseldorf, Stuttgart, Leipzig (Germany)
//  All rights reserved.

package de.gebit.plugins.autoconfig;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.impl.ProjectImpl;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import de.gebit.plugins.autoconfig.util.Notifications;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Service used to load yaml files from project directory.
 */
@Service(Service.Level.PROJECT)
public final class ConfigurationLoaderService {
	/**
	 * Configuration directory containing yaml sources.
	 */
	public static final String AUTOCONFIG_DIRECTORY = "autoconfig";

	private final Project project;

	private final Map<String, Object> configurationOptions = new HashMap<>();

	ConfigurationLoaderService(Project project) {
		this.project = project;
	}

	public <T> Optional<T> getConfiguration(Class<T> objectClass, String configFileName) {
		T configObject = null;
		Object o = configurationOptions.get(objectClass.getCanonicalName());
		if (o != null && o.getClass().isAssignableFrom(objectClass)) {
			//noinspection unchecked
			configObject = (T) o;
		}
		if (configObject != null) {
			return Optional.of(configObject);
		}
		Optional<VirtualFile> autoconfigDirectory = getConfigDirectory();
		if (autoconfigDirectory.isEmpty()) {
			return Optional.empty();
		}
		var configYaml = autoconfigDirectory.get().findChild(configFileName);
		if (configYaml == null) {
			return Optional.empty();
		}
		try {
			configObject = ReadAction.compute(() -> {
				final ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());
				return objectMapper.readValue(configYaml.getInputStream(), objectClass);
			});
			configurationOptions.put(objectClass.getCanonicalName(), configObject);
		} catch (IOException e) {
			Notifications.showInfo("Unable to parse configuration yaml: " + configFileName, project);
		}
		return Optional.ofNullable(configObject);
	}

	public boolean hasAutoconfigDir() {
		return getConfigDirectory().isPresent();
	}

	@SuppressWarnings("UnstableApiUsage")
	private Optional<VirtualFile> getConfigDirectory() {
		var projectFile = project.getProjectFile();
		if (projectFile == null) {
			// Fallback to find autoconfig directory. May happen when project is first opened and no misc.xml can be found
			if (project instanceof ProjectImpl projectImpl) {
				Path directoryStorePath = projectImpl.getComponentStore().getDirectoryStorePath();
				if (directoryStorePath != null) {
					return Optional.ofNullable(VirtualFileManager.getInstance().findFileByNioPath(directoryStorePath))
							.map(m -> m.findChild(AUTOCONFIG_DIRECTORY));
				}
			}
			return Optional.empty();
		}
		return Optional.ofNullable(projectFile.getParent().findChild(AUTOCONFIG_DIRECTORY));
	}

	public void resetConfigurationCache() {
		configurationOptions.clear();
	}
}
