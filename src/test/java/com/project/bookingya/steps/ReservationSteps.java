package com.project.bookingya.steps;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import io.cucumber.java.Given;
import io.cucumber.java.When;
import io.cucumber.java.Then;
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
public class ReservationSteps {

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private IReservationRepository reservationRepository;

    @Autowired
    private IRoomRepository roomRepository;

    @Autowired
    private IGuestRepository guestRepository;

    private ReservationDto currentReservationDto;
    private Reservation createdReservation;
    private Reservation queriedReservation;
    private List<Reservation> reservationsList;
    private UUID currentRoomId;
    private UUID currentGuestId;
    private Exception thrownException;

    @Given("que existe una habitación disponible")
    public void givenAvailableRoom() {
        RoomEntity room = new RoomEntity();
        room.setId(UUID.randomUUID());
        room.setName("Habitación Premium");
        room.setAvailable(true);
        room.setMaxGuests(4);
        room.setPricePerNight(100.0);
        
        RoomEntity savedRoom = roomRepository.save(room);
        currentRoomId = savedRoom.getId();
    }

    @Given("existe un huésped registrado")
    public void givenRegisteredGuest() {
        GuestEntity guest = new GuestEntity();
        guest.setId(UUID.randomUUID());
        guest.setFirstName("Carlos");
        guest.setLastName("Sanchez");
        guest.setEmail("carlos@example.com");
        guest.setPhoneNumber("3001234567");
        
        GuestEntity savedGuest = guestRepository.save(guest);
        currentGuestId = savedGuest.getId();
    }

    @When("creo una nueva reserva para los próximos días")
    public void whenCreatingNewReservation() {
        currentReservationDto = new ReservationDto();
        currentReservationDto.setGuestId(currentGuestId);
        currentReservationDto.setRoomId(currentRoomId);
        currentReservationDto.setCheckIn(LocalDateTime.now().plusDays(1));
        currentReservationDto.setCheckOut(LocalDateTime.now().plusDays(4));
        currentReservationDto.setGuestsCount(2);
        
        try {
            createdReservation = reservationService.create(currentReservationDto);
        } catch (Exception e) {
            thrownException = e;
        }
    }

    @Then("la reserva se crea exitosamente")
    public void thenReservationCreatedSuccessfully() {
        assertNull(thrownException, "No debería lanzar excepción");
        assertNotNull(createdReservation);
        assertNotNull(createdReservation.getId());
    }

    @Then("puedo obtener la reserva por su identificador")
    public void thenCanGetReservationById() {
        Reservation retrieved = reservationService.getById(createdReservation.getId());
        assertNotNull(retrieved);
        assertEquals(createdReservation.getId(), retrieved.getId());
    }

    @Given("que existe una reserva registrada en el sistema")
    public void givenExistingReservation() {
        givenAvailableRoom();
        givenRegisteredGuest();
        
        ReservationDto dto = new ReservationDto();
        dto.setGuestId(currentGuestId);
        dto.setRoomId(currentRoomId);
        dto.setCheckIn(LocalDateTime.now().plusDays(1));
        dto.setCheckOut(LocalDateTime.now().plusDays(3));
        dto.setGuestsCount(2);
        
        createdReservation = reservationService.create(dto);
    }

    @When("consulto la reserva por su identificador")
    public void whenQueryingReservationById() {
        queriedReservation = reservationService.getById(createdReservation.getId());
    }

    @Then("obtengo los datos correctos de la reserva")
    public void thenGetCorrectReservationData() {
        assertNotNull(queriedReservation);
        assertEquals(createdReservation.getId(), queriedReservation.getId());
    }

    @Then("los datos coinciden con lo que fue registrado")
    public void thenDataMatchesRegistered() {
        assertEquals(createdReservation.getGuestId(), queriedReservation.getGuestId());
        assertEquals(createdReservation.getRoomId(), queriedReservation.getRoomId());
    }

    @When("actualizo la cantidad de huéspedes")
    public void whenUpdatingGuestCount() {
        ReservationDto updateDto = new ReservationDto();
        updateDto.setGuestId(createdReservation.getGuestId());
        updateDto.setRoomId(createdReservation.getRoomId());
        updateDto.setCheckIn(createdReservation.getCheckIn());
        updateDto.setCheckOut(createdReservation.getCheckOut());
        updateDto.setGuestsCount(3);
        
        try {
            createdReservation = reservationService.update(updateDto, createdReservation.getId());
        } catch (Exception e) {
            thrownException = e;
        }
    }

