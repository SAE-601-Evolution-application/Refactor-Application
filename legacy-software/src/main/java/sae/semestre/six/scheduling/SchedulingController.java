package sae.semestre.six.scheduling;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import sae.semestre.six.appointment.AppointmentDao;
import sae.semestre.six.doctor.DoctorDao;
import sae.semestre.six.appointment.Appointment;
import sae.semestre.six.doctor.Doctor;
import sae.semestre.six.service.EmailService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@RestController
@RequestMapping("/scheduling")
public class SchedulingController {
    
    @Autowired
    private AppointmentDao appointmentDao;
    
    @Autowired
    private DoctorDao doctorDao;

    private final EmailService emailService;

    @Autowired
    public SchedulingController(AppointmentDao appointmentDao, DoctorDao doctorDao, EmailService emailService) {
        this.appointmentDao = appointmentDao;
        this.doctorDao = doctorDao;
        this.emailService = emailService;
    }



    @PostMapping("/appointment")
    public String scheduleAppointment(
            @RequestParam Long doctorId,
            @RequestParam Long patientId,
            @RequestParam LocalDateTime appointmentDate) {
        try {
            Doctor doctor = doctorDao.findById(doctorId);
            
            
            List<Appointment> doctorAppointments = appointmentDao.findByDoctorId(doctorId);
            for (Appointment existing : doctorAppointments) {
                
                if (existing.getAppointmentDate().equals(appointmentDate)) {
                    return "Doctor is not available at this time";
                }
            }

            int hour = appointmentDate.getHour();
            if (hour < 9 || hour > 17) {
                return "Appointments only available between 9 AM and 5 PM";
            }
            
            
            emailService.sendEmail(
                doctor.getEmail(),
                "New Appointment Scheduled",
                "You have a new appointment on " + appointmentDate
            );
            
            return "Appointment scheduled successfully";
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }
    
    
    @GetMapping("/available-slots")
    public List<LocalDateTime> getAvailableSlots(@RequestParam Long doctorId, @RequestParam LocalDate date) {
        List<LocalDateTime> availableSlots = new ArrayList<>();
        for (int hour = 9; hour <= 17; hour++) {
            LocalDateTime slot = LocalDateTime.of(date, LocalTime.of(hour,0));
            boolean slotAvailable = true;
            for (Appointment app : appointmentDao.findByDoctorId(doctorId)) {
                if (app.getAppointmentDate().getHour() == hour) {
                    slotAvailable = false;
                    break;
                }
            }
            
            if (slotAvailable) {
                availableSlots.add(slot);
            }
        }
        
        return availableSlots;
    }
} 