package de.gebit.plugins.autoconfig;


/**
 * Common interface for update handlers. Meta information about the handler is returned from the given methods.
 */
public interface UpdateSettings<T> {
	/**
	 * Provide the file name that is expected to contain the extensions' configuration.
	 *
	 * @return the file name that is expected to contain the extensions' configuration
	 */
	String getFileName();

	/**
	 * The json schema containing/providing the information on how to write the configuration file.
	 *
	 * @return json schema containing/providing the information on how to write the configuration file
	 */
	String getJsonSchema();

	/**
	 * The updater name is used for logging purposes and as a name for the json schema displayed in IntelliJ status
	 * bar.
	 *
	 * @return the name of this updater
	 */
	String getUpdaterName();

	/**
	 * The configuration object class used to read/deserialize the configuration file.
	 *
	 * @return configuration object class used to read/deserialize the configuration file
	 */
	Class<T> getConfigurationClass();

	/**
	 * Specifies, whether modules or projects are target for this handler object.
	 *
	 * @return whether modules or projects are target for this handler object
	 */
	UpdateTarget getUpdateTarget();
}