    @Then("la reserva se actualiza correctamente")
    public void thenReservationUpdatedCorrectly() {
        assertNull(thrownException, "No debería lanzar excepción al actualizar");
        assertNotNull(createdReservation);
    }

    @Then("los nuevos datos están disponibles")
    public void thenNewDataIsAvailable() {
        Reservation updated = reservationService.getById(createdReservation.getId());
        assertEquals(3, updated.getGuestsCount());
    }

    @When("cancelo la reserva")
    public void whenCancelingReservation() {
        try {
            reservationService.delete(createdReservation.getId());
        } catch (Exception e) {
            thrownException = e;
        }
    }

    @Then("la reserva se elimina del sistema")
    public void thenReservationDeletedFromSystem() {
        assertNull(thrownException, "No debería lanzar excepción al eliminar");
    }

    @Then("no puedo consultarla nuevamente")
    public void thenCannotQueryDeletedReservation() {
        assertThrows(Exception.class, () -> reservationService.getById(createdReservation.getId()));
    }

    @Given("que existen varias reservas en el sistema")
    public void givenMultipleReservations() {
        // Crear primera reserva
        givenAvailableRoom();
        givenRegisteredGuest();
        
        ReservationDto dto1 = new ReservationDto();
        dto1.setGuestId(currentGuestId);
        dto1.setRoomId(currentRoomId);
        dto1.setCheckIn(LocalDateTime.now().plusDays(1));
        dto1.setCheckOut(LocalDateTime.now().plusDays(3));
        dto1.setGuestsCount(2);
        
        reservationService.create(dto1);
        
        // Crear segunda reserva
        givenAvailableRoom();
        
        ReservationDto dto2 = new ReservationDto();
        dto2.setGuestId(currentGuestId);
        dto2.setRoomId(currentRoomId);
        dto2.setCheckIn(LocalDateTime.now().plusDays(5));
        dto2.setCheckOut(LocalDateTime.now().plusDays(7));
        dto2.setGuestsCount(1);
        
        reservationService.create(dto2);
    }

    @When("solicito todas las reservas")
    public void whenRequestingAllReservations() {
        reservationsList = reservationService.getAll();
    }

    @Then("recibo una lista con todas las reservas")
    public void thenReceiveListOfReservations() {
        assertNotNull(reservationsList);
    }

