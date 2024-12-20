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
public interface UpdateHandler<T> extends de.gebit.plugins.autoconfig.UpdateSettings<T> {

	/**
	 * The implementation of the configuration updates. A configuration update object is supplied containing the
	 * information gathered from the yaml file.
	 *
	 * @param configuration the configuration object used for this update handler
	 * @param project       the project that will receive the configuration updates in case they can be applied
	 * @return list of configuration parts that have been updated
	 */
	List<String> updateConfiguration(T configuration, Project project);

	@Override
	default UpdateTarget getUpdateTarget() {
		return UpdateTarget.PROJECT;
	}
}
