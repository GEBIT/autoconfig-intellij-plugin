//
//  RPMcoConfigurationJsonSchemeProviderFactory.java
//
//  Copyright (C) 2024
//  GEBIT Solutions GmbH,
//  Berlin, Duesseldorf, Stuttgart, Leipzig (Germany)
//  All rights reserved.

package de.gebit.plugins.autoconfig.json;

import com.intellij.openapi.project.Project;
import com.jetbrains.jsonSchema.extension.JsonSchemaFileProvider;
import com.jetbrains.jsonSchema.extension.JsonSchemaProviderFactory;
import de.gebit.plugins.autoconfig.UpdateHandler;
import de.gebit.plugins.autoconfig.UpdateModuleHandler;
import de.gebit.plugins.autoconfig.UpdateSettings;
import de.gebit.plugins.autoconfig.service.ConfigurationUpdaterService;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is invoked, when the configuration plugin starts up and will provide a json schema for YML and json
 * files.
 */
public class ConfigurationJsonSchemeProviderFactory implements JsonSchemaProviderFactory {

	@NotNull
	@Override
	public List<JsonSchemaFileProvider> getProviders(@NotNull Project project) {
		List<JsonSchemaFileProvider> schemaFileProviders = new ArrayList<>();
		for (UpdateHandler<?> updateHandler : ConfigurationUpdaterService.PROJECT_EP_NAME.getExtensionList()) {
			schemaFileProviders.add(toSchemeFileProvider(updateHandler));
		}
		for (UpdateModuleHandler<?> updateHandler : ConfigurationUpdaterService.MODULE_EP_NAME.getExtensionList()) {
			schemaFileProviders.add(toSchemeFileProvider(updateHandler));
		}
		return schemaFileProviders;
	}

	private static <T> SimpleJsonSchemeFileProvider toSchemeFileProvider(UpdateSettings<T> updateSettings) {
		return new SimpleJsonSchemeFileProvider(updateSettings.getUpdaterName(), updateSettings.getFileName(),
				updateSettings.getJsonSchema(), updateSettings.getConfigurationClass());
	}
}
