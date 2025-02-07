package de.gebit.plugins.autoconfig.messages;


import com.intellij.DynamicBundle;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.PropertyKey;

/**
 * Message bundle for autoconfig plugin.
 */
public class AutoconfigBundle {
	public static final @NonNls String IDEA_ACTIONS_BUNDLE = "de.gebit.plugins.autoconfig.messages.AutoconfigBundle";

	public static final DynamicBundle INSTANCE = new DynamicBundle(AutoconfigBundle.class, IDEA_ACTIONS_BUNDLE);

	public AutoconfigBundle() {
		// no initialisation necessary
	}

	public static @NotNull @Nls String message(@NotNull @PropertyKey(resourceBundle = IDEA_ACTIONS_BUNDLE) String key, Object... params) {
		return INSTANCE.getMessage(key, params);
	}
}
