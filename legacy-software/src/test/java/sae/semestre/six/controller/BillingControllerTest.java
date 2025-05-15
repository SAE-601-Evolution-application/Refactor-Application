package sae.semestre.six.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sae.semestre.six.billing.Bill;
import sae.semestre.six.billing.BillDao;
import sae.semestre.six.billing.BillingController;
import sae.semestre.six.doctor.DoctorDao;
import sae.semestre.six.doctor.Doctor;
import sae.semestre.six.patient.PatientDao;
import sae.semestre.six.patient.Patient;
import sae.semestre.six.service.EmailService;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class BillingControllerTest {

    private BillingController billingController;
    private BillDao billDao;
    private PatientDao patientDao;
    private DoctorDao doctorDao;
    private EmailService emailService;

    @BeforeEach
    public void setUp() {
        billDao = mock(BillDao.class);
        patientDao = mock(PatientDao.class);
        doctorDao = mock(DoctorDao.class);
        emailService = mock(EmailService.class);

        billingController = BillingController.getInstance();

        // Inject mocks via reflection (since fields are private and autowired)
        injectDependencies();
    }

    private void injectDependencies() {
        try {
            var billDaoField = BillingController.class.getDeclaredField("billDao");
            billDaoField.setAccessible(true);
            billDaoField.set(billingController, billDao);

            var patientDaoField = BillingController.class.getDeclaredField("patientDao");
            patientDaoField.setAccessible(true);
            patientDaoField.set(billingController, patientDao);

            var doctorDaoField = BillingController.class.getDeclaredField("doctorDao");
            doctorDaoField.setAccessible(true);
            doctorDaoField.set(billingController, doctorDao);

            var emailServiceField = BillingController.class.getDeclaredField("emailService");
            emailServiceField.setAccessible(true);
            emailServiceField.set(billingController, emailService);
        } catch (Exception e) {
            throw new RuntimeException("Failed to inject dependencies", e);
        }
    }

    @Test
    public void testProcessBill_Success() {
        Patient patient = new Patient();
        Doctor doctor = new Doctor();

        when(patientDao.findById(1L)).thenReturn(patient);
        when(doctorDao.findById(1L)).thenReturn(doctor);

        String response = billingController.processBill("1", "1", new String[]{"CONSULTATION"});

        assertTrue(response.contains("Bill processed successfully"));
        verify(billDao, times(1)).save(any(Bill.class));
    }

    @Test
    public void testUpdatePriceAndGetPrices() {
        billingController.updatePrice("TEST_TREATMENT", 123.45);
        Map<String, Double> prices = billingController.getPrices();

        assertEquals(123.45, prices.get("TEST_TREATMENT"));
    }

    @Test
    public void testCalculateInsurance() {
        String result = billingController.calculateInsurance(1000);
        assertEquals("Insurance coverage: $1000.0", result);
    }

    @Test
    public void testGetPendingBills() {
        List<String> pending = billingController.getPendingBills();
        assertNotNull(pending);
    }
}
