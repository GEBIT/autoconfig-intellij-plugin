package de.gebit.plugins.autoconfig;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.fileTypes.LanguageFileType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.yaml.YAMLLanguage;

import javax.swing.*;

/**
 * {@link LanguageFileType} for Autoconfig Files
 */
public class AutoconfigFileType extends LanguageFileType {
	@SuppressWarnings("unused")
	public static final AutoconfigFileType INSTANCE = new AutoconfigFileType();

	protected AutoconfigFileType() {
		super(YAMLLanguage.INSTANCE);
	}

	@Override
	public @NotNull String getName() {
		return "Autoconfig File";
	}

	@Override
	public @NotNull String getDescription() {
		return "Autoconfig Configuration file";
	}

	@Override
	public @NotNull String getDefaultExtension() {
		return "yaml";
	}

	@Override
	public Icon getIcon() {
		return AllIcons.FileTypes.Config;
	}
}
