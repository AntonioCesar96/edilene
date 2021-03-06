package br.com.eventos.rest;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import br.com.eventos.model.Evento;
import br.com.eventos.model.Filtro;
import br.com.eventos.model.Usuario;
import br.com.eventos.model.dto.EventoFullDTO;
import br.com.eventos.model.dto.EventosFeedDTO;
import br.com.eventos.service.EventosService;
import br.com.eventos.service.UploadService;

@Path("/evento")
@Consumes(MediaType.APPLICATION_JSON + ";charset=utf-8")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
@Component
public class EventosResource {

	@Autowired
	private EventosService service;

	@Autowired
	private UploadService uploadService;

	@POST
	@Path("/proximos/{page}/{max}")
	public List<EventosFeedDTO> getProximosEventosPaginado(Filtro filtro, @PathParam("page") int page,
			@PathParam("max") int max) {
		return service.getProximosEventos(filtro, page, max);
	}

	@GET
	@Path("/usuario/{id}")
	public List<EventosFeedDTO> getEventosPorUsuario(@PathParam("id") Long idUsuario) {
		return service.getEventosPorUsuario(idUsuario);
	}

	@GET
	@Path("/{id}")
	public EventoFullDTO getEventoPorId(@PathParam("id") Long idEvento) {
		return service.getEventosPorId(idEvento);
	}

	@POST
	public Response save(Evento e) {
		service.save(e);
		return Response.Ok("Evento salvo com sucesso");
	}

	@PUT
	public Response update(Evento e) {
		service.update(e);
		return Response.Ok("Evento atualizado com sucesso");
	}

	@POST
	@Path("/excluir/{id}")
	public Response excluir(Usuario u, @PathParam("id") Long id) {
		return service.excluir(u, id);
	}

	@POST
	@Path("/postFotoBase64")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public ResponseWithURL postFotoBase64(@FormParam("fileName") String fileName, @FormParam("base64") String base64) {
		if (fileName != null && base64 != null) {
			try {
				// Faz o upload
				String url = uploadService.upload(base64);
				// OK
				return ResponseWithURL.Ok("Arquivo recebido com sucesso", url);
			} catch (Exception e) {
				e.printStackTrace();
				return ResponseWithURL.Error("Erro ao enviar o arquivo.");
			}
		}
		return ResponseWithURL.Error("Requisição inválida.");
	}
}
