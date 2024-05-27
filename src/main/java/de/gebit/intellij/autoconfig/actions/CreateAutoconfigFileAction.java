package de.gebit.intellij.autoconfig.actions;

import com.intellij.ide.IdeView;
import com.intellij.ide.actions.CreateInDirectoryActionBase;
import com.intellij.ide.actions.OpenFileAction;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import de.gebit.intellij.autoconfig.UpdateHandler;
import de.gebit.intellij.autoconfig.create.CreateAutoconfigFileDialog;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Optional;

/**
 * An action to create/open an Autoconfig file for a specific handler/plugin
 */
public class CreateAutoconfigFileAction extends CreateInDirectoryActionBase {
	private static final @NotNull Logger LOGGER = Logger.getInstance(CreateAutoconfigFileAction.class);

	@Override
	public void actionPerformed(@NotNull AnActionEvent e) {
		Project project = e.getProject();
		if (project != null) {
			CreateAutoconfigFileDialog autoconfigFileDialog = new CreateAutoconfigFileDialog();
			if (!autoconfigFileDialog.showAndGet()) {
				// the dialog has been canceled
				return;
			}
			// the update handler selected in the dialogs form
			Optional<UpdateHandler<?>> optionalUpdateHandler = autoconfigFileDialog.getSelectedHandler();
			if (optionalUpdateHandler.isEmpty()) {
				return;
			}
			UpdateHandler<?> updateHandler = optionalUpdateHandler.get();
			VirtualFile projectFile = project.getProjectFile();
			if (projectFile == null) {
				LOGGER.warn("No project file? This is unusual. We can't automatically create a new Autoconfig file then. Please create it yourself.");
				return;
			}
			try {
				VirtualFile updateHandlerFile = WriteAction.compute(() -> {
					VirtualFile autoconfigDirectory = projectFile.getParent().findChild("autoconfig");
					if (autoconfigDirectory == null) {
						// no ".idea/autoconfig" directory found. We're going to create it.
						autoconfigDirectory = projectFile.getParent().createChildDirectory(this, "autoconfig");
					}
					return autoconfigDirectory.findOrCreateChildData(this, updateHandler.getFileName());
				});
				if (updateHandlerFile != null) {
					// we open the file
					OpenFileAction.openFile(updateHandlerFile, project);
					Document document = FileDocumentManager.getInstance().getDocument(updateHandlerFile);
					if (document != null) {
						PsiFile psiFile = PsiDocumentManager.getInstance(project).getPsiFile(document);
						IdeView ideView = e.getData(LangDataKeys.IDE_VIEW);
						if (psiFile != null && ideView != null) {
							// and then focus the project/file view on the file
							ideView.selectElement(psiFile);
						}
					}
					ActionManager actionManager = ActionManager.getInstance();
					AnAction codeCompletion = actionManager.getAction("CodeCompletion");
					// and then we invoke code-completion on the possibly newly created opened file.
					// now=false because otherwise it might get executed before other IDE actions are done,
					// which would result in it losing its focus right away
					actionManager.tryToExecute(codeCompletion, null, null, null, false);
				}
			} catch (IOException ex) {
				LOGGER.error("Couldn't create Autoconfig file!", ex);
			}
		}
	}
}
