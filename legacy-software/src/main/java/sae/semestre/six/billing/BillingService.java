package sae.semestre.six.billing;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sae.semestre.six.doctor.DoctorDao;
import sae.semestre.six.patient.PatientDao;
import sae.semestre.six.prestation.Prestation;
import sae.semestre.six.prestation.PrestationRepository;
import sae.semestre.six.prestation.PrestationService;
import sae.semestre.six.service.EmailService;
import sae.semestre.six.treatment.Treatment;
import sae.semestre.six.treatment.TreatmentRepository;

import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

@Service
public class BillingService {

    @Autowired
    private PrestationRepository prestationRepository;

    @Autowired
    private PatientDao patientDao;

    @Autowired
    private DoctorDao doctorDao;

    @Autowired
    private BillRepository billRepository;

    private final EmailService emailService = EmailService.getInstance();

    public void addPrestations(String[] prestations, Bill bill) {
        List<Prestation> ligneFacture = new ArrayList<>();
        for (String prestationName : prestations) {
            Prestation prestation = prestationRepository.findByName(prestationName)
                    .orElseThrow(() -> new IllegalArgumentException("Prestation not found: " + prestationName));
            ligneFacture.add(prestation);
        }
        bill.addPrestation(ligneFacture);
    }

    public Bill initializeBill(String patientId, String doctorId) {
        return new Bill("BILL" + System.currentTimeMillis(),
                patientDao.findById(Long.parseLong(patientId)),
                doctorDao.findById(Long.parseLong(doctorId)));
    }

    public Double getTotalRevenue() {
        return billRepository.findTotalRevenue();
    }
    public void processBill(String patientId, String doctorId, String[] prestations) throws Exception {
        Bill bill = initializeBill(patientId , doctorId);
        // Déplacement de la logique métier dans la classe Bill
        addPrestations(prestations , bill);

        try (FileWriter fw = new FileWriter("C:\\hospital\\billing.txt", true)) {
            fw.write(bill.getBillNumber() + ": $" + bill.getTotalAmount() + "\n");
        }
        billRepository.save(bill);

        emailService.sendEmail(
                "admin@hospital.com",
                "New Bill Generated",
                "Bill Number: " + bill.getBillNumber() + "\nTotal: $" + bill.getTotalAmount()
        );
    }
}
