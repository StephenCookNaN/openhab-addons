/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.asuswrt.internal.helpers;

import java.util.Calendar;
import java.util.Date;
import java.util.regex.Pattern;

import javax.measure.Unit;
import javax.measure.quantity.Energy;
import javax.measure.quantity.Power;
import javax.measure.quantity.Time;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.HSBType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.types.State;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

/**
 * {@link AsuswrtUtils} TapoUtils -
 * Utility Helper Functions
 *
 * @author Christian Wild - Initial Initial contribution
 */
@NonNullByDefault
public class AsuswrtUtils {
    private static Pattern patternMacPairs = Pattern.compile("^([a-fA-F0-9]{2}[:\\.-]?){5}[a-fA-F0-9]{2}$");
    private static Pattern patternMacTriples = Pattern.compile("^([a-fA-F0-9]{3}[:\\.-]?){3}[a-fA-F0-9]{3}$");

    /************************************
     * CALCULATION UTILS
     ***********************************/
    /**
     * Limit Value between limits
     * 
     * @param value Integer
     * @param lowerLimit
     * @param upperLimit
     * @return limited value
     */
    public static Integer limitVal(@Nullable Integer value, Integer lowerLimit, Integer upperLimit) {
        if (value == null || value < lowerLimit) {
            return lowerLimit;
        } else if (value > upperLimit) {
            return upperLimit;
        }
        return value;
    }

    /************************************
     * FORMAT UTILS
     ***********************************/
    /**
     * return value or default val if it's null
     * 
     * @param <T> Type of value
     * @param value value
     * @param defaultValue defaut value
     * @return value or default
     */
    public static <T> T getValueOrDefault(@Nullable T value, T defaultValue) {
        return value == null ? defaultValue : value;
    }

    /**
     * Format MAC-Address replacing old division chars and add new one
     * 
     * @param mac unformated mac-Address
     * @param newDivisionChar new division char (e.g. ":","-" )
     * @return new formated mac-Address
     */
    public static String formatMac(String mac, char newDivisionChar) {
        String unformatedMac = unformatMac(mac);
        String formatedMac = "";
        try {
            formatedMac = unformatedMac.replaceAll("(.{2})", "$1" + newDivisionChar).substring(0, 17);
            return formatedMac;
        } catch (Exception e) {
            return mac;
        }
    }

    /**
     * unformat MAC-Address replace all division chars
     */
    public static String unformatMac(String mac) {
        mac = mac.replace("-", "");
        mac = mac.replace(":", "");
        mac = mac.replace(".", "");
        mac = mac.replace(" ", "");
        return mac;
    }

    /**
     * check if isvalid MAC-Address
     */
    public static boolean isValidMacAddress(String mac) {
        // MAC-Addresses usually are 6 * 2 hex nibbles separated by colons,
        // but apparently it is legal to have 4 * 3 hex nibbles as well,
        // and the separators can be any of : or - or . or nothing.
        return (patternMacPairs.matcher(mac).find() || patternMacTriples.matcher(mac).find());
    }

