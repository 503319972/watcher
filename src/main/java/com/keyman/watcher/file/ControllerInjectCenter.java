package com.keyman.watcher.file;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ControllerInjectCenter {
    private static final Logger log = LoggerFactory.getLogger(ControllerInjectCenter.class);

    private ControllerInjectCenter() {}

    public static void controlCenter(Class<?> controllerClass, ApplicationContext context, Integer type) {
        if (controllerClass == null) {
            throw new IllegalArgumentException("controller class cannot be null");
        }

        RequestMappingHandlerMapping requestMappingHandlerMapping =
                (RequestMappingHandlerMapping) context.getBean("requestMappingHandlerMapping");
        Method getMappingForMethod =
                ReflectionUtils.findMethod(RequestMappingHandlerMapping.class, "getMappingForMethod", Method.class, Class.class);

        if (getMappingForMethod == null) {
            throw new UnsupportedOperationException("no spring mvc method: RequestMappingHandlerMapping-getMappingForMethod");
        }

        try {
            getMappingForMethod.setAccessible(true);
            Method[] methodArr = controllerClass.getMethods();
            for (Method method : methodArr) {
                RequestMapping requestMapping = method.getAnnotation(RequestMapping.class);
                if (requestMapping != null) {
                    RequestMappingInfo mappingInfo = (RequestMappingInfo) getMappingForMethod.invoke(requestMappingHandlerMapping,
                            method,controllerClass);
                    if(type == 1){
                        registerMapping(requestMappingHandlerMapping, mappingInfo, controllerClass, method);
                    }else if(type == 2){
                        unRegisterMapping(requestMappingHandlerMapping, mappingInfo);
                        registerMapping(requestMappingHandlerMapping, mappingInfo, controllerClass, method);
                    }else if(type == 3){
                        unRegisterMapping(requestMappingHandlerMapping, mappingInfo);
                    }
                    log.debug("load method: {}", method.getName());
                }
            }
            log.info("finish register controller: {}", controllerClass.getName());
        } catch (Exception e) {
            log.error("cannot register compiled controller", e);
        }
    }

    public static void registerMapping(RequestMappingHandlerMapping requestMappingHandlerMapping,
                                       RequestMappingInfo mappingInfo,
                                       Class<?> controllerClass,
                                       Method method)
            throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        requestMappingHandlerMapping.registerMapping(mappingInfo, controllerClass.getDeclaredConstructor().newInstance(),
                method);
    }


    public static void unRegisterMapping(RequestMappingHandlerMapping requestMappingHandlerMapping,RequestMappingInfo mappingInfo) {
        requestMappingHandlerMapping.unregisterMapping(mappingInfo);
    }
}
