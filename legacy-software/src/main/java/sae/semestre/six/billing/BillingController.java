package sae.semestre.six.billing;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sae.semestre.six.insurance.Insurance;
import sae.semestre.six.insurance.InsuranceRepository;

import java.util.*;

@RestController
@RequestMapping("/billing")
public class BillingController {

    private final BillingService billingService;
    private final InsuranceRepository insuranceRepository;
    private final List<String> pendingBills = new ArrayList<>();

    public BillingController(BillingService billingService, InsuranceRepository insuranceRepository) {
        this.billingService = billingService;
        this.insuranceRepository = insuranceRepository;
    }

    @PostMapping("/process")
    public ResponseEntity<String> processBill(
            @RequestParam String patientId,
            @RequestParam String doctorId,
            @RequestParam String[] prestations) {
        try {
            // Appel réel désactivé ici car non implémenté dans ton exemple
            // billingService.processBill(patientId, doctorId, prestations);
            return ResponseEntity.ok("Bill processed successfully");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/insurance")
    public ResponseEntity<String> calculateInsurance(
            @RequestParam double amount,
            @RequestParam int patientId) {
        try {
            Insurance insurance = insuranceRepository.findInsuranceByPatientId(patientId);
            if (insurance == null) {
                return ResponseEntity.status(404).body("No insurance found for this patient.");
            }
            if (!insurance.isValid()) {
                return ResponseEntity.ok("Insurance expired.");
            }
            double coverage = insurance.calculateCoverage(amount);
            return ResponseEntity.ok("Insurance coverage: $" + coverage);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/revenue")
    public ResponseEntity<String> getTotalRevenue() {
        try {
            double revenue = billingService.getTotalRevenue();
            return ResponseEntity.ok("Total Revenue: $" + revenue);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/pending")
    public ResponseEntity<List<String>> getPendingBills() {
        return ResponseEntity.ok(pendingBills);
    }
}
