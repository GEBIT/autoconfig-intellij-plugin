package de.gebit.plugins.autoconfig.sdk;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationGroup;
import com.intellij.notification.NotificationGroupManager;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.JavaSdk;
import com.intellij.openapi.projectRoots.ProjectJdkTable;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.projectRoots.SdkType;
import com.intellij.openapi.projectRoots.impl.UnknownSdkCollector;
import com.intellij.openapi.projectRoots.impl.UnknownSdkFix;
import com.intellij.openapi.projectRoots.impl.UnknownSdkFixAction;
import com.intellij.openapi.projectRoots.impl.UnknownSdkSnapshot;
import com.intellij.openapi.projectRoots.impl.UnknownSdkTracker;
import com.intellij.openapi.roots.ui.configuration.UnknownSdk;
import com.intellij.openapi.ui.MessageType;
import de.gebit.plugins.autoconfig.util.Notifications;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * Utility class to resolve missing JDKs
 */
public class JDKResolver {

	private JDKResolver() {
		// nothing
	}

	public static String findProjectSdk(String sdkName, Project project) {
		if (sdkName == null) {
			return null;
		}
		for (Sdk jdk : ProjectJdkTable.getInstance().getAllJdks()) {
			if (jdk.getName().equals(sdkName)) {
				return jdk.getName();
			}
		}

		resolveMissingJDK(sdkName, project);

		return sdkName;
	}

	/**
	 * Ensures, that the currently missing JDK is made available for the project. If the JDK is not found on the system
	 * it tries to assist the user in applying the IDEs suggestions.
	 *
	 * @param jdkName the name of the JDK to resolve
	 * @param project the project to resolve the JDK for
	 */
	public static void resolveMissingJDK(String jdkName, Project project) {
		final NotificationGroup notifier = Notifications.getNotifier();
		final NotificationGroup sdkNotification = NotificationGroupManager.getInstance()
				.getNotificationGroup("AutoconfigSDK");

		final Notification notification = notifier.createNotification(
				"JDK \"" + jdkName + "\" not found. Autoconfig is trying to resolve this SDK automatically. If this fails, please configure the SDK manually.",
				MessageType.WARNING);
		notification.notify(project);

		ProgressManager.getInstance().runProcessWithProgressSynchronously(() -> {
			final ProgressIndicator progressIndicator = ProgressManager.getInstance().getProgressIndicator();
			final UnknownSdkCollector unknownSdkCollector = new UnknownSdkCollector(project) {
				@NotNull
				@Override
				public UnknownSdkSnapshot collectSdksBlocking() {
					return new UnknownSdkSnapshot(Set.of(), List.of(new UnknownSdk() {
						@Override
						public @NotNull String getSdkName() {
							return jdkName;
						}

						@Override
						public @NotNull SdkType getSdkType() {
							return JavaSdk.getInstance();
						}
					}), Arrays.stream(ProjectJdkTable.getInstance().getAllJdks()).toList());
				}
			};

			final List<UnknownSdkFix> unknownSdkFixes = UnknownSdkTracker.getInstance(project)
					.collectUnknownSdks(unknownSdkCollector, progressIndicator);
			final List<UnknownSdkFix> otherFixes = UnknownSdkTracker.getInstance(project)
					.applyAutoFixesAndNotify(unknownSdkFixes, progressIndicator);

			if (otherFixes.isEmpty()) {
				// we've found an instant fix for this JDK
				notification.expire();
			}

			for (UnknownSdkFix unknownSdkFix : otherFixes) {
				final UnknownSdkFixAction suggestedFixAction = unknownSdkFix.getSuggestedFixAction();
				if (suggestedFixAction != null) {
					// it seems like we've found a fix for this JDK, but it's not an instant fix, e.g. downloading a jdk.
					// we'll prompt the user to apply the fix
					final Notification fixableSdkError = sdkNotification.createNotification("Fixable missing JDK",
							suggestedFixAction.getActionDetailedText(), NotificationType.WARNING);
					fixableSdkError.addAction(DumbAwareAction.create(suggestedFixAction.getActionShortText(), evt -> {
						suggestedFixAction.applySuggestionAsync(project);
						fixableSdkError.expire();
						notification.expire();
					}));
					fixableSdkError.setImportantSuggestion(true);
					fixableSdkError.setSuggestionType(true);
					fixableSdkError.notify(project);
				} else {
					// neither we nor IntelliJ were able to find a fix for the requested JDK. This isn't going anywhere.
					sdkNotification.createNotification("Unfixable missing JDK!",
							unknownSdkFix.getNotificationText() + "\nPlease install it manually and add it to your SDK list.",
							NotificationType.ERROR).notify(project);
				}
			}
		}, "Autoconfig: Resolving JDK \"" + jdkName + "\"", true, project);
	}
}
