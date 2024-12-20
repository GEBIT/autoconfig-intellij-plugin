package de.gebit.plugins.autoconfig.create;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.ui.DialogWrapper;
import de.gebit.plugins.autoconfig.UpdateSettings;
import de.gebit.plugins.autoconfig.messages.AutoconfigBundle;
import de.gebit.plugins.autoconfig.service.ConfigurationUpdaterService;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * DialogWrapper for {@link CreateAutoconfigFileForm}, creating the form with all available configuration updaters.
 */
public class CreateAutoconfigFileDialog extends DialogWrapper {

	private final List<Module> moduleList;
	private CreateAutoconfigFileForm autoconfigFileForm;

	public CreateAutoconfigFileDialog(List<Module> moduleList) {
		super(false);
		this.moduleList = moduleList;
		init();
		setTitle(AutoconfigBundle.message("createautoconfigfile.title"));
	}

	@Override
	protected @Nullable JComponent createCenterPanel() {
		List<UpdateSettings<?>> settings = new ArrayList<>(
				ConfigurationUpdaterService.PROJECT_EP_NAME.getExtensionList());
		settings.addAll(ConfigurationUpdaterService.MODULE_EP_NAME.getExtensionList());
		autoconfigFileForm = new CreateAutoconfigFileForm(settings, moduleList);
		return autoconfigFileForm.getForm();
	}

	public Optional<UpdateSettings<?>> getSelectedSettings() {
		return autoconfigFileForm.getSelectedSettings();
	}

	public Optional<Module> getSelectedModule() {
		return autoconfigFileForm.getSelectedModule();
	}
}
