//
//  UpdateHandler.java
//
//  Copyright (C) 2024
//  GEBIT Solutions GmbH,
//  Berlin, Duesseldorf, Stuttgart, Leipzig (Germany)
//  All rights reserved.

package de.gebit.plugins.autoconfig;

import com.intellij.openapi.project.Project;

import java.util.List;

/**
 * Extension point for used to supply configuration update handlers.
 */
public interface UpdateHandler<T> {

	/**
	 * Provide the file name that is expected to contain the extensions' configuration.
	 *
	 * @return the file name that is expected to contain the extensions' configuration
	 */
	String getFileName();

	/**
	 * The json schema containing/providing the information on how to write the configuration file.
	 *
	 * @return json schema containing/providing the information on how to write the configuration file
	 */
	String getJsonSchema();

	/**
	 * The updater name is used for logging purposes and as a name for the json schema displayed in IntelliJ status bar.
	 *
	 * @return the name of this updater
	 */
	String getUpdaterName();

	/**
	 * The configuration object class used to read/deserialize the configuration file.
	 *
	 * @return configuration object class used to read/deserialize the configuration file
	 */
	Class<T> getConfigurationClass();

	/**
	 * The implementation of the configuration updates. A configuration update object is supplied containing the information gathered from the yaml
	 * file.
	 *
	 * @param configuration the configuration object used for this update handler
	 * @param project       the project that will receive the configuration updates in case they can be applied
	 * @return list of configuration parts that have been updated
	 */
	List<String> updateConfiguration(T configuration, Project project);
}
