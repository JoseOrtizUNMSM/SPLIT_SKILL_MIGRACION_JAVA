---
name: plan
description: >-
  Analyzes Spring Boot Maven backends on JDK 8 and produces a phased migration
  plan to JDK 21. Use when the user invokes /plan, asks to plan a JDK 21
  migration, or mentions upgrading Java 8, Maven, or Spring Boot for Java 21.
disable-model-invocation: true
---

# Plan — Migración JDK 8 → JDK 21 (Spring Boot + Maven)

Analiza este proyecto Spring Boot que actualmente corre con JDK 8 y Maven.
Necesito planificar una migración del backend hacia JDK 21.

## Objetivo

Entregar un **plan de migración** (no implementar cambios salvo que el usuario lo pida después). El plan debe ser específico del repositorio analizado, con fases ordenadas, riesgos y criterios de validación.

## Flujo de trabajo

Copia este checklist y márcalo al avanzar:

```
- [ ] 1. Inventario del baseline
- [ ] 2. Análisis de código y dependencias
- [ ] 3. Evaluación de compatibilidad (Java + Spring Boot)
- [ ] 4. Diseño de fases de migración
- [ ] 5. Entrega del plan (plantilla de salida)
```

### 1. Inventario del baseline

Lee y documenta:

| Artefacto | Qué extraer |
|-----------|-------------|
| `pom.xml` | `java.version`, parent `spring-boot-starter-parent`, dependencias, plugins |
| `.mvn/wrapper/maven-wrapper.properties` | Versión de Maven |
| `src/main/resources/application*.properties` / `.yml` | Datasource, JPA/Hibernate, propiedades obsoletas |
| `Dockerfile`, `.github/workflows`, `Jenkinsfile` | JDK en CI/CD y contenedores |
| `README.md` | Instrucciones de build/run |

Ejecuta (adapta rutas si hace falta):

```bash
./mvnw -version
./mvnw -q dependency:tree -Dverbose=false
./mvnw test
```

Si `./mvnw` falla en Windows, usa `mvnw.cmd`. Registra versiones reales de JDK y Maven del entorno.

### 2. Análisis de código y dependencias

Busca en `src/` y en tests:

```bash
# javax (bloquean Boot 3 / Jakarta)
rg "javax\.(persistence|validation|servlet|annotation|transaction|inject|ws)" src/

# APIs internas / removidas
rg "sun\.|com\.sun\.|jdk\.internal" src/

# reflection / SecurityManager (JDK 17+)
rg "setAccessible|SecurityManager|AccessController" src/
```

Revisa también:

- Entidades JPA (`javax.persistence` → `jakarta.persistence` en Boot 3)
- `javax.transaction` vs `jakarta.transaction`
- Configuración Hibernate explícita (p. ej. `hibernate.dialect` puede simplificarse en Boot 3)
- Starters de terceros sin versión explícita (heredan del BOM de Boot)

### 3. Evaluación de compatibilidad

**Regla central:** JDK 21 en producción con Spring Boot implica, en la práctica, **Spring Boot 3.2+** (mínimo Java 17; soporte estable de 21 desde la línea 3.2).

| Estado típico | Spring Boot | Java soportado | Notas |
|---------------|-------------|----------------|-------|
| Actual (ej. 2.7.x + `java.version` 1.8) | 2.7.x | 8–19 (oficial) | No es destino final para JDK 21 |
| Puente opcional | 2.7.x | 17 | Valida tests antes del salto a Boot 3 |
| Destino | 3.2.x o 3.3.x LTS | 17–21 | Requiere migración `javax` → `jakarta` |

Documenta para **este** proyecto:

- Versión actual de Spring Boot y si el salto es 2.7 → 3.2/3.3 directo o con etapa intermedia en Java 17
- Dependencias que arrastran `javax.*` (JPA, validation, servlet API vía transitivas)
- Cambios de configuración conocidos (H2 console, `ddl-auto`, dialectos Hibernate)
- Herramientas: Maven ≥ 3.6.3 (3.9+ recomendado para JDK 21)

Para matrices detalladas de propiedades renombradas Boot 2→3, ver [boot3-properties.md](boot3-properties.md).

### 4. Diseño de fases

Propón **3–5 fases** con dependencias claras. Plantilla recomendada:

| Fase | Objetivo | Cambios principales | Validación |
|------|----------|---------------------|------------|
| 0 | Baseline | Tag/commit, `mvn test` verde | Tests + smoke manual |
| 1 | Tooling | Maven wrapper, `maven-compiler-plugin`, CI JDK | `mvn -version`, pipeline |
| 2 | Boot 3 + Jakarta | Parent 3.2/3.3, imports `jakarta.*`, propiedades | `mvn test`, arranque app |
| 3 | JDK 21 | `java.version` 21, toolchain si aplica | Tests + runtime 21 |
| 4 | Limpieza | Deprecations, dialectos, dependencias obsoletas | Regresión completa |

Incluye en cada fase: **esfuerzo estimado** (S/M/L), **riesgo** (bajo/medio/alto) y **rollback** (revertir commit / rama).

### 5. Entrega del plan

Responde en el idioma del usuario. Usa esta estructura:

```markdown
# Plan de migración JDK 8 → JDK 21

## Resumen ejecutivo
[2–4 oraciones: estado actual, destino, principal riesgo]

## Estado actual
- JDK (pom / runtime): …
- Spring Boot: …
- Maven: …
- Dependencias críticas: …

## Hallazgos
- [Bloqueantes / altos / medios / bajos]

## Fases de migración
### Fase 0: …
…
### Fase N: …
…

## Matriz de cambios previstos
| Área | Archivo / componente | Acción |
|------|----------------------|--------|

## Riesgos y mitigaciones
…

## Criterios de aceptación
- [ ] `mvn test` en JDK 21
- [ ] Aplicación arranca y CRUD principal funciona
- [ ] CI usa JDK 21

## Siguiente paso recomendado
[Una acción concreta para empezar Fase 0 o 1]
```

## Reglas

- **Solo planificar** por defecto; no modificar `pom.xml` ni código hasta que el usuario lo pida.
- Citar hallazgos con rutas reales del repo (`pom.xml`, clases con `javax.*`, etc.).
- Si `mvn test` falla en el baseline, documentar el fallo y no asumir verde en fases posteriores.
- Priorizar el camino **Boot 2.7 + Java 8 → Boot 3.x + JDK 21**; evitar quedarse solo en “subir `java.version` a 21” sin actualizar Spring Boot.
- Mencionar impacto en **base de datos H2 en archivo** (`testdb.mv.db`) si aplica: backup antes de cambios de dialecto/Hibernate.

## Recursos

- [boot3-properties.md](boot3-properties.md) — propiedades y paquetes frecuentes en migración Boot 2 → 3
