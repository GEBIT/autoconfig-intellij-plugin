//
//  FormatOnSaveOptionsDefaultsProvider.java
//
//  Copyright (C) 2024
//  GEBIT Solutions GmbH,
//  Berlin, Duesseldorf, Stuttgart, Leipzig (Germany)
//  All rights reserved.

package de.gebit.intellij.autoconfig;

import com.intellij.codeInsight.actions.onSave.FormatOnSaveOptionsBase;
import com.intellij.openapi.fileTypes.FileType;
import de.gebit.intellij.autoconfig.state.TransientPluginState;
import de.gebit.intellij.autoconfig.state.TransientPluginStateService;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;
import java.util.function.Function;

/**
 * Supplier for default file formats that should be formatted or used for optimization of imports.
 */
public class FormatOnSaveOptionsDefaultsProvider implements FormatOnSaveOptionsBase.DefaultsProvider {

	@Override
	public @NotNull Collection<@NotNull FileType> getFileTypesFormattedOnSaveByDefault() {
		return getFileTypes(TransientPluginState::formatFileTypes);
	}

	@Override
	public @NotNull Collection<@NotNull FileType> getFileTypesWithOptimizeImportsOnSaveByDefault() {
		return getFileTypes(TransientPluginState::organizeImportFileTypes);
	}

	@NotNull
	private static Collection<FileType> getFileTypes(Function<TransientPluginState, Collection<FileType>> getterMethod) {
		TransientPluginState pluginState = TransientPluginStateService.getInstance().getPluginState();
		if (pluginState == null) {
			return Collections.emptyList();
		}

		return getterMethod.apply(pluginState);
	}
}
