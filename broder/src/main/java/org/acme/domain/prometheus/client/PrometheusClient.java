package org.acme.domain.prometheus.client;

import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.acme.domain.prometheus.dto.PrometheusLabelsResponseDTO;
import org.acme.domain.prometheus.dto.PrometheusQueryResponseDTO;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.jboss.resteasy.reactive.RestQuery;

@Path("/api/v1")
@RegisterRestClient(configKey = "prometheus")
@Produces(MediaType.APPLICATION_JSON)
public interface PrometheusClient {

    @GET
    @Path("/query")
    Uni<PrometheusQueryResponseDTO> instantQuery(
            @RestQuery String query,
            @RestQuery String time
    );

    @GET
    @Path("/query_range")
    Uni<PrometheusQueryResponseDTO> rangeQuery(
            @RestQuery String query,
            @RestQuery String start,
            @RestQuery String end,
            @RestQuery String step
    );

    @GET
    @Path("/labels")
    Uni<PrometheusLabelsResponseDTO> queryLabels();

    @GET
    @Path("/label/{name}/values")
    Uni<PrometheusLabelsResponseDTO> labelValues(@PathParam("name") String labelName);
}
