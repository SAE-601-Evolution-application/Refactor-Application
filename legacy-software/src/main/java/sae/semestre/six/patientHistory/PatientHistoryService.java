package sae.semestre.six.patientHistory;

import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class PatientHistoryService {

    private final PatientHistoryDao patientHistoryDao;

    public PatientHistoryService(PatientHistoryDao patientHistoryDao) {
        this.patientHistoryDao = patientHistoryDao;
    }

    public List<PatientHistory> searchHistory(String keyword, Date startDate, Date endDate) {
        return patientHistoryDao.searchByMultipleCriteria(keyword, startDate, endDate);
    }

    public Map<String, Object> getPatientSummary(Long patientId) {
        List<PatientHistory> histories = patientHistoryDao.findCompleteHistoryByPatientId(patientId);

        Map<String, Object> summary = new HashMap<>();
        summary.put("visitCount", histories.size());

        double totalBilled = histories.stream()
                .mapToDouble(PatientHistory::getTotalBilledAmount)
                .sum();

        summary.put("totalBilled", totalBilled);
        return summary;
    }
}