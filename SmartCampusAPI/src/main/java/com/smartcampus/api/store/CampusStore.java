package com.smartcampus.api.store;

import com.smartcampus.api.exception.LinkedResourceNotFoundException;
import com.smartcampus.api.exception.RoomNotEmptyException;
import com.smartcampus.api.exception.SensorUnavailableException;
import com.smartcampus.api.model.Room;
import com.smartcampus.api.model.Sensor;
import com.smartcampus.api.model.SensorReading;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

public final class CampusStore {

    private static final CampusStore INSTANCE = new CampusStore();

    private final Map<String, Room> rooms = new ConcurrentHashMap<>();
    private final Map<String, Sensor> sensors = new ConcurrentHashMap<>();
    private final Map<String, List<SensorReading>> sensorReadings = new ConcurrentHashMap<>();

    private CampusStore() {
        seedData();
    }

    public static CampusStore getInstance() {
        return INSTANCE;
    }

    public List<Room> getAllRooms() {
        return new ArrayList<>(rooms.values());
    }

    public Room getRoom(String roomId) {
        return rooms.get(roomId);
    }

    public Room createRoom(Room room) {
        rooms.put(room.getId(), room);
        return room;
    }

    public void deleteRoom(String roomId) {
        Room room = rooms.get(roomId);
        if (room == null) {
            return;
        }
        if (!room.getSensorIds().isEmpty()) {
            throw new RoomNotEmptyException("Room " + roomId + " still has sensors assigned.");
        }
        rooms.remove(roomId);
    }

    public List<Sensor> getSensors(String type) {
        return sensors.values().stream()
                .filter(sensor -> type == null || type.isBlank() || sensor.getType().equalsIgnoreCase(type))
                .collect(Collectors.toList());
    }

    public Sensor getSensor(String sensorId) {
        return sensors.get(sensorId);
    }

    public Sensor createSensor(Sensor sensor) {
        Room room = rooms.get(sensor.getRoomId());
        if (room == null) {
            throw new LinkedResourceNotFoundException("Referenced room " + sensor.getRoomId() + " does not exist.");
        }

        sensors.put(sensor.getId(), sensor);
        room.getSensorIds().add(sensor.getId());
        sensorReadings.putIfAbsent(sensor.getId(), new CopyOnWriteArrayList<>());
        return sensor;
    }

    public List<SensorReading> getReadings(String sensorId) {
        ensureSensorExists(sensorId);
        return new ArrayList<>(sensorReadings.getOrDefault(sensorId, new CopyOnWriteArrayList<>()));
    }

    public SensorReading addReading(String sensorId, SensorReading reading) {
        Sensor sensor = ensureSensorExists(sensorId);
        if ("MAINTENANCE".equalsIgnoreCase(sensor.getStatus())) {
            throw new SensorUnavailableException("Sensor " + sensorId + " is in MAINTENANCE and cannot accept readings.");
        }

        if (reading.getId() == null || reading.getId().isBlank()) {
            reading.setId(UUID.randomUUID().toString());
        }
        if (reading.getTimestamp() == 0L) {
            reading.setTimestamp(System.currentTimeMillis());
        }

        sensorReadings.computeIfAbsent(sensorId, key -> new CopyOnWriteArrayList<>()).add(reading);
        sensor.setCurrentValue(reading.getValue());
        return reading;
    }

    private Sensor ensureSensorExists(String sensorId) {
        Sensor sensor = sensors.get(sensorId);
        if (sensor == null) {
            throw new LinkedResourceNotFoundException("Sensor " + sensorId + " does not exist.");
        }
        return sensor;
    }

    private void seedData() {
        Room room = new Room("LIB-301", "Library Quiet Study", 80);
        rooms.put(room.getId(), room);

        Sensor sensor = new Sensor("TEMP-001", "Temperature", "ACTIVE", 23.4, room.getId());
        sensors.put(sensor.getId(), sensor);
        room.getSensorIds().add(sensor.getId());

        List<SensorReading> readings = new CopyOnWriteArrayList<>();
        readings.add(new SensorReading(UUID.randomUUID().toString(), System.currentTimeMillis(), 23.4));
        sensorReadings.put(sensor.getId(), readings);
    }
}
