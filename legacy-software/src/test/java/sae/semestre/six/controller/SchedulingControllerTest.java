package sae.semestre.six.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import sae.semestre.six.appointment.AppointmentDao;
import sae.semestre.six.appointment.Appointment;
import sae.semestre.six.doctor.Doctor;
import sae.semestre.six.doctor.DoctorDao;
import sae.semestre.six.scheduling.SchedulingController;
import sae.semestre.six.service.EmailService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class SchedulingControllerTest {

    @Mock
    private AppointmentDao appointmentDao;

    @Mock
    private DoctorDao doctorDao;

    @Mock
    private EmailService emailService = EmailService.getInstance();

    @InjectMocks
    private SchedulingController schedulingController;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testScheduleAppointmentSuccess() {
        Long doctorId = 1L;
        Long patientId = 2L;
        LocalDateTime appointmentDate = LocalDateTime.of(2025, 5, 15, 10,0);
        String subject = new String ("New Appointment Scheduled");
        String body = new String ("You have a new appointment on " + appointmentDate);

        Doctor mockDoctor = new Doctor();
        mockDoctor.setEmail("doc@example.com");

        when(doctorDao.findById(doctorId)).thenReturn(mockDoctor);
        when(appointmentDao.findByDoctorId(doctorId)).thenReturn(Collections.emptyList());
        doNothing().when(emailService).sendEmail(anyString(), anyString(), anyString());

        String result = schedulingController.scheduleAppointment(doctorId, patientId, appointmentDate);

        assertEquals("Appointment scheduled successfully", result);
        verify(emailService).sendEmail(mockDoctor.getEmail(), subject, body);
    }

    @Test
    public void testScheduleAppointment_AlreadyTaken() {
        Long doctorId = 1L;
        Long patientId = 2L;
        LocalDateTime appointmentDate = LocalDateTime.of(2025, 5, 15, 10,0);

        Doctor mockDoctor = new Doctor();
        mockDoctor.setEmail("doc@example.com");

        Appointment existing = new Appointment();
        existing.setAppointmentDate(appointmentDate);

        when(doctorDao.findById(doctorId)).thenReturn(mockDoctor);
        when(appointmentDao.findByDoctorId(doctorId)).thenReturn(List.of(existing));

        String result = schedulingController.scheduleAppointment(doctorId, patientId, appointmentDate);

        assertEquals("Doctor is not available at this time", result);
    }

    @Test
    public void testScheduleAppointment_OutOfHours() {
        Long doctorId = 1L;
        Long patientId = 2L;
        LocalDateTime earlyMorning = LocalDateTime.of(2025, 5, 15, 8,0); // before 9 AM

        Doctor mockDoctor = new Doctor();
        mockDoctor.setEmail("doc@example.com");

        when(doctorDao.findById(doctorId)).thenReturn(mockDoctor);
        when(appointmentDao.findByDoctorId(doctorId)).thenReturn(Collections.emptyList());

        String result = schedulingController.scheduleAppointment(doctorId, patientId, earlyMorning);

        assertEquals("Appointments only available between 9 AM and 5 PM", result);
    }

    @Test
    public void testGetAvailableSlots() {
        Long doctorId = 1L;
        LocalDate date = LocalDate.of(2025, 5, 15); // date without time
        Appointment takenSlot = new Appointment();
        takenSlot.setAppointmentDate(LocalDateTime.of(2025, 5, 15, 10,0)); // 10 AM is taken

        when(appointmentDao.findByDoctorId(doctorId)).thenReturn(List.of(takenSlot));

        List<LocalDateTime> slots = schedulingController.getAvailableSlots(doctorId, date);

        assertEquals(8, slots.size()); // from 9 to 17 = 9 slots, 1 taken
        assertFalse(slots.contains(LocalDateTime.of(2025, 5, 15, 10,0)));
        assertTrue(slots.contains(LocalDateTime.of(2025, 5, 15, 11,0)));
    }

    // Helper method to create Date
    private Date createDate(int year, int month, int day, int hour) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, month - 1); // 0-indexed
        cal.set(Calendar.DAY_OF_MONTH, day);
        cal.set(Calendar.HOUR_OF_DAY, hour);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }
}
