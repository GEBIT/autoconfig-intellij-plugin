//
//  Notifications.java
//
//  Copyright (C) 2024
//  GEBIT Solutions GmbH,
//  Berlin, Duesseldorf, Stuttgart, Leipzig (Germany)
//  All rights reserved.

package de.gebit.intellij.autoconfig.util;

import com.intellij.notification.NotificationGroup;
import com.intellij.notification.NotificationGroupManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;

/**
 * Utility class used to display balloon notification messages.
 */
public class Notifications {
	private static NotificationGroup notificationGroup;

	private Notifications() {
	}

	public static NotificationGroup getNotifier() {
		if (notificationGroup == null) {
			notificationGroup = NotificationGroupManager.getInstance().getNotificationGroup("Autoconfig");
		}
		return notificationGroup;
	}

	public static void showInfo(String text, Project project) {
		getNotifier().createNotification(text, MessageType.INFO).notify(project);
	}

	public static void showError(String text, Project project) {
		getNotifier().createNotification(text, MessageType.ERROR).notify(project);
	}
}
