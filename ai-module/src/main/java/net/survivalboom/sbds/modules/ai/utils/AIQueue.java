package net.survivalboom.sbds.modules.ai.utils;

import io.github.sashirestela.cleverclient.Event;
import io.github.sashirestela.openai.domain.response.Response;
import io.github.sashirestela.openai.domain.response.ResponseRequest;
import net.survivalboom.sbds.api.scheduler.ISchedulerTask;
import net.survivalboom.sbds.api.utils.CommonUtils;
import net.survivalboom.sbds.api.utils.Manager;
import net.survivalboom.sbds.modules.ai.AIModule;
import org.jetbrains.annotations.NotNull;

import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Stream;

public class AIQueue extends Manager {

    private final AIModule module;

    private final Queue<Request> queue = new ConcurrentLinkedQueue<>();

    private ISchedulerTask task;

    public AIQueue(@NotNull AIModule module) {
        this.module = module;
    }


    @Override
    protected void init0() {
        this.task = module.getSbds().getScheduler().schedule(module, "AIQueue", this::task, 0, 100);
    }

    @Override
    protected void shutdown0() {

        task.cancel();

        if (!queue.isEmpty()) {
            CommonUtils.waitUntil(queue::isEmpty, 60000, 5000, () -> module.getLogger().warn("There is `{}` requests to AI in queue. Waiting them to finish...", queue.size()));
            module.getLogger().info("All requests were completed. Shutting down...");
        }

        task.waitForCancel(5000);
        this.task = null;

    }


    private void task() {

        if (queue.isEmpty()) {
            return;
        }

        var request = queue.poll();

        var result = module.getManager().responses().createStream(request.request).join();

        request.future.complete(result);

    }


    public @NotNull CompletableFuture<Stream<Event>> request(@NotNull ResponseRequest request) {

        CompletableFuture<Stream<Event>> future = new CompletableFuture<>();

        queue.add(new Request(request, future));

        return future;

    }


    record Request(@NotNull ResponseRequest request, @NotNull CompletableFuture<Stream<Event>> future) {}

}
