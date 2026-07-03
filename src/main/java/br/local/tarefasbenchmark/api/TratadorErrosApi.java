package br.local.tarefasbenchmark.api;

import br.local.tarefasbenchmark.api.dto.ErroResposta;
import br.local.tarefasbenchmark.aplicacao.RecursoNaoEncontradoException;
import br.local.tarefasbenchmark.aplicacao.RegraNegocioException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class TratadorErrosApi {

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErroResposta tratarNaoEncontrado(RecursoNaoEncontradoException excecao) {
        return new ErroResposta(excecao.getMessage());
    }

    @ExceptionHandler(RegraNegocioException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErroResposta tratarRegraNegocio(RegraNegocioException excecao) {
        return new ErroResposta(excecao.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErroResposta tratarValidacao(MethodArgumentNotValidException excecao) {
        String mensagem = excecao.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(erro -> erro.getDefaultMessage())
                .orElse("Requisicao invalida.");
        return new ErroResposta(mensagem);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErroResposta tratarCorpoInvalido(HttpMessageNotReadableException excecao) {
        return new ErroResposta("Corpo da requisicao invalido ou mal formado.");
    }
}
