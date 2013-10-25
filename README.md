# Juzu

Juzu Web is a web framework for developing MVC applications, emphasing on simplicity and type safety.

# Website

http://juzuweb.org

# Build status

[![Build Status](https://ci.exoplatform.org/buildStatus/icon?job=juzu-master-ci)](https://ci.exoplatform.org/job/juzu-master-ci/)

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
