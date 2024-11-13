package microservice.context;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import microservice.exception.MicroServiceContextException;
import microservice.templates.dtos.SetCookieWrapper;

public class MicroServiceContext {
    private static final ThreadLocal<Map<ContextKey, Object>> CONTEXT = new ThreadLocal<>();

    public static void setRequestId(String requestId) {
        CONTEXT.get().put(ContextKey.REQUEST_ID, requestId);
    }

    public static void setRootClientId(String rootClientId) {
        CONTEXT.get().put(ContextKey.ROOT_CLIENT_ID, rootClientId);
    }

    public static String getRequestId() {
        try {
            return (String) CONTEXT.get().get(ContextKey.REQUEST_ID);
        } catch (NullPointerException e) {
            throw new MicroServiceContextException(ContextKey.REQUEST_ID.name());
        }
    }

    public static String getRootClientId() {
        try {
            return (String) CONTEXT.get().get(ContextKey.ROOT_CLIENT_ID);
        } catch (NullPointerException e) {
            throw new MicroServiceContextException(ContextKey.ROOT_CLIENT_ID.name());
        }
    }

    public static void setMyClientId(String myClientId) {
        CONTEXT.get().put(ContextKey.MY_CLIENT_ID, myClientId);
    }

    public static String getMyClientId() {
        try {
            return (String) CONTEXT.get().get(ContextKey.MY_CLIENT_ID);
        } catch (NullPointerException e) {
            throw new MicroServiceContextException(ContextKey.MY_CLIENT_ID.name());
        }
    }

    public static void setMetaInfo(Map<String, Object> metaInfo) {
        CONTEXT.get().put(ContextKey.META_INFO, metaInfo);
    }

    public static Map<String, Object> getMetaInfo() {
        try {
            return (Map<String, Object>) CONTEXT.get().get(ContextKey.META_INFO);
        } catch (NullPointerException e) {
            throw new MicroServiceContextException(ContextKey.META_INFO.name());
        }
    }

    public static void setHttpStatus(int httpStatus) {
        CONTEXT.get().put(ContextKey.HTTP_STATUS, httpStatus);
    }

    public static Integer getHttpStatus() {
        try {
            return (Integer) CONTEXT.get().get(ContextKey.HTTP_STATUS);
        } catch (NullPointerException e) {
            throw new MicroServiceContextException(ContextKey.HTTP_STATUS.name());
        }
    }

    public static void setSetCookies(List<SetCookieWrapper> setCookies) {
        CONTEXT.get().put(ContextKey.SET_COOKIES, setCookies);
    }

    public static List<SetCookieWrapper> getSetCookies() {
        try {
            return (List<SetCookieWrapper>) CONTEXT.get().get(ContextKey.SET_COOKIES);
        } catch (NullPointerException e) {
            throw new MicroServiceContextException(ContextKey.SET_COOKIES.name());
        }
    }

    public static void addSetCookies(SetCookieWrapper setCookies) {
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

    public static void init() {
        CONTEXT.set(new HashMap<>());
    }
}
