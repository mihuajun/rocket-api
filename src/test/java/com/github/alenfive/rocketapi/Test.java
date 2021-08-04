package com.github.alenfive.rocketapi;

import java.util.regex.Pattern;

public class Test {
    public static void main(String[] args) {
        String str = "  delete       from  -user ";
        String selectReg = "^( *select )";
        String insertReg = "^( *(replace|insert) +into )";
        String updateReg = "^( *update [A-Za-z\\-0-9_]+ +set )";
        String deleteReg = "^( *delete +from +[A-Za-z\\-0-9_]+)";
        Pattern test_ = Pattern.compile(deleteReg, Pattern.CASE_INSENSITIVE);

        System.out.println(test_.matcher(str).find());
    }
}
