package com.github.alenfive.rocketapi.utils;

import com.github.alenfive.rocketapi.RocketAPIApplication;


public class PackageUtils {
    public static String getVersion() {
        Package pkg = RocketAPIApplication.class.getPackage();
        return (pkg != null ? pkg.getImplementationVersion() : null);
    }
}
