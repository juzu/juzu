# Juzu

Juzu Web is a web framework matching portlet MVC.

# Status

Inception and prototyping phase.

# Principles

## Controller

Juzu programming model is inspired by the Play! Framework that provides a simple and efficient programming model for
the web.

### Controller

Controller are methods annotated with @Action or @Render

    @Action
    public void purchaseProduct(String productId) { ... }

    @Render
    public void showProduct(String productId) { ... }

## Injection

Juzu leverages the Context and Dependency Injection framework to bring type safe injection.

### Template injection

    @Inject
    @Template("MyTemplate.gtmpl");
    private Template template;

### Printer injection

    @Inject
    private Printer printer;

## Compiler integration

Juzu integrates at the heart of the Java compiler thanks to Annotation Processing Tools (APT). APT allows to perform
code generation in a fully portable manner, it just work with the Java compiler in a very transparent manner
and thus is integrated with any build system or IDE that uses a Java 6 compiler. Juzu uses APT for
various features.

### Build time validation

Validates various things during at build time.

### Template compilation

A template declaration

    @Resource("myTemplate.gtml")
    private Template myTemplate;

Takes the corresponding template file and generates the corresponding Groovy source code to be compiled.

### URL literal generation

URL literal emulates first call support for application URLs, it allows an application to uses valid URLs at build time.

A controller method such as

    public class Controller {
      @Render
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

## Extension

Juzu relies on Context and Dependency Injection to integrate an application with extensions.

# Technical notes

The chapter is about stuff that are related to technical implementation of Juzu. It may be already done features or things
to do at some point.

## Todo

- provide a relocatable template folder with a `templatePath` field in the annotation `@Application`
- support portlet event phase

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

The `TemplateProcessor` plugin is triggered by the `@Resource` annotation, it will lookup a template provide based on the
template extension (for instance `gtmpl` -> `GroovyTemplateProvider`). Templates are located in the template folder
of the application which is by default the `templates` child package of the application.

#### Template provider

Template providers are located using the java service loader mechanism.

### Context and Dependency Injection

#### Provider

For now CDI support is provided by the Weld reference implementation which is abstracted in the SPI package.

#### Integration

##### Bean visibility

By default CDI consider that any class can be a managed bean, we want to restrict that in Juzu and instead decide which beans
can be seen or not. The `@Export` annotation is used to declare the classes seen by CDI.

##### Invocation scopes

Three scopes exist matching the various portlet life cycles
- @RequestScoped : similar to the spec one
- @ActionScoped : valid during process action
- @RenderScoped : valid during render action
