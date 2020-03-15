package io.itms.handler;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.file.Paths;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import io.itms.ServerConfig;

@Path("clips/{clip}")
@Produces("audio/mp3")
public class FileHandler {

  @GET
  public Response staticContent(@PathParam("clip") String clip) {
    File file = Paths.get(ServerConfig.CLIP_DIRECTORY.toString(), clip).toFile();
    InputStream fileStream;
    try {
      fileStream = new FileInputStream(file);
      return Response.ok(fileStream).build();
    } catch (FileNotFoundException e) {
      return Response.status(Status.NOT_FOUND).build();
    }
  }
}