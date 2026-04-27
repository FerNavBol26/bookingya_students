# Fase 3: ATDD - Pruebas de Aceptación con Playwright

## Descripción General

Este directorio contiene las pruebas ATDD (Acceptance Test-Driven Development) para la aplicación BookingYa. Las pruebas están implementadas usando **Playwright con TypeScript** y validan los criterios de aceptación del sistema de reservas.

## Criterios de Aceptación Validados

### CA-01: Creación de una reserva
- ✓ Crear una habitación disponible
- ✓ Crear un huésped
- ✓ Crear una reserva con fechas válidas
- ✓ Verificar que el sistema devuelve la reserva con ID único
- ✓ Verificar que los datos coinciden con los enviados

### CA-02: Consulta de reservas
- ✓ Listar todas las reservas
- ✓ Verificar que la lista contiene la reserva creada

### CA-03: Obtención de una reserva por ID
- ✓ Consultar una reserva específica por su ID
- ✓ Verificar que los datos retornados son correctos

### CA-04: Actualización de una reserva existente
- ✓ Actualizar fechas de check-in y check-out
- ✓ Actualizar cantidad de huéspedes
- ✓ Actualizar notas
- ✓ Verificar que los cambios persisten

### CA-05: Eliminación de una reserva
- ✓ Eliminar una reserva por su ID
- ✓ Verificar que el sistema confirma la eliminación (Status 200)
- ✓ Verificar que al intentar consultarla se obtiene un error 404

## Requisitos Previos

- **Node.js** v18 o superior
- **npm** o yarn
- **API Java Spring Boot** corriendo en `http://localhost:8080/api`

## Instalación Local

### 1. Instalar dependencias

```bash
cd atdd-playwright
npm install
```

### 2. Iniciar la aplicación Spring Boot

En otra terminal, desde la raíz del proyecto:

```bash
mvn spring-boot:run -DskipTests
```

La aplicación estará disponible en: `http://localhost:8080/api`

### 3. Ejecutar los tests

```bash
npm test
```

O ejecutar con reporte HTML:

```bash
npm test -- --reporter=html
```

### 4. Ver el reporte

```bash
npm run test:report
```

## Estructura del Proyecto

```
atdd-playwright/
├── tests/
│   └── reservation-atdd.spec.ts    # Casos de prueba ATDD
├── playwright.config.ts             # Configuración de Playwright
├── package.json                     # Dependencias del proyecto
├── tsconfig.json                    # Configuración de TypeScript
└── README.md                        # Este archivo
```

## Configuración de Playwright

El archivo `playwright.config.ts` incluye:
- **Base URL**: `http://localhost:8080`
- **Timeout**: 30 segundos por test
- **Reporter**: HTML (sin abrir automáticamente)
- **Headers**: Content-Type y Accept en application/json

## Tests en GitHub Actions

Los tests ATDD se ejecutan automáticamente en el pipeline CI/CD:

1. **Trigger**: Push a `main` o `estudiantes`, o Pull Request
2. **Ambiente**: Ubuntu Latest con Node.js 20 y Java 21
3. **Pasos**:
   - Instalar dependencias
   - Compilar la aplicación
   - Iniciar Spring Boot en background
   - Esperar a que la aplicación esté lista
   - Ejecutar tests de Playwright
   - Subir reporte como artefacto

## Características de los Tests

### Suite Serial
Los tests se ejecutan en orden secuencial para mantener el estado compartido (IDs de habitación, huésped y reserva).

### Datos Únicos
- Se usa un `uniqueSuffix` basado en timestamp para evitar conflisiones entre ejecuciones
- Cada guest y room recibe identificadores únicos

### Validación Completa
- Se valida status HTTP
- Se validan todos los campos retornados
- Se verifica la persistencia con GET después de UPDATE
- Se verifica el error 404 después de DELETE

### Cleanup Automático
- Limpieza de datos de prueba al finalizar
- Eliminación de huéspedes y habitaciones creadas

## Troubleshooting

### "No es posible conectar con el servidor remoto"

Asegúrate de que:
1. Spring Boot está corriendo: `mvn spring-boot:run -DskipTests`
2. La aplicación está accesible en `http://localhost:8080/api`
3. Prueba: `curl http://localhost:8080/api/room`

### Tests fallan con 500 Internal Server Error

Verifica:
1. El archivo `src/main/resources/application.properties` existe
2. La configuración de base de datos H2 es correcta
3. Las entidades de JPA están correctamente mapeadas

### Playwright no se instala correctamente

Intenta:
```bash
rm -rf node_modules package-lock.json
npm install
npm install -D @playwright/test
npx playwright install
```

## Resultados de Ejecución

### Local (Exitosa)
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

## Notas Importantes

1. **Base de Datos en Memoria**: Los tests usan H2 en memoria (`create-drop`), por lo que cada ejecución comienza con una BD limpia
2. **Tests Seriales**: El orden es importante; los tests dependen de IDs compartidos
3. **Fechas Futuras**: Las reservas se crean con fechas futuras para evitar validaciones
4. **Timestamps Únicos**: Se generan nuevos datos en cada ejecución para permitir múltiples ejecuciones consecutivas

## Próximos Pasos

- Expandir tests con más criterios de aceptación
- Agregar tests para errores y casos excepcionales
- Implementar reportes con más detalles
- Integrar con herramientas de análisis de cobertura

## Referencias

- [Documentación de Playwright](https://playwright.dev)
- [Playwright Testing Guide](https://playwright.dev/docs/intro)
- [Cucumber + Playwright (Alternativa BDD)](https://cucumber.io)
