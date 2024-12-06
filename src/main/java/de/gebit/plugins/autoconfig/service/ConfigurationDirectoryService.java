package de.gebit.plugins.autoconfig.service;


import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.project.ProjectKt;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;

/**
 * Service used to determine directories for project and modules.
 */
@RequiredArgsConstructor
@Service(Service.Level.PROJECT)
public final class ConfigurationDirectoryService {
	private static final @NotNull Logger LOGGER = Logger.getInstance(ConfigurationDirectoryService.class);

	/**
	 * Configuration directory containing yaml sources.
	 */
	public static final String AUTOCONFIG_DIRECTORY = "autoconfig";

	private final Project project;

	/**
	 * Get project configuration directory ".idea/autoconfig".
	 *
	 * @return project configuration directory ".idea/autoconfig", or else an empty Optional.
	 */
	public Optional<VirtualFile> getProjectAutoconfigDirectory() {
		return getProjectDirectory().map(f -> f.findChild(AUTOCONFIG_DIRECTORY));
	}

	/**
	 * Get module configuration directory ".idea/autoconfig". Whether ".idea" is used depends on the project.
	 *
	 * @return module configuration directory ".idea/autoconfig", or else an empty Optional.
	 */
	public Optional<VirtualFile> getModuleAutoconfigDirectory(Module module) {
		VirtualFile moduleDir = ProjectUtil.guessModuleDir(module);
		if (moduleDir == null) {
			return Optional.empty();
		}
		String projectIdeaDirectoryName = getIdeaDirectoryName();
		VirtualFile moduleIdeaDirectory = moduleDir.findChild(projectIdeaDirectoryName);
		if (moduleIdeaDirectory == null) {
			return Optional.empty();
		}
		return Optional.ofNullable(moduleIdeaDirectory.findChild(AUTOCONFIG_DIRECTORY));
	}

	private @NotNull String getIdeaDirectoryName() {
		Optional<VirtualFile> projectDirectory = getProjectDirectory();
		return projectDirectory.map(VirtualFile::getName).orElse(".idea");
	}

	/**
	 * Determine the location of the ".idea/autoconfig" directory (and create it if necessary). A new configuration file
	 * with the given name is created in the directory.
	 *
	 * @param fileName the configuration file name to be created
	 * @return the file handle for the newly created file, or an empty Optional
	 */
	public Optional<VirtualFile> findOrCreateProjectAutoconfigFile(String fileName) {
		Optional<VirtualFile> projectDirectory = getProjectDirectory();

		if (projectDirectory.isEmpty()) {
			return Optional.empty();
		}
		return findOrCreateAutoconfigFile(projectDirectory.get(), fileName, AUTOCONFIG_DIRECTORY);
	}

	/**
	 * Determine the location of the ".idea/autoconfig" directory (and create it if necessary). A new configuration file
	 * with the given name is created in the directory.
	 *
	 * @param module   the module to create the file for
	 * @param fileName the configuration file name to be created
	 * @return the file handle for the newly created file, or an empty Optional
	 */
	public Optional<VirtualFile> findOrCreateModuleAutoconfigFile(Module module, String fileName) {
		VirtualFile moduleDir = ProjectUtil.guessModuleDir(module);
		if (moduleDir == null) {
			return Optional.empty();
		}
		String projectIdeaDirectoryName = getIdeaDirectoryName();
		return findOrCreateAutoconfigFile(moduleDir, fileName, projectIdeaDirectoryName, AUTOCONFIG_DIRECTORY);
	}

	private Optional<VirtualFile> findOrCreateAutoconfigFile(VirtualFile baseDirectory, String fileName, String... dirList) {
		try {
			return Optional.of(WriteAction.compute(() -> {
				VirtualFile autoconfigDirectory = baseDirectory;
				for (String subDirectory : dirList) {
					VirtualFile subDirectoryFile = autoconfigDirectory.findChild(subDirectory);
					if (subDirectoryFile == null) {
						// no ".idea" or ".idea/autoconfig" directory found. We're going to create it.
						autoconfigDirectory = autoconfigDirectory.createChildDirectory(this, subDirectory);
					} else {
						autoconfigDirectory = subDirectoryFile;
					}
				}

				return autoconfigDirectory.findOrCreateChildData(this, fileName);
			}));
		} catch (IOException ex) {
			LOGGER.error("Couldn't create Autoconfig file!", ex);
			return Optional.empty();
		}
	}

	/**
	 * Determine the location of the .idea directory.
	 *
	 * @return the location of the .idea directory
	 */
	public Optional<VirtualFile> getProjectDirectory() {
		var projectFile = project.getProjectFile();
		if (projectFile == null) {
			// Fallback to find .idea directory. May happen when project is first opened and no misc.xml can be found
			Path directoryStorePath = ProjectKt.getStateStore(project).getDirectoryStorePath();
			if (directoryStorePath != null) {
				return Optional.ofNullable(VirtualFileManager.getInstance().findFileByNioPath(directoryStorePath));
			}
			return Optional.empty();
		}
		return Optional.ofNullable(projectFile.getParent());
	}
}
