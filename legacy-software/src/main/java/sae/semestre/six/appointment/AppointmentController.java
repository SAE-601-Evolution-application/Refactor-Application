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
public class AppointmentController {

    @Autowired
    private AppointmentService appointmentService;

    @PostMapping("/appointment")
    public ResponseEntity<String> scheduleAppointment(
            @RequestParam Long doctorId,
            @RequestParam Long patientId,
            @RequestParam String roomNumber,
            @RequestParam LocalDateTime appointmentDate) {
        String response = appointmentService.scheduleAppointment(doctorId, patientId, roomNumber, appointmentDate);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
    
    
    @GetMapping("/available-slots")
    public ResponseEntity<List<LocalDateTime>> getDoctorAvailableSlots(@RequestParam Long doctorId, @RequestParam LocalDate date) {
        List<LocalDateTime> slots = appointmentService.getDoctorAvailableSlots(doctorId,date);
        return new ResponseEntity<>(slots,HttpStatus.OK);
    }

    @GetMapping("/room/available-slots")
    public ResponseEntity<List<LocalDateTime>> getRoomAvailableSlots(@RequestParam Long roomNumber, @RequestParam LocalDate date) {
        List<LocalDateTime> slots = appointmentService.getRoomAvailableSlots(roomNumber,date);
        return new ResponseEntity<>(slots, HttpStatus.OK);
    }
} 