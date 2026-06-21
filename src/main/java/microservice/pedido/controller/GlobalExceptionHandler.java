package microservice.pedido.controller;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import microservice.pedido.exception.ResourceNotFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(ResourceNotFoundException.class)
	public ResponseEntity<Map<String, Object>> manejarNoEncontrado(ResourceNotFoundException exception) {
		return crearRespuesta(HttpStatus.NOT_FOUND, exception.getMessage());
	}

	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<Map<String, Object>> manejarSolicitudInvalida(IllegalArgumentException exception) {
		return crearRespuesta(HttpStatus.BAD_REQUEST, exception.getMessage());
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<Map<String, Object>> manejarValidacion(MethodArgumentNotValidException exception) {
		String mensaje = exception.getBindingResult().getFieldErrors().stream()
				.findFirst()
				.map(error -> error.getDefaultMessage())
				.orElse("Solicitud invalida");
		return crearRespuesta(HttpStatus.BAD_REQUEST, mensaje);
	}

	private ResponseEntity<Map<String, Object>> crearRespuesta(HttpStatus status, String mensaje) {
		Map<String, Object> respuesta = new LinkedHashMap<>();
		respuesta.put("timestamp", LocalDateTime.now());
		respuesta.put("status", status.value());
		respuesta.put("error", status.getReasonPhrase());
		respuesta.put("message", mensaje);
		return ResponseEntity.status(status).body(respuesta);
	}
}
