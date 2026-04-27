# FASE 3 - ATDD (Acceptance Test-Driven Development) - Resumen de Implementación

## Estado: ✅ COMPLETADA Y FUNCIONAL

### Cambios Realizados

#### 1. **Configuración de la Aplicación**

**Archivo**: `src/main/resources/application.properties`
- Creado: Archivo de configuración de la aplicación
- Configuración de H2 en memoria
- Puerto: 8080 con contexto `/api`

**Archivo**: `pom.xml`
- Cambio de scope H2 de `test` a `runtime` para que esté disponible en desarrollo
- Cambio de PostgreSQL a `provided` para permitir el uso de H2 localmente

#### 2. **Corrección de Entidades JPA**

**Archivo**: `src/main/java/com/project/bookingya/entities/GuestEntity.java`
- Simplificación: Cambio de `firstName` + `lastName` a campo único `name`
- Eliminación de `phoneNumber` (no estaba en los requisitos)
- Alineación con `GuestDto` que solo utiliza `name`

**Archivo**: `src/test/java/com/project/bookingya/acceptance/ReservationAcceptanceTest.java`
- Actualización de campos a usar `setName()` en lugar de `setFirstName()` y `setLastName()`
- Eliminación de referencias a `setPhoneNumber()`

#### 3. **Configuración de Playwright ATDD**

**Archivo**: `atdd-playwright/playwright.config.ts`
- Base URL: `http://localhost:8080`
- Timeout: 30 segundos
- Reporters: HTML (sin abrir automáticamente) + list
- Headers: Content-Type y Accept en application/json

**Archivo**: `atdd-playwright/package.json`
- Dependencias: `@playwright/test` v1.52.0
- Dependencias: `typescript` v5.7.0
- Scripts: `test` para ejecutar tests, `test:report` para ver resultados

#### 4. **Pruebas ATDD Implementadas**

**Archivo**: `atdd-playwright/tests/reservation-atdd.spec.ts`

**Escenarios Implementados** (8 tests):

1. **Preparación: Crear una habitación disponible**
   - POST `/api/room`
   - Validación: Status 200, ID generado, datos coinciden

2. **Preparación: Crear un huésped**
   - POST `/api/guest`
   - Validación: Status 200, ID generado, datos coinciden

3. **CA-01: Creación de una reserva**
   - POST `/api/reservation`
   - Validación: Status 200, reserva creada con datos correctos

4. **CA-02: Consulta de reservas**
   - GET `/api/reservation`
   - Validación: Status 200, lista contiene la reserva creada

5. **CA-03: Obtención de una reserva por ID**
   - GET `/api/reservation/{id}`
   - Validación: Status 200, datos completos correctos

6. **CA-04: Actualización de una reserva existente**
   - PUT `/api/reservation/{id}`
   - Validación: Status 200, cambios aplicados y persistentes

7. **CA-05: Eliminación de una reserva**
   - DELETE `/api/reservation/{id}` → verificar status 200
   - GET `/api/reservation/{id}` → verificar status 404

8. **Limpieza: Eliminar huésped y habitación de prueba**
   - DELETE `/api/guest/{id}` y DELETE `/api/room/{id}`
   - Validación: Status 200 para ambas

#### 5. **Actualización del Pipeline CI/CD**

**Archivo**: `.github/workflows/ci.yml`

**Cambios**:
- Actualización de Java 17 a Java 21
- Agregación de Node.js 20 para Playwright
- Nueva etapa "Fase 3 - ATDD" que:
  - Instala dependencias de npm
  - Compila la aplicación Maven
  - Inicia Spring Boot en background
  - Espera a que la aplicación esté lista
  - Ejecuta tests de Playwright
  - Carga artefactos con el reporte HTML

#### 6. **Documentación**

**Archivo**: `atdd-playwright/README.md`
- Descripción de criterios de aceptación
- Instrucciones de instalación local
- Cómo ejecutar tests
- Configuración de Playwright
- Troubleshooting
- Información sobre CI/CD

### Resultados de Ejecución

#### Local ✅ (8/8 tests pasados)
```
Running 8 tests using 1 worker

  ✓ Preparación: Crear una habitación disponible (724ms)
  ✓ Preparación: Crear un huésped (55ms)
  ✓ CA-01: Creación de una reserva (116ms)
  ✓ CA-02: Consulta de reservas (35ms)
  ✓ CA-03: Obtención de una reserva por ID (23ms)
  ✓ CA-04: Actualización de una reserva existente (69ms)
  ✓ CA-05: Eliminación de una reserva (57ms)
  ✓ Limpieza: Eliminar huésped y habitación de prueba (40ms)

  8 passed (2.6s)
```

#### GitHub Actions
- Configurado con Java 21 y Node.js 20
- Ejecuta automáticamente en push y pull requests
- Carga reportes HTML como artefactos

### Cómo Ejecutar Localmente

1. **Iniciar la aplicación Spring Boot**:
   ```bash
   mvn spring-boot:run -DskipTests
   ```

2. **Ejecutar los tests ATDD en otra terminal**:
   ```bash
   cd atdd-playwright
   npm install
   npm test
   ```

3. **Ver el reporte**:
   ```bash
   npm run test:report
   ```

### Puntos Clave de Implementación

✅ **Criterios de Aceptación Completados**:
- Creación de una reserva
- Consulta de una reserva
- Actualización de una reserva existente
- Eliminación de una reserva
- Obtención de una reserva por ID

✅ **Framework Utilizado**: Playwright + TypeScript

✅ **Tests Ejecutándose**: Local (8/8) y listos para GitHub Actions

✅ **Otras Fases Preservadas**: Las Fases 1 (TDD) y 2 (BDD) permanecen intactas

### Notas Importantes

1. Los tests se ejecutan en orden secuencial para mantener el estado compartido
2. Utilizan timestamp único para evitar colisiones de datos
3. Incluyen limpieza automática de datos de prueba
4. H2 en memoria reinicia en cada ejecución
5. Compatible con Java 21 (último LTS)

### Archivos Modificados

- `pom.xml` - Cambios en scopes de dependencias
- `src/main/resources/application.properties` - Creado
- `src/main/java/com/project/bookingya/entities/GuestEntity.java` - Simplificación de campos
- `src/test/java/com/project/bookingya/acceptance/ReservationAcceptanceTest.java` - Actualización de campos
- `.github/workflows/ci.yml` - Actualización a Java 21 y agregación de Playwright
- `atdd-playwright/playwright.config.ts` - Actualización de baseURL
- `atdd-playwright/tests/reservation-atdd.spec.ts` - Actualización de endpoints API
- `atdd-playwright/README.md` - Documentación completa (creado)

### Siguientes Pasos Opcionales

- Agregar más casos de prueba para errores y excepciones
- Implementar datos paramétricos para variantes de pruebas
- Agregar métricas de cobertura de código
- Documentar en Gherkin (Cucumber) si se requiere lenguaje natural
