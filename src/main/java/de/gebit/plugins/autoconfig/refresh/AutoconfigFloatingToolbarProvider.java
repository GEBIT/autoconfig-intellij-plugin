package de.gebit.plugins.autoconfig.refresh;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.editor.toolbar.floating.AbstractFloatingToolbarProvider;
import com.intellij.openapi.editor.toolbar.floating.FloatingToolbarComponent;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.ApplicationKt;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Floating toolbar provider that shows a "Reload Autoconfig" toolbar button whenever an autoconfig file has unsaved
 * changes in the editor.
 */
public class AutoconfigFloatingToolbarProvider extends AbstractFloatingToolbarProvider implements Disposable {

	private List<FloatingToolbarComponent> fileToolbarComponents = new ArrayList<>();

	public AutoconfigFloatingToolbarProvider() {
		super("Autoconfig.FloatingToolbar");
	}

	@Override
	public boolean getAutoHideable() {
		return false;
	}

	@Override
	public void register(@NotNull DataContext dataContext, @NotNull FloatingToolbarComponent component, @NotNull Disposable parentDisposable) {
		super.register(dataContext, component, parentDisposable);

		FileEditor fileEditor = dataContext.getData(PlatformDataKeys.FILE_EDITOR);
		if (fileEditor == null || fileEditor.getFile() == null) {
			return;
		}

		Project project = dataContext.getData(PlatformDataKeys.PROJECT);
		if (project == null) {
			return;
		}

		VirtualFile file = fileEditor.getFile();
		if (!isAutoconfigFile(file)) {
			return;
		}

		FileDocumentManager documentManager = FileDocumentManager.getInstance();
		Document document = documentManager.getDocument(file);
		if (document == null) {
			return;
		}

		if (fileToolbarComponents.size() > 0) {
			component.scheduleShow();
		}

		if (!fileToolbarComponents.contains(component)) {
			fileToolbarComponents.add(component);
		}

		if (documentManager.isDocumentUnsaved(document)) {
			ApplicationKt.getApplication().executeOnPooledThread(() -> component.scheduleShow());
		}

		document.addDocumentListener(new DocumentListener() {
			@Override
			public void documentChanged(@NotNull DocumentEvent event) {
				ApplicationKt.getApplication().executeOnPooledThread(() -> component.scheduleShow());
			}
		}, parentDisposable);
	}

	@Override
	public void dispose() {
		clearToolbars();
	}

	public void clearToolbars() {
		for (FloatingToolbarComponent fileToolbarComponent : fileToolbarComponents) {
			fileToolbarComponent.hideImmediately();
		}
		fileToolbarComponents.clear();
	}

	private boolean isAutoconfigFile(@NotNull VirtualFile file) {
		String name = file.getName();
		return name.startsWith("autoconfig") && name.endsWith(".yaml");
	}
}