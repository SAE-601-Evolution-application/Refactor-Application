package sae.semestre.six.patient;

import sae.semestre.six.generic.GenericDao;

import java.util.List;

public interface PatientDao extends GenericDao<Patient, Long> {
    Patient findByPatientNumber(String patientNumber);
    List<Patient> findByLastName(String lastName);
} 