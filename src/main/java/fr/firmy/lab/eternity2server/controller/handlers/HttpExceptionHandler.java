package fr.firmy.lab.eternity2server.controller.handlers;

import com.fasterxml.jackson.databind.JsonMappingException;
import fr.firmy.lab.eternity2server.controller.exception.*;
import fr.firmy.lab.eternity2server.model.dto.ErrorDescription;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@ControllerAdvice
public class HttpExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(value = { JobSizeException.class, JobUpdateFailedException.class })
    protected ResponseEntity<Object> handleSingleError(AbstractSingleErrorException ex, ServletWebRequest request) {

        ErrorDescription error = ex.getError();
        error.setRequestURI( request.getRequest().getRequestURI() );
        error.setParameters( request.getRequest().getParameterMap() );

        return handleExceptionInternal(ex, error, new HttpHeaders(), HttpStatus.valueOf(error.getStatus()), request);
    }

    @ExceptionHandler(value = { JobDevelopmentFailedException.class, ResultSubmissionFailedException.class, JobPruneFailedException.class })
    protected ResponseEntity<Object> handleMultipleErrors(AbstractMultipleErrorsException ex, ServletWebRequest request) {

        List<ErrorDescription> errors = ex.getErrors();
        Set<Integer> statuses = errors.stream().map( ErrorDescription::getStatus ).collect(Collectors.toSet());
        HttpStatus resultingStatus = statuses.size() > 1 ? HttpStatus.MULTI_STATUS : HttpStatus.valueOf(statuses.iterator().next());
        for( ErrorDescription error: errors ) {
            error.setRequestURI( request.getRequest().getRequestURI() );
            error.setParameters( request.getRequest().getParameterMap() );
        }

        return handleExceptionInternal(ex, errors, new HttpHeaders(), resultingStatus, request);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {

        ErrorDescription error = null;
        Throwable cause = ex.getCause();
        if( cause instanceof JsonMappingException) {
            Throwable cause2 = cause.getCause();
            if( cause2 instanceof BoardDescriptionParseException ) {
                BoardDescriptionParseException bdpe = (BoardDescriptionParseException)cause2;
                Throwable cause3 = bdpe.getCause();
                if( cause3 instanceof MalformedBoardDescriptionException ) {
                    MalformedBoardDescriptionException mbde = (MalformedBoardDescriptionException)cause3;
                    error = mbde.getError();
                }

            }
        } else {
            error = new ErrorDescription(HttpStatus.BAD_REQUEST, ((ServletWebRequest)request).getRequest().getRequestURI(), ex.getMessage() );
        }

        ServletWebRequest servletRequest = (ServletWebRequest)request;
        error.setRequestURI( servletRequest.getRequest().getRequestURI() );
        error.setParameters( servletRequest.getRequest().getParameterMap() );

        return handleExceptionInternal(ex, error, headers, HttpStatus.valueOf(error.getStatus()), request);
    }

    @Override
    protected ResponseEntity<Object> handleMissingServletRequestParameter(
        MissingServletRequestParameterException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {

        ErrorDescription error = new ErrorDescription(HttpStatus.BAD_REQUEST, ((ServletWebRequest)request).getRequest().getRequestURI(), ex.getMessage());

        ServletWebRequest servletRequest = (ServletWebRequest)request;
        error.setRequestURI( servletRequest.getRequest().getRequestURI() );
        error.setParameters( servletRequest.getRequest().getParameterMap() );

        return handleExceptionInternal(ex, error, headers, HttpStatus.valueOf(error.getStatus()), request);
    }

}
