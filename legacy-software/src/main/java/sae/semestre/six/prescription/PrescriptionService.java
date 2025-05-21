package sae.semestre.six.prescription;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import sae.semestre.six.billing.BillingService;
import sae.semestre.six.patient.Patient;
import sae.semestre.six.patient.PatientDao;

import java.io.FileWriter;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class PrescriptionService {

    private static final Map<String, List<String>> patientPrescriptions = new ConcurrentHashMap<>();
    private static final Map<String, Integer> medicineInventory = new ConcurrentHashMap<>();

    private static final Map<String, Double> medicinePrices = new HashMap<String, Double>() {{
        put("PARACETAMOL", 5.0);
        put("ANTIBIOTICS", 25.0);
        put("VITAMINS", 15.0);
    }};

    private static int prescriptionCounter = 0;

    private final PatientDao patientDao;
    private final PrescriptionDao prescriptionDao;
    private final BillingService billingService;

    @Value("${spring.hospital.prescriptions.path}")
    private String prescriptionsFilePath;

    @Autowired
    public PrescriptionService(PatientDao patientDao, PrescriptionDao prescriptionDao, BillingService billingService) {
        this.patientDao = patientDao;
        this.prescriptionDao = prescriptionDao;
        this.billingService = billingService;
    }

    public String addPrescription(String patientId, String[] medicines, String notes) throws Exception {
        prescriptionCounter++;
        String prescriptionId = "RX" + prescriptionCounter;

        Patient patient = patientDao.findById(Long.parseLong(patientId));
        if (patient == null) {
            throw new IllegalArgumentException("Patient not found");
        }

        Prescription prescription = new Prescription();
        prescription.setPrescriptionNumber(prescriptionId);
        prescription.setPatient(patient);
        prescription.setMedicines(String.join(",", medicines));
        prescription.setNotes(notes);

        double cost = calculateCost(medicines);
        prescription.setTotalCost(cost);
        prescriptionDao.save(prescription);

        // Audit log
        try (FileWriter writer = new FileWriter(prescriptionsFilePath, true)) {
            writer.append(new Date().toString())
                    .append(" - ")
                    .append(prescriptionId)
                    .append(System.lineSeparator());
        }

        // Update in-memory structures
        patientPrescriptions
                .computeIfAbsent(patientId, k -> new ArrayList<>())
                .add(prescriptionId);

        billingService.processBill(
                patientId,
                "SYSTEM",
                new String[]{"PRESCRIPTION_" + prescriptionId}
        );

        for (String medicine : medicines) {
            medicineInventory.compute(medicine, (k, v) -> (v == null) ? -1 : v - 1);
        }

        return prescriptionId;
    }

    public List<String> getPrescriptions(String patientId) {
        return patientPrescriptions.getOrDefault(patientId, new ArrayList<>());
    }

    public Map<String, Integer> getInventory() {
        return new HashMap<>(medicineInventory);
    }

    public void refillMedicine(String medicine, int quantity) {
        medicineInventory.merge(medicine, quantity, Integer::sum);
    }

    public double getCost(String prescriptionId) {
        return medicinePrices.values().stream()
                .mapToDouble(Double::doubleValue)
                .sum() * 1.2; // simulate cost logic
    }

    public void clearAllData() {
        patientPrescriptions.clear();
        medicineInventory.clear();
        prescriptionCounter = 0;
    }

    private double calculateCost(String[] medicines) {
        return Arrays.stream(medicines)
                .map(String::toUpperCase)
                .mapToDouble(med -> medicinePrices.getOrDefault(med, 10.0)) // Default price fallback
                .sum() * 1.2; // Apply a tax factor
    }
}
