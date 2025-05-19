package sae.semestre.six.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import sae.semestre.six.billing.BillDao;
import sae.semestre.six.billing.BillingController;
import sae.semestre.six.doctor.DoctorDao;
import sae.semestre.six.patient.PatientDao;
import sae.semestre.six.service.EmailService;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
                properties = "spring.config.name=application-test")
@AutoConfigureMockMvc
public class BillingIntegrationTest {

    private BillingController billingController;
    private BillDao billDao;
    private PatientDao patientDao;
    private DoctorDao doctorDao;
    private EmailService emailService;

    @Autowired
    private MockMvc mockMvc;

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
    void testProcessBillEndpoint_success() throws Exception {
        mockMvc.perform(post("/billing/process")
                        .param("patientId", "1")
                        .param("doctorId", "1")
                        .param("treatments", "CONSULTATION"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Bill processed successfully")));
    }

    @Test
    void testProcessBill_invalidPatient() throws Exception {
        mockMvc.perform(post("/billing/process")
                        .param("patientId", "9999")  // Patient inexistant
                        .param("doctorId", "1")
                        .param("treatments", "CONSULTATION"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Error")));
    }

    @Test
    void testProcessBill_invalidDoctor() throws Exception {
        mockMvc.perform(post("/billing/process")
                        .param("patientId", "1") // Patient existant
                        .param("doctorId", "9999")  // Docteur inexistant
                        .param("treatments", "CONSULTATION"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Error")));
    }

    @Test
    public void testUpdatePriceAndGetPrices() {
        billingController.updatePrice("TEST_TREATMENT", 123.45);
        Map<String, Double> prices = billingController.getPrices();

        assertEquals(123.45, prices.get("TEST_TREATMENT"));
    }


    @Test
    void testCalculateInsuranceEndpoint() throws Exception {
        mockMvc.perform(get("/billing/insurance")
                        .param("amount", "500"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Insurance coverage")));
    }

    @Test
    void testGetPrices() throws Exception {
        mockMvc.perform(get("/billing/prices"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.CONSULTATION").isNumber());
    }

    @Test
    void testUpdatePrice() throws Exception {
        mockMvc.perform(put("/billing/price")
                        .param("treatment", "TEST_TREATMENT")
                        .param("price", "123.45"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("updated")));
    }

    @Test
    void testGetPendingBills() throws Exception {
        mockMvc.perform(get("/billing/pending"))
                .andExpect(status().isOk());
    }
}
