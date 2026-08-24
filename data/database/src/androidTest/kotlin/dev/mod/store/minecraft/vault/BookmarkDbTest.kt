package dev.mod.store.minecraft.data.database

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import dev.mod.store.minecraft.data.database.bookmark.BookmarkLocalDataSource
import dev.mod.store.minecraft.data.database.db.createVaultDatabase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BookmarkDbTest {

    private fun newDataSource(): BookmarkLocalDataSource {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        return BookmarkLocalDataSource(createVaultDatabase(context))
    }

    @Test
    fun toggle_inserts_then_deletes_and_count_reflects_it() = runBlocking {
        val source = newDataSource()
        val id = 987654

        // Clean slate for this id.
        if (source.isBookmarked(id)) source.toggle(id)
        val baseline = source.observeCount().first()
        assertFalse(source.isBookmarked(id))

        val turnedOn = source.toggle(id)
        assertTrue("toggle should report bookmarked", turnedOn)
        assertTrue(source.isBookmarked(id))
        assertEquals(baseline + 1, source.observeCount().first())
        assertTrue(source.bookmarkedIds().contains(id))
        assertTrue(source.page(limit = 10, offset = 0).contains(id))

        val turnedOff = source.toggle(id)
        assertFalse("toggle should report not bookmarked", turnedOff)
        assertFalse(source.isBookmarked(id))
        assertEquals(baseline, source.observeCount().first())
        assertFalse(source.bookmarkedIds().contains(id))
    }
}
