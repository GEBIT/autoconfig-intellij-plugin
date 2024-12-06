//
//  ConfigurationLoaderService.java
//
//  Copyright (C) 2024
//  GEBIT Solutions GmbH,
//  Berlin, Duesseldorf, Stuttgart, Leipzig (Germany)
//  All rights reserved.

package de.gebit.plugins.autoconfig.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import de.gebit.plugins.autoconfig.util.Notifications;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.util.Optional;

/**
 * Service used to load yaml files from project directory.
 */
@RequiredArgsConstructor
@Service(Service.Level.PROJECT)
public final class ConfigurationLoaderService {

	private final Project project;

	public <T> Optional<T> getConfiguration(Class<T> objectClass, String configFileName, VirtualFile autoconfigDirectory) {
		T configObject = null;
		var configYaml = autoconfigDirectory.findChild(configFileName);
		if (configYaml == null) {
			return Optional.empty();
		}
		try {
			configObject = ReadAction.compute(() -> {
				final ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());
				return objectMapper.readValue(configYaml.getInputStream(), objectClass);
			});
		} catch (IOException e) {
			Notifications.showInfo("Unable to parse configuration yaml: " + configFileName, project);
		}
		return Optional.ofNullable(configObject);
	}
}
