package sae.semestre.six.treatment;

import sae.semestre.six.patientHistory.PatientHistory;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.Date;

@Entity
@Table(name = "treatments")
public class Treatment {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "name")
    private String name;
    
    @ManyToOne
    @JoinColumn(name = "patient_history_id")
    private PatientHistory patientHistory;
    
    @Column(name = "treatment_date")
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime treatmentDate;
    
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
    
    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public PatientHistory getPatientHistory() { return patientHistory; }
    public void setPatientHistory(PatientHistory patientHistory) { this.patientHistory = patientHistory; }
    
    public LocalDateTime getTreatmentDate() { return treatmentDate; }
    public void setTreatmentDate(LocalDateTime treatmentDate) { this.treatmentDate = treatmentDate; }
    
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
} 