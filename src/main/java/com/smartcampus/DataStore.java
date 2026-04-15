package com.smartcampus;

import com.smartcampus.model.Room;
import com.smartcampus.model.Sensor;
import com.smartcampus.model.SensorReading;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class DataStore {

    // ---- Rooms: roomId -> Room ----
    private static final Map<String, Room> rooms = new ConcurrentHashMap<>();

    // ---- Sensors: sensorId -> Sensor ----
    private static final Map<String, Sensor> sensors = new ConcurrentHashMap<>();

    // ---- Sensor Readings: sensorId -> List<SensorReading> ----
    private static final Map<String, List<SensorReading>> sensorReadings = new ConcurrentHashMap<>();

    // Prevent instantiation
    private DataStore() {}

    public static Map<String, Room> getRooms() {
        return rooms;
    }

    public static Map<String, Sensor> getSensors() {
        return sensors;
    }

    public static Map<String, List<SensorReading>> getSensorReadings() {
        return sensorReadings;
    }
    public static List<SensorReading> getOrCreateReadings(String sensorId) {
        return sensorReadings.computeIfAbsent(sensorId, k -> new CopyOnWriteArrayList<>());
    }
}
