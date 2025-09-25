package org.qualitydxb.processors;

import org.qualitydxb.common.Models.ProcessRequest;
import org.reflections.Reflections;

import java.util.Set;

public abstract class Process {
    protected abstract String getFileTag();

    protected static <T extends Process> T getProcessor(Class<T> clazz, String fileTag) {
        T integration = null;
        try {
            Reflections reflections = new Reflections("org.qualitydxb.processors");
            Set<Class<? extends Process>> subTypes = reflections.getSubTypesOf(Process.class);

            for (Class<? extends Process> type : subTypes) {
                if (java.lang.reflect.Modifier.isAbstract(type.getModifiers())) {
                    continue;
                }
                Process instance = type.newInstance();
                if (instance.getFileTag().equalsIgnoreCase(fileTag)) {
                    integration = clazz.cast(instance);
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (integration == null) {
            //log error
        }
        return integration;
    }

    protected abstract ProcessRequest processDocument(ProcessRequest request) throws Exception;
}
