package sae.semestre.six.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import sae.semestre.six.patientHistory.PatientHistory;
import sae.semestre.six.patientHistory.PatientHistoryController;
import sae.semestre.six.patientHistory.PatientHistoryService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class PatientHistoryControllerTest {

    private MockMvc mockMvc;

    private PatientHistoryService patientHistoryService;

    @BeforeEach
    public void setup() {
        patientHistoryService = mock(PatientHistoryService.class);
        PatientHistoryController controller = new PatientHistoryController(patientHistoryService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void testSearchHistoryReturnsResults() throws Exception {
        PatientHistory history = new PatientHistory();
        history.setDiagnosis("Consultation");
        history.setVisitDate(LocalDateTime.now());

        when(patientHistoryService.searchHistory(anyString(), any(), any()))
                .thenReturn(List.of(history));

        mockMvc.perform(get("/patient-history/search")
                        .param("keyword", "consultation")
                        .param("startDate", "2023-01-01")
                        .param("endDate", "2024-01-01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].diagnosis").value("Consultation"));
    }

    @Test
    void testSearchHistoryReturnsEmptyList() throws Exception {
        when(patientHistoryService.searchHistory(anyString(), any(), any()))
                .thenReturn(List.of());

        mockMvc.perform(get("/patient-history/search")
                        .param("keyword", "nothing")
                        .param("startDate", "2023-01-01")
                        .param("endDate", "2024-01-01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void testSearchHistoryReturnsBadRequestOnInvalidDate() throws Exception {
        mockMvc.perform(get("/patient-history/search")
                        .param("keyword", "consultation")
                        .param("startDate", "not-a-date")
                        .param("endDate", "2024-01-01"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGetPatientSummaryReturnsSummary() throws Exception {
        Long patientId = 1L;
        Map<String, Object> summaryMap = Map.of(
                "totalVisits", (Object) 5,
                "totalBills", (Object) 3,
                "totalAmount", (Object) 1234.56
        );

        when(patientHistoryService.getPatientSummary(patientId)).thenReturn(summaryMap);

        mockMvc.perform(get("/patient-history/patient/{patientId}/summary", patientId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalVisits").value(5))
                .andExpect(jsonPath("$.totalBills").value(3))
                .andExpect(jsonPath("$.totalAmount").value(1234.56));
    }
}
