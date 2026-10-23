package io.github.pabi277.dayledger.data

import android.content.Context
import java.time.LocalDate

class PlanningRepository(private val context: Context) {
    private val db = DayLedgerDatabase.getInstance(context)
    private val blockDao = db.blockDao()
    private val dayPlanDao = db.dayPlanDao()
    private val templateDao = db.templateDao()

    /**
     * Seeds **empty** template placeholders.
     *
     * Before Phase 8 this seeded four templates *with blocks already filled in*
     * ("College 08:00–16:00", "Offerwall 10:00–12:00", …). That is a routine guessed on the
     * owner's behalf, which `docs/DECISIONS.md` and UI Design v2 §2 both forbid: never seed
     * a personal timetable, and keep examples generic. The owner asked for this review on
     * 2026-10-23 after seeing them on the Plan screen.
     *
     * Nothing is deleted here. Rows already on a device stay until the owner removes them,
     * because deleting someone's plan on upgrade is not a decision an upgrade may make.
     */
    suspend fun seedTemplatesIfNeeded() {
        if (templateDao.templates().isEmpty()) {
            listOf(
                TemplateEntity("tmpl-a", "Template A"),
                TemplateEntity("tmpl-b", "Template B")
            ).forEach { templateDao.upsertTemplate(it) }
        }
    }

    /** Item counts per template, for the Plan screen's template rows. */
    suspend fun templateItemCounts(): Map<String, Int> =
        templateDao.itemCounts().associate { it.templateId to it.itemCount }

    /**
     * Remove a template and its items. Only ever called from an explicit owner action on the
     * Plan screen — an upgrade must not do this by itself.
     */
    suspend fun deleteTemplate(id: String) {
        templateDao.deleteItems(id)
        templateDao.deleteTemplate(id)
    }

    suspend fun templates() = templateDao.templates()

    suspend fun templateItems(templateId: String) = templateDao.items(templateId)

    suspend fun blocksForDate(dateKey: String) = blockDao.forDate(dateKey)

    suspend fun applyTemplate(dateKey: String, templateId: String) {
        blockDao.deleteForDate(dateKey)
        for (item in templateDao.items(templateId)) {
            blockDao.upsert(BlockEntity(dateKey = dateKey, startMin = item.startMin, endMin = item.endMin, title = item.title))
        }
        dayPlanDao.upsert(DayPlanEntity(dateKey = dateKey, templateId = templateId))
        templateDao.touch(templateId, System.currentTimeMillis())
    }

    suspend fun addBlock(dateKey: String, startMin: Int, endMin: Int, title: String) {
        blockDao.upsert(BlockEntity(dateKey = dateKey, startMin = startMin, endMin = endMin, title = title))
    }

    suspend fun deleteBlock(id: Long) = blockDao.delete(id)

    suspend fun tomorrowKey(): String = LocalDate.now().plusDays(1).toString()

    suspend fun todayKey(): String = LocalDate.now().toString()

    suspend fun shiftDay(dateKey: String, minutes: Int) {
        val blocks = blockDao.forDate(dateKey)
        for (b in blocks) {
            blockDao.upsert(b.copy(startMin = b.startMin + minutes, endMin = b.endMin + minutes))
        }
    }
}
