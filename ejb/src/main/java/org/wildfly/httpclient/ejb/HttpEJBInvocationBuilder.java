package org.wildfly.httpclient.ejb;

import java.lang.reflect.Method;

import io.undertow.client.ClientRequest;
import io.undertow.util.Headers;
import io.undertow.util.Methods;


/**
 * Builder for invocations against a specific EJB, such as invocation and session open
 *
 * @author Stuart Douglas
 */
class HttpEJBInvocationBuilder {

    private static final String INVOCATION_ACCEPT = "application/x-wf-ejb-response;version=1,application/x-wf-jbmar-exception;version=1";
    private static final String STATEFUL_CREATE_ACCEPT = "application/x-wf-jbmar-exception;version=1";

    private String appName;
    private String moduleName;
    private String distinctName;
    private String beanName;
    private String beanId;
    private String view;
    private Method method;
    private InvocationType invocationType;
    private Long invocationId;
    private int version = 1;
    private boolean cancelIfRunning;

    public String getAppName() {
        return appName;
    }

    public HttpEJBInvocationBuilder setAppName(String appName) {
        this.appName = appName;
        return this;
    }

    public String getModuleName() {
        return moduleName;
    }

    public HttpEJBInvocationBuilder setModuleName(String moduleName) {
        this.moduleName = moduleName;
        return this;
    }

    public String getDistinctName() {
        return distinctName;
    }

    public HttpEJBInvocationBuilder setDistinctName(String distinctName) {
        this.distinctName = distinctName;
        return this;
    }

    public String getBeanName() {
        return beanName;
    }

    public HttpEJBInvocationBuilder setBeanName(String beanName) {
        this.beanName = beanName;
        return this;
    }

    public String getBeanId() {
        return beanId;
    }

    public HttpEJBInvocationBuilder setBeanId(String beanId) {
        this.beanId = beanId;
        return this;
    }

    public Method getMethod() {
        return method;
    }

    public HttpEJBInvocationBuilder setMethod(Method method) {
        this.method = method;
        return this;
    }

    public String getView() {
        return view;
    }

    public HttpEJBInvocationBuilder setView(String view) {
        this.view = view;
        return this;
    }

    public InvocationType getInvocationType() {
        return invocationType;
    }

    public HttpEJBInvocationBuilder setInvocationType(InvocationType invocationType) {
        this.invocationType = invocationType;
        return this;
    }

    public Long getInvocationId() {
        return invocationId;
    }

    public HttpEJBInvocationBuilder setInvocationId(Long invocationId) {
        this.invocationId = invocationId;
        return this;
    }

    public int getVersion() {
        return version;
    }

    public HttpEJBInvocationBuilder setVersion(int version) {
        this.version = version;
        return this;
    }

    /**
     * Constructs an EJB invocation path
     *
     * @param mountPoint   The mount point of the EJB context
     * @param appName      The application name
     * @param moduleName   The module name
     * @param distinctName The distinct name
     * @param beanName     The bean name
     * @return The request path to invoke
     */
    private String buildPath(final String mountPoint, String type, final String appName, final String moduleName, final String distinctName, final String beanName) {
        StringBuilder sb = new StringBuilder();
        buildBeanPath(mountPoint, type, appName, moduleName, distinctName, beanName, sb);
        return sb.toString();
    }

