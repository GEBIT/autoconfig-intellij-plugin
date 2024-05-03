//
//  AbstractHandler.java
//
//  Copyright (C) 2024
//  GEBIT Solutions GmbH,
//  Berlin, Duesseldorf, Stuttgart, Leipzig (Germany)
//  All rights reserved.

package de.gebit.intellij.autoconfig.handlers;

import java.util.List;
import java.util.function.Consumer;

/**
 * Base handler for configuration updater classes.
 */
public abstract class AbstractHandler {

	protected <T> void applySetting(T newValue, T originalValue, Consumer<T> setter, List<String> changedConfigs, String description) {
		if (newValue != null && !newValue.equals("") && !newValue.equals(originalValue)) {
			setter.accept(newValue);
			if (originalValue != null) {
				// originalValue == null means always setting the newValue
				changedConfigs.add(description);
			}
		}
	}
}
