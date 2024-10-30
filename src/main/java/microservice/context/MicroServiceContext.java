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

    public static void setClientId(String clientId) {
        if (CONTEXT.get() == null) {
            CONTEXT.set(new HashMap<>());
        }
        CONTEXT.get().put(ContextKey.CLIENT_ID, clientId);
    }

    public static String getRequestId() {
        return CONTEXT.get().get(ContextKey.REQUEST_ID);
    }

    public static String getClientId() {
        return CONTEXT.get().get(ContextKey.CLIENT_ID);
    }

    public static boolean isContextEmpty() {
        return CONTEXT.get() == null;
    }

    public static void clear() {
        CONTEXT.remove();
    }
}
