package com.smartcampus.resource;

import com.smartcampus.DataStore;
import com.smartcampus.exception.RoomNotEmptyException;
import com.smartcampus.model.ApiResponse;
import com.smartcampus.model.Room;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

@Path("/rooms")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class RoomResource {

    // GET /api/v1/rooms 
    @GET
    public Response getAllRooms() {
        List<Room> roomList = new ArrayList<>(DataStore.getRooms().values());
        return Response.ok(
                ApiResponse.success("Retrieved " + roomList.size() + " room(s).", roomList)
        ).build();
    }

    // POST /api/v1/rooms 
    @POST
    public Response createRoom(Room room) {
        if (room == null || room.getId() == null || room.getId().isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(ApiResponse.error("Room 'id' is required.", null))
                    .build();
        }
        if (room.getName() == null || room.getName().isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(ApiResponse.error("Room 'name' is required.", null))
                    .build();
        }
        if (DataStore.getRooms().containsKey(room.getId())) {
            return Response.status(Response.Status.CONFLICT)
                    .entity(ApiResponse.error("A room with id '" + room.getId() + "' already exists.", null))
                    .build();
        }

        // Ensure sensorIds list is initialised
        if (room.getSensorIds() == null) {
            room.setSensorIds(new ArrayList<>());
        }

        DataStore.getRooms().put(room.getId(), room);

        return Response.status(Response.Status.CREATED)
                .entity(ApiResponse.success("Room created successfully.", room))
                .build();
    }

    // GET /api/v1/rooms/{roomId} 
    @GET
    @Path("/{roomId}")
    public Response getRoomById(@PathParam("roomId") String roomId) {
        Room room = DataStore.getRooms().get(roomId);
        if (room == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(ApiResponse.error("Room with id '" + roomId + "' not found.", null))
                    .build();
        }
        return Response.ok(ApiResponse.success("Room found.", room)).build();
    }

    // DELETE /api/v1/rooms/{roomId} 
    @DELETE
    @Path("/{roomId}")
    public Response deleteRoom(@PathParam("roomId") String roomId) {
        Room room = DataStore.getRooms().get(roomId);
        if (room == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(ApiResponse.error("Room with id '" + roomId + "' not found.", null))
                    .build();
        }

        // Business Logic Constraint: cannot delete room that still has sensors
        if (!room.getSensorIds().isEmpty()) {
            throw new RoomNotEmptyException(roomId);
        }

        DataStore.getRooms().remove(roomId);

        return Response.ok(
                ApiResponse.success("Room '" + roomId + "' has been successfully decommissioned.", null)
        ).build();
    }
}
