/*
 * Copyright 2021-2021 Monkey Group.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.monkey.mmq.core.utils;

import org.monkey.mmq.core.exception.runtime.MmqRuntimeException;
import org.springframework.core.ResolvableType;

import java.util.Objects;

import static org.monkey.mmq.core.exception.MmqException.SERVER_ERROR;

/**
 * class operation utils.
 *
 * @author solley
 */
@SuppressWarnings("all")
public final class ClassUtils {
    /**
     * Finds and returns class by className.
     *
     * @param className String value for className.
     * @return class Instances of the class represent classes and interfaces.
     */
    public static Class findClassByName(String className) {
        try {
            return Class.forName(className);
        } catch (Exception e) {
            throw new MmqRuntimeException(SERVER_ERROR, "this class name not found");
        }
    }

    /**
     * Determines if the class or interface represented by this object is either the same as, or is a superclass or
     * superinterface of, the class or interface represented by the specified parameter.
     *
     * @param clazz Instances of the class represent classes and interfaces.
     * @param cls   Instances of the class represent classes and interfaces.
     * @return the value indicating whether objects of the type can be assigned to objects of this class.
     */
    public static boolean isAssignableFrom(Class clazz, Class cls) {
        Objects.requireNonNull(cls, "cls");
        return clazz.isAssignableFrom(cls);
    }
    
    public static <T> Class<T> resolveGenericType(Class<?> declaredClass) {
        return (Class<T>) ResolvableType.forClass(declaredClass).getSuperType().resolveGeneric(0);
    }
    
    public static <T> Class<T> resolveGenericTypeByInterface(Class<?> declaredClass) {
        return (Class<T>) ResolvableType.forClass(declaredClass).getInterfaces()[0].resolveGeneric(0);
    }
    
    public static String getName(Object obj) {
        Objects.requireNonNull(obj, "obj");
        return obj.getClass().getName();
    }
    
    public static String getCanonicalName(Object obj) {
        Objects.requireNonNull(obj, "obj");
        return obj.getClass().getCanonicalName();
    }
    
    public static String getSimplaName(Object obj) {
        Objects.requireNonNull(obj, "obj");
        return obj.getClass().getSimpleName();
    }
    
    public static String getName(Class cls) {
        Objects.requireNonNull(cls, "cls");
        return cls.getName();
    }
    
    public static String getCanonicalName(Class cls) {
        Objects.requireNonNull(cls, "cls");
        return cls.getCanonicalName();
    }
    
    public static String getSimplaName(Class cls) {
        Objects.requireNonNull(cls, "cls");
        return cls.getSimpleName();
    }
    
}