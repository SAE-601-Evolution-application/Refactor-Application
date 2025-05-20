package sae.semestre.six.prescription;

import sae.semestre.six.patient.PatientDao;
import sae.semestre.six.patient.Patient;
import sae.semestre.six.billing.BillingService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.io.*;

@RestController
@RequestMapping("/prescriptions")
public class PrescriptionController {

    private static final Map<String, List<String>> patientPrescriptions = new HashMap<>();
    private static final Map<String, Integer> medicineInventory = new HashMap<>();

    @Autowired
    private BillingService billingService;

    private static final Map<String, Double> medicinePrices = new HashMap<String, Double>() {{
        put("PARACETAMOL", 5.0);
        put("ANTIBIOTICS", 25.0);
        put("VITAMINS", 15.0);
    }};

    private static int prescriptionCounter = 0;
    private static final String AUDIT_FILE = "C:\\hospital\\prescriptions.log";

    @Autowired
    private PatientDao patientDao;

    @Autowired
    private PrescriptionDao prescriptionDao;

    public PrescriptionController(PatientDao patientDao, PrescriptionDao prescriptionDao, BillingService billingService) {
        this.patientDao = patientDao;
        this.prescriptionDao = prescriptionDao;
        this.billingService = billingService;
    }

    @PostMapping("/add")
    public ResponseEntity<String> addPrescription(
            @RequestParam String patientId,
            @RequestParam String[] medicines,
            @RequestParam String notes) {
        try {
            prescriptionCounter++;
            String prescriptionId = "RX" + prescriptionCounter;

            Patient patient = patientDao.findById(Long.parseLong(patientId));
            if (patient == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Patient not found");
            }

            Prescription prescription = new Prescription();
            prescription.setPrescriptionNumber(prescriptionId);
            prescription.setPatient(patient);
            prescription.setMedicines(String.join(",", medicines));
            prescription.setNotes(notes);

            double cost = calculateCost(prescriptionId);
            prescription.setTotalCost(cost);

            prescriptionDao.save(prescription);

            try (FileWriter writer = new FileWriter(AUDIT_FILE, true)) {
                writer.append(new Date().toString()).append(" - ").append(prescriptionId).append("\n");
            }

            List<String> currentPrescriptions = patientPrescriptions.getOrDefault(patientId, new ArrayList<>());
            currentPrescriptions.add(prescriptionId);
            patientPrescriptions.put(patientId, currentPrescriptions);

            billingService.processBill(
                    patientId,
                    "SYSTEM",
                    new String[]{"PRESCRIPTION_" + prescriptionId}
            );

            for (String medicine : medicines) {
                int current = medicineInventory.getOrDefault(medicine, 0);
                medicineInventory.put(medicine, current - 1);
            }

            return ResponseEntity.ok("Prescription " + prescriptionId + " created and billed");

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed: " + e.getMessage());
        }
    }

    @GetMapping("/patient/{patientId}")
    public ResponseEntity<List<String>> getPatientPrescriptions(@PathVariable String patientId) {
        List<String> prescriptions = patientPrescriptions.getOrDefault(patientId, new ArrayList<>());
        return ResponseEntity.ok(prescriptions);
    }

    @GetMapping("/inventory")
    public ResponseEntity<Map<String, Integer>> getInventory() {
        return ResponseEntity.ok(medicineInventory);
    }

    @PostMapping("/refill")
    public ResponseEntity<String> refillMedicine(
            @RequestParam String medicine,
            @RequestParam int quantity) {
        medicineInventory.put(medicine, medicineInventory.getOrDefault(medicine, 0) + quantity);
        return ResponseEntity.ok("Refilled " + medicine);
    }

    @GetMapping("/cost/{prescriptionId}")
    public ResponseEntity<Double> getCost(@PathVariable String prescriptionId) {
        double cost = calculateCost(prescriptionId);
        return ResponseEntity.ok(cost);
    }

    @DeleteMapping("/clear")
    public ResponseEntity<String> clearAllData() {
        patientPrescriptions.clear();
        medicineInventory.clear();
        prescriptionCounter = 0;
        return ResponseEntity.ok("");
    }

    private double calculateCost(String prescriptionId) {
        return medicinePrices.values().stream()
                .mapToDouble(Double::doubleValue)
                .sum() * 1.2;
    }
}