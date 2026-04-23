/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.smartcampus.resources;

import com.smartcampus.data.DataStore;
import com.smartcampus.exceptions.LinkedResourceNotFoundException;
import com.smartcampus.models.Sensor;
import com.smartcampus.models.SensorReading;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 *
 * @author ijama
 */
@Path("/sensors")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorResource {

    private final DataStore store = DataStore.getInstance();

    // GET /api/v1/sensors or GET /api/v1/sensors?type=CO2
    @GET
    public Response getAllSensors(@QueryParam("type") String type) {
        List<Sensor> allSensors = new ArrayList<>(store.getSensors().values());

        if (type != null && !type.isEmpty()) {
            allSensors = allSensors.stream()
                    .filter(s -> s.getType().equalsIgnoreCase(type))
                    .collect(Collectors.toList());
        }

        return Response.ok(allSensors).build();
    }

    // POST /api/v1/sensors - create a new sensor
    @POST
    public Response createSensor(Sensor sensor) {
        if (sensor.getId() == null || sensor.getId().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "Sensor ID is required"))
                    .build();
        }

        // Check the roomId actually exists
        if (sensor.getRoomId() == null || !store.getRooms().containsKey(sensor.getRoomId())) {
            throw new LinkedResourceNotFoundException(
                "Room not found: " + sensor.getRoomId() + ". Please create the room first.");
        }

        if (store.getSensors().containsKey(sensor.getId())) {
            return Response.status(Response.Status.CONFLICT)
                    .entity(Map.of("error", "A sensor with this ID already exists"))
                    .build();
        }

        // Add sensor to the room's sensorIds list
        store.getRooms().get(sensor.getRoomId()).getSensorIds().add(sensor.getId());

        // Initialise an empty readings list for this sensor
        store.getReadings().put(sensor.getId(), new ArrayList<>());

        store.getSensors().put(sensor.getId(), sensor);
        return Response.status(Response.Status.CREATED).entity(sensor).build();
    }

    // GET /api/v1/sensors/{sensorId}
    @GET
    @Path("/{sensorId}")
    public Response getSensor(@PathParam("sensorId") String sensorId) {
        Sensor sensor = store.getSensors().get(sensorId);
        if (sensor == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", "Sensor not found: " + sensorId))
                    .build();
        }
        return Response.ok(sensor).build();
    }

    // Sub-resource locator for readings
    @Path("/{sensorId}/readings")
    public SensorReadingResource getReadingResource(@PathParam("sensorId") String sensorId) {
        return new SensorReadingResource(sensorId);
    }
}
