package sae.semestre.six.billing;

import org.springframework.beans.factory.annotation.Autowired;
import sae.semestre.six.doctor.Doctor;
import sae.semestre.six.patient.Patient;
import sae.semestre.six.patientHistory.PatientHistory;

import jakarta.persistence.*;
import sae.semestre.six.prestation.Prestation;
import sae.semestre.six.treatment.Treatment;
import sae.semestre.six.treatment.TreatmentRepository;

import java.util.*;

@Entity
@Table(name = "bills")
public class Bill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "bill_number", unique = true)
    private String billNumber;
    
    @ManyToOne
    @JoinColumn(name = "patient_id")
    private Patient patient;
    
    @ManyToOne
    @JoinColumn(name = "doctor_id")
    private Doctor doctor;
    
    @Column(name = "bill_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date billDate = new Date();
    
    @Column(name = "total_amount")
    private Double totalAmount = 0.0;
    
    @Column(name = "status")
    private Status status = Status.EN_ATTENTE;
    
    @OneToMany(mappedBy = "bill", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<BillDetail> billDetails = new HashSet<>();
    
    
    @Column(name = "created_date")
    private Date createdDate = new Date();
    
    @Column(name = "last_modified")
    private Date lastModified = new Date();

    @ManyToOne
    private PatientHistory patientHistory;

    public Bill(String s, Patient patient, Doctor doctor) {
        this.billNumber = s;
        this.patient = patient;
        this.doctor = doctor;
    }

    public Bill() {

    }


    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getBillNumber() { return billNumber; }

    public void setBillNumber(String billNumber) { this.billNumber = billNumber; }
    
    public Patient getPatient() { return patient; }
    public void setPatient(Patient patient) { this.patient = patient; }
    
    public Doctor getDoctor() { return doctor; }
    public void setDoctor(Doctor doctor) { this.doctor = doctor; }
    
    public Date getBillDate() { return billDate; }
    public void setBillDate(Date billDate) { this.billDate = billDate; }
    
    public Double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(Double totalAmount) { this.totalAmount = totalAmount; }
    
    public Status getStatus() { return status; }
    public void setStatus(Status status) {
        this.status = status;
        this.lastModified = new Date(); 
    }
    
    public Set<BillDetail> getBillDetails() { return billDetails; }
    public void setBillDetails(Set<BillDetail> billDetails) { this.billDetails = billDetails; }

    public void addPrestation(List<Prestation> ligneFacture) {
        Double total = 0.0;
        for (Prestation p : ligneFacture) {
            BillDetail billDetail = new BillDetail();
            billDetail.setTreatmentName(p.getName());
            billDetail.setUnitPrice(p.getPrice());
            billDetail.setBill(this);
            billDetails.add(billDetail);
            total += billDetail.getUnitPrice();
        }
        if (total > 500) {
            total *= 0.9;
        }
        this.totalAmount = total;
    }

    public Double calculateTotal() {
        Double total = 0.0;
        for (BillDetail billDetail : billDetails) {
            total += billDetail.getLineTotal();
        }
        return total;
    }
}