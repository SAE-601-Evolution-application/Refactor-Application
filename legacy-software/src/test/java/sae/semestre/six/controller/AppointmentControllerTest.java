package sae.semestre.six.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import sae.semestre.six.appointment.AppointmentDao;
import sae.semestre.six.appointment.Appointment;
import sae.semestre.six.doctor.Doctor;
import sae.semestre.six.doctor.DoctorDao;
import sae.semestre.six.patient.Patient;
import sae.semestre.six.patient.PatientDao;
import sae.semestre.six.room.Room;
import sae.semestre.six.room.RoomDao;
import sae.semestre.six.appointment.AppointmentService;
import sae.semestre.six.service.EmailService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class AppointmentControllerTest {

    @Mock
    private AppointmentDao appointmentDao;

    @Mock
    private DoctorDao doctorDao;

    @Mock
    private PatientDao patientDao;

    @Mock
    private RoomDao roomDao;

    @Mock
    private EmailService emailService = EmailService.getInstance();

    @InjectMocks
    private AppointmentService appointmentService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testScheduleAppointmentSuccess() {
        Long doctorId = 1L;
        Long patientId = 2L;
        Long roomId = 1L;
        String roomNumber = "A202";
        LocalDateTime appointmentDate = LocalDateTime.of(2025, 5, 15, 10,0);
        String subject = new String ("New Appointment Scheduled");
        String body = new String ("You have a new appointment on " + appointmentDate);

        Doctor mockDoctor = new Doctor();
        mockDoctor.setEmail("doc@example.com");

        Patient mockPatient = new Patient();
        mockPatient.setEmail("pat@example.com");

        Room room = new Room();
        room.setId(roomId);

        when(doctorDao.findById(doctorId)).thenReturn(mockDoctor);
        when(patientDao.findById(patientId)).thenReturn(mockPatient);
        when(roomDao.findByRoomNumber(roomNumber)).thenReturn(room);
        when(appointmentDao.findByDoctorId(doctorId)).thenReturn(Collections.emptyList());
        when(appointmentDao.findByRoomId(roomId)).thenReturn(Collections.emptyList());
        doNothing().when(emailService).sendAppointmentMail(anyString(),anyString(), anyString(), anyString());

        String result = appointmentService.scheduleAppointment(doctorId, patientId, roomNumber,appointmentDate);

        assertEquals("Appointment scheduled successfully", result);
        verify(emailService).sendAppointmentMail(mockPatient.getEmail(),mockDoctor.getEmail(), subject, body);
    }

    @Test
    public void testScheduleAppointment_AlreadyTaken() {
        Long doctorId = 1L;
        Long patientId = 2L;
        Long roomId = 1L;
        String roomNumber = "A202";
        LocalDateTime appointmentDate = LocalDateTime.of(2025, 5, 15, 10,0);

        Doctor mockDoctor = new Doctor();
        mockDoctor.setEmail("doc@example.com");

        Patient mockPatient = new Patient();

        Appointment existing = new Appointment();
        existing.setAppointmentDate(appointmentDate);

        Room room = new Room();
        room.setRoomNumber("A202");

        when(doctorDao.findById(doctorId)).thenReturn(mockDoctor);
        when(patientDao.findById(patientId)).thenReturn(mockPatient);
        when(appointmentDao.findByDoctorId(doctorId)).thenReturn(List.of(existing));
        when(appointmentDao.findByRoomId(roomId)).thenReturn(Collections.emptyList());
        when(appointmentDao.findMaxId()).thenReturn(0L);

        String result = appointmentService.scheduleAppointment(doctorId, patientId, roomNumber,appointmentDate);

        assertEquals("Doctor is not available at this time", result);
    }

    @Test
    public void testScheduleAppointment_OutOfHours() {
        Long doctorId = 1L;
        Long patientId = 2L;
        Long roomId = 1L;
        String roomNumber = "A202";
        LocalDateTime earlyMorning = LocalDateTime.of(2025, 5, 15, 8,0); // before 9 AM

        Doctor mockDoctor = new Doctor();
        mockDoctor.setEmail("doc@example.com");

        Patient mockPatient = new Patient();

        Room room = new Room();
        room.setId(roomId);

        when(doctorDao.findById(doctorId)).thenReturn(mockDoctor);
        when(patientDao.findById(patientId)).thenReturn(mockPatient);
        when(roomDao.findByRoomNumber(roomNumber)).thenReturn(room);
        when(appointmentDao.findByDoctorId(doctorId)).thenReturn(Collections.emptyList());
        when(appointmentDao.findByRoomId(roomId)).thenReturn(Collections.emptyList());

        String result = appointmentService.scheduleAppointment(doctorId, patientId, roomNumber,earlyMorning);

        assertEquals("Appointments only available between 9 AM and 5 PM", result);
    }

    @Test
    public void testGetAvailableSlots() {
        Long doctorId = 1L;
        LocalDate date = LocalDate.of(2025, 5, 15); // date without time
        Appointment takenSlot = new Appointment();
        takenSlot.setAppointmentDate(LocalDateTime.of(2025, 5, 15, 10,0)); // 10 AM is taken

        when(appointmentDao.findByDoctorId(doctorId)).thenReturn(List.of(takenSlot));

        List<LocalDateTime> slots = appointmentService.getDoctorAvailableSlots(doctorId, date);

        assertEquals(8, slots.size()); // from 9 to 17 = 9 slots, 1 taken
        assertFalse(slots.contains(LocalDateTime.of(2025, 5, 15, 10,0)));
        assertTrue(slots.contains(LocalDateTime.of(2025, 5, 15, 11,0)));
    }
}
