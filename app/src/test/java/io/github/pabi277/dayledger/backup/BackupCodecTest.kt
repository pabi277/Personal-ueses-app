package io.github.pabi277.dayledger.backup

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Backup format tests: encode → decode → identical document, with fictional data.
 * These are the round-trip proof for Phase 7 part 7.2 on the CI side.
 */
class BackupCodecTest {

    private fun fictionalDocument(): BackupDocument = BackupDocument(
        formatVersion = BackupCodec.FORMAT_VERSION,
        schemaVersion = 5,
        appId = BackupCodec.APP_ID,
        appVersionName = "0.1.0",
        createdAtEpochMs = 1767225600000L,
        tables = listOf(
            BackupTable(
                name = "app_usage",
                columns = listOf("packageName", "dateKey", "foregroundMinutes", "pickups", "longestSessionMin", "source", "updatedAt"),
                rows = listOf(
                    listOf(
                        BackupCell.Text("com.example.game"),
                        BackupCell.Text("2026-01-05"),
                        BackupCell.Integer(62),
                        BackupCell.Integer(11),
                        BackupCell.Integer(18),
                        BackupCell.Text("device"),
                        BackupCell.Integer(1767225600000L)
                    ),
                    listOf(
                        BackupCell.Text("com.example.reader"),
                        BackupCell.Text("2026-01-05"),
                        BackupCell.Integer(45),
                        BackupCell.Integer(4),
                        BackupCell.Integer(25),
                        BackupCell.Text("device"),
                        BackupCell.Integer(1767225600000L)
                    )
                )
            ),
            BackupTable(
                name = "categories",
                columns = listOf("id", "name", "kind", "budgetMin", "color"),
                rows = listOf(
                    listOf(BackupCell.Text("study"), BackupCell.Text("Study"), BackupCell.Text("floor"),
                        BackupCell.Integer(90), BackupCell.Integer(4280391411L))
                )
            ),
            BackupTable(name = "settings", columns = listOf("key", "value"), rows = emptyList())
        )
    )

    @Test
    fun `round trip preserves every table, column and cell`() {
        val original = fictionalDocument()
        val encoded = BackupCodec.encode(original)
        val decoded = BackupCodec.decode(encoded)

        assertTrue(decoded is BackupCodec.DecodeResult.Success)
        val copy = (decoded as BackupCodec.DecodeResult.Success).document

        assertEquals(original.formatVersion, copy.formatVersion)
        assertEquals(original.schemaVersion, copy.schemaVersion)
        assertEquals(original.appId, copy.appId)
        assertEquals(original.appVersionName, copy.appVersionName)
        assertEquals(original.createdAtEpochMs, copy.createdAtEpochMs)
        assertEquals(original.tables.size, copy.tables.size)
        original.tables.zip(copy.tables).forEach { (before, after) ->
            assertEquals(before.name, after.name)
            assertEquals(before.columns, after.columns)
            assertEquals(before.rows, after.rows)
        }
        assertEquals(original.rowCount(), copy.rowCount())
    }

    @Test
    fun `encoded file is deterministic`() {
        assertEquals(BackupCodec.encode(fictionalDocument()), BackupCodec.encode(fictionalDocument()))
    }

    @Test
    fun `awkward text survives quotation marks newlines emoji and rupee`() {
        val awkward = "He said \"hi\"\nnext\tline\\backslash ₹12.50 🌙🎮 \u0001 end"
        val document = BackupDocument(
            formatVersion = BackupCodec.FORMAT_VERSION,
            schemaVersion = 5,
            appId = BackupCodec.APP_ID,
            appVersionName = "0.1.0",
            createdAtEpochMs = 1L,
            tables = listOf(
                BackupTable("learning", listOf("text"), listOf(listOf(BackupCell.Text(awkward))))
            )
        )
        val decoded = BackupCodec.decode(BackupCodec.encode(document)) as BackupCodec.DecodeResult.Success
        assertEquals(awkward, (decoded.document.tables[0].rows[0][0] as BackupCell.Text).value)
    }

    @Test
    fun `nulls and real numbers survive`() {
        val document = BackupDocument(
            formatVersion = BackupCodec.FORMAT_VERSION,
            schemaVersion = 5,
            appId = BackupCodec.APP_ID,
            appVersionName = "0.1.0",
            createdAtEpochMs = 2L,
            tables = listOf(
                BackupTable(
                    "app_info",
                    listOf("packageName", "limitMin", "ratio"),
                    listOf(listOf(BackupCell.Text("com.example.reader"), BackupCell.Null, BackupCell.Real(0.5)))
                )
            )
        )
        val decoded = BackupCodec.decode(BackupCodec.encode(document)) as BackupCodec.DecodeResult.Success
        val row = decoded.document.tables[0].rows[0]
        assertTrue(row[1] is BackupCell.Null)
        assertEquals(0.5, (row[2] as BackupCell.Real).value, 0.0000001)
    }

    @Test
    fun `empty database still round trips`() {
        val document = fictionalDocument().copy(tables = emptyList())
        val decoded = BackupCodec.decode(BackupCodec.encode(document)) as BackupCodec.DecodeResult.Success
        assertEquals(0, decoded.document.tables.size)
        assertEquals(0, decoded.document.rowCount())
    }

    @Test
    fun `a newer format is refused with a readable message`() {
        val newer = BackupCodec.encode(fictionalDocument().copy(formatVersion = BackupCodec.FORMAT_VERSION + 1))
        val decoded = BackupCodec.decode(newer)
        assertTrue(decoded is BackupCodec.DecodeResult.Failure)
        assertTrue((decoded as BackupCodec.DecodeResult.Failure).message.contains("newer app version"))
    }

    @Test
    fun `a backup from another app is refused`() {
        val foreign = BackupCodec.encode(fictionalDocument().copy(appId = "com.example.other"))
        val decoded = BackupCodec.decode(foreign)
        assertTrue(decoded is BackupCodec.DecodeResult.Failure)
        assertTrue((decoded as BackupCodec.DecodeResult.Failure).message.contains("belongs to"))
    }

    @Test
    fun `malformed files are refused instead of throwing`() {
        listOf(
            "",
            "not json at all",
            "{}",
            """{"formatVersion":1,"app":"io.github.pabi277.dayledger"}""",
            """{"formatVersion":1,"app":"io.github.pabi277.dayledger","schemaVersion":5,"tables":[{"name":"a","columns":["x"],"rows":[["too","many"]]}]}""",
            BackupCodec.encode(fictionalDocument()).dropLast(12)
        ).forEach { text ->
            val decoded = BackupCodec.decode(text)
            assertTrue("expected refusal for: ${text.take(60)}", decoded is BackupCodec.DecodeResult.Failure)
            assertTrue((decoded as BackupCodec.DecodeResult.Failure).message.isNotBlank())
        }
    }

    @Test
    fun `decode enforces one value per column`() {
        val broken = BackupCodec.encode(fictionalDocument())
            .replace("\"rows\":[]", "\"rows\":[[\"only-one\"]]")
        assertTrue(BackupCodec.decode(broken) is BackupCodec.DecodeResult.Failure)
    }
}
