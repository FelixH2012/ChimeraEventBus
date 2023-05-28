package com.felix.chimera.base.implementation.event;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;

public class EventManager {

    public interface EventListener<T extends Event> {
        void onEvent(T event);
        default int getPriority() {
            return 0;
        }
    }

    private final ConcurrentHashMap<Type, PriorityQueue<EventListener<?>>> listenerMap = new ConcurrentHashMap<>();
    private final Map<Class<?>, Field[]> declaredFieldsCache = new HashMap<>();

    private Field[] getCachedDeclaredFields(final Class<?> clazz) {
        return declaredFieldsCache.computeIfAbsent(clazz, Class::getDeclaredFields);
    }

    public void subscribe(final Object o) {
        modifyEventListenerState(o, (type, listener) ->
                listenerMap.computeIfAbsent(type, k -> new PriorityQueue<>(Comparator.comparingInt((EventListener<?> eventListener) -> eventListener.getPriority()).reversed())).add(listener));

    }

    public <T extends Event> void triggerEvent(T event) {
        Type eventType = event.getClass();
        PriorityQueue<EventListener<?>> listeners = listenerMap.get(eventType);

        if (listeners != null) {
            for (EventListener<?> listener : listeners) {
                @SuppressWarnings("unchecked")
                EventListener<T> castedListener = (EventListener<T>) listener;
                castedListener.onEvent(event);
            }
        }
    }


    public void unsubscribe(final Object o) {
        modifyEventListenerState(o, (type, listener) ->
                listenerMap.computeIfPresent(type, (k, v) -> {
                    v.remove(listener);
                    return v.isEmpty() ? null : v;
                }));
    }

    private void modifyEventListenerState(final Object o, final BiConsumer<Type, EventListener<?>> eventListenerBiConsumer) {
        final Class<?> type = o.getClass();
        for (final Field field : getCachedDeclaredFields(type)) {
            if (field.getType() == EventListener.class) {
                final EventListener<?> eventListener = getEventListener(o, field);
                final Type eventType = ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0];
                eventListenerBiConsumer.accept(eventType, eventListener);
            }
        }
    }

    private EventListener<?> getEventListener(final Object o, final Field field) {
        final boolean accessible = field.canAccess(o);
        field.setAccessible(true);
        EventListener<?> fieldSubscription = null;
        try {
            fieldSubscription = (EventListener<?>) field.get(o);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } finally {
            field.setAccessible(accessible);
        }
        return fieldSubscription;
    }
}