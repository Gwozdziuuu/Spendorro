# quarkus-setup
This project uses Quarkus, the Supersonic Subatomic Java Framework.
## Running the application in dev mode
You can run your application in dev mode that enables live coding using:
```shell script
./mvnw clean && ./mvnw quarkus:dev
```
> **_NOTE:_**  Quarkus now ships with a Dev UI, which is available in dev mode only at <http://localhost:8080/q/dev/>.

## Running tests

Run unit tests:
```shell script
./mvnw test
```

Run integration tests:
```shell script
./mvnw verify
```

Run all tests (unit + integration):
```shell script
./mvnw clean test verify
```

## Creating a native executable

You can create a native executable using:

```shell script
./mvnw package -Dnative
```

Or, if you don't have GraalVM installed, you can run the native executable build in a container using:

```shell script
./mvnw package -Dnative -Dquarkus.native.container-build=true
```

You can then execute your native executable with: `./target/quarkus-setup-1.0.0-SNAPSHOT-runner`

If you want to learn more about building native executables, please consult <https://quarkus.io/guides/maven-tooling>.

## Related Guides

- Dashbuilder ([guide](https://quarkiverse.github.io/quarkiverse-docs/quarkus-dashbuilder/dev/index.html)): Dashbuilder extension for embedding dashboards in a Quarkus application
- Liquibase ([guide](https://quarkus.io/guides/liquibase)): Handle your database schema migrations with Liquibase
- JDBC Driver - PostgreSQL ([guide](https://quarkus.io/guides/datasource)): Connect to the PostgreSQL database via JDBC
..