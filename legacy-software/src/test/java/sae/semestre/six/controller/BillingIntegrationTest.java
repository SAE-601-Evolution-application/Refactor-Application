package sae.semestre.six.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import sae.semestre.six.billing.BillingController;
import sae.semestre.six.billing.BillingService;
import sae.semestre.six.insurance.Insurance;
import sae.semestre.six.insurance.InsuranceRepository;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class BillingIntegrationTest {

    private MockMvc mockMvc;

    private BillingService billingService;
    private InsuranceRepository insuranceRepository;

    @BeforeEach
    public void setup() {
        billingService = mock(BillingService.class);
        insuranceRepository = mock(InsuranceRepository.class);
        BillingController billingController = new BillingController(billingService, insuranceRepository);
        mockMvc = MockMvcBuilders.standaloneSetup(billingController).build();
    }

    @Test
    void testProcessBill_success() throws Exception {
        mockMvc.perform(post("/billing/process")
                        .param("patientId", "123")
                        .param("doctorId", "456")
                        .param("prestations", "Consultation", "Scan"))
                .andExpect(status().isOk())
                .andExpect(content().string("Bill processed successfully"));
    }

    @Test
    void testCalculateInsurance_withValidInsurance() throws Exception {
        Insurance mockInsurance = mock(Insurance.class);
        when(insuranceRepository.findInsuranceByPatientId(1)).thenReturn(mockInsurance);
        when(mockInsurance.isValid()).thenReturn(true);
        when(mockInsurance.calculateCoverage(200.0)).thenReturn(150.0);

        mockMvc.perform(get("/billing/insurance")
                        .param("amount", "200.0")
                        .param("patientId", "1"))
                .andExpect(status().isOk())
                .andExpect(content().string("Insurance coverage: $150.0"));
    }

    @Test
    void testCalculateInsurance_noInsuranceFound() throws Exception {
        when(insuranceRepository.findInsuranceByPatientId(99)).thenReturn(null);

        mockMvc.perform(get("/billing/insurance")
                        .param("amount", "100.0")
                        .param("patientId", "99"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("No insurance found for this patient."));
    }

    @Test
    void testCalculateInsurance_expiredInsurance() throws Exception {
        Insurance expiredInsurance = mock(Insurance.class);
        when(insuranceRepository.findInsuranceByPatientId(2)).thenReturn(expiredInsurance);
        when(expiredInsurance.isValid()).thenReturn(false);

        mockMvc.perform(get("/billing/insurance")
                        .param("amount", "100.0")
                        .param("patientId", "2"))
                .andExpect(status().isOk())
                .andExpect(content().string("Insurance expired."));
    }

    @Test
    void testGetTotalRevenue_success() throws Exception {
        when(billingService.getTotalRevenue()).thenReturn(987.65);

        mockMvc.perform(get("/billing/revenue"))
                .andExpect(status().isOk())
                .andExpect(content().string("Total Revenue: $987.65"));
    }

    @Test
    void testGetPendingBills_returnsEmptyList() throws Exception {
        mockMvc.perform(get("/billing/pending"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }
}
