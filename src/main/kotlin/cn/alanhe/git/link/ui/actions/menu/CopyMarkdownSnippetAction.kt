package cn.alanhe.git.link.ui.actions.menu

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.components.service
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.progress.runBackgroundableTask
import cn.alanhe.git.link.ContextCurrentFile
import cn.alanhe.git.link.GitLinkBundle
import cn.alanhe.git.link.pipeline.Pipeline
import cn.alanhe.git.link.platform.PlatformLocator
import cn.alanhe.git.link.ui.LineSelection
import cn.alanhe.git.link.ui.lineSelection
import cn.alanhe.git.link.ui.notification.Notification
import cn.alanhe.git.link.ui.notification.sendNotification
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection

open class CopyMarkdownSnippetAction @JvmOverloads constructor(
    private val placement: Placement = Placement.MAIN,
) : DumbAwareAction() {

    enum class Placement {
        MAIN,
        SUBMENU,
    }

    override fun actionPerformed(event: AnActionEvent) {
        val project = event.project ?: return
        val editor = FileEditorManager.getInstance(project).selectedTextEditor ?: return
        val selectedText = editor.selectionModel.selectedText ?: return
        val file = event.getData(CommonDataKeys.VIRTUAL_FILE) ?: return

        // Get line selection for URL generation
        val lineSelection = editor.lineSelection
        val context = ContextCurrentFile(file, lineSelection)

        // Determine language from file extension
        val language = getLanguageFromExtension(file.extension)

        // Generate URL with line range using existing pipeline
        runBackgroundableTask(GitLinkBundle.message("name"), project, false) {
            val pipeline = project.service<Pipeline>()
            val url = pipeline.accept(context)

            if (url != null) {
                // Create markdown snippet with file link and code block
                val markdownSnippet = "[${file.name}]($url)\n\n```$language\n$selectedText\n```"

                // Copy to clipboard
                Toolkit.getDefaultToolkit().systemClipboard.setContents(
                    StringSelection(markdownSnippet),
                    null
                )

                // Show notification
                sendNotification(Notification(
                    GitLinkBundle.message("notification.markdown-snippet.copied.title"),
                    GitLinkBundle.message("notification.markdown-snippet.copied.message"),
                    type = Notification.Type.TRANSIENT
                ), project)
            } else {
                // Fallback: just copy code block without URL
                val markdownSnippet = "```$language\n$selectedText\n```"

                Toolkit.getDefaultToolkit().systemClipboard.setContents(
                    StringSelection(markdownSnippet),
                    null
                )

                sendNotification(Notification(
                    GitLinkBundle.message("notification.markdown-snippet.copied.title"),
                    "Code snippet copied without URL (Git repository not detected)",
                    type = Notification.Type.TRANSIENT
                ), project)
            }
        }
    }

    override fun update(event: AnActionEvent) {
        super.update(event)

        val project = event.project
        val editor = project?.let { FileEditorManager.getInstance(it).selectedTextEditor }
        val hasSelection = editor?.selectionModel?.hasSelection() == true
        val hasMultiLineSelection = hasSelection && isMultiLineSelection(editor)

        event.presentation.isEnabledAndVisible = hasMultiLineSelection
        event.presentation.text = GitLinkBundle.message("actions.copy-markdown-snippet.title")

        // Set icon from platform (same as other GitLink actions)
        if (project != null) {
            val host = project.service<PlatformLocator>().locate()
            if (host != null) {
                event.presentation.icon = host.icon
            }
        }

        if (placement == Placement.MAIN &&
            !MenuLevelChecker.shouldShowInMainMenu(event, MenuLevelChecker.ActionType.COPY_GITHUB_MARKDOWN_SNIPPET)
        ) {
            event.presentation.isEnabledAndVisible = false
        }
    }

    private fun isMultiLineSelection(editor: com.intellij.openapi.editor.Editor?): Boolean {
        editor ?: return false
        val selectionModel = editor.selectionModel
        if (!selectionModel.hasSelection()) return false

        val document = editor.document
        val startLine = document.getLineNumber(selectionModel.selectionStart)
        val endLine = document.getLineNumber(selectionModel.selectionEnd)

        return endLine > startLine
    }

    private fun getLanguageFromExtension(extension: String?): String {
        return when (extension?.lowercase()) {
            "kt" -> "kotlin"
            "java" -> "java"
            "js" -> "javascript"
            "ts" -> "typescript"
            "py" -> "python"
            "rb" -> "ruby"
            "php" -> "php"
            "cpp", "cc", "cxx" -> "cpp"
            "c" -> "c"
            "cs" -> "csharp"
            "go" -> "go"
            "rs" -> "rust"
            "swift" -> "swift"
            "sh", "bash" -> "bash"
            "sql" -> "sql"
            "html" -> "html"
            "css" -> "css"
            "scss" -> "scss"
            "sass" -> "sass"
            "xml" -> "xml"
            "json" -> "json"
            "yaml", "yml" -> "yaml"
            "md" -> "markdown"
            "gradle" -> "gradle"
            "groovy" -> "groovy"
            "scala" -> "scala"
            "r" -> "r"
            "m" -> "objective-c"
            "vue" -> "vue"
            "jsx" -> "jsx"
            "tsx" -> "tsx"
            else -> extension ?: ""
        }
    }

    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.BGT
    }
}

class CopyMarkdownSnippetSubmenuAction : CopyMarkdownSnippetAction(Placement.SUBMENU)
