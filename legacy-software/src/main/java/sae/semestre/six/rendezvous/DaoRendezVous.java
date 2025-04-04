package sae.semestre.six.rendezvous;

import sae.semestre.six.generic.DaoGenerique;
import sae.semestre.six.appointment.Appointment;
import java.util.Date;
import java.util.List;

public interface DaoRendezVous extends DaoGenerique<Appointment, Long> {
    List<Appointment> trouverParIdPatient(Long idPatient);
    List<Appointment> trouverParIdMedecin(Long idMedecin);
    List<Appointment> trouverParPlageDates(Date dateDebut, Date dateFin);
} 