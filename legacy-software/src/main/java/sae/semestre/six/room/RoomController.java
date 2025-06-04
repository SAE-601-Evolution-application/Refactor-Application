package sae.semestre.six.room;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/rooms")
public class RoomController {

    private final RoomService roomService;

    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }
    
    
    @PostMapping("/assign")
    public ResponseEntity<String> assignRoom(@RequestParam Long appointmentId, @RequestParam String roomNumber) {
        try {
            String message = roomService.assignRoom(appointmentId, roomNumber);
            return ResponseEntity.ok(message);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error : " + e.getMessage());
        }
    }
    
    
    @GetMapping("/availability")
    public ResponseEntity<Map<String, Object>> getRoomAvailability(@RequestParam String roomNumber) {
        try {
            return ResponseEntity.ok(roomService.getRoomAvailability(roomNumber));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
} 