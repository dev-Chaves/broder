package org.acme.domain.alarm;

import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.acme.domain.shared.api.BaseResource;
import org.jboss.logging.Logger;

@Path("/alarms/builder")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AlarmBuilderResource implements BaseResource {

    private static final Logger LOG = Logger.getLogger(AlarmBuilderResource.class);

    private final AlarmBuilderService alarmBuilderService;

    public AlarmBuilderResource(AlarmBuilderService alarmBuilderService) {
        this.alarmBuilderService = alarmBuilderService;
    }

    @GET
    @Path("/templates")
    public Response listTemplates() {
        LOG.info("[API] GET /alarms/builder/templates - Listing all templates");
        return toOk(alarmBuilderService.listTemplates());
    }

    @GET
    @Path("/categories")
    public Response listCategories() {
        LOG.info("[API] GET /alarms/builder/categories - Listing categories");
        return toOk(alarmBuilderService.listCategories());
    }

    @GET
    @Path("/metrics")
    public Response listMetrics() {
        LOG.info("[API] GET /alarms/builder/metrics - Listing available metrics");
        return toOk(alarmBuilderService.listMetrics());
    }

    @POST
    @Path("/preview")
    public Response preview(@Valid org.acme.domain.alarm.dto.AlarmBuilderRequestDTO dto) {
        LOG.infof("[API] POST /alarms/builder/preview - template=%s, name='%s'", dto.templateId(), dto.name());
        return toOk(alarmBuilderService.preview(dto));
    }

    @POST
    @Path("/validate")
    public Response validate(@Valid org.acme.domain.alarm.dto.AlarmBuilderRequestDTO dto) {
        LOG.infof("[API] POST /alarms/builder/validate - template=%s, name='%s'", dto.templateId(), dto.name());
        return toOk(alarmBuilderService.validate(dto));
    }

    @POST
    public Response build(@Valid org.acme.domain.alarm.dto.AlarmBuilderRequestDTO dto) {
        LOG.infof("[API] POST /alarms/builder - Building alarm: template=%s, name='%s'", dto.templateId(), dto.name());
        return toCreated(alarmBuilderService.buildAndSave(dto));
    }
}
