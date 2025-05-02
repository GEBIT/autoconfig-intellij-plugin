package de.gebit.plugins.autoconfig.actions;

import com.intellij.ide.IdeView;
import com.intellij.ide.actions.OpenFileAction;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.IdeActions;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import de.gebit.plugins.autoconfig.UpdateSettings;
import de.gebit.plugins.autoconfig.UpdateTarget;
import de.gebit.plugins.autoconfig.create.CreateAutoconfigFileDialog;
import de.gebit.plugins.autoconfig.service.ConfigurationDirectoryService;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * An action to create/open an Autoconfig file for a specific handler/plugin
 */
public class CreateAutoconfigFileAction extends DumbAwareAction {
	private static final @NotNull Logger LOGGER = Logger.getInstance(CreateAutoconfigFileAction.class);

	@Override
	public void actionPerformed(@NotNull AnActionEvent e) {
		Project project = e.getProject();
		if (project != null) {
			Module[] modules = ModuleManager.getInstance(project).getModules();
			List<Module> moduleList = Arrays.stream(modules).toList();
			CreateAutoconfigFileDialog autoconfigFileDialog = new CreateAutoconfigFileDialog(moduleList);
			if (!autoconfigFileDialog.showAndGet()) {
				// the dialog has been canceled
				return;
			}
			// the update handler selected in the dialogs form
			Optional<UpdateSettings<?>> optionalUpdateSettings = autoconfigFileDialog.getSelectedSettings();
			if (optionalUpdateSettings.isEmpty()) {
				return;
			}

			UpdateSettings<?> updateSettings = optionalUpdateSettings.get();
			ConfigurationDirectoryService configurationDirectoryService = project.getService(
					ConfigurationDirectoryService.class);
			Optional<VirtualFile> updateSettingsFile;

			// try to find a file for either a project...
			if (updateSettings.getUpdateTarget().equals(UpdateTarget.PROJECT)) {
				updateSettingsFile = configurationDirectoryService.findOrCreateProjectAutoconfigFile(
						updateSettings.getFileName());
			} else {
				// ... or a module
				Optional<Module> selectedModule = autoconfigFileDialog.getSelectedModule();
				updateSettingsFile = selectedModule.flatMap(
						module -> configurationDirectoryService.findOrCreateModuleAutoconfigFile(module,
								updateSettings.getFileName()));
			}

			updateSettingsFile.ifPresentOrElse(file -> {
				// we open the file
				OpenFileAction.openFile(file, project);
				Document document = FileDocumentManager.getInstance().getDocument(file);
				if (document != null) {
					PsiFile psiFile = PsiDocumentManager.getInstance(project).getPsiFile(document);
					IdeView ideView = e.getData(LangDataKeys.IDE_VIEW);
					if (psiFile != null && ideView != null) {
						// and then focus the project/file view on the file
						ideView.selectElement(psiFile);
					}
				}
				ActionManager actionManager = ActionManager.getInstance();
				AnAction codeCompletion = actionManager.getAction(IdeActions.ACTION_CODE_COMPLETION);
				// and then we invoke code-completion on the possibly newly created opened file.
				// now=false because otherwise it might get executed before other IDE actions are done,
				// which would result in it losing its focus right away
				actionManager.tryToExecute(codeCompletion, null, null, null, false);
			}, () -> LOGGER.error("Unable to create autoconfig file: " + updateSettings.getFileName()));
		}
	}
}
