# Vert.x bridge for Juzu

This is an implementation of Juzu working on top of the [Vert.x](http://vertx.io) server.

# Features

In addition of Juzu features (see [Juzu Web](http://juzuweb.org)) the bridge provides:

- Application hot reload
- Support flash and session scopes as serialized java objects
- Injection of Vert.x object in controllers
- Asynchronous push of the response

# Status

The bridge is at an alpha stage, however it is functionnal enough for developing small applications like the Booking application example.

# The booking demo

The booking demo can executed on top of Vert.x minor modifications, here are the steps:

1. Instal juzu-mod: unzip the juzu [mod](http://repository.exoplatform.org/service/local/repo_groups/public/content/org/juzu/juzu-vertx/0.7.0-beta6/juzu-vertx-0.7.0-beta6.zip
) into your `$VERTX_MODS` directory 
2. Get and unzip the [sources jar](http://repository.exoplatform.org/service/local/repo_groups/public/content/org/juzu/juzu-booking/0.7.0-beta6/juzu-booking-0.7.0-beta6-sources.jar
) of the booking application : `jar -xvf juzu-booking-sources.jar`
3. Edit the file `org/sample/booking/package-info.java` to remove the usage of the *Servlet* and *Portlet* plugins
    1. Remove `@Servlet("/")` and `import juzu.plugin.servlet.Servlet;`
    2. Remove `@Portlet()` and `import juzu.plugin.portlet.Portlet;`
3. Go in the juzu-booking-sources
4. Run `vertx runmod juzu-v1.0 -conf conf.json`
