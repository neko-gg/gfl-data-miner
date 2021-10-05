package gg.neko.gfl.gfldataminer.service.reactor;

import org.springframework.stereotype.Component;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

@Component
public class SchedulerProvider {

    private static final Scheduler BOUNDED_ELASTIC_SCHEDULER = Schedulers.boundedElastic();

    public Scheduler defaultScheduler() {
        return BOUNDED_ELASTIC_SCHEDULER;
    }

}
