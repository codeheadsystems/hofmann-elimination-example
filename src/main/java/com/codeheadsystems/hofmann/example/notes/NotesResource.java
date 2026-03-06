package com.codeheadsystems.hofmann.example.notes;

import com.codeheadsystems.hofmann.dropwizard.auth.HofmannPrincipal;
import io.dropwizard.auth.Auth;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;

@Path("/notes")
@Produces(MediaType.APPLICATION_JSON)
@Singleton
public class NotesResource {

  public record CreateNoteRequest(@NotBlank String title, @NotBlank String content) {}

  public record NoteResponse(String id, String title, String content, String createdAt) {}

  private final NoteStore noteStore;

  @Inject
  public NotesResource(NoteStore noteStore) {
    this.noteStore = noteStore;
  }

  @GET
  public List<NoteResponse> list(@Auth HofmannPrincipal principal) {
    return noteStore.list(principal.credentialIdentifier()).stream()
        .sorted((a, b) -> b.createdAt().compareTo(a.createdAt()))
        .map(this::toResponse)
        .toList();
  }

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  public Response create(@Auth HofmannPrincipal principal, @Valid CreateNoteRequest request) {
    Note note = noteStore.create(principal.credentialIdentifier(), request.title(), request.content());
    return Response.status(Response.Status.CREATED).entity(toResponse(note)).build();
  }

  @DELETE
  @Path("/{id}")
  public Response delete(@Auth HofmannPrincipal principal, @PathParam("id") String id) {
    boolean deleted = noteStore.delete(principal.credentialIdentifier(), id);
    return deleted
        ? Response.noContent().build()
        : Response.status(Response.Status.NOT_FOUND).build();
  }

  private NoteResponse toResponse(Note note) {
    return new NoteResponse(note.id(), note.title(), note.content(), note.createdAt().toString());
  }
}
