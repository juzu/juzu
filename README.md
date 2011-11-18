# Juzu

Juzu Web is a web framework matching portlet MVC. Version 0.3 is [available](https://github.com/downloads/juzu/juzu/juzu-0.3.zip) and
at the moment contains the sample booking application that works on GateIn and Liferay. At the moment there is no formal
documentation, yet the sample application is good enough to discover how to use Juzu and can be used as a bootstrap to
create applications.

# Status

- 0.1 milestone reached : booking demo adapted from Play!
- 0.2 milestone reached
- 0.3 milestone reached
    - Liferay support
    - Weld and Spring integration
    - basic packaging

# Roadmap

- 0.4
    - eclipse incremental compilation support
    - initial documentation and sample
    - maven archetype
- 0.5
    - bean mapping
    - bean validation
    - IDE screencast
    - portlet eventing support
- 0.6
    - header taglib
    - servlet support
    - routing engine
    - cross site scripting (xss) support
- 0.7
    - request context stacking
    - extensible taglib
    - application import

Various things to do, not exhaustive that needs to go in the roadmap

- Provide a deployment mode that get source from the file system wherever they are (in order to use the real sources)
- UndeclaredIOException
- parse error in template parser
- stack trace sanitization
- consider resolving a template via its variable name instead of @Path
- honour life cycle of objects (specially flash scope)


# Deploy on

- GateIn/Tomcat
- GateIn/JBoss5.1 requires to remove the seam and cdi deployer in AS5.1
- Liferay/Tomcat tested on Liferay 6.10

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

### Template URL resolution

Juzu validates the application templates during the compilation phase and will attempt to resolve any link against
 a controller method: application links are validated at compilation time.

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

- `ReadFileSystem` provides a read view of a file system, The compiler uses it for reading sources.
- `ReadWriteFileSystem` extends the  read view and provides write operations, the compiler uses it for writing the generated sources and classes

There are several implementation of the file system:

- File system for `java.io.File` (RW) : not really used
- Memory (RW) : used by the compiler to write the compilation output in dev mode
- Jar (R) : used for the jar in the web application
- War (R) : used to load the application classes from `/WEB-INF/classes` mainly

### Template system

The template system is extensible and provides an initial support with the Groovy language.

#### Compiler integration

The `TemplateProcessor` plugin is triggered by the `@Path` annotation, it will lookup a template provide based on the
template extension (for instance `gtmpl` -> `GroovyTemplateProvider`). Templates are located in the template folder
of the application which is by default the `templates` child package of the application.

#### Template provider

Template providers are located using the java service loader mechanism.

### Tag library

To describe
