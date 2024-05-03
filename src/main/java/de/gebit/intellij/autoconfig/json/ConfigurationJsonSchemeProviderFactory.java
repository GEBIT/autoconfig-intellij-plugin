//
//  RPMcoConfigurationJsonSchemeProviderFactory.java
//
//  Copyright (C) 2024
//  GEBIT Solutions GmbH,
//  Berlin, Duesseldorf, Stuttgart, Leipzig (Germany)
//  All rights reserved.

package de.gebit.intellij.autoconfig.json;

import com.intellij.openapi.project.Project;
import com.jetbrains.jsonSchema.extension.JsonSchemaFileProvider;
import com.jetbrains.jsonSchema.extension.JsonSchemaProviderFactory;
import de.gebit.intellij.autoconfig.AutoconfigStartup;
import de.gebit.intellij.autoconfig.UpdateHandler;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is invoked, when the configuration plugin starts up and will provide a json schema for YML and json files.
 */
public class ConfigurationJsonSchemeProviderFactory implements JsonSchemaProviderFactory {

	@NotNull
	@Override
	public List<JsonSchemaFileProvider> getProviders(@NotNull Project project) {
		List<JsonSchemaFileProvider> schemaFileProviders = new ArrayList<>();
		for (UpdateHandler<?> updateHandler : AutoconfigStartup.EP_NAME.getExtensionList()) {
			schemaFileProviders.add(new SimpleJsonSchemeFileProvider(updateHandler.getUpdaterName(), updateHandler.getFileName(), updateHandler.getJsonSchema(), updateHandler.getConfigurationClass()));
		}
		return schemaFileProviders;
	}
}
