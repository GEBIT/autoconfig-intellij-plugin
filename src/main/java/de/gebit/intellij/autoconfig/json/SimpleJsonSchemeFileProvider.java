//
//  SimpleJsonSchemeFileProvider.java
//
//  Copyright (C) 2024
//  GEBIT Solutions GmbH,
//  Berlin, Duesseldorf, Stuttgart, Leipzig (Germany)
//  All rights reserved.

package de.gebit.intellij.autoconfig.json;

import com.intellij.openapi.vfs.VirtualFile;
import com.jetbrains.jsonSchema.extension.JsonSchemaFileProvider;
import com.jetbrains.jsonSchema.extension.JsonSchemaProviderFactory;
import com.jetbrains.jsonSchema.extension.SchemaType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A schema file provider used to handle mapping of json file to json schema.s
 */
class SimpleJsonSchemeFileProvider implements JsonSchemaFileProvider {
	private final String name;
	private final String fileName;
	private final String schemaFile;
	private final Class<?> resourceSourceClass;

	SimpleJsonSchemeFileProvider(String aName, String aFileName, String aSchemaFile, Class<?> resourceSourceClass) {
		name = aName;
		fileName = aFileName;
		schemaFile = aSchemaFile;
		this.resourceSourceClass = resourceSourceClass;
	}

	public boolean isAvailable(@NotNull VirtualFile file) {
		return fileName.equals(file.getName());
	}

	@NotNull
	@Override
	public String getName() {
		return name;
	}

	@Nullable
	@Override
	public VirtualFile getSchemaFile() {
		return JsonSchemaProviderFactory.getResourceFile(resourceSourceClass, schemaFile);
	}

	@NotNull
	@Override
	public SchemaType getSchemaType() {
		return SchemaType.embeddedSchema;
	}
}
