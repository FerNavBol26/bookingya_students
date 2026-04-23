package com.project.bookingya.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;

import com.project.bookingya.dtos.ReservationDto;
import com.project.bookingya.entities.GuestEntity;
import com.project.bookingya.entities.ReservationEntity;
import com.project.bookingya.entities.RoomEntity;
import com.project.bookingya.exceptions.EntityNotExistsException;
import com.project.bookingya.models.Reservation;
import com.project.bookingya.repositories.IGuestRepository;
import com.project.bookingya.repositories.IReservationRepository;
import com.project.bookingya.repositories.IRoomRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas Unitarias del Servicio de Reservas")
class ReservationServiceTest {

    @Mock(lenient = true)
    private IReservationRepository reservationRepository;

    @Mock(lenient = true)
    private IRoomRepository roomRepository;

    @Mock(lenient = true)
    private IGuestRepository guestRepository;

    @Mock(lenient = true)
    private ModelMapper mapper;

    @InjectMocks
    private ReservationService reservationService;

    private UUID reservationId;
    private UUID roomId;
    private UUID guestId;
    private LocalDateTime checkIn;
    private LocalDateTime checkOut;

    @BeforeEach
    void setUp() {
        reservationId = UUID.randomUUID();
        roomId = UUID.randomUUID();
        guestId = UUID.randomUUID();
        checkIn = LocalDateTime.now().plusDays(1);
        checkOut = LocalDateTime.now().plusDays(3);
    }

    @Test
    @DisplayName("Crear una reserva exitosamente")
    void testCreateReservationSuccessfully() {
        // Arrange
        ReservationDto reservationDto = new ReservationDto();
        reservationDto.setGuestId(guestId);
        reservationDto.setRoomId(roomId);
        reservationDto.setCheckIn(checkIn);
        reservationDto.setCheckOut(checkOut);
        reservationDto.setGuestsCount(2);

        ReservationEntity entity = new ReservationEntity();
        entity.setId(reservationId);
        entity.setGuestId(guestId);
        entity.setRoomId(roomId);

        Reservation expectedReservation = new Reservation();
        expectedReservation.setId(reservationId);
        expectedReservation.setGuestId(guestId);
        expectedReservation.setRoomId(roomId);

        RoomEntity room = new RoomEntity();
        room.setId(roomId);
        room.setAvailable(true);
        room.setMaxGuests(4);

        // Mocking
        when(roomRepository.findById(roomId)).thenReturn(Optional.of(room));
        when(guestRepository.findById(guestId)).thenReturn(Optional.of(new GuestEntity()));
        when(reservationRepository.existsOverlappingReservationForRoom(any(), any(), any(), any())).thenReturn(false);
        when(reservationRepository.existsOverlappingReservationForGuest(any(), any(), any(), any())).thenReturn(false);
        when(mapper.map(reservationDto, ReservationEntity.class)).thenReturn(entity);
        when(reservationRepository.saveAndFlush(entity)).thenReturn(entity);
        when(mapper.map(entity, Reservation.class)).thenReturn(expectedReservation);

        // Act
        Reservation result = reservationService.create(reservationDto);

        // Assert
        assertNotNull(result);
        assertEquals(reservationId, result.getId());
        assertEquals(guestId, result.getGuestId());
        assertEquals(roomId, result.getRoomId());
        verify(reservationRepository, times(1)).saveAndFlush(any());
    }

    @Test
    @DisplayName("Obtener una reserva por ID")
    void testGetReservationById() {
        // Arrange
        ReservationEntity entity = new ReservationEntity();
        entity.setId(reservationId);
        entity.setGuestId(guestId);
        entity.setRoomId(roomId);

        Reservation expected = new Reservation();
        expected.setId(reservationId);
        expected.setGuestId(guestId);
        expected.setRoomId(roomId);

        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(entity));
        when(mapper.map(entity, Reservation.class)).thenReturn(expected);

        // Act
        Reservation result = reservationService.getById(reservationId);

