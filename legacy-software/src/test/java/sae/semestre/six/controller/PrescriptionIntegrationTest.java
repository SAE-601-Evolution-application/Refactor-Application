package sae.semestre.six.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import sae.semestre.six.billing.BillingService;
import sae.semestre.six.patient.Patient;
import sae.semestre.six.patient.PatientDao;
import sae.semestre.six.prescription.PrescriptionController;
import sae.semestre.six.prescription.PrescriptionDao;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class PrescriptionIntegrationTest {

    private PrescriptionController prescriptionController;
    private PatientDao patientDao;
    private PrescriptionDao prescriptionDao;
    private BillingService billingService;

    @BeforeEach
    public void setUp() {
        patientDao = mock(PatientDao.class);
        prescriptionDao = mock(PrescriptionDao.class);
        billingService = mock(BillingService.class);

        prescriptionController = new PrescriptionController(patientDao, prescriptionDao, billingService);

        Patient patient = new Patient();
        patient.setId(1L);
        when(patientDao.findById(1L)).thenReturn(patient);
    }

    // ----------------------
    // Test de addPrescription
    // ----------------------
    @Test
    public void testAddPrescriptionSuccess() {
        ResponseEntity<String> response = prescriptionController.addPrescription(
                "1",
                new String[]{"AMOXICILLINE"},
                "Test notes"
        );

        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("created"));

        verify(billingService).processBill(eq("1"), eq("SYSTEM"), any());
    }

    @Test
    public void testAddPrescriptionWithUnknownPatient() {
        when(patientDao.findById(-1L)).thenReturn(null);

        ResponseEntity<String> response = prescriptionController.addPrescription(
                "-1",
                new String[]{"VITAMINS"},
                "Note"
        );

        assertEquals(404, response.getStatusCodeValue());
        assertEquals("Patient not found", response.getBody());
    }

    // ----------------------
    // Test de getPatientPrescriptions
    // ----------------------
    @Test
    public void testGetPatientPrescriptions() {
        // Ajout d'une prescription
        prescriptionController.addPrescription("1", new String[]{"PARACETAMOL"}, "Notes");

        ResponseEntity<List<String>> response = prescriptionController.getPatientPrescriptions("1");
        assertEquals(200, response.getStatusCodeValue());

        List<String> prescriptions = response.getBody();
        assertNotNull(prescriptions);
        assertFalse(prescriptions.isEmpty());
        assertTrue(prescriptions.get(0).startsWith("RX"));
    }

    // ----------------------
    // Test de refillMedicine
    // ----------------------
    @Test
    public void testRefillMedicine() {
        ResponseEntity<String> response = prescriptionController.refillMedicine("PARACETAMOL", 10);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals("Refilled PARACETAMOL", response.getBody());
    }

    // ----------------------
    // Test de getInventory
    // ----------------------
    @Test
    public void testGetInventoryAfterRefill() {
        prescriptionController.refillMedicine("PARACETAMOL", 10);

        ResponseEntity<Map<String, Integer>> response = prescriptionController.getInventory();
        assertEquals(200, response.getStatusCodeValue());

        Map<String, Integer> inventory = response.getBody();
        assertNotNull(inventory);
        assertEquals(10, inventory.getOrDefault("PARACETAMOL", 0).intValue());
    }

    // ----------------------
    // Test de getCost
    // ----------------------
    @Test
    public void testGetCost() {
        ResponseEntity<Double> response = prescriptionController.getCost("RX1");
        assertEquals(200, response.getStatusCodeValue());

        Double cost = response.getBody();
        assertNotNull(cost);
        assertEquals(54.0, cost, 0.01); // 45.0 * 1.2
    }

    // ----------------------
    // Test de clearAllData
    // ----------------------
    @Test
    public void testClearAllData() {
        // Ajoute un peu d'inventaire pour tester le reset
        prescriptionController.refillMedicine("VITAMINS", 5);

        ResponseEntity<String> response = prescriptionController.clearAllData();
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals("", response.getBody());

        ResponseEntity<Map<String, Integer>> inventoryResponse = prescriptionController.getInventory();
        assertTrue(inventoryResponse.getBody().isEmpty());
    }
}
