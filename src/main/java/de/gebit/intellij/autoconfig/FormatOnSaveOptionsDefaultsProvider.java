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
import com.intellij.openapi.fileTypes.FileTypeRegistry;
import com.intellij.openapi.fileTypes.UnknownFileType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Supplier for default file formats that should be formatted or used for optimization of imports.
 */
public class FormatOnSaveOptionsDefaultsProvider implements FormatOnSaveOptionsBase.DefaultsProvider {

	/**
	 * List of file types used as defaults.
	 */
	private static Collection<FileType> fileTypes;

	@Override
	public @NotNull Collection<@NotNull FileType> getFileTypesFormattedOnSaveByDefault() {
		return getFileTypes();
	}

	@Override
	public @NotNull Collection<@NotNull FileType> getFileTypesWithOptimizeImportsOnSaveByDefault() {
		return getFileTypes();
	}

	@NotNull
	private static Collection<FileType> getFileTypes() {
		if (fileTypes != null) {
			return fileTypes;
		}

		fileTypes = new ArrayList<>();

		for (String type : new String[]{"java", "dart", "groovy", "kt"}) {
			FileType fileTypeByExtension = FileTypeRegistry.getInstance().getFileTypeByExtension(type);
			if (!(fileTypeByExtension instanceof UnknownFileType)) {
				fileTypes.add(fileTypeByExtension);
			}
		}
		return fileTypes;
	}
}