    /**
     * HEX-STRING to byte convertion
     */
    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        try {
            for (int i = 0; i < len; i += 2) {
                data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i + 1), 16));
            }
        } catch (Exception e) {
        }
        return data;
    }

    /**
     * Return Boolean from string
     * 
     * @param s - string to be converted
     * @param defVal - Default Value
     */
    public static Boolean stringToBool(@Nullable String s, boolean defVal) {
        if (s == null) {
            return defVal;
        } else if ("1".equals(s) || "-1".equals(s)) {
            return true;
        } else if ("0".equals(s)) {
            return false;
        } else {
            try {
                return Boolean.parseBoolean(s);
            } catch (Exception e) {
                return defVal;
            }
        }
    }

    /**
     * Return Integer from string
     * 
     * @param s - string to be converted
     * @param defVal - Default Value
     */
    public static Integer stringToInteger(@Nullable String s, Integer defVal) {
        if (s == null) {
            return defVal;
        }
        try {
            return Integer.valueOf(s);
        } catch (Exception e) {
            return defVal;
        }
    }

    /**
     * 
     * @param s - string to get value
     * @param defVal - default if is blank or null
     * @return - string or default val
     */
    public static String stringOrDefault(@Nullable String s, String defVal) {
        if (s == null || s.isEmpty() || s.isBlank()) {
            return defVal;
        }
        return s;
    }

    /***********************************
     * JSON-FORMATER
     ************************************/
    /**
     * Check if string is valid JSON-Format
     */
    public static boolean isValidJson(String json) {
        try {
            Gson gson = new Gson();
            JsonObject jsnObject = gson.fromJson(json, JsonObject.class);
            return jsnObject != null;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * JSON Object to String
     * 
     * @param name parameter name
     * @param defVal - default value;
     * @return string value
     */
    public static String jsonObjectToString(@Nullable JsonObject jsonObject, String name, String defVal) {
        if (jsonObject != null && jsonObject.has(name)) {
            return jsonObject.get(name).getAsString();
        } else {
            return defVal;
        }
    }

    /**
     * JSON Object to String
     * 
     * @param name parameter name
     * @return string value
     */
    public static String jsonObjectToString(@Nullable JsonObject jsonObject, String name) {
        return jsonObjectToString(jsonObject, name, "");
    }

    /**
     * 
     * @param name parameter name
     * @param defVal - default value;
     * @return boolean value
     */
    public static Boolean jsonObjectToBool(@Nullable JsonObject jsonObject, String name, Boolean defVal) {
        if (jsonObject != null && jsonObject.has(name)) {
            JsonPrimitive o = jsonObject.getAsJsonPrimitive(name);
            if (o.isBoolean()) {
                return jsonObject.get(name).getAsBoolean();
            } else if (o.isNumber()) {
                Integer iVal = jsonObject.get(name).getAsInt();
                return (iVal.equals(1) || iVal.equals(-1));
            } else if (o.isString()) {
                String val = jsonObject.get(name).getAsString();
                return stringToBool(val, defVal);
            }
        }
        return defVal;
    }

    /**
     * JSON Object to Boolean
     * 
     * @param name parameter name
     * @return boolean value
     */
    public static Boolean jsonObjectToBool(@Nullable JsonObject jsonObject, String name) {
        return jsonObjectToBool(jsonObject, name, false);
    }

    /**
     * JSON Object to Integer
     * 
     * @param name parameter name
     * @param defVal - default value;
     * @return integer value
     */
    public static Integer jsonObjectToInt(@Nullable JsonObject jsonObject, String name, Integer defVal) {
        if (jsonObject != null && jsonObject.has(name)) {
            JsonPrimitive o = jsonObject.getAsJsonPrimitive(name);
            if (o.isNumber()) {
                return jsonObject.get(name).getAsInt();
            } else if (o.isString()) {
                String val = jsonObject.get(name).getAsString();
                return stringToInteger(val, defVal);
            }
        }
        return defVal;
    }

    /**
     * JSON Object to Integer
     * 
     * @param name parameter name
     * @return integer value
     */
    public static Integer jsonObjectToInt(@Nullable JsonObject jsonObject, String name) {
        return jsonObjectToInt(jsonObject, name, 0);
    }

    /**
     * JSON Object to Number
     * 
     * @param name parameter name
     * @param defVal - default value;
     * @return number value
     */
    public static Number jsonObjectToNumber(@Nullable JsonObject jsonObject, String name, Number defVal) {
        if (jsonObject != null && jsonObject.has(name)) {
            return jsonObject.get(name).getAsNumber();
        } else {
            return defVal;
        }
    }

    /**
     * JSON Object to Number
     * 
     * @param name parameter name
     * @return number value
     */
    public static Number jsonObjectToNumber(@Nullable JsonObject jsonObject, String name) {
        return jsonObjectToNumber(jsonObject, name, 0);
    }

    /************************************
     * TYPE UTILS
     ***********************************/

    /**
     * Return OnOffType from bool
     * 
     * @param boolVal
     */
    public static OnOffType getOnOffType(@Nullable Boolean boolVal) {
        return (boolVal != null ? boolVal ? OnOffType.ON : OnOffType.OFF : OnOffType.OFF);
    }

    /**
     * Return OnOffType from bool
     * 
     * @param boolVal
     */
    public static OnOffType getOnOffType(Integer intVal) {
        return intVal == 0 ? OnOffType.OFF : OnOffType.ON;
    }

    /**
     * Return StringType from String
     * 
     * @param strVal
     */
    public static StringType getStringType(@Nullable String strVal) {
        return new StringType(strVal != null ? strVal : "");
    }

    /**
     * Return DecimalType from Double
     * 
     * @param numVal
     */
    public static DecimalType getDecimalType(@Nullable Double numVal) {
        return new DecimalType((numVal != null ? numVal : 0));
    }

    /**
     * Return DecimalType from Integer
     * 
     * @param numVal
     */
    public static DecimalType getDecimalType(@Nullable Integer numVal) {
        return new DecimalType((numVal != null ? numVal : 0));
    }

    /**
     * Return DecimalType from Long
     * 
     * @param numVal
     */
    public static DecimalType getDecimalType(@Nullable Long numVal) {
        return new DecimalType((numVal != null ? numVal : 0));
    }

    /**
     * JSON Object to PercentType
     * 
     * @param numVal value 0-100
     * @return PercentType
     */
    public static PercentType getPercentType(@Nullable Integer numVal) {
        Integer val = limitVal(numVal, 0, 100);
        return new PercentType(val);
    }

    /**
     * Return HSBType from integers
     * 
     * @param hue integer hue-color
     * @param saturation integer saturation
     * @param brightness integer brightness
     * @return HSBType
     */
    public static HSBType getHSBType(Integer hue, Integer saturation, Integer brightness) {
        DecimalType h = new DecimalType(hue);
        PercentType s = new PercentType(saturation);
        PercentType b = new PercentType(brightness);
        return new HSBType(h, s, b);
    }

    /**
     * Return QuantityType with Time
     * 
     * @param numVal Number with value
     * @param unit TimeUnit (Unit<Time>)
     * @return QuantityType<Time>
     */
    public static QuantityType<Time> getTimeType(@Nullable Number numVal, Unit<Time> unit) {
        return new QuantityType<>((numVal != null ? numVal : 0), unit);
    }

    /**
     * Return QuantityType with Power
     * 
     * @param numVal Number with value
     * @param unit PowerUnit (Unit<Power>)
     * @return QuantityType<Power>
     */
    public static QuantityType<Power> getPowerType(@Nullable Number numVal, Unit<Power> unit) {
        return new QuantityType<>((numVal != null ? numVal : 0), unit);
    }

    /**
     * Return QuantityType with Energy
     * 
     * @param numVal Number with value
     * @param unit PowerUnit (Unit<Power>)
     * @return QuantityType<Energy>
     */
    public static QuantityType<Energy> getEnergyType(@Nullable Number numVal, Unit<Energy> unit) {
        return new QuantityType<>((numVal != null ? numVal : 0), unit);
    }

    /**
     * Return QuantityType with variable Unit
     * 
     * @param numVal Number with value
     * @param unit Unit
     * @return OH core StateType
     */
    public static State getQuantityType(@Nullable Number numVal, Unit<?> unit) {
        return new QuantityType<>((numVal != null ? numVal : 0), unit);
    }

    /************************************
     * DATE UTILS
     ***********************************/
    /**
     * Checks if two date objects are on the same day ignoring time.
     *
     * @param date1 the first date, not altered, not null
     * @param date2 the second date, not altered, not null
     * @return true if they represent the same day
     */
    public static boolean isSameDay(final Date date1, final Date date2) {
        final Calendar cal1 = Calendar.getInstance();
        cal1.setTime(date1);
        final Calendar cal2 = Calendar.getInstance();
        cal2.setTime(date2);
        return cal1.get(Calendar.ERA) == cal2.get(Calendar.ERA) && cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR)
                && cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }
}
