package sae.semestre.six.billing;

import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import sae.semestre.six.insurance.Insurance;
import sae.semestre.six.insurance.InsuranceRepository;
import java.util.*;

@RestController
@RequestMapping("/billing")
public class BillingController {

    private static volatile BillingController instance;


    private List<String> pendingBills = new ArrayList<>();



    @Autowired
    private  BillingService billingService;

    @Autowired
    private InsuranceRepository insuranceRepository;

    public static BillingController getInstance() {
        if (instance == null) {
            synchronized (BillingController.class) {
                if (instance == null) {
                    instance = new BillingController();
                }
            }
        }
        return instance;
    }

    @PostMapping("/process")
    public String processBill(
            @RequestParam String patientId,
            @RequestParam String doctorId,
            @RequestParam String[] prestations) {
        try {
            return "Bill processed successfully";
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    @GetMapping("/insurance")
    public String calculateInsurance(@RequestParam double amount, @RequestParam int patientId) {
        Insurance insurance = insuranceRepository.findInsuranceByPatientId(patientId);
        if (insurance == null) {
            return "No insurance found for this patient.";
        }
        if (!insurance.isValid()) {
            return "Insurance expired.";
        }
        double coverage = insurance.calculateCoverage(amount);
        return "Insurance coverage: $" + coverage;
    }


    @GetMapping("/revenue")
    public String getTotalRevenue() {
        return "Total Revenue: $" + billingService.getTotalRevenue();
    }

    @GetMapping("/pending")
    public List<String> getPendingBills() {
        return pendingBills;
    }
}