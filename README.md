# Autoconfig for IntelliJ

Autoconfig is an extension for IntelliJ IDEA which allows you to persist and share project specific IDE-settings outside the `workspace.xml`-file.

## Goal

Several user specific IDE settings are stored for each project in the VCS-ignored-`workspace.xml`-file. This is a good thing, but it can be cumbersome to configure your IDE differently for each project. Some IDE settings that might want to be shared across users:

- Format on Save
- Custom Plugin Repositories
- Compiler Settings
- Maven Importing Settings

Autoconfig helps you with sharing settings across multiple users by allowing you to configure settings in a separate file which can be checked in into your version control. This lessens the need of project specific instructions on how to configure your IDE for working on a project, and it ensures that all users share the same settings.

## How to use

1. Go to `Tools -> Autoconfig -> Create Autoconfig File`
2. Select the type of Autoconfig you want to create
3. Create an Autoconfig file for your project
4. Restart your IDE
5. Autoconfig automatically configures your IDE on restart

## Available settings

See the available settings configurable by Autoconfig in: [schemas](src/main/resources/schema)

## How to contribute

See [CONTRIBUTING.md](CONTRIBUTING.md)
