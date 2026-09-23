package me.kavishdevar.librepods.utils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import io.github.libxposed.api.XposedInterface;
import io.github.libxposed.api.XposedModule;

/**
 * Adapts the upstream interception callbacks to the pinned libxposed API 100 hook contract.
 *
 * @author WU
 */
public final class Api100Interception {
    private static final Map<Member, Entry> ENTRIES = new ConcurrentHashMap<>();

    private Api100Interception() {
    }

    @FunctionalInterface
    public interface Interceptor {
        Object intercept(Chain chain) throws Throwable;
    }

    private static final class Entry {
        final XposedModule module;
        final Interceptor interceptor;

        Entry(XposedModule module, Interceptor interceptor) {
            this.module = module;
            this.interceptor = interceptor;
        }
    }

    public static void install(XposedModule module, Member member, Interceptor interceptor) {
        Entry entry = new Entry(module, interceptor);
        ENTRIES.put(member, entry);
        try {
            if (member instanceof Method) {
                module.hook((Method) member, Callback.class);
            } else if (member instanceof Constructor<?>) {
                module.hook((Constructor<?>) member, Callback.class);
            } else {
                throw new IllegalArgumentException("Unsupported hook member: " + member);
            }
        } catch (RuntimeException | Error failure) {
            ENTRIES.remove(member, entry);
            throw failure;
        }
    }

    public static final class Chain {
        private final XposedInterface.BeforeHookCallback callback;
        private final XposedModule module;

        private Chain(XposedInterface.BeforeHookCallback callback, XposedModule module) {
            this.callback = callback;
            this.module = module;
        }

        public Object[] getArgs() {
            return callback.getArgs();
        }

        public Object getArg(int index) {
            return callback.getArgs()[index];
        }

        public Object getThisObject() {
            return callback.getThisObject();
        }

        public Object proceed() throws Throwable {
            return proceed(callback.getArgs());
        }

        /** Calls the unhooked original with the supplied arguments. */
        public Object proceed(Object[] args) throws Throwable {
            Member member = callback.getMember();
            if (member instanceof Method) {
                return module.invokeOrigin((Method) member, callback.getThisObject(), args);
            }
            Constructor<?> constructor = (Constructor<?>) member;
            Object instance = callback.getThisObject();
            module.invokeOrigin(constructor, instance, args);
            return instance;
        }
    }

    /** Dispatches callbacks registered for each reflected member. */
    public static final class Callback implements XposedInterface.Hooker {
        private Callback() {
        }

        public static void before(XposedInterface.BeforeHookCallback callback) {
            Entry entry = ENTRIES.get(callback.getMember());
            if (entry == null) return;
            try {
                callback.returnAndSkip(entry.interceptor.intercept(new Chain(callback, entry.module)));
            } catch (Throwable failure) {
                callback.throwAndSkip(failure);
            }
        }
    }
}
