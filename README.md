# DotNet as script plugin for Jenkins

This plugin, enables you to run c# code as a normal build step.

Features:

 * Run your c# code "dotnet core" like a script language.
 * Multiplatform plugin (as dotnet core is now).
 * Based in your c# code this plugin creates a dotnet core project with all the required information.
 * Add custom nuget packages in a JSON format.
 * The code is compiled again when it changes or the packages changed.
 * For future executions of unchanged code the compiled executable will be used.
 * Inject environment variables for the next build step.

You need to be sure to have installed a dotnet core installation.

