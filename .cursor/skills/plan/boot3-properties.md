# Referencia rápida: Boot 2.7 → Boot 3.x (Jakarta + JDK 21)

Consultar solo cuando el plan requiera detalle de renombres. No memorizar; buscar en el proyecto con `rg`.

## Paquetes (código)

| Boot 2 / Java EE | Boot 3 / Jakarta |
|------------------|------------------|
| `javax.persistence.*` | `jakarta.persistence.*` |
| `javax.validation.*` | `jakarta.validation.*` |
| `javax.servlet.*` | `jakarta.servlet.*` |
| `javax.annotation.*` | `jakarta.annotation.*` |
| `javax.transaction.*` | `jakarta.transaction.*` |

## `pom.xml` típico

```xml
<parent>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-parent</artifactId>
  <version>3.3.x</version> <!-- o 3.2.x LTS -->
</parent>
<properties>
  <java.version>21</java.version>
</properties>
```

Opcional: `maven.compiler.release` 21 vía parent de Boot.

## Propiedades de aplicación (frecuentes)

| Boot 2 | Boot 3 |
|--------|--------|
| `spring.jpa.properties.hibernate.dialect=...H2Dialect` | A menudo innecesario; Hibernate 6 autodetecta |
| `spring.datasource.driverClassName` | Preferir `spring.datasource.driver-class-name` (ambos suelen funcionar) |
| `spring.h2.console.path` | Sin cambio relevante |

Revisar [Spring Boot 3.0 Migration Guide](https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-3.0-Migration-Guide) para la lista completa.

## Dependencias de este stack (Thymeleaf CRUD)

- `spring-boot-starter-data-jpa`, `web`, `thymeleaf`: versiones gestionadas por el parent 3.x
- `h2`: compatible; validar consola y archivo `./testdb`
- `webjars` (bootstrap, jquery): suelen seguir igual; comprobar versiones si hay conflictos

## Validación post-migración

```bash
./mvnw clean test
./mvnw spring-boot:run
# Smoke: listar tutorials, crear/editar/borrar, /h2-ui si está habilitada
```
