import { test, expect, APIRequestContext } from '@playwright/test';

/**
 * ============================================================
 *  FASE 3 — Pruebas ATDD (Acceptance Test-Driven Development)
 *  Proyecto: BookingYa — API de Reservas de Habitaciones
 *  Framework: Playwright + TypeScript
 * ============================================================
 *
 *  Criterios de aceptación verificados:
 *    1. Creación de una reserva
 *    2. Consulta de reservas (listar todas)
 *    3. Obtención de una reserva por ID
 *    4. Actualización de una reserva existente
 *    5. Eliminación de una reserva
 *
 *  Pre-condiciones:
 *    - La API Spring Boot debe estar corriendo en http://localhost:8080
 *    - La base de datos debe estar accesible
 * ============================================================
 */

// ─── Variables compartidas entre tests ───────────────────────
let roomId: string;
let guestId: string;
let reservationId: string;

// Timestamp único para evitar colisiones de datos entre ejecuciones
const uniqueSuffix = Date.now();

// ─── Fechas futuras para la reserva ──────────────────────────
function futureDate(daysFromNow: number): string {
  const date = new Date();
  date.setDate(date.getDate() + daysFromNow);
  date.setHours(14, 0, 0, 0);
  return date.toISOString().replace('Z', '');
}

function futureDateCheckOut(daysFromNow: number): string {
  const date = new Date();
  date.setDate(date.getDate() + daysFromNow);
  date.setHours(11, 0, 0, 0);
  return date.toISOString().replace('Z', '');
}

// ─── Fechas para la actualización ────────────────────────────
function updatedCheckIn(): string {
  return futureDate(20);
}

function updatedCheckOut(): string {
  return futureDateCheckOut(23);
}

