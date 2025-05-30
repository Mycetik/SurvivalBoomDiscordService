package net.survivalboom.sbds.core.events;

import net.dv8tion.jda.api.events.GenericEvent;
import net.survivalboom.sbds.api.events.Listener;
import net.survivalboom.sbds.core.modules.Module;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EventHandler {

    private final Module module;

    private final Listener listener;

    private final Logger logger;

    private final Map<Method, Class<?>> eventMethods = new HashMap<>();


    public EventHandler(@NotNull Logger logger, @Nullable Module module, @NotNull Listener listener) {
        this.module = module;
        this.listener = listener;
        this.logger = logger;
    }


    public void scan() {

        eventMethods.clear();

        for (Method method : listener.getClass().getMethods()) {

            if (!method.isAnnotationPresent(net.survivalboom.sbds.api.events.EventHandler.class)) continue;

            Parameter[] parameters = method.getParameters();
            if (parameters.length != 1) continue;

            Parameter parameter = parameters[0];

            eventMethods.put(method, parameter.getType());

        }

    }


    public void onEvent(@NotNull GenericEvent event) {

        Class<?> clazz = event.getClass();
        List<Method> methods = eventMethods.entrySet().stream().filter(entry -> entry.getValue().equals(clazz)).map(Map.Entry::getKey).toList();

        for (Method method : methods) {

            try {
                method.invoke(listener, event);
            }

            catch (InvocationTargetException e) {
                Throwable t = e.getTargetException();
                logger.error("Could not pass {} to {}. ({}.{}())", clazz.getSimpleName(), module != null ? module : listener.getClass().getSimpleName(), listener.getClass().getSimpleName(), method.getName(), t);
            }

            catch (IllegalAccessException ignored) {}

        }

    }


    public @Nullable Module getModule() {
        return module;
    }

    public @NotNull Listener getListener() {
        return listener;
    }


}
