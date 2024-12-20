package de.gebit.plugins.autoconfig;


import com.intellij.openapi.module.Module;

import java.util.List;

/**
 * Extension point for used to supply module configuration update handlers.
 */
public interface UpdateModuleHandler<T> extends UpdateSettings<T> {

	/**
	 * Implementing handlers should carefully select the modules in which to apply any settings changes.
	 *
	 * @param configuration the configuration object used for this update handler
	 * @param module        module to accept or deny
	 * @return whether the given module should be accepted for this module handler, default is {@code false} (no changes
	 * will be applied)
	 */
	default boolean acceptModule(T configuration, Module module) {
		return false;
	}

	/**
	 * The implementation of the configuration updates. A configuration update object is supplied containing the
	 * information gathered from the yaml file.
	 *
	 * @param configuration the configuration object used for this update handler
	 * @param module        the module that will receive the configuration updates in case they can be applied
	 * @return list of configuration parts that have been updated
	 */
	List<String> updateConfiguration(T configuration, Module module);

	@Override
	default UpdateTarget getUpdateTarget() {
		return UpdateTarget.MODULE;
	}

	default boolean matchesAnyName(Module module, List<String> patterns) {
		return patterns.stream().anyMatch(p -> module.getName().matches(p));
	}
}
