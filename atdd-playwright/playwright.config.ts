import { defineConfig } from '@playwright/test';

/**
 * Configuración de Playwright para pruebas ATDD de la API BookingYa.
 *
 * Se utiliza únicamente el APIRequestContext de Playwright (sin navegador)
 * para interactuar con los endpoints REST de la aplicación Spring Boot.
 */
export default defineConfig({
  testDir: './tests',
  timeout: 30_000,
  retries: 0,
  reporter: [['html', { open: 'never' }], ['list']],
  use: {
    baseURL: 'http://localhost:8080',
    extraHTTPHeaders: {
      'Content-Type': 'application/json',
      'Accept': 'application/json',
    },
  },
});
