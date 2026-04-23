# language: es
Característica: Gestión de Reservas de Habitaciones
  Como un cliente de la plataforma
  Quiero poder gestionar mis reservas
  Para tener un control completo de mis alojamientos

  Escenario: Crear una reserva exitosamente
    Dado que existe una habitación disponible
    Y existe un huésped registrado
    Cuando creo una nueva reserva para los próximos días
    Entonces la reserva se crea exitosamente
    Y puedo obtener la reserva por su identificador

  Escenario: Consultar una reserva existente
    Dado que existe una reserva registrada en el sistema
    Cuando consulto la reserva por su identificador
    Entonces obtengo los datos correctos de la reserva
    Y los datos coinciden con lo que fue registrado

  Escenario: Actualizar una reserva
    Dado que existe una reserva registrada en el sistema
    Cuando actualizo la cantidad de huéspedes
    Entonces la reserva se actualiza correctamente
    Y los nuevos datos están disponibles

  Escenario: Cancelar una reserva
    Dado que existe una reserva registrada en el sistema
    Cuando cancelo la reserva
    Entonces la reserva se elimina del sistema
    Y no puedo consultarla nuevamente

  Escenario: Consultar todas las reservas
    Dado que existen varias reservas en el sistema
    Cuando solicito todas las reservas
    Entonces recibo una lista con todas las reservas
    Y la lista contiene al menos dos registros
