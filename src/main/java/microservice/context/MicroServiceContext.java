package microservice.context;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import microservice.templates.dtos.SetCookieWrapper;

public class MicroServiceContext {
    private static final ThreadLocal<Map<ContextKey, Object>> CONTEXT = new ThreadLocal<>();

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
        return (String) CONTEXT.get().get(ContextKey.REQUEST_ID);
    }

    public static String getRootClientId() {
        return (String) CONTEXT.get().get(ContextKey.ROOT_CLIENT_ID);
    }

    public static void setMyClientId(String myClientId) {
        if (CONTEXT.get() == null) {
            CONTEXT.set(new HashMap<>());
        }
        CONTEXT.get().put(ContextKey.MY_CLIENT_ID, myClientId);
    }

    public static String getMyClientId() {
        return (String) CONTEXT.get().get(ContextKey.MY_CLIENT_ID);
    }

    public static void setMetaInfo(Map<String, Object> metaInfo) {
        if (CONTEXT.get() == null) {
            CONTEXT.set(new HashMap<>());
        }
        CONTEXT.get().put(ContextKey.META_INFO, metaInfo);
    }

    public static Map<String, Object> getMetaInfo() {
        return (Map<String, Object>) CONTEXT.get().get(ContextKey.META_INFO);
    }

    public static void setHttpStatus(int httpStatus) {
        if (CONTEXT.get() == null) {
            CONTEXT.set(new HashMap<>());
        }
        CONTEXT.get().put(ContextKey.HTTP_STATUS, httpStatus);
    }

    public static Integer getHttpStatus() {
        if (CONTEXT.get().containsKey(ContextKey.HTTP_STATUS)) {
            return (Integer) CONTEXT.get().get(ContextKey.HTTP_STATUS);
        }
        return null;
    }

    public static void setSetCookies(List<SetCookieWrapper> setCookies) {
        if (CONTEXT.get() == null) {
            CONTEXT.set(new HashMap<>());
        }
        CONTEXT.get().put(ContextKey.SET_COOKIES, setCookies);
    }

    public static List<SetCookieWrapper> getSetCookies() {
        if (CONTEXT.get().containsKey(ContextKey.SET_COOKIES)) {
            return (List<SetCookieWrapper>) CONTEXT.get().get(ContextKey.SET_COOKIES);
        }
        return null;
    }

    public static void addSetCookies(SetCookieWrapper setCookies) {
        if (CONTEXT.get() == null) {
            CONTEXT.set(new HashMap<>());
        }
        if (!CONTEXT.get().containsKey(ContextKey.SET_COOKIES)) {
            CONTEXT.get().put(ContextKey.SET_COOKIES, new ArrayList<>());
        }
        ((List<SetCookieWrapper>) CONTEXT.get().get(ContextKey.SET_COOKIES)).add(setCookies);
    }

    public static boolean isContextEmpty() {
        return CONTEXT.get() == null;
    }

    public static void clear() {
        CONTEXT.remove();
    }
}
