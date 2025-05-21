package sae.semestre.six.room;

import org.springframework.stereotype.Service;
import sae.semestre.six.appointment.Appointment;
import sae.semestre.six.appointment.AppointmentDao;

import java.util.HashMap;
import java.util.Map;

@Service
public class RoomService {

    private final RoomDao roomDao;
    private final AppointmentDao appointmentDao;

    public RoomService(RoomDao roomDao, AppointmentDao appointmentDao) {
        this.roomDao = roomDao;
        this.appointmentDao = appointmentDao;
    }

    public String assignRoom(Long appointmentId, String roomNumber) {
        Room room = roomDao.findByRoomNumber(roomNumber);
        Appointment appointment = appointmentDao.findById(appointmentId);

        if (room == null || appointment == null) {
            throw new IllegalArgumentException("Room or appointment not found");
        }

        if ("SURGERY".equals(room.getType()) &&
                !"SURGEON".equalsIgnoreCase(appointment.getDoctor().getSpecialization())) {
            throw new IllegalArgumentException("Only surgeons can use surgery rooms");
        }

        if (room.getCurrentPatientCount() >= room.getCapacity()) {
            throw new IllegalArgumentException("Room is at full capacity");
        }

        room.setCurrentPatientCount(room.getCurrentPatientCount() + 1);
        appointment.setRoomNumber(roomNumber);

        roomDao.update(room);
        appointmentDao.update(appointment);

        return "Room assigned successfully";
    }

    public Map<String, Object> getRoomAvailability(String roomNumber) {
        Room room = roomDao.findByRoomNumber(roomNumber);
        if (room == null) {
            throw new IllegalArgumentException("Room not found");
        }

        Map<String, Object> result = new HashMap<>();
        result.put("roomNumber", room.getRoomNumber());
        result.put("capacity", room.getCapacity());
        result.put("currentPatients", room.getCurrentPatientCount());
        result.put("available", room.canAcceptPatient());
        return result;
    }
}
