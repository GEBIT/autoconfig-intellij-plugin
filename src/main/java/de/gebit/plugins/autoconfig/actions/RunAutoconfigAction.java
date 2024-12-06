package de.gebit.plugins.autoconfig.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import de.gebit.plugins.autoconfig.service.ConfigurationUpdaterService;
import de.gebit.plugins.autoconfig.util.Notifications;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

/**
 * Action to apply the projects Autoconfig-Configurations manually.
 */
public class RunAutoconfigAction extends DumbAwareAction {
	@Override
	public void actionPerformed(@NotNull AnActionEvent e) {
		Project project = e.getProject();
		if (project == null) {
			return;
		}

		// save currently open and unsaved config files
		saveUnsavedAutoconfigs();

		// perform changes
		List<String> changedSettings = project.getService(ConfigurationUpdaterService.class).runAutoconfig();

		// notify user if no changes were necessary
		if (changedSettings == null || changedSettings.isEmpty()) {
			Notifications.showInfo("No settings have been changed.", project);
		}
	}

	private void saveUnsavedAutoconfigs() {
		FileDocumentManager fileDocumentManager = FileDocumentManager.getInstance();
		Arrays.stream(fileDocumentManager.getUnsavedDocuments())
				.filter(d -> isAutoconfigFile(fileDocumentManager.getFile(d)))
				.forEach(fileDocumentManager::saveDocument);
	}

	private boolean isAutoconfigFile(@Nullable VirtualFile document) {
		if (document == null) {
			return false;
		}
		String[] components = document.getUrl().split("/");
		String lastComponent = components[components.length - 1];
		return lastComponent.startsWith("autoconfig") && lastComponent.endsWith(".yaml");
	}
}
