package gg.neko.gfl.gfldataminer.service.reactor;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.function.Supplier;

@Component
@AllArgsConstructor
public class BlockingWrapper {

    private final SchedulerProvider schedulerProvider;

    public <T> Mono<T> wrapBlockingCall(Supplier<T> blockingSupplier) {
        return Mono.fromCallable(blockingSupplier::get)
                   .subscribeOn(schedulerProvider.defaultScheduler());
    }

}
