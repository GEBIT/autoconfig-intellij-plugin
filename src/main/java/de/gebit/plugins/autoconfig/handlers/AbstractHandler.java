//
//  AbstractHandler.java
//
//  Copyright (C) 2024
//  GEBIT Solutions GmbH,
//  Berlin, Duesseldorf, Stuttgart, Leipzig (Germany)
//  All rights reserved.

package de.gebit.plugins.autoconfig.handlers;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;

/**
 * Base handler for configuration updater classes.
 */
public abstract class AbstractHandler {

	protected <T> void applySetting(T newValue, T originalValue, Consumer<T> setter, List<String> changedConfigs, String description) {
		applySetting(newValue, originalValue, setter, Object::equals, changedConfigs, description);
	}

	protected <T> void applySetting(T newValue, T originalValue, Consumer<T> setter, BiFunction<T, T, Boolean> equalityComparison, List<String> changedConfigs, String description) {
		if (newValue != null && !newValue.equals("") && !equalityComparison.apply(newValue, originalValue)) {
			setter.accept(newValue);
			if (originalValue != null) {
				// originalValue == null means always setting the newValue
				changedConfigs.add(description);
			}
		}
	}
}
