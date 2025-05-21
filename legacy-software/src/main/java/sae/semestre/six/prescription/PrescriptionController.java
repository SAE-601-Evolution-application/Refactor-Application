package sae.semestre.six.prescription;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/prescriptions")
public class PrescriptionController {

    @Autowired
    private PrescriptionService prescriptionService;

    @PostMapping("/add")
    public ResponseEntity<String> addPrescription(
            @RequestParam String patientId,
            @RequestParam String[] medicines,
            @RequestParam String notes) {
        try {
            String prescriptionId = prescriptionService.addPrescription(patientId, medicines, notes);
            return ResponseEntity.ok("Prescription " + prescriptionId + " created and billed");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Error: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed: " + e.getMessage());
        }
    }

    @GetMapping("/patient/{patientId}")
    public ResponseEntity<List<String>> getPatientPrescriptions(@PathVariable String patientId) {
        List<String> prescriptions = prescriptionService.getPrescriptions(patientId);
        return ResponseEntity.ok(prescriptions);
    }

    @GetMapping("/inventory")
    public ResponseEntity<Map<String, Integer>> getInventory() {
        return ResponseEntity.ok(prescriptionService.getInventory());
    }

    @PostMapping("/refill")
    public ResponseEntity<String> refillMedicine(
            @RequestParam String medicine,
            @RequestParam int quantity) {
        prescriptionService.refillMedicine(medicine, quantity);
        return ResponseEntity.ok("Refilled " + quantity + " units of " + medicine);
    }

    @GetMapping("/cost/{prescriptionId}")
    public ResponseEntity<Double> getCost(@PathVariable String prescriptionId) {
        double cost = prescriptionService.getCost(prescriptionId);
        return ResponseEntity.ok(cost);
    }

    @DeleteMapping("/clear")
    public ResponseEntity<String> clearAllData() {
        prescriptionService.clearAllData();
        return ResponseEntity.ok("All data cleared");
    }
}
