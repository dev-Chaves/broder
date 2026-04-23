package org.acme.domain.history;

import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.acme.domain.history.dto.HistoryRequestDTO;
import org.acme.domain.shared.api.BaseResource;
import org.jboss.logging.Logger;

@Path("/history")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class HistoryResource implements BaseResource {

    private static final Logger LOG = Logger.getLogger(HistoryResource.class);

    private final HistoryService historyService;

    public HistoryResource(HistoryService historyService) {
        this.historyService = historyService;
    }

    @GET
    public Response listAll(
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("20") int size) {
        LOG.infof("[API] GET /history - Listing history: page=%d, size=%d", page, size);
        return toOk(historyService.listAll(page, size));
    }

    @GET
    @Path("/alarm/{alarmId}")
    public Response listByAlarm(
            @PathParam("alarmId") Long alarmId,
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("20") int size) {
        LOG.infof("[API] GET /history/alarm/%d - Listing history: page=%d, size=%d", alarmId, page, size);
        return toOk(historyService.listByAlarmId(alarmId, page, size));
    }

    @GET
    @Path("/alarm/{alarmId}/latest")
    public Response findLatestByAlarm(@PathParam("alarmId") Long alarmId) {
        LOG.infof("[API] GET /history/alarm/%d/latest - Finding latest history", alarmId);
        return toOk(historyService.findLatestByAlarmId(alarmId));
    }

    /**
     * Creates a history entry manually. Intended for administrative/testing use;
     * in normal operation history is recorded automatically by the scheduler.
     */
    @POST
    public Response save(@Valid HistoryRequestDTO dto) {
        return toCreated(historyService.save(dto));
    }
}
