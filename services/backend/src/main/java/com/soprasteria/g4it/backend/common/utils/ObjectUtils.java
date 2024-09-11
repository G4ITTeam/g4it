/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.common.utils;

import java.lang.reflect.InvocationTargetException;

public class ObjectUtils {

    /**
     * Get String value from field name, object instance and instance class
     * In case of NullPointerException, returns ""
     *
     * @param fieldName     the field name
     * @param instance      the instance object
     * @param instanceClass the instance class
     * @return the value as string
     */
    public static String getCsvString(String fieldName, Object instance, Class<?> instanceClass) {
        return getCsvString(fieldName, instance, instanceClass, "");
    }

    /**
     * Get String value from field name, object instance and instance class
     * In case of NullPointerException, returns the defaultValue
     *
     * @param fieldName     the field name
     * @param instance      the instance object
     * @param instanceClass the instance class
     * @param defaultValue  the default value
     * @return the value as string
     */
    public static String getCsvString(String fieldName, Object instance, Class<?> instanceClass, String defaultValue) {
        String fieldNameBase = fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
        String errorMessage = "! Cannot read value by calling %s !";

        try {
            return instanceClass.getDeclaredMethod("get" + fieldNameBase).invoke(instance).toString();
        } catch (NullPointerException e) {
            return defaultValue;
        } catch (NoSuchMethodException e) {
            try {
                return instanceClass.getDeclaredMethod("is" + fieldNameBase).invoke(instance).toString();
            } catch (NullPointerException ex) {
                return defaultValue;
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ex) {
                return String.format(errorMessage, "is" + fieldNameBase);
            }
        } catch (InvocationTargetException | IllegalAccessException e) {
            return String.format(errorMessage, "get" + fieldNameBase);
        }
    }
}
