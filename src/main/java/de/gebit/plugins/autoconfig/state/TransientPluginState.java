package de.gebit.plugins.autoconfig.state;

import com.intellij.openapi.fileTypes.FileType;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

/**
 * Transient plugin state used for configuration parts that can not be reliably tied to a project.
 *
 * @param formatFileTypes         List of file types that should be formatted.
 * @param organizeImportFileTypes List of file types having their imports organised.
 */
public record TransientPluginState(@NotNull Collection<FileType> formatFileTypes, @NotNull Collection<FileType> organizeImportFileTypes) {
}
