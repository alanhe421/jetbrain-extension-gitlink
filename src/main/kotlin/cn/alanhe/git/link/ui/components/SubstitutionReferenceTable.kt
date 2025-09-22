package cn.alanhe.git.link.ui.components

import com.intellij.openapi.ide.CopyPasteManager
import com.intellij.openapi.util.SystemInfoRt
import com.intellij.ui.table.TableView
import com.intellij.util.ui.ColumnInfo
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.ListTableModel
import java.awt.datatransfer.StringSelection
import java.awt.event.ActionEvent
import java.awt.Dimension
import javax.swing.AbstractAction
import javax.swing.KeyStroke

private val references = listOf(
    SubstitutionReference(
        "{commit}",
        "The complete hash of the commit.",
        "05fc48765f69d52aa229fc5edc3842ab3d9ff517"
    ),
    SubstitutionReference(
        "{commit:short}",
        "The first 6 characters of the commit hash.",
        "05fc48"
    ),
    SubstitutionReference(
        "{branch}",
        "The branch.",
        "master"
    ),
    SubstitutionReference(
        "{file:name}",
        "The selected file name.",
        "NotificationDispatcher.java"
    ),
    SubstitutionReference(
        "{file:path}",
        "The selected file path.",
        "src/uk/co/ben_gibson/git/link"
    ),
    SubstitutionReference(
        "{line:start}",
        "The line selection start.",
        "10"
    ),
    SubstitutionReference(
        "{line:end}",
        "The line selection end.",
        "30"
    ),
    SubstitutionReference(
        "{remote:url}",
        "The full remote url.",
        "https://example.com/ben-gibson/super-project"
    ),
    SubstitutionReference(
        "{remote:url:host}",
        "The remote url host.",
        "example.com"
    ),
    SubstitutionReference(
        "{remote:url:path}",
        "The remote url path.",
        "ben-gibson/super-project"
    ),
    SubstitutionReference(
        "{remote:url:path:n}",
        "A specific part of the remote url path starting at 0.",
        "super-project"
    ),
)

class SubstitutionReferenceTable : TableView<SubstitutionReference>(
    ListTableModel(
        arrayOf(
            SubstitutionColumnInfo("Substitution") { it.substitution },
            SubstitutionColumnInfo("Description") { it.description },
            SubstitutionColumnInfo("Example") { it.example }
        ),
        references
    )
) {

    init {
        // Enable standard copy shortcut so users can reuse substitution strings in templates
        setupCopyHandler()

        val defaultWidth = preferredScrollableViewportSize.width.takeIf { it > 0 } ?: JBUI.scale(520)
        preferredScrollableViewportSize = Dimension(defaultWidth, JBUI.scale(260))
    }

    private fun setupCopyHandler() {
        val modifiers = if (SystemInfoRt.isMac) java.awt.event.KeyEvent.META_DOWN_MASK else java.awt.event.KeyEvent.CTRL_DOWN_MASK
        val copyKeyStroke = KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_C, modifiers)

        getInputMap(WHEN_FOCUSED).put(copyKeyStroke, COPY_ACTION)
        actionMap.put(COPY_ACTION, object : AbstractAction() {
            override fun actionPerformed(event: ActionEvent?) {
                val rows = selectedRows
                if (rows.isEmpty()) {
                    return
                }

                val values = rows
                    .map { row -> getValueAt(row, SUBSTITUTION_COLUMN_INDEX)?.toString() }
                    .filterNotNull()

                if (values.isEmpty()) {
                    return
                }

                CopyPasteManager.getInstance().setContents(StringSelection(values.joinToString(System.lineSeparator())))
            }
        })
    }

    companion object {
        private const val COPY_ACTION = "substitutionCopy"
        private const val SUBSTITUTION_COLUMN_INDEX = 0
    }
}

private class SubstitutionColumnInfo(name: String, val formatter: (SubstitutionReference) -> String) :
    ColumnInfo<SubstitutionReference, String>(name) {
    override fun valueOf(item: SubstitutionReference): String {
        return formatter(item)
    }
}

data class SubstitutionReference(val substitution: String, val description: String, val example: String)
