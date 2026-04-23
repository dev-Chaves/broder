package org.acme.domain.alarm;

import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.acme.domain.alarm.dto.AlarmRequestDTO;
import org.acme.domain.alarm.dto.AlarmUpdateDTO;
import org.acme.domain.shared.api.BaseResource;
import org.jboss.logging.Logger;

@Path("/alarms")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AlarmResource implements BaseResource {

    private static final Logger LOG = Logger.getLogger(AlarmResource.class);

    private final AlarmService alarmService;

    public AlarmResource(AlarmService alarmService) {
        this.alarmService = alarmService;
    }

    @GET
    public Response listAll(
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("20") int size) {
        LOG.infof("[API] GET /alarms - Listing alarms: page=%d, size=%d", page, size);
        return toOk(alarmService.listAll(page, size));
    }

    @GET
    @Path("/{id}")
    public Response findById(@PathParam("id") Long id) {
        LOG.infof("[API] GET /alarms/%d - Finding alarm", id);
        return toOk(alarmService.findById(id));
    }

    @POST
    public Response save(@Valid AlarmRequestDTO dto) {
        LOG.infof("[API] POST /alarms - Creating alarm: name='%s'", dto.name());
        return toCreated(alarmService.save(dto));
    }

    @PUT
    @Path("/{id}")
    public Response update(@PathParam("id") Long id, @Valid AlarmUpdateDTO dto) {
        LOG.infof("[API] PUT /alarms/%d - Updating alarm", id);
        return toOk(alarmService.update(id, dto));
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") Long id) {
        LOG.infof("[API] DELETE /alarms/%d - Deleting alarm", id);
        alarmService.delete(id);
        return toNoContent();
    }
}
