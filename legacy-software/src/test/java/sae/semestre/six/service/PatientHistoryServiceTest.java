package sae.semestre.six.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sae.semestre.six.patientHistory.PatientHistory;
import sae.semestre.six.patientHistory.PatientHistoryDao;
import sae.semestre.six.patientHistory.PatientHistoryService;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class PatientHistoryServiceTest {

    private PatientHistoryDao patientHistoryDao;
    private PatientHistoryService patientHistoryService;

    @BeforeEach
    void setUp() {
        patientHistoryDao = mock(PatientHistoryDao.class);
        patientHistoryService = new PatientHistoryService(patientHistoryDao);
    }

    @Test
    void searchHistorySuccess() {
        String keyword = "disease";
        Date startDate = new GregorianCalendar(2024, Calendar.JANUARY, 1).getTime();
        Date endDate = new GregorianCalendar(2024, Calendar.DECEMBER, 31).getTime();

        List<PatientHistory> histories = List.of(new PatientHistory(), new PatientHistory());

        when(patientHistoryDao.searchByMultipleCriteria(keyword, startDate, endDate))
                .thenReturn(histories);

        List<PatientHistory> result = patientHistoryService.searchHistory(keyword, startDate, endDate);

        assertEquals(2, result.size());
        verify(patientHistoryDao).searchByMultipleCriteria(keyword, startDate, endDate);
    }

    @Test
    void searchHistoryNoResult() {
        String keyword = "unknown";
        Date startDate = new Date();
        Date endDate = new Date();

        when(patientHistoryDao.searchByMultipleCriteria(keyword, startDate, endDate))
                .thenReturn(Collections.emptyList());

        List<PatientHistory> result = patientHistoryService.searchHistory(keyword, startDate, endDate);

        assertTrue(result.isEmpty());
        verify(patientHistoryDao).searchByMultipleCriteria(keyword, startDate, endDate);
    }

    @Test
    void getPatientSummarySuccess() {
        Long patientId = 42L;

        PatientHistory h1 = mock(PatientHistory.class);
        PatientHistory h2 = mock(PatientHistory.class);

        when(h1.getTotalBilledAmount()).thenReturn(100.0);
        when(h2.getTotalBilledAmount()).thenReturn(200.0);

        List<PatientHistory> histories = List.of(h1, h2);
        when(patientHistoryDao.findCompleteHistoryByPatientId(patientId)).thenReturn(histories);

        Map<String, Object> summary = patientHistoryService.getPatientSummary(patientId);

        assertEquals(2, summary.get("visitCount"));
        assertEquals(300.0, (Double) summary.get("totalBilled"), 0.001);
        verify(patientHistoryDao).findCompleteHistoryByPatientId(patientId);
    }

    @Test
    void getPatientSummaryNoHistory() {
        Long patientId = 99L;

        when(patientHistoryDao.findCompleteHistoryByPatientId(patientId))
                .thenReturn(Collections.emptyList());

        Map<String, Object> summary = patientHistoryService.getPatientSummary(patientId);

        assertEquals(0, summary.get("visitCount"));
        assertEquals(0.0, (Double) summary.get("totalBilled"), 0.001);
        verify(patientHistoryDao).findCompleteHistoryByPatientId(patientId);
    }

}
