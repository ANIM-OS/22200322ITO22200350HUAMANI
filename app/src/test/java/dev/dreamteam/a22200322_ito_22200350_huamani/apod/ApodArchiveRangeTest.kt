package dev.dreamteam.a22200322_ito_22200350_huamani.apod

import dev.dreamteam.a22200322_ito_22200350_huamani.apod.data.ApodDateRange
import java.time.LocalDate
import org.junit.Assert.*
import org.junit.Test

class ApodArchiveRangeTest {
    private val today = LocalDate.parse("2026-11-14")

    @Test fun acceptsThirtyOneInclusiveDaysAcrossMonths() {
        val range = ApodDateRange.archive("2026-10-15", "2026-11-14", today)
        assertEquals(LocalDate.parse("2026-10-15"), range.start)
        assertEquals(today, range.end)
    }

    @Test fun acceptsOneDayAtTheBeginningOfApod() {
        val range = ApodDateRange.archive("1995-06-16", "1995-06-16", today)
        assertEquals(range.start, range.end)
    }

    @Test fun rejectsOverlargeOrReversedRanges() {
        assertThrows(IllegalArgumentException::class.java) { ApodDateRange.archive("2026-10-14", "2026-11-14", today) }
        assertThrows(IllegalArgumentException::class.java) { ApodDateRange.archive("2026-11-14", "2026-11-13", today) }
    }

    @Test fun rejectsFutureDaysAndDatesBeforeApodStarted() {
        assertThrows(IllegalArgumentException::class.java) { ApodDateRange.archive("2026-11-14", "2026-11-15", today) }
        assertThrows(IllegalArgumentException::class.java) { ApodDateRange.archive("1995-06-15", "1995-06-16", today) }
    }

    @Test fun rejectsMissingOrImpossibleCalendarDates() {
        assertThrows(IllegalArgumentException::class.java) { ApodDateRange.archive("", "2026-11-14", today) }
        assertThrows(IllegalArgumentException::class.java) { ApodDateRange.archive("2026-02-30", "2026-03-01", today) }
        assertThrows(IllegalArgumentException::class.java) { ApodDateRange.archive("2026-11-14", "14/11/2026", today) }
    }
}
