package org.acme.domain.prometheus;

import io.smallrye.mutiny.Uni;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.acme.domain.prometheus.dto.PrometheusQueryRequestDTO;
import org.acme.domain.shared.api.BaseResource;
import org.jboss.logging.Logger;

@Path("/prometheus")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PrometheusResource implements BaseResource {

    private static final Logger LOG = Logger.getLogger(PrometheusResource.class);

    private final PrometheusService prometheusService;

    public PrometheusResource(PrometheusService prometheusService) {
        this.prometheusService = prometheusService;
    }

    @POST
    @Path("/query")
    public Uni<Response> query(@Valid PrometheusQueryRequestDTO dto) {
        LOG.infof("[API] POST /prometheus/query - Query: %s", dto.query());
        return prometheusService.instantQuery(dto)
                .map(this::toOk);
    }

    @POST
    @Path("/query/range")
    public Uni<Response> rangeQuery(@Valid PrometheusQueryRequestDTO dto) {
        LOG.infof("[API] POST /prometheus/query/range - Query: %s, start=%s, end=%s, step=%s",
                dto.query(), dto.start(), dto.end(), dto.step());
        return prometheusService.rangeQuery(dto)
                .map(this::toOk);
    }

    @GET
    @Path("/labels")
    public Uni<Response> labels() {
        LOG.info("[API] GET /prometheus/labels - Querying all labels");
        return prometheusService.queryLabels()
                .map(this::toOk);
    }

    @GET
    @Path("/labels/{name}/values")
    public Uni<Response> labelValues(@PathParam("name") String labelName) {
        LOG.infof("[API] GET /prometheus/labels/%s/values - Querying label values", labelName);
        return prometheusService.labelValues(labelName)
                .map(this::toOk);
    }

}
