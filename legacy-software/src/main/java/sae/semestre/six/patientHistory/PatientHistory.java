package sae.semestre.six.patientHistory;

import sae.semestre.six.billing.Bill;
import sae.semestre.six.appointment.Appointment;
import sae.semestre.six.medical.LabResult;
import sae.semestre.six.patient.Patient;
import sae.semestre.six.prescription.Prescription;
import sae.semestre.six.treatment.Treatment;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table(name = "patient_history")
public class PatientHistory {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.EAGER) 
    @JoinColumn(name = "patient_id")
    private Patient patient;
    
    @OneToMany(mappedBy = "patientHistory", fetch = FetchType.EAGER) 
    private Set<Appointment> appointments = new HashSet<>();
    
    @OneToMany(fetch = FetchType.EAGER) 
    private Set<Prescription> prescriptions = new HashSet<>();
    
    @OneToMany(fetch = FetchType.EAGER) 
    private Set<Treatment> treatments = new HashSet<>();
    
    @OneToMany(fetch = FetchType.EAGER) 
    private Set<Bill> bills = new HashSet<>();
    
    @OneToMany(fetch = FetchType.EAGER) 
    private Set<LabResult> labResults = new HashSet<>();
    
    @Column(name = "visit_date")
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime visitDate;
    
    @Column(columnDefinition = "TEXT")
    private String diagnosis;
    
    @Column(columnDefinition = "TEXT")
    private String symptoms;
    
    @Column(columnDefinition = "TEXT")
    private String notes;
    
    
    public Set<Appointment> getAppointments() {
        
        return new TreeSet<>(appointments);
    }
    
    public List<Bill> getBillsSorted() {
        
        List<Bill> sortedBills = new ArrayList<>(bills);
        Collections.sort(sortedBills, (b1, b2) -> b2.getBillDate().compareTo(b1.getBillDate()));
        return sortedBills;
    }
    
    
    public Double getTotalBilledAmount() {
        return bills.stream()
            .mapToDouble(Bill::getTotalAmount)
            .sum();
    }

    public void setDiagnosis(String diagnosis) {
        this.diagnosis = diagnosis;
    }

    public void setVisitDate(LocalDateTime visitDate) {
        this.visitDate = visitDate;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getDiagnosis() {
        return this.diagnosis;
    }

} 