    @Then("la lista contiene al menos dos registros")
    public void thenListContainsAtLeastTwoRecords() {
        assertTrue(reservationsList.size() >= 2);
    }
}
        RoomEntity room = new RoomEntity();
        room.setId(UUID.randomUUID());
        room.setName("Habitación Premium");
        room.setAvailable(true);
        room.setMaxGuests(4);
        room.setPricePerNight(100.0);
        
        RoomEntity savedRoom = roomRepository.save(room);
        currentRoomId = savedRoom.getId();
    }

    @Dado("existe un huésped registrado")
    public void givenRegisteredGuest() {
        GuestEntity guest = new GuestEntity();
        guest.setId(UUID.randomUUID());
        guest.setFirstName("Carlos");
        guest.setLastName("Sanchez");
        guest.setEmail("carlos@example.com");
        guest.setPhoneNumber("3001234567");
        
        GuestEntity savedGuest = guestRepository.save(guest);
        currentGuestId = savedGuest.getId();
    }

    @Cuando("creo una nueva reserva para los próximos días")
    public void whenCreatingNewReservation() {
        currentReservationDto = new ReservationDto();
        currentReservationDto.setGuestId(currentGuestId);
        currentReservationDto.setRoomId(currentRoomId);
        currentReservationDto.setCheckIn(LocalDateTime.now().plusDays(1));
        currentReservationDto.setCheckOut(LocalDateTime.now().plusDays(4));
        currentReservationDto.setGuestsCount(2);
        
        try {
            createdReservation = reservationService.create(currentReservationDto);
        } catch (Exception e) {
            thrownException = e;
        }
    }

    @Entonces("la reserva se crea exitosamente")
    public void thenReservationCreatedSuccessfully() {
        assertNull(thrownException, "No debería lanzar excepción");
        assertNotNull(createdReservation);
        assertNotNull(createdReservation.getId());
    }

    @Entonces("puedo obtener la reserva por su identificador")
    public void thenCanGetReservationById() {
        Reservation retrieved = reservationService.getById(createdReservation.getId());
        assertNotNull(retrieved);
        assertEquals(createdReservation.getId(), retrieved.getId());
    }

    @Dado("que existe una reserva registrada en el sistema")
    public void givenExistingReservation() {
        givenAvailableRoom();
        givenRegisteredGuest();
        
        ReservationDto dto = new ReservationDto();
        dto.setGuestId(currentGuestId);
        dto.setRoomId(currentRoomId);
        dto.setCheckIn(LocalDateTime.now().plusDays(1));
        dto.setCheckOut(LocalDateTime.now().plusDays(3));
        dto.setGuestsCount(2);
        
        createdReservation = reservationService.create(dto);
    }

    @Cuando("consulto la reserva por su identificador")
    public void whenQueryingReservationById() {
        queriedReservation = reservationService.getById(createdReservation.getId());
    }

    @Entonces("obtengo los datos correctos de la reserva")
    public void thenGetCorrectReservationData() {
        assertNotNull(queriedReservation);
        assertEquals(createdReservation.getId(), queriedReservation.getId());
    }

    @Entonces("los datos coinciden con lo que fue registrado")
    public void thenDataMatchesRegistered() {
        assertEquals(createdReservation.getGuestId(), queriedReservation.getGuestId());
        assertEquals(createdReservation.getRoomId(), queriedReservation.getRoomId());
    }

    @Cuando("actualizo la cantidad de huéspedes")
    public void whenUpdatingGuestCount() {
        ReservationDto updateDto = new ReservationDto();
        updateDto.setGuestId(createdReservation.getGuestId());
        updateDto.setRoomId(createdReservation.getRoomId());
        updateDto.setCheckIn(createdReservation.getCheckIn());
        updateDto.setCheckOut(createdReservation.getCheckOut());
        updateDto.setGuestsCount(3);
        
        try {
            createdReservation = reservationService.update(updateDto, createdReservation.getId());
        } catch (Exception e) {
            thrownException = e;
        }
    }

    @Entonces("la reserva se actualiza correctamente")
    public void thenReservationUpdatedCorrectly() {
        assertNull(thrownException, "No debería lanzar excepción al actualizar");
        assertNotNull(createdReservation);
    }

    @Entonces("los nuevos datos están disponibles")
    public void thenNewDataIsAvailable() {
        Reservation updated = reservationService.getById(createdReservation.getId());
        assertEquals(3, updated.getGuestsCount());
    }

    @Cuando("cancelo la reserva")
    public void whenCancelingReservation() {
        try {
            reservationService.delete(createdReservation.getId());
        } catch (Exception e) {
            thrownException = e;
        }
    }

    @Entonces("la reserva se elimina del sistema")
    public void thenReservationDeletedFromSystem() {
        assertNull(thrownException, "No debería lanzar excepción al eliminar");
    }

    @Entonces("no puedo consultarla nuevamente")
    public void thenCannotQueryDeletedReservation() {
        assertThrows(Exception.class, () -> reservationService.getById(createdReservation.getId()));
    }

    @Dado("que existen varias reservas en el sistema")
    public void givenMultipleReservations() {
        // Crear primera reserva
        givenAvailableRoom();
        givenRegisteredGuest();
        
        ReservationDto dto1 = new ReservationDto();
        dto1.setGuestId(currentGuestId);
        dto1.setRoomId(currentRoomId);
        dto1.setCheckIn(LocalDateTime.now().plusDays(1));
        dto1.setCheckOut(LocalDateTime.now().plusDays(3));
        dto1.setGuestsCount(2);
        
        reservationService.create(dto1);
        
        // Crear segunda reserva
        givenAvailableRoom();
        
        ReservationDto dto2 = new ReservationDto();
        dto2.setGuestId(currentGuestId);
        dto2.setRoomId(currentRoomId);
        dto2.setCheckIn(LocalDateTime.now().plusDays(5));
        dto2.setCheckOut(LocalDateTime.now().plusDays(7));
        dto2.setGuestsCount(1);
        
        reservationService.create(dto2);
    }

    @Cuando("solicito todas las reservas")
    public void whenRequestingAllReservations() {
        reservationsList = reservationService.getAll();
    }

    @Entonces("recibo una lista con todas las reservas")
    public void thenReceiveListOfReservations() {
        assertNotNull(reservationsList);
    }

    @Entonces("la lista contiene al menos dos registros")
    public void thenListContainsAtLeastTwoRecords() {
        assertTrue(reservationsList.size() >= 2);
    }
}
