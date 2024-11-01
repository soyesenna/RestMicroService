package microservice.context;

import java.util.HashMap;
import java.util.Map;

public class MicroServiceContext {
    private static final ThreadLocal<Map<ContextKey, String>> CONTEXT = new ThreadLocal<>();

    public static void setRequestId(String requestId) {
        if (CONTEXT.get() == null) {
            CONTEXT.set(new HashMap<>());
        }
        CONTEXT.get().put(ContextKey.REQUEST_ID, requestId);
    }

    public static void setRootClientId(String rootClientId) {
        if (CONTEXT.get() == null) {
            CONTEXT.set(new HashMap<>());
        }
        CONTEXT.get().put(ContextKey.ROOT_CLIENT_ID, rootClientId);
    }

    public static String getRequestId() {
        return CONTEXT.get().get(ContextKey.REQUEST_ID);
    }

    public static String getRootClientId() {
        return CONTEXT.get().get(ContextKey.ROOT_CLIENT_ID);
    }

    public static void setMyClientId(String myClientId) {
        if (CONTEXT.get() == null) {
            CONTEXT.set(new HashMap<>());
        }
        CONTEXT.get().put(ContextKey.MY_CLIENT_ID, myClientId);
    }

    public static String getMyClientId() {
        return CONTEXT.get().get(ContextKey.MY_CLIENT_ID);
    }

    public static boolean isContextEmpty() {
        return CONTEXT.get() == null;
    }

    public static void clear() {
        CONTEXT.remove();
    }
}
