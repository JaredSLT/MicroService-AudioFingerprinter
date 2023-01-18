package tech.tresearchgroup.audiofingerprinter;

import tech.tresearchgroup.audiofingerprinter.view.Endpoint;
import io.activej.http.AsyncServlet;
import io.activej.http.RoutingServlet;
import io.activej.inject.Injector;
import io.activej.inject.Key;
import io.activej.inject.binding.Multibinder;
import io.activej.inject.binding.Multibinders;
import io.activej.inject.module.Module;
import io.activej.inject.module.ModuleBuilder;
import io.activej.inject.module.Modules;
import io.activej.launchers.http.MultithreadedHttpServerLauncher;
import io.activej.worker.annotation.Worker;

public class AudioFingerprinter extends MultithreadedHttpServerLauncher {
    public static final Multibinder<RoutingServlet> SERVLET_MULTIBINDER =
            Multibinders.ofBinaryOperator((servlet1, servlet2) -> servlet1.merge(servlet2));

    public static void main(String[] args) throws Exception {
        new AudioFingerprinter().launch(args);
    }

    @Override
    @Worker
    public Module getBusinessLogicModule() {
        try {
            Injector.useSpecializer();
            return Modules.combine(
                    new Endpoint(),
                    ModuleBuilder.create()
                            .bind(AsyncServlet.class)
                            .to(RoutingServlet.class)
                            .multibind(Key.of(RoutingServlet.class), SERVLET_MULTIBINDER)
                            .build()
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onStart() throws Exception {
        super.onStart();
    }

    @Override
    protected void onStop() throws Exception {
        System.out.println("Finished.");
        super.onStop();
    }
}
