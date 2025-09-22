package uk.co.ben_gibson.git.link.ui.actions.menu

import com.intellij.ide.BrowserUtil
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.components.service
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.DumbAwareAction
import uk.co.ben_gibson.git.link.GitLinkBundle
import uk.co.ben_gibson.git.link.platform.PlatformLocator
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.*

class CreateGitHubSnippetImageAction : DumbAwareAction() {

    override fun actionPerformed(event: AnActionEvent) {
        val project = event.project ?: return
        val editor = FileEditorManager.getInstance(project).selectedTextEditor ?: return
        val selectedText = editor.selectionModel.selectedText ?: return
        val file = event.getData(CommonDataKeys.VIRTUAL_FILE) ?: return

        // Determine language from file extension
        val language = getLanguageFromExtension(file.extension)

        // Generate ray.so URL
        val rayUrl = getRemoteImageUrl(selectedText, language)

        // Open in browser
        BrowserUtil.browse(rayUrl)
    }

    override fun update(event: AnActionEvent) {
        super.update(event)

        val project = event.project
        val editor = project?.let { FileEditorManager.getInstance(it).selectedTextEditor }
        val hasSelection = editor?.selectionModel?.hasSelection() == true
        val hasMultiLineSelection = hasSelection && isMultiLineSelection(editor)

        event.presentation.isEnabledAndVisible = hasMultiLineSelection
        event.presentation.text = GitLinkBundle.message("actions.create-snippet-image.title")

        // Set icon from platform (same as other GitLink actions)
        if (project != null) {
            val host = project.service<PlatformLocator>().locate()
            if (host != null) {
                event.presentation.icon = host.icon
            }
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

    private fun getRemoteImageUrl(code: String, language: String): String {
        val encodedContent = Base64.getUrlEncoder().encodeToString(code.toByteArray(StandardCharsets.UTF_8))
        val mappedLanguage = mapLanguageId(language)
        return "https://ray.so/#theme=candy&background=white&padding=128&code=$encodedContent&language=$mappedLanguage"
    }

    private fun mapLanguageId(language: String): String {
        return when (language.lowercase()) {
            "kt", "kotlin" -> "kotlin"
            "java" -> "java"
            "js", "javascript" -> "javascript"
            "ts", "typescript" -> "typescript"
            "py", "python" -> "python"
            "rb", "ruby" -> "ruby"
            "php" -> "php"
            "cpp", "cc", "cxx" -> "cpp"
            "c" -> "c"
            "cs", "csharp" -> "csharp"
            "go" -> "go"
            "rs", "rust" -> "rust"
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
            "md", "markdown" -> "markdown"
            "gradle" -> "gradle"
            "groovy" -> "groovy"
            "scala" -> "scala"
            "r" -> "r"
            "m", "objective-c" -> "objectivec"
            "vue" -> "vue"
            "jsx" -> "jsx"
            "tsx" -> "tsx"
            else -> language.ifEmpty { "auto" }
        }
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