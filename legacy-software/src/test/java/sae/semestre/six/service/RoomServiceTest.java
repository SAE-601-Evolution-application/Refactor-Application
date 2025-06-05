package sae.semestre.six.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sae.semestre.six.appointment.Appointment;
import sae.semestre.six.appointment.AppointmentDao;
import sae.semestre.six.doctor.Doctor;
import sae.semestre.six.room.Room;
import sae.semestre.six.room.RoomDao;
import sae.semestre.six.room.RoomService;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class RoomServiceTest {

    private RoomDao roomDao;
    private AppointmentDao appointmentDao;
    private RoomService roomService;

    @BeforeEach
    void setUp() {
        roomDao = mock(RoomDao.class);
        appointmentDao = mock(AppointmentDao.class);
        roomService = new RoomService(roomDao, appointmentDao);
    }

    @Test
    void assignRoom_success() {
        Room room = new Room();
        room.setRoomNumber("R101");
        room.setCapacity(2);
        room.setCurrentPatientCount(1);
        room.setType("NORMAL");

        Doctor doctor = new Doctor();
        doctor.setSpecialization("GENERAL");

        Appointment appointment = new Appointment();
        appointment.setId(1L);
        appointment.setDoctor(doctor);

        when(roomDao.findByRoomNumber("R101")).thenReturn(room);
        when(appointmentDao.findById(1L)).thenReturn(appointment);

        String result = roomService.assignRoom(1L, "R101");

        assertEquals("Room assigned successfully", result);
        assertEquals(2, room.getCurrentPatientCount());
        assertEquals("R101", appointment.getRoomNumber());
        verify(roomDao).update(room);
        verify(appointmentDao).update(appointment);
    }

    @Test
    void assignRoom_roomNotFound_throwsException() {
        when(roomDao.findByRoomNumber("R999")).thenReturn(null);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> roomService.assignRoom(1L, "R999"));

        assertEquals("Room or appointment not found", exception.getMessage());
    }

    @Test
    void assignRoom_appointmentNotFound_throwsException() {
        Room room = new Room();
        room.setRoomNumber("R101");
        when(roomDao.findByRoomNumber("R101")).thenReturn(room);
        when(appointmentDao.findById(1L)).thenReturn(null);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> roomService.assignRoom(1L, "R101"));

        assertEquals("Room or appointment not found", exception.getMessage());
    }

    @Test
    void assignRoom_surgeryRoomButDoctorNotSurgeon_throwsException() {
        Room room = new Room();
        room.setRoomNumber("R202");
        room.setType("SURGERY");

        Doctor doctor = new Doctor();
        doctor.setSpecialization("General");

        Appointment appointment = new Appointment();
        appointment.setDoctor(doctor);

        when(roomDao.findByRoomNumber("R202")).thenReturn(room);
        when(appointmentDao.findById(1L)).thenReturn(appointment);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> roomService.assignRoom(1L, "R202"));

        assertEquals("Only surgeons can use surgery rooms", exception.getMessage());
    }

    @Test
    void assignRoom_roomFull_throwsException() {
        Room room = new Room();
        room.setRoomNumber("R103");
        room.setCapacity(2);
        room.setCurrentPatientCount(2);
        room.setType("NORMAL");

        Doctor doctor = new Doctor();
        doctor.setSpecialization("GENERAL");

        Appointment appointment = new Appointment();
        appointment.setDoctor(doctor);

        when(roomDao.findByRoomNumber("R103")).thenReturn(room);
        when(appointmentDao.findById(1L)).thenReturn(appointment);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> roomService.assignRoom(1L, "R103"));

        assertEquals("Room is at full capacity", exception.getMessage());
    }

    @Test
    void getRoomAvailability_success() {
        Room room = new Room();
        room.setRoomNumber("R200");
        room.setCapacity(3);
        room.setCurrentPatientCount(1);
        room.setType("NORMAL");

        when(roomDao.findByRoomNumber("R200")).thenReturn(room);

        Map<String, Object> result = roomService.getRoomAvailability("R200");

        assertEquals("R200", result.get("roomNumber"));
        assertEquals(3, result.get("capacity"));
        assertEquals(1, result.get("currentPatients"));
        assertTrue((Boolean) result.get("available"));
    }

    @Test
    void getRoomAvailability_roomNotFound_throwsException() {
        when(roomDao.findByRoomNumber("UNKNOWN")).thenReturn(null);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> roomService.getRoomAvailability("UNKNOWN"));

        assertEquals("Room not found", exception.getMessage());
    }
}
