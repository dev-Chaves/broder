package org.acme.domain.shared.api;

import jakarta.ws.rs.core.Response;

public interface BaseResource {

    default <T> Response toCreated(T entity){
        return Response.status(Response.Status.CREATED).entity(entity).build();
    }

    default <T> Response toOk(T entity){
        return Response.ok(entity).build();
    }

     default Response toNoContent(){
        return Response.noContent().build();
    }

}
