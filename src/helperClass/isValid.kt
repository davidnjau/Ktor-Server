package com.example.helperClass

import java.util.regex.Pattern

class isValid {

    fun emailChecker(email: String):Boolean{

        val emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\."+
                "[a-zA-Z0-9_+&*-]+)*@" +
                "(?:[a-zA-Z0-9-]+\\.)+[a-z" +
                "A-Z]{2,7}$"

        val pattern = Pattern.compile(emailRegex)
        val checker = pattern.matcher(email).matches()

        return checker

    }

}

