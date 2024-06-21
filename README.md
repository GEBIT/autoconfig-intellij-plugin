# Autoconfig for Jetbrains IDEs

Autoconfig is an extension for IntelliJ IDEA and other Jetbrains IDEs that enables persistence and sharing of project specific IDE-settings outside the `workspace.xml`-file.

## Goal

Several user-specific IDE settings are stored for each project in the VCS-ignored-`workspace.xml`-file. This is a good thing for individual workspace look and feel, but some project-specific settings are also stored in the workspace.xml. That's why it can be cumbersome to configure your IDE differently for each project. Here are some examples for IDE settings that might be a good idea to be shared across users:

- Format on Save
- Custom Plugin Repositories
- Compiler Settings
- Maven Importing Settings

Autoconfig helps with sharing settings across multiple users by allowing to configure settings in a separate file which can be committed into your version control. This lessens the need of project specific instructions on how to configure your IDE for working on a project, and it ensures that all users share the same settings.

## How to use

1. Go to `Tools -> Autoconfig -> Create Autoconfig File`
2. Select the type of Autoconfig that should be created
3. Create an Autoconfig file for your project
4. Reopen your project
5. Autoconfig automatically configures the IDE on restart
6. Commit the Autoconfig configuration file(s)

## How to set up
Detailed setup instructions can be found in the [project Wiki](https://github.com/GEBIT/autoconfig-intellij-plugin/wiki).

## Available settings

See the available settings configurable by Autoconfig in: [schemas](src/main/resources/schema)

## How to contribute

See [CONTRIBUTING.md](CONTRIBUTING.md)
