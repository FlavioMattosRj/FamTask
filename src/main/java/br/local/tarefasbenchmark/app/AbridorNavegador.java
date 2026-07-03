package br.local.tarefasbenchmark.app;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.web.context.WebServerApplicationContext;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;

@Component
public class AbridorNavegador {

    private static final Logger log = LoggerFactory.getLogger(AbridorNavegador.class);

    private final boolean abrirNavegador;

    public AbridorNavegador(@Value("${app.abrirNavegador:true}") boolean abrirNavegador) {
        this.abrirNavegador = abrirNavegador;
    }

    @EventListener
    public void aoFicarPronta(ApplicationReadyEvent evento) {
        if (!abrirNavegador) {
            log.info("Abertura automatica do navegador desligada (app.abrirNavegador=false).");
            return;
        }
        if (!(evento.getApplicationContext() instanceof WebServerApplicationContext contextoWeb)) {
            return;
        }
        int porta = contextoWeb.getWebServer().getPort();
        String url = "http://localhost:" + porta + "/";
        log.info("Abrindo o navegador padrao em {}", url);
        abrir(url);
    }

    private void abrir(String url) {
        try {
            if (Desktop.isDesktopSupported()
                    && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(URI.create(url));
                return;
            }
        } catch (Exception excecao) {
            log.warn("Falha ao abrir o navegador via Desktop: {}", excecao.getMessage());
        }
        try {
            // Alternativa especifica do Windows (REQ-PREM-001)
            new ProcessBuilder("rundll32", "url.dll,FileProtocolHandler", url).start();
        } catch (IOException excecao) {
            log.warn("Nao foi possivel abrir o navegador automaticamente: {}. Acesse {} manualmente.",
                    excecao.getMessage(), url);
        }
    }
}
