package com.universal.spring.restsecurity

import org.junit.Assert
import org.junit.Test

class TestDigest {
    @Test
    fun testMD5() {
        val md5 = MD5Util.toMD5("123456")

        Assert.assertEquals("E10ADC3949BA59ABBE56E057F20F883E", md5)
    }
}