// ══════════════════════════════════════════════════════════════
//  SUITE PRINCIPAL: Ciclo de vida completo de una Reserva
// ══════════════════════════════════════════════════════════════
test.describe.serial('ATDD — Ciclo de vida de una Reserva', () => {

  // ────────────────────────────────────────────────────────────
  //  PREPARACIÓN: Crear habitación y huésped necesarios
  // ────────────────────────────────────────────────────────────
  test('Preparación: Crear una habitación disponible', async ({ request }) => {
    const roomPayload = {
      code: `ROOM-ATDD-${uniqueSuffix}`,
      name: `Suite ATDD ${uniqueSuffix}`,
      city: 'Bogotá',
      maxGuests: 4,
      nightlyPrice: 150.00,
      available: true,
    };

    const response = await request.post('/api/room', { data: roomPayload });

    expect(response.status()).toBe(200);

    const body = await response.json();
    expect(body.id).toBeTruthy();
    expect(body.code).toBe(roomPayload.code);
    expect(body.name).toBe(roomPayload.name);
    expect(body.city).toBe(roomPayload.city);
    expect(body.maxGuests).toBe(roomPayload.maxGuests);
    expect(body.available).toBe(true);

    roomId = body.id;
  });

  test('Preparación: Crear un huésped', async ({ request }) => {
    const guestPayload = {
      identification: `ID-ATDD-${uniqueSuffix}`,
      name: `Huésped ATDD ${uniqueSuffix}`,
      email: `atdd_${uniqueSuffix}@bookingya.com`,
    };

    const response = await request.post('/api/guest', { data: guestPayload });

    expect(response.status()).toBe(200);

    const body = await response.json();
    expect(body.id).toBeTruthy();
    expect(body.identification).toBe(guestPayload.identification);
    expect(body.name).toBe(guestPayload.name);
    expect(body.email).toBe(guestPayload.email);

    guestId = body.id;
  });

  // ────────────────────────────────────────────────────────────
  //  CASO 1: Creación de una reserva
  // ────────────────────────────────────────────────────────────
  test('CA-01: Creación de una reserva', async ({ request }) => {
    /*
     * Criterio de aceptación:
     *   DADO un huésped registrado y una habitación disponible
     *   CUANDO el usuario crea una reserva con fechas válidas
     *   ENTONCES el sistema devuelve la reserva creada con un ID único
     *     Y los datos coinciden con los enviados
     */
    const reservationPayload = {
      guestId: guestId,
      roomId: roomId,
      checkIn: futureDate(10),
      checkOut: futureDateCheckOut(13),
      guestsCount: 2,
      notes: 'Reserva creada por prueba ATDD - Fase 3',
    };

    const response = await request.post('/api/reservation', {
      data: reservationPayload,
    });

    expect(response.status()).toBe(200);

    const body = await response.json();
    expect(body.id).toBeTruthy();
    expect(body.guestId).toBe(guestId);
    expect(body.roomId).toBe(roomId);
    expect(body.guestsCount).toBe(2);
    expect(body.notes).toBe('Reserva creada por prueba ATDD - Fase 3');

    // Guardar el ID para los siguientes tests
    reservationId = body.id;
  });

  // ────────────────────────────────────────────────────────────
  //  CASO 2: Consulta de reservas (listar todas)
  // ────────────────────────────────────────────────────────────
  test('CA-02: Consulta de reservas', async ({ request }) => {
    /*
     * Criterio de aceptación:
     *   DADO que existen reservas registradas en el sistema
     *   CUANDO el usuario consulta todas las reservas
     *   ENTONCES el sistema devuelve una lista que contiene
     *     al menos la reserva creada previamente
     */
    const response = await request.get('/api/reservation');

    expect(response.status()).toBe(200);

    const body = await response.json();
    expect(Array.isArray(body)).toBe(true);
    expect(body.length).toBeGreaterThanOrEqual(1);

    // Verificar que la reserva creada está en la lista
    const found = body.find((r: any) => r.id === reservationId);
    expect(found).toBeTruthy();
    expect(found.guestId).toBe(guestId);
    expect(found.roomId).toBe(roomId);
  });

  // ────────────────────────────────────────────────────────────
  //  CASO 3: Obtención de una reserva por ID
  // ────────────────────────────────────────────────────────────
  test('CA-03: Obtención de una reserva por ID', async ({ request }) => {
    /*
     * Criterio de aceptación:
     *   DADO que existe una reserva con un ID conocido
     *   CUANDO el usuario consulta esa reserva por su ID
     *   ENTONCES el sistema devuelve los datos completos de la reserva
     *     Y el ID coincide con el solicitado
     */
    const response = await request.get(`/api/reservation/${reservationId}`);

    expect(response.status()).toBe(200);

    const body = await response.json();
    expect(body.id).toBe(reservationId);
    expect(body.guestId).toBe(guestId);
    expect(body.roomId).toBe(roomId);
    expect(body.guestsCount).toBe(2);
    expect(body.notes).toBe('Reserva creada por prueba ATDD - Fase 3');
  });

  // ────────────────────────────────────────────────────────────
  //  CASO 4: Actualización de una reserva existente
  // ────────────────────────────────────────────────────────────
  test('CA-04: Actualización de una reserva existente', async ({ request }) => {
    /*
     * Criterio de aceptación:
     *   DADO que existe una reserva registrada
     *   CUANDO el usuario actualiza las fechas, la cantidad de huéspedes
     *     y las notas de la reserva
     *   ENTONCES el sistema devuelve la reserva con los datos actualizados
     *     Y el ID de la reserva se mantiene igual
     */
    const updatePayload = {
      guestId: guestId,
      roomId: roomId,
      checkIn: updatedCheckIn(),
      checkOut: updatedCheckOut(),
      guestsCount: 3,
      notes: 'Reserva ACTUALIZADA por prueba ATDD - Fase 3',
    };

    const response = await request.put(`/api/reservation/${reservationId}`, {
      data: updatePayload,
    });

    expect(response.status()).toBe(200);

    const body = await response.json();
    expect(body.id).toBe(reservationId);
    expect(body.guestsCount).toBe(3);
    expect(body.notes).toBe('Reserva ACTUALIZADA por prueba ATDD - Fase 3');

    // Verificar con un GET que los cambios persisten
    const getResponse = await request.get(`/api/reservation/${reservationId}`);
    expect(getResponse.status()).toBe(200);

    const getBody = await getResponse.json();
    expect(getBody.guestsCount).toBe(3);
    expect(getBody.notes).toBe('Reserva ACTUALIZADA por prueba ATDD - Fase 3');
  });

  // ────────────────────────────────────────────────────────────
  //  CASO 5: Eliminación de una reserva
  // ────────────────────────────────────────────────────────────
  test('CA-05: Eliminación de una reserva', async ({ request }) => {
    /*
     * Criterio de aceptación:
     *   DADO que existe una reserva registrada
     *   CUANDO el usuario elimina la reserva por su ID
     *   ENTONCES el sistema confirma la eliminación con status 200
     *     Y al intentar consultarla de nuevo por ID, se obtiene un error 404
     */

    // Paso 1: Eliminar la reserva
    const deleteResponse = await request.delete(`/api/reservation/${reservationId}`);
    expect(deleteResponse.status()).toBe(200);

    // Paso 2: Verificar que ya no existe
    const getResponse = await request.get(`/api/reservation/${reservationId}`);
    expect(getResponse.status()).toBe(404);
  });

  // ────────────────────────────────────────────────────────────
  //  LIMPIEZA: Eliminar datos de prueba
  // ────────────────────────────────────────────────────────────
  test('Limpieza: Eliminar huésped y habitación de prueba', async ({ request }) => {
    // Eliminar huésped
    const deleteGuest = await request.delete(`/api/guest/${guestId}`);
    expect(deleteGuest.status()).toBe(200);

    // Eliminar habitación
    const deleteRoom = await request.delete(`/api/room/${roomId}`);
    expect(deleteRoom.status()).toBe(200);
  });
});
