# Mass Relocator for Gradle Shadow

This is a Gradle plugin which makes the `shadowJar` task relocate all dependencies which can be moved. It is useful when the shaded jar is loaded as a plugin and its dependencies may be overridden with those which had already been loaded before, causing issues with version conflicts (NoSuchMethodError and similar).

A package is moved when:
* It contains at least one class.
* None of the classes in the package contain a native method.
* It is not explicitly excluded.

Link to its Gradle plugin repository page:
https://plugins.gradle.org/plugin/com.sedmelluq.mass-relocator

## Usage

The plugin first needs to be included:

```groovy
plugins {
  id 'com.sedmelluq.mass-relocator' version '1.0.0'
}
```

Then it can be configured to set the base package where everything is moved and specifying excludes. For example:
```groovy
massRelocator {
  base 'myapp'
  excludePrefix 'org/apache/commons/logging'
}
```

This would prepend `myapp.` to the name of all packages, meaning it moves all of them into `myapp/` directory in the JAR file. Sometimes some packages cannot be moved because their location is used as a string (in case of the commons logging in the example above), so those should be added as excludes.
