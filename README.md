# Juzu

Juzu Web is a web framework for developing MVC applications, emphasing on simplicity and type safety.

# Deploys on

- GateIn 3.2 with Tomcat
- GateIn 3.3 with Tomcat and JBoss AS7
- Liferay 5.x and 6.10 (tested with Tomcat)

# How to

The distribution contains
- booking : the Booking application example
- tutorial : the Juzu tutorial
- reference : the API and the reference guide

# Website

http://juzu.github.com

# Build instructions

Prerequisites
- Java 1.7
- Apache Maven 3

Build the project

    mvn verify

Build the project and generate tests coverage information (not human readable they are useful to be integrated in another software like SonarQube)

   mvn verify -Pcoverage

Build the project and generate tests coverage reports (in target/site/jacoco)

   mvn verify -Pcoverage-report
