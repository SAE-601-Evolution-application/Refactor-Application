package sae.semestre.six.controller;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import sae.semestre.six.patient.Patient;
import sae.semestre.six.patient.PatientDao;
import sae.semestre.six.prescription.PrescriptionController;
import sae.semestre.six.prescription.PrescriptionDao;
import sae.semestre.six.billing.BillingService;

import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class PrescriptionControllerTest {

    private PrescriptionController prescriptionController;

    private PatientDao patientDao;
    private PrescriptionDao prescriptionDao;
    private BillingService billingService;

    @Before
    public void setUp() {
        patientDao = mock(PatientDao.class);
        prescriptionDao = mock(PrescriptionDao.class);
        billingService = mock(BillingService.class);

        prescriptionController = new PrescriptionController(patientDao, prescriptionDao, billingService);

        Patient patient = new Patient();
        patient.setId(1L);
        when(patientDao.findById(1L)).thenReturn(patient);
    }

    @Test
    public void testAddAndRetrievePrescription() {
        // Appel de la méthode à tester
        String result = prescriptionController.addPrescription(
                "1",
                new String[]{"AMOXICILLINE"},
                "Test notes"
        );

        // Vérification du résultat
        assertTrue(result.contains("created"));

        // Récupération des prescriptions
        List<String> prescriptions = prescriptionController.getPatientPrescriptions("1");

        assertNotNull(prescriptions);
        assertFalse(prescriptions.isEmpty());
        assertTrue(prescriptions.get(0).startsWith("RX"));

        // Vérifie si billingService.processBill() a été appelé
        verify(billingService).processBill(eq("1"), eq("SYSTEM"), any());
    }



    @Test
    public void testInventory() {
        prescriptionController.refillMedicine("PARACETAMOL", 10);
        assertEquals(10, (int) prescriptionController.getInventory().get("PARACETAMOL"));
    }
    
    
    @Test
    public void testClearData() {
        prescriptionController.clearAllData();
        assertTrue(prescriptionController.getInventory().isEmpty());
    }
} 