# ADR 0004 — Development build uses `-source`/`-target` instead of `--release`

- **Status:** Accepted
- **Date:** 2026-10-01
- **Phase:** 1 — Foundation

## Context

The build failed on this machine before any code was written:

```
[ERROR] maven-compiler-plugin:3.14.1:compile (default-compile)
        Fatal error compiling: error: release version 21 not supported
```

The Spring Boot parent sets `maven.compiler.release=${java.version}` = 21, and
maven-compiler-plugin therefore passes `javac --release 21`.

The cause is the shape of the installed JDK, not the project. `java -version`
reports 21.0.12.1 and `jdk.compiler` is present, so `javac` runs — but the
package installed is `openjdk-21-jre-headless`, and its tree has neither a
`javac` launcher nor `lib/ct.sym`:

```
$ ls /usr/lib/jvm/java-21-openjdk-amd64/bin/
java  jpackage  keytool  rmiregistry

$ ls /usr/lib/jvm/java-21-openjdk-amd64/lib/ct.sym
No such file or directory
```

`ct.sym` holds the platform API signatures for older language levels. Without it
`javac` cannot resolve *any* `--release` value — not 21, not 17 — which is why
the error names a version the compiler plainly understands. Reproduced outside
Maven:

```
$ java -m jdk.compiler/com.sun.tools.javac.Main --release 21 T.java
error: release version 21 not supported

$ java -m jdk.compiler/com.sun.tools.javac.Main -source 21 -target 21 T.java
(exit 0)
```

The second form works because `-source`/`-target` read the running platform
directly instead of consulting `ct.sym`.

## Decision

Do not change the Java installation, the `JAVA_HOME`, or the language level.
`backend/.mvn/maven.config` clears `maven.compiler.release` and pins
`maven.compiler.source` and `maven.compiler.target` to 21.

The file is three lines with no comments, because Maven only gained comment
support in `maven.config` in 3.9.0 and this repository is also buildable with the
system Maven 3.8.7. The rationale lives here instead.

Verified: `./mvnw clean compile` and `mvn clean compile` both succeed, and output
is class file major version 65 — Java 21 bytecode, unchanged.

## Consequences

**What is lost.** `--release 21` is the stronger flag: it validates that only
Java 21 APIs are used, catching accidental use of a newer JDK's methods. This
build does not get that check, and a build agent that *does* have a complete JDK
21 would still get it — the setting is local to this checkout.

**What is not lost.** The language level and the bytecode target are 21. Nothing
about the artifact changes.

**How to undo it.** Install a JDK that includes `lib/ct.sym` and delete
`backend/.mvn/maven.config`. The Spring Boot parent then supplies `release 21` on
its own, which is what CI should do. This file is a local-environment
workaround and should not survive into a properly provisioned build environment.
