package sae.semestre.six.prescription;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.*;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PrescriptionController.class)
class PrescriptionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PrescriptionService prescriptionService;

    @Test
    void addPrescription_shouldReturnOk() throws Exception {
        when(prescriptionService.addPrescription("1", new String[]{"PARACETAMOL"}, "Take 2/day"))
                .thenReturn("RX1");

        mockMvc.perform(post("/prescriptions/add")
                        .param("patientId", "1")
                        .param("medicines", "PARACETAMOL")
                        .param("notes", "Take 2/day"))
                .andExpect(status().isOk())
                .andExpect(content().string("Prescription RX1 created and billed"));
    }

    @Test
    void getPatientPrescriptions_shouldReturnList() throws Exception {
        when(prescriptionService.getPrescriptions("1")).thenReturn(List.of("RX1", "RX2"));

        mockMvc.perform(get("/prescriptions/patient/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0]").value("RX1"))
                .andExpect(jsonPath("$[1]").value("RX2"));
    }

    @Test
    void getInventory_shouldReturnInventoryMap() throws Exception {
        Map<String, Integer> inventory = new HashMap<>();
        inventory.put("PARACETAMOL", 10);
        inventory.put("VITAMINS", 5);

        when(prescriptionService.getInventory()).thenReturn(inventory);

        mockMvc.perform(get("/prescriptions/inventory"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.PARACETAMOL").value(10))
                .andExpect(jsonPath("$.VITAMINS").value(5));
    }

    @Test
    void refillMedicine_shouldReturnOkMessage() throws Exception {
        mockMvc.perform(post("/prescriptions/refill")
                        .param("medicine", "PARACETAMOL")
                        .param("quantity", "5"))
                .andExpect(status().isOk())
                .andExpect(content().string("Refilled 5 units of PARACETAMOL"));

        verify(prescriptionService).refillMedicine("PARACETAMOL", 5);
    }

    @Test
    void getCost_shouldReturnCorrectCost() throws Exception {
        when(prescriptionService.getCost("RX1")).thenReturn(48.0);

        mockMvc.perform(get("/prescriptions/cost/RX1"))
                .andExpect(status().isOk())
                .andExpect(content().string("48.0"));
    }

    @Test
    void clearAllData_shouldReturnOk() throws Exception {
        mockMvc.perform(delete("/prescriptions/clear"))
                .andExpect(status().isOk())
                .andExpect(content().string("All data cleared"));

        verify(prescriptionService).clearAllData();
    }
}
