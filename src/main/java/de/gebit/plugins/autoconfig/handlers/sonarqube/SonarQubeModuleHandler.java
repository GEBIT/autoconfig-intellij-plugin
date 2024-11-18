package de.gebit.plugins.autoconfig.handlers.sonarqube;

import com.intellij.openapi.module.Module;
import de.gebit.plugins.autoconfig.UpdateModuleHandler;
import de.gebit.plugins.autoconfig.handlers.AbstractHandler;
import de.gebit.plugins.autoconfig.model.SonarQubeConfiguration;
import org.jetbrains.annotations.NonNls;
import org.sonarlint.intellij.config.Settings;
import org.sonarlint.intellij.config.module.SonarLintModuleSettings;
import org.sonarlint.intellij.config.project.SonarLintProjectSettings;

import java.util.ArrayList;
import java.util.List;

/**
 * Configuration handler used to synchronise SonarQube module bindings in projects.
 */
public class SonarQubeModuleHandler extends AbstractHandler implements UpdateModuleHandler<SonarQubeConfiguration> {
	private static final @NonNls String CONFIG_SCHEMA_JSON = "/schema/sonarqubeModule.schema.json";

	public static final @NonNls String CONFIG_FILE_NAME = "autoconfigSonarQubeModule.yaml";

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
		return "SonarQube module configuration updater";
	}

	@Override
	public Class<SonarQubeConfiguration> getConfigurationClass() {
		return SonarQubeConfiguration.class;
	}

	@Override
	public boolean acceptModule(SonarQubeConfiguration configuration, Module module) {
		return matchesAnyName(module, configuration.getModuleFilter());
	}

	@Override
	public List<String> updateConfiguration(SonarQubeConfiguration configuration, Module module) {
		List<String> updatedConfigs = new ArrayList<>();
		SonarLintProjectSettings projectSettings = Settings.getSettingsFor(module.getProject());
		if (projectSettings.isBindingEnabled() && configuration.getProjectKey() != null) {
			SonarLintModuleSettings moduleSettings = Settings.getSettingsFor(module);
			applySetting(configuration.getProjectKey(), moduleSettings.getProjectKey(), moduleSettings::setProjectKey,
					updatedConfigs, "SonarQube module key");
		}
		return updatedConfigs;
	}
}
