package com.desk_sharing.project.utils;

import com.desk_sharing.project.bean.entity.User;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

import javax.servlet.http.Cookie;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class Utils {
    private Utils() {}

    public static Set<String> getNullPropertyNames(User user) {
        final BeanWrapper src = new BeanWrapperImpl(user);
        java.beans.PropertyDescriptor[] pds = src.getPropertyDescriptors();

        Set<String> emptyNames = new HashSet<>();
        for (java.beans.PropertyDescriptor pd : pds) {
            Object srcValue = src.getPropertyValue(pd.getName());
            if (srcValue == null) emptyNames.add(pd.getName());
        }
        return emptyNames;
    }

    public static String[] stringSetToStringArray(Set<String> set) {
        String[] vals = new String[set.size()];
        set.toArray(vals);
        return vals;
    }

    public static Cookie setCookie(String token, boolean delete, boolean isSecureEnabled, String expiryDate) {

        Cookie cookie = new Cookie(Constants.TOKEN, token);
        int expiry = 0;
        if (!delete) {
            Objects.requireNonNull(expiryDate, "Please ensure {" + Constants.JWT_TOKEN_VALIDITY + "} is set in property file!");
            expiry = Integer.parseInt(expiryDate);
        }
        cookie.setMaxAge(expiry); // expires in 7 days

        cookie.setSecure(isSecureEnabled);
        cookie.setHttpOnly(true);
        cookie.setPath("/");

        return cookie;
    }

    public static byte[] getPasswordInCSV(String username, String password, String applicationUrl, String[] headers) {

        byte[] out;
        try (var output = new ByteArrayOutputStream();
             var printer = new CSVPrinter(new OutputStreamWriter(output), CSVFormat.DEFAULT)) {
            printer.printRecord(headers);
            printer.printRecord(username, password, applicationUrl);
            printer.flush();
            out = output.toByteArray();
        } catch (IOException e) {
            out = null;
            e.printStackTrace();
        }
        return out;
    }
}
