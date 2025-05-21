package sae.semestre.six.patientHistory;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/patient-history")
public class PatientHistoryController {

    private final PatientHistoryService patientHistoryService;

    public PatientHistoryController(PatientHistoryService patientHistoryService) {
        this.patientHistoryService = patientHistoryService;
    }

    @GetMapping("/search")
    public ResponseEntity<List<PatientHistory>> searchHistory(
            @RequestParam String keyword,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date endDate) {

        List<PatientHistory> results = patientHistoryService.searchHistory(keyword, startDate, endDate);
        return ResponseEntity.ok(results);
    }

    @GetMapping("/patient/{patientId}/summary")
    public ResponseEntity<Map<String, Object>> getPatientSummary(@PathVariable Long patientId) {
        Map<String, Object> summary = patientHistoryService.getPatientSummary(patientId);
        return ResponseEntity.ok(summary);
    }
} 