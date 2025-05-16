package sae.semestre.six.prescription;

import sae.semestre.six.generic.GenericDao;

import java.util.List;

public interface PrescriptionDao extends GenericDao<Prescription, Long> {
    List<Prescription> findByPatientId(Long patientId);
} 