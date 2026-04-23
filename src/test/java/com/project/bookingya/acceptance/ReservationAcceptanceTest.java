package com.project.bookingya.acceptance;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.project.bookingya.dtos.ReservationDto;
import com.project.bookingya.entities.GuestEntity;
import com.project.bookingya.entities.RoomEntity;
import com.project.bookingya.models.Reservation;
import com.project.bookingya.repositories.IGuestRepository;
import com.project.bookingya.repositories.IReservationRepository;
import com.project.bookingya.repositories.IRoomRepository;
import com.project.bookingya.services.ReservationService;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("Pruebas de Aceptación - Requisitos del Cliente")
class ReservationAcceptanceTest {

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private IReservationRepository reservationRepository;

    @Autowired
    private IRoomRepository roomRepository;

    @Autowired
    private IGuestRepository guestRepository;

    private UUID guestId;
    private UUID roomId;

    @BeforeEach
    void setUp() {
        // Crear un huésped de prueba
        GuestEntity guest = new GuestEntity();
        guest.setFirstName("Juan");
        guest.setLastName("Pérez");
        guest.setEmail("juan@example.com");
        guest.setPhoneNumber("3001234567");
        GuestEntity savedGuest = guestRepository.save(guest);
        guestId = savedGuest.getId();

        // Crear una habitación disponible
        RoomEntity room = new RoomEntity();
        room.setName("Habitación Deluxe");
        room.setAvailable(true);
        room.setMaxGuests(6);
        room.setPricePerNight(150.0);
        RoomEntity savedRoom = roomRepository.save(room);
        roomId = savedRoom.getId();
    }

    @Test
    @DisplayName("AC1: El cliente puede crear una reserva para una fecha disponible")
    void acceptanceTest_ClientCanCreateReservation() {
        // Criterio: Dado que hay una habitación disponible para las fechas solicitadas
        // Cuando el cliente crea una nueva reserva
        // Entonces el sistema confirma la creación con un identificador único
        
        ReservationDto reservationDto = new ReservationDto();
        reservationDto.setGuestId(guestId);
        reservationDto.setRoomId(roomId);
        reservationDto.setCheckIn(LocalDateTime.now().plusDays(2));
        reservationDto.setCheckOut(LocalDateTime.now().plusDays(5));
        reservationDto.setGuestsCount(2);

        Reservation created = reservationService.create(reservationDto);

        assertNotNull(created);
        assertNotNull(created.getId());
        assertEquals(guestId, created.getGuestId());
        assertEquals(roomId, created.getRoomId());
    }

    @Test
    @DisplayName("AC2: El cliente puede consultar sus reservas por ID")
    void acceptanceTest_ClientCanQueryReservationById() {
        // Criterio: Dado que existe una reserva en el sistema
        // Cuando el cliente consulta por su ID
        // Entonces obtiene los detalles completos de la reserva

        ReservationDto dto = new ReservationDto();
        dto.setGuestId(guestId);
        dto.setRoomId(roomId);
        dto.setCheckIn(LocalDateTime.now().plusDays(1));
        dto.setCheckOut(LocalDateTime.now().plusDays(3));
        dto.setGuestsCount(1);

        Reservation saved = reservationService.create(dto);
        Reservation queried = reservationService.getById(saved.getId());

        assertNotNull(queried);
        assertEquals(saved.getId(), queried.getId());
        assertEquals(saved.getCheckIn(), queried.getCheckIn());
        assertEquals(saved.getCheckOut(), queried.getCheckOut());
    }

    @Test
    @DisplayName("AC3: El cliente puede ver todas sus reservas")
    void acceptanceTest_ClientCanViewAllReservations() {
        // Criterio: Dado que el cliente tiene múltiples reservas
        // Cuando consulta todas las reservas
        // Entonces recibe una lista completa

        // Crear múltiples reservas
        for (int i = 0; i < 3; i++) {
            ReservationDto dto = new ReservationDto();
            dto.setGuestId(guestId);
            dto.setRoomId(roomId);
            dto.setCheckIn(LocalDateTime.now().plusDays(1 + i * 4));
            dto.setCheckOut(LocalDateTime.now().plusDays(3 + i * 4));
            dto.setGuestsCount(1);
            reservationService.create(dto);
        }

        List<Reservation> all = reservationService.getAll();
        assertTrue(all.size() >= 3);
    }

