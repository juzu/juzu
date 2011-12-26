# Juzu

Juzu Web is a web framework matching portlet MVC.

You can join the Juzu [mailing list](http://groups.google.com/group/juzu) to discuss or contribute.

Version 0.4.1 is [available](https://github.com/downloads/juzu/juzu/juzu-distrib-0.4.1.zip) it contains

- a tutorial (example and doc)
- the booking application example

Screencast

- [Hello World developed with Eclipse](http://vimeo.com/33184509)
- [Template type safe parameters in Eclipse](http://vimeo.com/33370178)
- [Juzu dev mode](http://vimeo.com/33519963)

# Status

- 0.1 milestone reached : booking demo adapted from Play!
- 0.2 milestone reached
- 0.3 milestone reached
    - Liferay support
    - Weld and Spring integration
    - basic packaging
- 0.4.1
    - eclipse incremental compilation support (experimental)
    - tutorial: documentation and sample
    - maven archetype
- 0.4.2
    - template type safe parameters
    - Eclipse Hello World screencast
    - Template type safe parameters screencast
    - use unchecked exception for wrapping IOException
    - integration with web application classloading in dev mode
    - introduce declarative injection binding to register injection provider
    - update to Weld 1.1.4
    - friendly error display
    - bug fixes
- 0.4.2
    - URL xml escaping

# Roadmap

- 0.5
    - tutorial (cont)
    - portlet eventing support
    - header taglib
- 0.6
    - bean mapping
    - bean validation
- 0.7
    - request context stacking
    - extensible taglib
    - application import
- 0.8
    - module system
- 0.9
    - cross site scripting (xss) support
    - servlet support
    - routing engine

Various things to do, not exhaustive that needs to go in the roadmap

- clarify how invocation URL are constructed and dispatched to the target (id versus resolution)
- provide a deployment mode that get source from the file system wherever they are (in order to use the real sources)
- parse error in template parser
- more stack trace sanitization
- consider resolving a template via its variable name instead of @Path
- honour life cycle of objects (specially flash scope)
- handle internal error correctly
- think about doing a "debug" portlet that would cooperate with juzu portlets


# Deploy on

- GateIn/Tomcat
- GateIn/JBoss5.1 requires to remove the seam and cdi deployer in AS5.1
- Liferay/Tomcat tested on Liferay 6.10 and 5.x

# Dependency Injection integration

Juzu leverages the JSR-330 specification to perform dependency injection, this specification is supported by the most
popular dependency injection frameworks such as Weld, Spring and Guice. At the moment the Weld and Spring frameworks are
 supported by Juzu.

Configuration happens in the portlet.xml:

    <init-param>
      <name>juzu.inject</name>
      <value>weld</value>
      <!--
      <value>spring</value>
      -->
    </init-param>

## Weld integration

Weld is the default dependency injection framework.

# Spring integration

Spring is used by setting the `spring` value for the `juzu.inject` portlet init parameter. Spring beans are declared in the
`spring.xml` file.

# Maven archetype

The command line to use for creating a simple Juzu application using the Maven archetype

    >mvn archetype:generate \
    -DarchetypeGroupId=org.juzu \
    -DarchetypeArtifactId=juzu-archetype \
    -DarchetypeVersion=0.4.2 \
    -DgroupId=<my.groupid> \
    -DartifactId=<my-artifactId>

# Principles

## Controller

Juzu programming model is inspired by the Play! Framework that provides a simple and efficient programming model for
the web. Portlet programming model is very close to Play! programmint model and thus it makes a lot of sense to deliver
a powerful and simple programming model.

### Controller

Controller are methods annotated with `@Action` or `@View`

    @Action
    public Response purchaseProduct(String productId) { ... }

    @View
    public void showProduct(String productId) { ... }

## Compile time validation

Juzu integrates at the heart of the Java compiler thanks to Annotation Processing Tools (APT). APT allows to perform
code generation in a fully portable manner, it just work with the Java compiler in a very transparent manner
and thus is integrated with any build system or IDE that uses a Java 6 compiler.

### Build time validation

Validates various things during at build time.

### Template compilation

A template declaration

    @Path("myTemplate.gtml")
    private Template myTemplate;

Takes the corresponding template file and generates the corresponding Groovy source code to be compiled.

### Template injection

    @Inject
    @Path("MyTemplate.gtmpl");
    private Template template;

### Templates

Juzu templates engine is based on Groovy and provide a powerful set of instructions.

#### Template URL resolution

Juzu validates the application templates during the compilation phase and will attempt to resolve any link against
 a controller method: application links are validated at compilation time.

#### Template parameters

Template can have parameters

    #{param name=temperature/}
    The ${temperature}

Such parameters that can be used in a type safe way in a controller that renders the template

    template.temperature("30").render();

### Scopes

The @inject specification provides the notion of scope and Juzu leverages it to manage the life cycle of the various
objects. Juzu provides a set of scope that can be used to control the scope of objects:

- Application scope (@Singleton annotation provides by @inject specification)
- Request scope
- Session scope
- Flash scope

## Compiler integration

### URL literal generation

URL literal emulates first call support for application URLs, it allows an application to uses valid URLs at build time.

A controller method such as

    public class Controller {
      @View
      public void showProduct(String productId) { }
    }

Generates an url literal like

    public class Controller_ {
      public static URLBuilder showProduct(String productId) { ... }
    }

That can be used

    URLBuilder url = Controller_.showProduct("myproduct");

## Dev mode

Juzu features a dev mode in which the application is recompiled in real time.

## Error reporting

Juzu should report error precisely, specially in dev mode.

# Technical notes

The chapter is about stuff that are related to technical implementation of Juzu. It may be already done features or things
to do at some point.

## Architecture

### Compiler

Juzu leverages the Java Compiler API provided since Java 6. The compiler is mostly used for the dev mode.

### Virtual File System

The virtual file system  is mostly used by the compiler

- `SimpleFileSystem` provides a degenerated view of a file system, the compiler uses it for classpath integration
- `ReadFileSystem` provides a read view of a file system, the compiler uses it for reading sources.
- `ReadWriteFileSystem` extends the read view and provides write operations, the compiler uses it for writing the generated sources and classes

There are several implementation of the file system:

- File system for `java.io.File` (RW) : not really used
- Memory (RW) : used by the compiler to write the compilation output in dev mode
- Jar (R) : used for the jar in the web application
- War (R) : used to load the application classes from `/WEB-INF/classes` mainly
- ClassLoader (S) : used to provide a classpath during compilation

### Template system

The template system is extensible and provides an initial support with the Groovy language.

#### Compiler integration

The `MainProcessor` plugin is triggered by the `@Path` annotation, it will lookup a template provide based on the
template extension (for instance `gtmpl` -> `GroovyTemplateProvider`). Templates are located in the template folder
of the application which is by default the `templates` child package of the application.

#### Template provider

Template providers are located using the java service loader mechanism.

### Tag library

To describe
