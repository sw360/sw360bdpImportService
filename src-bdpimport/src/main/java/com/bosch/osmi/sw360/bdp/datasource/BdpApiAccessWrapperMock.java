/*
 * Copyright (c) Bosch Software Innovations GmbH 2015.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.bosch.osmi.sw360.bdp.datasource;

import com.bosch.osmi.bdp.access.api.model.ProjectInfo;
import com.bosch.osmi.bdp.access.api.model.User;
import com.google.common.base.Strings;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

public class BdpApiAccessWrapperMock implements BdpApiAccessWrapper {

    private static final String MOCKDATA_CLASSPATH_LOCATION = "/mockdata.json";
    private static final Logger LOGGER = LogManager.getLogger(BdpApiAccessWrapperMock.class);

    private final User user;

    public BdpApiAccessWrapperMock() {
        this(new InputStreamReader(BdpApiAccessWrapperMock.class.getResourceAsStream(MOCKDATA_CLASSPATH_LOCATION)));
        LOGGER.info("Initialize mock implementation with data from classpath.");
    }

    public BdpApiAccessWrapperMock(String jsonFilePath) throws FileNotFoundException {
        this(new FileReader(jsonFilePath));
        LOGGER.info("Initialize mock implementation with data from " + jsonFilePath + ".");
    }

    private BdpApiAccessWrapperMock(Reader jsonReader) {
        JsonObject sourceFile = readJsonFile(jsonReader);
        user = retrieveUser(sourceFile);
    }

    private User retrieveUser(JsonObject sourceFile) {
        JsonInvocationHandler handler = new JsonInvocationHandler(sourceFile);
        return (User) Proxy.newProxyInstance(
                User.class.getClassLoader(),
                new Class[]{User.class},
                handler);
    }

    private JsonObject readJsonFile(Reader jsonReader) {
        // Read from File to String
        JsonParser parser = new JsonParser();
        JsonElement jsonElement = parser.parse(jsonReader);
        return jsonElement.getAsJsonObject();
    }

    @Override
    public boolean validateCredentials() {
        return true;
    }

    @Override
    public Collection<ProjectInfo> getUserProjectInfos() {
        return user.getProjectInfos();
    }

    @Override
    public Collection<ProjectInfo> suggestProjectInfos(String projectName) {
        return user.getProjectInfos()
                .stream()
                .filter(projectInfo -> Strings.nullToEmpty(projectInfo.getProjectName()).startsWith(projectName))
                .collect(Collectors.toList());
    }

    @Override
    public ProjectInfo getProjectInfo(String bdpId) {
        return getUserProjectInfos()
                .stream()
                .filter(projectInfo -> bdpId.equals(projectInfo.getProjectId()))
                .findAny()
                .orElse(null);
    }

    /**
     * InvocationHandler that handles incoming method requests to api methods and forwards them to the respective entry
     * in a json object with the data. The json-object is retrieved when an object of the host class is created.  The
     * invocation handler is registered at a proxy object that acts as an implementation of the bdp api.
     */
    private static class JsonInvocationHandler implements InvocationHandler {
        // http://tutorials.jenkov.com/java-reflection/dynamic-proxies.html

        private JsonObject context = null;

        JsonInvocationHandler(JsonObject context) {
            this.context = context;
        }

        /**
         * called every time a method on the api is called.
         */
        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            // With the type- and method name we search the corresponding entries in the json document
            String typeName = method.getDeclaringClass().getCanonicalName();
            String methodName = method.getName();

            if (Object.class == method.getDeclaringClass()) {
                if ("equals".equals(methodName)) {
                    return proxy == args[0];
                } else if ("hashCode".equals(methodName)) {
                    return System.identityHashCode(proxy);
                } else if ("toString".equals(methodName)) {
                    return proxy.getClass().getName() + "@" +
                            Integer.toHexString(System.identityHashCode(proxy)) +
                            ", with InvocationHandler " + this;
                } else {
                    throw new IllegalStateException(String.valueOf(method));
                }
            }

            // We search on the Json document that is stored in context. Every JsonInvocationHandler
            JsonElement jsonElement = context.get(typeName);
            JsonObject jsonObject = jsonElement.getAsJsonObject();

            JsonElement element = jsonObject.get(methodName);

            if (element.isJsonPrimitive()) {
                // Maybe add some return type check at method object
                return element.getAsString();
            } else if (element.isJsonArray()) {
                JsonArray content = element.getAsJsonArray();
                return constructCollection(content);
            } else if (element.isJsonObject()) {
                JsonObject content = element.getAsJsonObject();
                return constructObject(content);
            } else {
                throw new IllegalStateException("No proper way to handle " + element.getClass().getCanonicalName() +
                        " implemented");
            }
        }


        private Collection<Object> constructCollection(JsonArray array) throws ClassNotFoundException {
            Collection<Object> result = new ArrayList<>();
            for (JsonElement element : array) {
                if (element.isJsonPrimitive()) {
                    result.add(element.getAsString());
                } else if (element.isJsonObject()) {
                    JsonObject jsonObject = element.getAsJsonObject();
                    Object proxy = constructObject(jsonObject);
                    result.add(proxy);
                } else {
                    throw new IllegalStateException("Illegal entry in json document: " + element);
                }
            }

            return result;
        }

        private Object constructObject(JsonObject jsonObject) throws ClassNotFoundException {
            Map.Entry<String, JsonElement> entry = jsonObject.entrySet().iterator().next();
            String typeName = entry.getKey();
            Class<?> type = loadClass(typeName);
            JsonInvocationHandler handler = new JsonInvocationHandler(jsonObject);
            return Proxy.newProxyInstance(
                    JsonInvocationHandler.class.getClassLoader(),
                    new Class[]{type}, handler);
        }

        private Class<?> loadClass(String typeName) throws ClassNotFoundException {
            // http://tutorials.jenkov.com/java-reflection/dynamic-class-loading-reloading.html
            ClassLoader classLoader = JsonInvocationHandler.class.getClassLoader();
            return classLoader.loadClass(typeName);
        }
    }
}
