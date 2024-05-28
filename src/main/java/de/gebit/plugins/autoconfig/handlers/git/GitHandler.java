//
//  GitHandler.java
//
//  Copyright (C) 2024
//  GEBIT Solutions GmbH,
//  Berlin, Duesseldorf, Stuttgart, Leipzig (Germany)
//  All rights reserved.

package de.gebit.plugins.autoconfig.handlers.git;

import com.intellij.openapi.project.Project;
import de.gebit.plugins.autoconfig.UpdateHandler;
import de.gebit.plugins.autoconfig.handlers.AbstractHandler;
import de.gebit.plugins.autoconfig.model.GitConfiguration;
import git4idea.config.GitVcsSettings;
import git4idea.config.UpdateMethod;
import org.jetbrains.annotations.NonNls;

import java.util.ArrayList;
import java.util.List;

/**
 * Handler implementing git configuration settings updates.
 */
public class GitHandler extends AbstractHandler implements UpdateHandler<GitConfiguration> {
	private static final @NonNls String CONFIG_SCHEMA_JSON = "/schema/git.schema.json";

	public static final @NonNls String CONFIG_FILE_NAME = "autoconfigGit.yaml";

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
		return "Git configuration updater";
	}

	@Override
	public Class<GitConfiguration> getConfigurationClass() {
		return GitConfiguration.class;
	}

	@Override
	public List<String> updateConfiguration(GitConfiguration configuration, Project project) {
		List<String> changedConfigs = new ArrayList<>();
		GitConfiguration.UpdateMethod updateMethod = configuration.getUpdateMethod();
		if (updateMethod != null) {
			var method = switch (updateMethod) {
				case MERGE -> UpdateMethod.MERGE;
				case REBASE -> UpdateMethod.REBASE;
			};
			GitVcsSettings vcsSettings = GitVcsSettings.getInstance(project);
			applySetting(method, vcsSettings.getUpdateMethod(), vcsSettings::setUpdateMethod, changedConfigs, "Git update method");
		}
		return changedConfigs;
	}
}