        // Assert
        assertNotNull(result);
        assertEquals(reservationId, result.getId());
        assertEquals(guestId, result.getGuestId());
        verify(reservationRepository, times(1)).findById(reservationId);
    }

    @Test
    @DisplayName("Lanzar excepción cuando no existe la reserva")
    void testGetReservationByIdNotFound() {
        // Arrange
        when(reservationRepository.findById(reservationId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotExistsException.class, () -> reservationService.getById(reservationId));
        verify(reservationRepository, times(1)).findById(reservationId);
    }

    @Test
    @DisplayName("Obtener todas las reservas")
    void testGetAllReservations() {
        // Arrange
        ReservationEntity entity1 = new ReservationEntity();
        entity1.setId(reservationId);
        ReservationEntity entity2 = new ReservationEntity();
        entity2.setId(UUID.randomUUID());

        Reservation res1 = new Reservation();
        res1.setId(reservationId);
        Reservation res2 = new Reservation();
        res2.setId(entity2.getId());

        when(reservationRepository.findAll()).thenReturn(Arrays.asList(entity1, entity2));
        when(mapper.map(any(List.class), any(Type.class))).thenAnswer(invocation -> {
            // Simply return the list of expected results
            return Arrays.asList(res1, res2);
        });

        // Act
        List<Reservation> result = reservationService.getAll();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(reservationRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Actualizar una reserva existente")
    void testUpdateReservationSuccessfully() {
        // Arrange
        ReservationDto updateDto = new ReservationDto();
        updateDto.setGuestId(guestId);
        updateDto.setRoomId(roomId);
        updateDto.setCheckIn(checkIn);
        updateDto.setCheckOut(checkOut);
        updateDto.setGuestsCount(3);

        ReservationEntity existing = new ReservationEntity();
        existing.setId(reservationId);
        existing.setGuestId(guestId);
        existing.setRoomId(roomId);

        Reservation expectedResult = new Reservation();
        expectedResult.setId(reservationId);
        expectedResult.setGuestId(guestId);
        expectedResult.setGuestsCount(3);

        RoomEntity room = new RoomEntity();
        room.setId(roomId);
        room.setAvailable(true);
        room.setMaxGuests(4);

        // Mocking
        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(existing));
        when(roomRepository.findById(roomId)).thenReturn(Optional.of(room));
        when(guestRepository.findById(guestId)).thenReturn(Optional.of(new GuestEntity()));
        when(reservationRepository.existsOverlappingReservationForRoom(any(), any(), any(), any())).thenReturn(false);
        when(reservationRepository.existsOverlappingReservationForGuest(any(), any(), any(), any())).thenReturn(false);
        // mapper.map with void return - use doAnswer to handle it
        doAnswer(invocation -> {
            ReservationDto dto = invocation.getArgument(0);
            ReservationEntity entity = invocation.getArgument(1);
            entity.setGuestsCount(dto.getGuestsCount());
            return null;
        }).when(mapper).map(any(ReservationDto.class), any(ReservationEntity.class));
        when(reservationRepository.saveAndFlush(existing)).thenReturn(existing);
        when(mapper.map(existing, Reservation.class)).thenReturn(expectedResult);

        // Act
        Reservation result = reservationService.update(updateDto, reservationId);

        // Assert
        assertNotNull(result);
        assertEquals(reservationId, result.getId());
        verify(reservationRepository, times(1)).saveAndFlush(any());
    }

    @Test
    @DisplayName("Eliminar una reserva")
    void testDeleteReservationSuccessfully() {
        // Arrange
        ReservationEntity entity = new ReservationEntity();
        entity.setId(reservationId);

        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(entity));
        doNothing().when(reservationRepository).delete(entity);
        doNothing().when(reservationRepository).flush();

        // Act
        assertDoesNotThrow(() -> reservationService.delete(reservationId));

        // Assert
        verify(reservationRepository, times(1)).findById(reservationId);
        verify(reservationRepository, times(1)).delete(entity);
        verify(reservationRepository, times(1)).flush();
    }

    @Test
    @DisplayName("Lanzar excepción al eliminar reserva inexistente")
    void testDeleteReservationNotFound() {
        // Arrange
        when(reservationRepository.findById(reservationId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotExistsException.class, () -> reservationService.delete(reservationId));
        verify(reservationRepository, times(1)).findById(reservationId);
        verify(reservationRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Obtener reservas por huésped")
    void testGetReservationsByGuestId() {
        // Arrange
        ReservationEntity entity = new ReservationEntity();
        entity.setId(reservationId);
        entity.setGuestId(guestId);

        Reservation reservation = new Reservation();
        reservation.setId(reservationId);
        reservation.setGuestId(guestId);

        when(reservationRepository.findByGuestId(guestId)).thenReturn(Arrays.asList(entity));
        when(mapper.map(any(List.class), any(Type.class))).thenAnswer(invocation -> {
            // Simply return the list of expected results
            return Arrays.asList(reservation);
        });

        // Act
        List<Reservation> result = reservationService.getByGuestId(guestId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(guestId, result.get(0).getGuestId());
        verify(reservationRepository, times(1)).findByGuestId(guestId);
    }
}
