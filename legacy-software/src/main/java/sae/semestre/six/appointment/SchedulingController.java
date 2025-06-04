package sae.semestre.six.appointment;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/scheduling")
public class SchedulingController {

    @Autowired
    private SchedulingService schedulingService;

    @PostMapping("/appointment")
    public ResponseEntity<String> scheduleAppointment(
            @RequestParam Long doctorId,
            @RequestParam Long patientId,
            @RequestParam LocalDateTime appointmentDate) {
        String response = schedulingService.scheduleAppointment(doctorId,patientId,appointmentDate);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
    
    
    @GetMapping("/available-slots")
    public List<LocalDateTime> getAvailableSlots(@RequestParam Long doctorId, @RequestParam LocalDate date) {
        return schedulingService.getPatientAvailableSlots(doctorId,date);
    }
} 