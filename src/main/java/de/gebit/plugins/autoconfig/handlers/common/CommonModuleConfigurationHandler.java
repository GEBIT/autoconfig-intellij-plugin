package de.gebit.plugins.autoconfig.handlers.common;


import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.projectRoots.SdkType;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.ProjectRootManager;
import de.gebit.plugins.autoconfig.UpdateModuleHandler;
import de.gebit.plugins.autoconfig.handlers.AbstractHandler;
import de.gebit.plugins.autoconfig.model.GeneralModuleConfiguration;
import de.gebit.plugins.autoconfig.model.ModuleSDK;
import de.gebit.plugins.autoconfig.sdk.JDKResolver;
import de.gebit.plugins.autoconfig.util.Notifications;
import org.jetbrains.annotations.NonNls;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Allow setting module SDK for a limited number of matching modules.
 */
public class CommonModuleConfigurationHandler extends AbstractHandler implements UpdateModuleHandler<GeneralModuleConfiguration> {
	private static final @NonNls String CONFIG_SCHEMA_JSON = "/schema/configModule.schema.json";

	private static final @NonNls String CONFIG_FILE_NAME = "autoconfigModule.yaml";

	private static final @NonNls Class<GeneralModuleConfiguration> CONFIGURATION_CLASS = GeneralModuleConfiguration.class;

	@Override
	public String getFileName() {
		return CONFIG_FILE_NAME;
	}

	@Override
	public String getJsonSchema() {
		return CONFIG_SCHEMA_JSON;
	}

	@Override
	public String getUpdaterName() {
		return "Common module configuration updater";
	}

	@Override
	public Class<GeneralModuleConfiguration> getConfigurationClass() {
		return CONFIGURATION_CLASS;
	}

	@Override
	public boolean acceptModule(GeneralModuleConfiguration configuration, Module module) {
		return matchesAnyName(module, configuration.getModuleFilter());
	}

	@Override
	public List<String> updateConfiguration(GeneralModuleConfiguration configuration, Module module) {
		List<String> updatedConfigs = new ArrayList<>();
		applyModuleSDKOptions(configuration.getModuleSDK(), module, updatedConfigs);
		return updatedConfigs;
	}

	private void applyModuleSDKOptions(ModuleSDK sdkOptions, Module module, List<String> updatedConfigs) {
		if (sdkOptions != null) {
			SdkType sdk = SdkType.findByName(sdkOptions.getType());
			if (sdk != null) {
				Sdk moduleSdk = JDKResolver.findSdk(sdkOptions.getName(), module.getProject());
				if (moduleSdk != null) {
					ProjectRootManager projectRootManager = ProjectRootManager.getInstance(module.getProject());
					ModifiableRootModel modifiableModel = ModuleRootManager.getInstance(module).getModifiableModel();
					Sdk currentModuleSdk = modifiableModel.getSdk();
					Sdk projectSdk = projectRootManager.getProjectSdk();
					if (projectSdk != null && projectSdk.equals(moduleSdk)) {
						// reset module SDK in case project SDK and designated module SDK are identical
						if (!modifiableModel.isSdkInherited()) {
							commitModifiableModelChange(modifiableModel, ModifiableRootModel::inheritSdk);
							updatedConfigs.add("Module SDK");
						}
					} else {
						applySetting(moduleSdk, currentModuleSdk, s -> commitModifiableModelChange(modifiableModel,
								modifiableRootModel -> modifiableRootModel.setSdk(s)), updatedConfigs, "Module SDK");
					}
				}
			} else {
				Notifications.showWarning(
						"SDK type \"" + sdkOptions.getType() + "\" has not been found for auto configuration",
						module.getProject());
			}
		}
	}

	private void commitModifiableModelChange(ModifiableRootModel modifiableModel, Consumer<ModifiableRootModel> consumer) {
		WriteAction.runAndWait(() -> {
			consumer.accept(modifiableModel);
			modifiableModel.commit();
		});
	}
}