    /**
     * Constructs an EJB invocation path
     *
     * @param mountPoint   The mount point of the EJB context
     * @param appName      The application name
     * @param moduleName   The module name
     * @param distinctName The distinct name
     * @param beanName     The bean name
     * @return The request path to invoke
     */
    private String buildPath(final String mountPoint, String type, final String appName, final String moduleName, final String distinctName, final String beanName, boolean cancelIfRunning) {
        StringBuilder sb = new StringBuilder();
        buildBeanPath(mountPoint, type, appName, moduleName, distinctName, beanName, sb);
        sb.append("/");
        sb.append(Boolean.toString(cancelIfRunning));
        return sb.toString();
    }
    /**
     * Constructs an EJB invocation path
     *
     * @param mountPoint   The mount point of the EJB context
     * @param appName      The application name
     * @param moduleName   The module name
     * @param distinctName The distinct name
     * @param beanName     The bean name
     * @param beanId       The bean id
     * @return The request path to invoke
     */
    private String buildPath(final String mountPoint, String type, final String appName, final String moduleName, final String distinctName, final String beanName, final String beanId, final String view, final Method method) {
        StringBuilder sb = new StringBuilder();
        buildBeanPath(mountPoint, type, appName, moduleName, distinctName, beanName, sb);
        sb.append("/");
        if (beanId == null) {
            sb.append("-");
        } else {
            sb.append(beanId);
        }
        sb.append("/");
        sb.append(view);
        sb.append("/");
        sb.append(method.getName());
        for (Class<?> param : method.getParameterTypes()) {
            sb.append("/");
            sb.append(param.getName());
        }
        return sb.toString();
    }

    private void buildBeanPath(String mountPoint, String type, String appName, String moduleName, String distinctName, String beanName, StringBuilder sb) {
        buildModulePath(mountPoint, type, appName, moduleName, distinctName, sb);
        sb.append("/");
        sb.append(beanName);
    }

    private void buildModulePath(String mountPoint, String type, String appName, String moduleName, String distinctName, StringBuilder sb) {
        if (mountPoint != null) {
            sb.append(mountPoint);
        }
        sb.append("/ejb/v");
        sb.append(version);
        sb.append("/");
        sb.append(type);
        sb.append("/");
        if (appName == null || appName.isEmpty()) {
            sb.append("-");
        } else {
            sb.append(appName);
        }
        sb.append("/");
        if (moduleName == null || moduleName.isEmpty()) {
            sb.append("-");
        } else {
            sb.append(moduleName);
        }
        sb.append("/");
        if (distinctName == null || distinctName.isEmpty()) {
            sb.append("-");
        } else {
            sb.append(distinctName);
        }
    }

    public ClientRequest createRequest(String mountPoint) {
        ClientRequest clientRequest = new ClientRequest();
        if (invocationType == InvocationType.METHOD_INVOCATION) {
            clientRequest.setMethod(Methods.POST);
            clientRequest.getRequestHeaders().add(Headers.ACCEPT, INVOCATION_ACCEPT);
            if (invocationId != null) {
                clientRequest.getRequestHeaders().put(EjbHeaders.INVOCATION_ID, invocationId);
            }
            clientRequest.setPath(buildPath(mountPoint, "invoke", appName, moduleName, distinctName, beanName, beanId, view, method));
            clientRequest.getRequestHeaders().put(Headers.CONTENT_TYPE, EjbHeaders.INVOCATION_VERSION_ONE);
        } else if (invocationType == InvocationType.STATEFUL_CREATE) {
            clientRequest.setMethod(Methods.POST);
            clientRequest.getRequestHeaders().put(Headers.CONTENT_TYPE, EjbHeaders.SESSION_OPEN_VERSION_ONE);
            clientRequest.setPath(buildPath(mountPoint,"open", appName, moduleName, distinctName, beanName));
            clientRequest.getRequestHeaders().add(Headers.ACCEPT, STATEFUL_CREATE_ACCEPT);
        } else if(invocationType == InvocationType.CANCEL) {
            clientRequest.setMethod(Methods.DELETE);
            clientRequest.setPath(buildPath(mountPoint,"cancel", appName, moduleName, distinctName, beanName));
        }
        return clientRequest;
    }

    public HttpEJBInvocationBuilder setCancelIfRunning(boolean cancelIfRunning) {
        this.cancelIfRunning = cancelIfRunning;
        return this;
    }

    public boolean isCancelIfRunning() {
        return cancelIfRunning;
    }


    public enum InvocationType {
        METHOD_INVOCATION,
        STATEFUL_CREATE,
        CANCEL,
    }

}