    @Test
    @DisplayName("AC4: El cliente puede modificar una reserva existente")
    void acceptanceTest_ClientCanUpdateReservation() {
        // Criterio: Dado que el cliente tiene una reserva confirmada
        // Cuando actualiza detalles como cantidad de huéspedes
        // Entonces los cambios se aplican correctamente

        ReservationDto initial = new ReservationDto();
        initial.setGuestId(guestId);
        initial.setRoomId(roomId);
        initial.setCheckIn(LocalDateTime.now().plusDays(1));
        initial.setCheckOut(LocalDateTime.now().plusDays(3));
        initial.setGuestsCount(2);

        Reservation saved = reservationService.create(initial);

        ReservationDto updated = new ReservationDto();
        updated.setGuestId(guestId);
        updated.setRoomId(roomId);
        updated.setCheckIn(LocalDateTime.now().plusDays(1));
        updated.setCheckOut(LocalDateTime.now().plusDays(3));
        updated.setGuestsCount(4);

        Reservation modified = reservationService.update(updated, saved.getId());

        assertEquals(4, modified.getGuestsCount());
    }

    @Test
    @DisplayName("AC5: El cliente puede cancelar una reserva")
    void acceptanceTest_ClientCanCancelReservation() {
        // Criterio: Dado que el cliente tiene una reserva confirmada
        // Cuando cancela la reserva
        // Entonces se elimina del sistema y no puede ser consultada

        ReservationDto dto = new ReservationDto();
        dto.setGuestId(guestId);
        dto.setRoomId(roomId);
        dto.setCheckIn(LocalDateTime.now().plusDays(1));
        dto.setCheckOut(LocalDateTime.now().plusDays(3));
        dto.setGuestsCount(2);

        Reservation saved = reservationService.create(dto);
        UUID reservationId = saved.getId();

        reservationService.delete(reservationId);

        assertThrows(Exception.class, () -> reservationService.getById(reservationId));
    }

    @Test
    @DisplayName("AC6: El cliente no puede hacer overbooking de habitaciones")
    void acceptanceTest_ClientCannotOverbookRoom() {
        // Criterio: Dado que existe una reserva para una habitación
        // Cuando otro cliente intenta reservar en las mismas fechas
        // Entonces el sistema rechaza la solicitud

        ReservationDto first = new ReservationDto();
        first.setGuestId(guestId);
        first.setRoomId(roomId);
        first.setCheckIn(LocalDateTime.now().plusDays(10));
        first.setCheckOut(LocalDateTime.now().plusDays(12));
        first.setGuestsCount(2);

        reservationService.create(first);

        // Intentar crear otra reserva que se superpone
        GuestEntity guest2 = new GuestEntity();
        guest2.setFirstName("Maria");
        guest2.setLastName("Garcia");
        guest2.setEmail("maria@example.com");
        guest2.setPhoneNumber("3002345678");
        GuestEntity savedGuest2 = guestRepository.save(guest2);

        ReservationDto overlapping = new ReservationDto();
        overlapping.setGuestId(savedGuest2.getId());
        overlapping.setRoomId(roomId);
        overlapping.setCheckIn(LocalDateTime.now().plusDays(11));
        overlapping.setCheckOut(LocalDateTime.now().plusDays(13));
        overlapping.setGuestsCount(2);

        assertThrows(Exception.class, () -> reservationService.create(overlapping));
    }

    @Test
    @DisplayName("AC7: El cliente no puede exceder la capacidad de la habitación")
    void acceptanceTest_ClientCannotExceedRoomCapacity() {
        // Criterio: Dado que una habitación tiene una capacidad máxima
        // Cuando se intenta reservar con más huéspedes
        // Entonces el sistema rechaza la solicitud

        ReservationDto exceeding = new ReservationDto();
        exceeding.setGuestId(guestId);
        exceeding.setRoomId(roomId);
        exceeding.setCheckIn(LocalDateTime.now().plusDays(1));
        exceeding.setCheckOut(LocalDateTime.now().plusDays(3));
        exceeding.setGuestsCount(10); // Más que el máximo de 6

        assertThrows(Exception.class, () -> reservationService.create(exceeding));
    }
}
