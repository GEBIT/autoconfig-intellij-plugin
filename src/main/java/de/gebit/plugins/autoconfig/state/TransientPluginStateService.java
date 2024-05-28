package de.gebit.plugins.autoconfig.state;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.FileTypeRegistry;
import com.intellij.openapi.fileTypes.UnknownFileType;
import com.intellij.openapi.project.Project;
import de.gebit.plugins.autoconfig.FormatOnSaveOptionsDefaultsProvider;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * The application wide settings for the autoconfig plugin. They are necessary because the {@link FormatOnSaveOptionsDefaultsProvider} does not have
 * access to the {@link Project} and can therefore not read the configured values.
 */
@Getter
@Service(Service.Level.APP)
public final class TransientPluginStateService {
	/**
	 * Transient state of plugin.
	 */
	private TransientPluginState pluginState;

	public static TransientPluginStateService getInstance() {
		return ApplicationManager.getApplication().getService(TransientPluginStateService.class);
	}

	public void initFormatterSettings(List<String> formatFileTypes, List<String> organizeImportFileTypes) {
		pluginState = new TransientPluginState(getFileTypes(formatFileTypes), getFileTypes(organizeImportFileTypes));
	}

	@NotNull
	private static List<FileType> getFileTypes(List<String> formatFileTypes) {
		List<FileType> fileTypes = new ArrayList<>();
		for (String type : formatFileTypes) {
			FileType fileTypeByExtension = FileTypeRegistry.getInstance().getFileTypeByExtension(type);
			if (!(fileTypeByExtension instanceof UnknownFileType)) {
				fileTypes.add(fileTypeByExtension);
			}
		}
		return fileTypes;
	}
}
