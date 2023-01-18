package tech.tresearchgroup.audiofingerprinter.view;

import io.activej.csp.file.ChannelFileWriter;
import io.activej.http.HttpMethod;
import io.activej.http.HttpResponse;
import io.activej.http.MultipartDecoder;
import io.activej.http.RoutingServlet;
import io.activej.inject.annotation.Provides;
import io.activej.inject.module.AbstractModule;
import tech.tresearchgroup.fpcalc.FPCalc;
import tech.tresearchgroup.fpcalc.model.FPCalcOptions;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executor;

import static java.util.concurrent.Executors.newSingleThreadExecutor;

public class Endpoint extends AbstractModule {
    @Provides
    Executor executor() {
        return newSingleThreadExecutor();
    }

    @Provides
    public RoutingServlet servlet() {
        return RoutingServlet.create()
                .map(HttpMethod.POST, "/submit", request -> {
                    Path file = new File("temp/" + UUID.randomUUID() + ".tmp").toPath();
                    return request.handleMultipart(MultipartDecoder.MultipartDataHandler.file(fileName ->
                                    ChannelFileWriter.open(executor(), file)))
                            .map($ -> calculateFingerprint(file));
                });
    }

    public HttpResponse calculateFingerprint(Path file) {
        FPCalc fpCalc = new FPCalc();
        FPCalcOptions fpCalcOptions = new FPCalcOptions();
        fpCalcOptions.setJson(true);
        fpCalc.setFile("temp/" + file.getFileName());
        fpCalc.setFpCalcOptions(fpCalcOptions);
        String json = fpCalc.getFingerprint();
        try {
            Files.delete(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return HttpResponse.ok200().withJson(json);
    }
}
