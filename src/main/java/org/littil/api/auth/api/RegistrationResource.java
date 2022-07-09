package org.littil.api.auth.api;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.littil.api.school.service.School;
import org.littil.api.teacher.service.Teacher;
import org.littil.api.auth.service.SchoolUser;
import org.littil.api.auth.service.TeacherUser;
import org.littil.api.auth.service.UserService;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/api/v1/user/registration")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "User", description = "User operations")
public class RegistrationResource {

    @Inject
    UserService userService;

    @POST
    @Path(value = "/teacher")
    @Operation(summary = "Registration a new teacher")
    @APIResponse(
            responseCode = "200",
            description = "Registrate a new teacher to the platform",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(type = SchemaType.OBJECT, implementation = Teacher.class)
            )
    )
    public Response registrate(@Valid TeacherUser teacher) {
        Teacher createdTeacher = userService.registrate(teacher);
        return Response.ok(createdTeacher).build();
    }

    @POST
    @Path(value = "/school")
    @Operation(summary = "Registration a new school")
    @APIResponse(
            responseCode = "200",
            description = "Registrate a new school to the platform",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(type = SchemaType.OBJECT, implementation = School.class)
            )
    )
    public Response registrate(@Valid SchoolUser school) {
        School createdSchool = userService.registrate(school);
        return Response.ok(createdSchool).build();
    }
}
