package sae.semestre.six.appointment;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sae.semestre.six.doctor.Doctor;
import sae.semestre.six.doctor.DoctorDao;
import sae.semestre.six.patient.Patient;
import sae.semestre.six.patient.PatientDao;
import sae.semestre.six.room.Room;
import sae.semestre.six.room.RoomDao;
import sae.semestre.six.service.EmailService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class AppointmentService {

    @Autowired
    private AppointmentDao appointmentDao;

    @Autowired
    private DoctorDao doctorDao;

    @Autowired
    private PatientDao patientDao;

    @Autowired
    private RoomDao roomDao;

    @Autowired
    private EmailService emailService;

    public String scheduleAppointment(Long doctorId,Long patientId, String roomNumber, LocalDateTime appointmentDate){
        try {
            Doctor doctor = doctorDao.findById(doctorId);
            Patient patient = patientDao.findById(patientId);
            Room room = roomDao.findByRoomNumber(roomNumber);


            List<Appointment> doctorAppointments = appointmentDao.findByDoctorId(doctorId);
            for (Appointment existing : doctorAppointments) {

                if (existing.getAppointmentDate().equals(appointmentDate)) {
                    return "Doctor is not available at this time";
                }
            }

            List<Appointment> roomAppointments = appointmentDao.findByRoomId(room.getId());
            for (Appointment existing : roomAppointments){
                if (existing.getAppointmentDate().equals(appointmentDate)) {
                    return "The room is not available at this time";
                }
            }

            int hour = appointmentDate.getHour();
            if (hour < 9 || hour > 17) {
                return "Appointments only available between 9 AM and 5 PM";
            }

            Appointment newAppointment = new Appointment();
            newAppointment.setDoctor(doctor);
            newAppointment.setPatient(patient);
            newAppointment.setRoomNumber(roomNumber);
            newAppointment.setAppointmentNumber(setAppointmentNumber());
            newAppointment.setRoom(room);
            newAppointment.setAppointmentDate(appointmentDate);

            emailService.sendAppointmentMail(patient.getEmail(),doctor.getEmail(),
                    "New Appointment Scheduled",
                    "You have a new appointment on " + appointmentDate);
            appointmentDao.save(newAppointment);

            return "Appointment scheduled successfully";
        } catch (Exception e) {
            e.printStackTrace();
            return "Error: " + e.getMessage();
        }
    }

    private String setAppointmentNumber(){
        long id = appointmentDao.findMaxId() != null ? appointmentDao.findMaxId() + 1 : 1;
        return "RDV00" + id;
    }

    public List<LocalDateTime> getDoctorAvailableSlots(Long doctorId, LocalDate date){
        List<LocalDateTime> availableSlots = new ArrayList<>();
        for (int hour = 9; hour <= 17; hour++) {
            LocalDateTime slot = LocalDateTime.of(date, LocalTime.of(hour,0));
            boolean slotAvailable = true;
            for (Appointment app : appointmentDao.findByDoctorId(doctorId)) {
                if (app.getAppointmentDate().getHour() == hour) {
                    slotAvailable = false;
                    break;
                }
            }

            if (slotAvailable) {
                availableSlots.add(slot);
            }
        }

        return availableSlots;
    }

    public List<LocalDateTime> getRoomAvailableSlots(Long roomNumber, LocalDate date){
        List<LocalDateTime> availableSlots = new ArrayList<>();
        for (int hour = 9; hour <= 17; hour++) {
            LocalDateTime slot = LocalDateTime.of(date, LocalTime.of(hour,0));
            boolean slotAvailable = true;
            for (Appointment app : appointmentDao.findByRoomId(roomNumber)) {
                if (app.getAppointmentDate().getHour() == hour) {
                    slotAvailable = false;
                    break;
                }
            }

            if (slotAvailable) {
                availableSlots.add(slot);
            }
        }
        return availableSlots;
    }
}
