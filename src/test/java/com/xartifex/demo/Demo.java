package com.xartifex.demo;

import org.apache.ignite.Ignition;

public class Demo {
    public static void main(String[] args) {
        Ignition.start("custom-config.xml");
    }
}
