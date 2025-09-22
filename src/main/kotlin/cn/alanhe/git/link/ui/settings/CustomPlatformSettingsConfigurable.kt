package cn.alanhe.git.link.ui.settings

import com.intellij.openapi.components.service
import com.intellij.openapi.options.BoundConfigurable
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.ToolbarDecorator
import com.intellij.ui.dsl.builder.Align
import com.intellij.ui.dsl.builder.bindText
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.table.TableView
import com.intellij.util.ui.ColumnInfo
import com.intellij.util.ui.ListTableModel
import com.intellij.util.ui.JBUI
import cn.alanhe.git.link.settings.ApplicationSettings
import cn.alanhe.git.link.settings.ApplicationSettings.CustomHostSettings
import cn.alanhe.git.link.platform.Platform
import cn.alanhe.git.link.platform.PlatformRepository
import cn.alanhe.git.link.url.factory.PLATFORM_MAP
import javax.swing.ListSelectionModel.SINGLE_SELECTION
import cn.alanhe.git.link.GitLinkBundle.message
import cn.alanhe.git.link.ui.components.SubstitutionReferenceTable
import cn.alanhe.git.link.ui.validation.*
import java.awt.Dimension

class CustomPlatformSettingsConfigurable : BoundConfigurable(message("settings.custom-platform.group.title")) {
    private var settings = service<ApplicationSettings>()
    private val platformRepository = service<PlatformRepository>()
    private var customPlatforms = settings.customHosts
    private val tableModel = createTableModel()

    private val table = TableView<PlatformTableRow>(tableModel).apply {
        setShowColumns(true)
        setSelectionMode(SINGLE_SELECTION)
        emptyText.text = message("settings.custom-platform.table.empty")
        preferredScrollableViewportSize = Dimension(JBUI.scale(480), rowHeight * 10)
    }

    private val tableContainer = ToolbarDecorator.createDecorator(table)
        .setAddAction { addCustomPlatform() }
        .setEditAction { editCustomPlatform() }
        .setRemoveAction { removeCustomPlatform() }
        .setRemoveActionUpdater { table.selectedObject is PlatformTableRow.Custom }
        .createPanel()

    override fun createPanel() = panel {
        row {
            cell(tableContainer)
                .align(Align.FILL)
        }
    }

    private fun createTableModel(): ListTableModel<PlatformTableRow> = ListTableModel(
        arrayOf(
            createColumn(message("settings.custom-platform.table.column.name")) { row -> row?.displayName },
            createColumn(message("settings.custom-platform.table.column.domain")) { row -> row?.domainDescription },
        ),
        buildTableRows()
    )

    private fun createColumn(name: String, formatter: (PlatformTableRow?) -> String?) : ColumnInfo<PlatformTableRow, String> {
        return object : ColumnInfo<PlatformTableRow, String>(name) {
            override fun valueOf(item: PlatformTableRow?): String? {
                return formatter(item)
            }
        }
    }

    private fun addCustomPlatform() {
        val dialog = CustomPlatformDialog()

        if (dialog.showAndGet()) {
            customPlatforms = customPlatforms.plus(dialog.platform)
            refreshTableModel()
        }
    }

    private fun removeCustomPlatform() {
        val row = table.selectedObject as? PlatformTableRow.Custom ?: return

        customPlatforms = customPlatforms.filterNot { it.id == row.settings.id }
        refreshTableModel()
    }

    private fun editCustomPlatform() {
        val row = table.selectedObject as? PlatformTableRow ?: return

        when (row) {
            is PlatformTableRow.Custom -> {
                val dialog = CustomPlatformDialog(row.settings.copy())

                if (dialog.showAndGet()) {
                    customPlatforms = customPlatforms.map { existing ->
                        if (existing.id == row.settings.id) dialog.platform else existing
                    }
                    refreshTableModel()
                }
            }
            is PlatformTableRow.BuiltIn -> {
                BuiltInPlatformDialog(row.platform).show()
            }
        }
    }

    private fun refreshTableModel() {
        tableModel.items = buildTableRows()
    }

    private fun buildTableRows(): MutableList<PlatformTableRow> {
        val builtIn = platformRepository.getDefaults().map { PlatformTableRow.BuiltIn(it) }
        val custom = customPlatforms.map { PlatformTableRow.Custom(it) }

        return (builtIn + custom).toMutableList()
    }

    override fun reset() {
        super.reset()

        customPlatforms = settings.customHosts
        refreshTableModel()
    }

    override fun isModified() : Boolean {
        return super.isModified() || customPlatforms != settings.customHosts
    }

