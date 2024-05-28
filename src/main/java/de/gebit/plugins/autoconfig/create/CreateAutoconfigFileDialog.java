package de.gebit.plugins.autoconfig.create;

import com.intellij.openapi.ui.DialogWrapper;
import de.gebit.plugins.autoconfig.AutoconfigStartup;
import de.gebit.plugins.autoconfig.UpdateHandler;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Optional;

/**
 * DialogWrapper for {@link CreateAutoconfigFileForm}, creating the form with all available configuration updaters.
 */
public class CreateAutoconfigFileDialog extends DialogWrapper {

	private CreateAutoconfigFileForm autoconfigFileForm;

	public CreateAutoconfigFileDialog() {
		super(false);
		init();
		setTitle("Create Autoconfig File");
	}

	@Override
	protected @Nullable JComponent createCenterPanel() {
		autoconfigFileForm = new CreateAutoconfigFileForm(AutoconfigStartup.EP_NAME.getExtensionList());
		return autoconfigFileForm.getForm();
	}

	public Optional<UpdateHandler<?>> getSelectedHandler() {
		return autoconfigFileForm.getSelectedHandler();
	}
}
