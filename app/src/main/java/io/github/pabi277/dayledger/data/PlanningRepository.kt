package io.github.pabi277.dayledger.data

import android.content.Context
import java.time.LocalDate

class PlanningRepository(private val context: Context) {
    private val db = DayLedgerDatabase.getInstance(context)
    private val blockDao = db.blockDao()
    private val dayPlanDao = db.dayPlanDao()
    private val templateDao = db.templateDao()

    suspend fun seedTemplatesIfNeeded() {
        if (templateDao.templates().isEmpty()) {
            val templates = listOf(
                TemplateEntity("college", "College day"),
                TemplateEntity("study", "Study day"),
                TemplateEntity("earning", "Earning day"),
                TemplateEntity("off", "Off day")
            )
            templates.forEach { templateDao.upsertTemplate(it) }
            // College day items
            templateDao.upsertItem(TemplateItemEntity(templateId = "college", startMin = 8 * 60, endMin = 16 * 60, title = "College"))
            templateDao.upsertItem(TemplateItemEntity(templateId = "college", startMin = 19 * 60, endMin = 20 * 60, title = "Study"))
            // Study day
            templateDao.upsertItem(TemplateItemEntity(templateId = "study", startMin = 9 * 60, endMin = 12 * 60, title = "Study"))
            templateDao.upsertItem(TemplateItemEntity(templateId = "study", startMin = 14 * 60, endMin = 17 * 60, title = "Study"))
            // Earning day
            templateDao.upsertItem(TemplateItemEntity(templateId = "earning", startMin = 10 * 60, endMin = 12 * 60, title = "Offerwall"))
            templateDao.upsertItem(TemplateItemEntity(templateId = "earning", startMin = 16 * 60, endMin = 18 * 60, title = "Offerwall"))
            // Off day
            templateDao.upsertItem(TemplateItemEntity(templateId = "off", startMin = 11 * 60, endMin = 12 * 60, title = "Light task"))
        }
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
