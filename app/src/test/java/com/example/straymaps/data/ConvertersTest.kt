package com.example.straymaps.data

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDateTime


@RunWith(AndroidJUnit4::class)
@SmallTest
class ConvertersTest {

    private val converter = Converters()
    private val localDateTime: LocalDateTime = LocalDateTime.of(2024,1,2,12,12,12,12)
    private val localDateTimeAsString: String = "2024-01-02T12:12:12.000000012"

    @Test
    fun fromLocalDateTime_success_convertsLocalDateTimeToString() {

        val localDateTimeToString = converter.fromLocalDateTime(localDateTime)
        val expectedResult = "2024-01-02T12:12:12.000000012"

        assertTrue(localDateTimeToString is String)
        assertTrue(localDateTimeToString.equals(expectedResult))
    }

    @Test
    fun toLocalDateTime() {

        val stringToLocalDateTime = converter.toLocalDateTime(localDateTimeAsString)
        val expectedResult: LocalDateTime = LocalDateTime.of(2024,1,2,12,12,12,12)

        assertTrue(stringToLocalDateTime is LocalDateTime)
        assertTrue(stringToLocalDateTime.equals(expectedResult))
    }
}

