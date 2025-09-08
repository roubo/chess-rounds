package com.airoubo.chessrounds.util;

import java.util.Collection;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * 字符串工具类
 * 
 * @author AiRoubo
 * @version 1.0.0
 */
public class StringUtil {
    
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$"
    );
    
    private static final Pattern PHONE_PATTERN = Pattern.compile(
        "^1[3-9]\\d{9}$"
    );
    
    /**
     * 判断字符串是否为空
     * 
     * @param str 字符串
     * @return 是否为空
     */
    public static boolean isEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }
    
    /**
     * 判断字符串是否不为空
     * 
     * @param str 字符串
     * @return 是否不为空
     */
    public static boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }
    
    /**
     * 判断字符串是否为空白
     * 
     * @param str 字符串
     * @return 是否为空白
     */
    public static boolean isBlank(String str) {
        return str == null || str.trim().isEmpty();
    }
    
    /**
     * 判断字符串是否不为空白
     * 
     * @param str 字符串
     * @return 是否不为空白
     */
    public static boolean isNotBlank(String str) {
        return !isBlank(str);
    }
    
    /**
     * 生成UUID
     * 
     * @return UUID字符串
     */
    public static String generateUUID() {
        return UUID.randomUUID().toString().replace("-", "");
    }
    
    /**
     * 生成带连字符的UUID
     * 
     * @return UUID字符串
     */
    public static String generateUUIDWithHyphen() {
        return UUID.randomUUID().toString();
    }
    
    /**
     * 验证邮箱格式
     * 
     * @param email 邮箱地址
     * @return 是否为有效邮箱
     */
    public static boolean isValidEmail(String email) {
        if (isEmpty(email)) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email).matches();
    }
    
    /**
     * 验证手机号格式
     * 
     * @param phone 手机号
     * @return 是否为有效手机号
     */
    public static boolean isValidPhone(String phone) {
        if (isEmpty(phone)) {
            return false;
        }
        return PHONE_PATTERN.matcher(phone).matches();
    }
    
    /**
     * 首字母大写
     * 
     * @param str 字符串
     * @return 首字母大写的字符串
     */
    public static String capitalize(String str) {
        if (isEmpty(str)) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
    
    /**
     * 首字母小写
     * 
     * @param str 字符串
     * @return 首字母小写的字符串
     */
    public static String uncapitalize(String str) {
        if (isEmpty(str)) {
            return str;
        }
        return str.substring(0, 1).toLowerCase() + str.substring(1);
    }
    
    /**
     * 驼峰转下划线
     * 
     * @param str 驼峰字符串
     * @return 下划线字符串
     */
    public static String camelToUnderscore(String str) {
        if (isEmpty(str)) {
            return str;
        }
        // 先处理连续大写字母的情况，如ABC -> A_B_C
        String result = str.replaceAll("([A-Z])(?=[A-Z])", "$1_");
        // 再处理小写字母后跟大写字母的情况
        result = result.replaceAll("([a-z])([A-Z])", "$1_$2");
        return result.toLowerCase();
    }
    
    /**
     * 下划线转驼峰
     * 
     * @param str 下划线字符串
     * @return 驼峰字符串
     */
    public static String underscoreToCamel(String str) {
        if (isEmpty(str)) {
            return str;
        }
        StringBuilder result = new StringBuilder();
        String[] parts = str.split("_");
        for (int i = 0; i < parts.length; i++) {
            if (i == 0) {
                result.append(parts[i].toLowerCase());
            } else {
                result.append(capitalize(parts[i].toLowerCase()));
            }
        }
        return result.toString();
    }
    
    /**
     * 字符串连接
     * 
     * @param delimiter 分隔符
     * @param elements 元素
     * @return 连接后的字符串
     */
    public static String join(String delimiter, String... elements) {
        if (elements == null || elements.length == 0) {
            return "";
        }
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < elements.length; i++) {
            if (i > 0) {
                result.append(delimiter);
            }
            result.append(elements[i]);
        }
        return result.toString();
    }
    
    /**
     * 集合连接
     * 
     * @param delimiter 分隔符
     * @param collection 集合
     * @return 连接后的字符串
     */
    public static String join(String delimiter, Collection<?> collection) {
        if (collection == null || collection.isEmpty()) {
            return "";
        }
        StringBuilder result = new StringBuilder();
        boolean first = true;
        for (Object item : collection) {
            if (!first) {
                result.append(delimiter);
            }
            result.append(item.toString());
            first = false;
        }
        return result.toString();
    }
    
    /**
     * 截取字符串
     * 
     * @param str 原字符串
     * @param maxLength 最大长度
     * @param suffix 后缀
     * @return 截取后的字符串
     */
    public static String truncate(String str, int maxLength, String suffix) {
        if (isEmpty(str) || str.length() <= maxLength) {
            return str;
        }
        return str.substring(0, maxLength) + (suffix != null ? suffix : "");
    }
    
    /**
     * 截取字符串（默认后缀为...）
     * 
     * @param str 原字符串
     * @param maxLength 最大长度
     * @return 截取后的字符串
     */
    public static String truncate(String str, int maxLength) {
        return truncate(str, maxLength, "...");
    }
    
    /**
     * 掩码处理
     * 
     * @param str 原字符串
     * @param start 开始位置
     * @param end 结束位置
     * @param mask 掩码字符
     * @return 掩码后的字符串
     */
    public static String mask(String str, int start, int end, char mask) {
        if (isEmpty(str) || start < 0 || end > str.length() || start >= end) {
            return str;
        }
        StringBuilder result = new StringBuilder(str);
        for (int i = start; i < end; i++) {
            result.setCharAt(i, mask);
        }
        return result.toString();
    }
    
    /**
     * 手机号掩码
     * 
     * @param phone 手机号
     * @return 掩码后的手机号
     */
    public static String maskPhone(String phone) {
        if (isEmpty(phone) || phone.length() != 11) {
            return phone;
        }
        return mask(phone, 3, 7, '*');
    }
    
    /**
     * 邮箱掩码
     * 
     * @param email 邮箱
     * @return 掩码后的邮箱
     */
    public static String maskEmail(String email) {
        if (isEmpty(email) || !email.contains("@")) {
            return email;
        }
        int atIndex = email.indexOf("@");
        if (atIndex <= 1) {
            return email;
        }
        int maskStart = 1;
        int maskEnd = atIndex;
        return mask(email, maskStart, maskEnd, '*');
    }
}