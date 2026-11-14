package dev.dreamteam.a22200322_ito_22200350_huamani.apod

import com.google.gson.Gson
import dev.dreamteam.a22200322_ito_22200350_huamani.apod.data.remote.ApodDto
import dev.dreamteam.a22200322_ito_22200350_huamani.apod.data.remote.toEntityOrNull
import org.junit.Assert.*
import org.junit.Test

class ApodDtoTest {
    @Test fun missingFieldsHaveSafeDefaults() {
        val dto = Gson().fromJson("""{"date":"2026-11-14","title":null,"explanation":null,"url":null}""", ApodDto::class.java)
        val entity = requireNotNull(dto.toEntityOrNull())
        assertEquals("Sin título", entity.title)
        assertEquals("Explicación no disponible.", entity.explanation)
        assertEquals("unknown", entity.mediaType)
        assertNull(entity.url)
        assertNull(entity.hdUrl)
        assertNull(entity.copyright)
        assertFalse(entity.isFavorite)
    }

    @Test fun missingOrInvalidDatesAreRejected() {
        listOf(null, "", "2026-02-30", "not-a-date").forEach {
            assertNull(ApodDto(date = it).toEntityOrNull())
        }
    }

    @Test fun imageFieldsAndOptionalAuthorAreMappedExplicitly() {
        val dto = Gson().fromJson("""{"date":"2026-11-14","title":" Galaxia ","explanation":" Texto completo ","media_type":"image","url":"https://example.com/small.jpg","hdurl":"https://example.com/large.jpg","copyright":" NASA "}""", ApodDto::class.java)
        val entity = requireNotNull(dto.toEntityOrNull())
        assertEquals("Galaxia", entity.title)
        assertEquals("Texto completo", entity.explanation)
        assertEquals("image", entity.mediaType)
        assertEquals("https://example.com/small.jpg", entity.url)
        assertEquals("https://example.com/large.jpg", entity.hdUrl)
        assertEquals("NASA", entity.copyright)
    }

    @Test fun videosRemainVideosAndMalformedUrlsAreDiscarded() {
        val video = requireNotNull(ApodDto(date = "2026-11-14", mediaType = "video", url = "https://example.com/video").toEntityOrNull())
        assertEquals("video", video.mediaType)
        val malformed = requireNotNull(ApodDto(date = "2026-11-14", mediaType = "image", url = "javascript:alert(1)", hdUrl = "invalid").toEntityOrNull())
        assertNull(malformed.url)
        assertNull(malformed.hdUrl)
    }
}