    override fun apply() {
        super.apply()

        settings.customHosts = customPlatforms
    }
}

private class CustomPlatformDialog(customPlatform: CustomHostSettings? = null) : DialogWrapper(false) {
    val platform = customPlatform ?: CustomHostSettings()
    private val substitutionReferenceTable = SubstitutionReferenceTable().apply { setShowColumns(true) }

    init {
        title = message("settings.custom-platform.add-dialog.title")
        setOKButtonText(customPlatform?.let { message("actions.update") } ?: message("actions.add"))
        setSize(700, 700)
        init()
    }

    override fun createCenterPanel() = panel {
        row(message("settings.custom-platform.add-dialog.field.name.label")) {
            textField()
                .bindText(platform::displayName)
                .focused()
                .validationOnApply { notBlank(it.text) ?: alphaNumeric(it.text) ?: length(it.text, 3, 15) }
                .comment(message("settings.custom-platform.add-dialog.field.name.comment"))
        }
        row(message("settings.custom-platform.add-dialog.field.domain.label")) {
            textField()
                .bindText(platform::baseUrl)
                .validationOnApply { notBlank(it.text) ?: domain(it.text) }
                .comment(message("settings.custom-platform.add-dialog.field.domain.comment"))
        }
        row(message("settings.custom-platform.add-dialog.field.file-at-branch-template.label")) {
            textField()
                .bindText(platform::fileAtBranchTemplate)
                .validationOnApply { notBlank(it.text) ?: fileAtBranchTemplate(it.text) }
                .comment(message("settings.custom-platform.add-dialog.field.file-at-branch-template.comment"))
        }
        row(message("settings.custom-platform.add-dialog.field.file-at-commit-template.label")) {
            textField()
                .bindText(platform::fileAtCommitTemplate)
                .validationOnApply { notBlank(it.text) ?: fileAtCommitTemplate(it.text) }
                .comment(message("settings.custom-platform.add-dialog.field.file-at-commit-template.comment"))
        }
        row(message("settings.custom-platform.add-dialog.field.commit-template.label")) {
            textField()
                .bindText(platform::commitTemplate)
                .validationOnApply { notBlank(it.text) ?: commitTemplate(it.text) }
                .comment(message("settings.custom-platform.add-dialog.field.commit-template.comment"))
        }
        row {
            scrollCell(substitutionReferenceTable)
                .align(Align.FILL)
        }
    }
}

private sealed class PlatformTableRow {
    abstract val displayName: String
    abstract val domainDescription: String

    class BuiltIn(val platform: Platform) : PlatformTableRow() {
        override val displayName: String = platform.name
        override val domainDescription: String = when {
            platform.domains.isNotEmpty() -> platform.domains.joinToString(", ") { it.toString() }
            platform.domainPattern != null -> platform.domainPattern.pattern()
            else -> ""
        }
    }

    class Custom(val settings: CustomHostSettings) : PlatformTableRow() {
        override val displayName: String = settings.displayName
        override val domainDescription: String = settings.baseUrl
    }
}

private class BuiltInPlatformDialog(private val platform: Platform) : DialogWrapper(false) {
    private val templates = PLATFORM_MAP[platform::class.java]

    init {
        title = message("settings.custom-platform.view-dialog.title", platform.name)
        setOKButtonText(message("actions.close"))
        init()
    }

    override fun createCenterPanel() = panel {
        row(message("settings.custom-platform.view-dialog.field.name.label")) {
            textField()
                .applyToComponent {
                    text = platform.name
                    isEditable = false
                }
        }
        row(message("settings.custom-platform.view-dialog.field.domain.label")) {
            textField()
                .applyToComponent {
                    text = when {
                        platform.domains.isNotEmpty() -> platform.domains.joinToString(", ") { it.toString() }
                        platform.domainPattern != null -> platform.domainPattern.pattern()
                        else -> ""
                    }
                    isEditable = false
                }
        }
        if (templates != null) {
            row(message("settings.custom-platform.add-dialog.field.file-at-branch-template.label")) {
                textField()
                    .applyToComponent {
                        text = templates.fileAtBranch
                        isEditable = false
                    }
            }
            row(message("settings.custom-platform.add-dialog.field.file-at-commit-template.label")) {
                textField()
                    .applyToComponent {
                        text = templates.fileAtCommit
                        isEditable = false
                    }
            }
            row(message("settings.custom-platform.add-dialog.field.commit-template.label")) {
                textField()
                    .applyToComponent {
                        text = templates.commit
                        isEditable = false
                    }
            }
        } else {
            row {
                comment(message("settings.custom-platform.view-dialog.no-templates"))
            }
        }
    }
}
