package uk.co.ben_gibson.git.link.ui

import com.intellij.ide.BrowserUtil
import com.intellij.openapi.Disposable
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.ActionPlaces
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.SimpleToolWindowPanel
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.ui.content.ContentFactory
import com.intellij.ui.jcef.JBCefBrowser
import com.intellij.ui.jcef.JBCefJSQuery
import uk.co.ben_gibson.git.link.settings.ApplicationSettings
import uk.co.ben_gibson.git.link.ui.notification.Notification
import uk.co.ben_gibson.git.link.ui.notification.sendNotification
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection
import javax.swing.JComponent

class CodeSnippetPreviewPanel(private val project: Project) : SimpleToolWindowPanel(true, true), Disposable {

    private val browser: JBCefBrowser = JBCefBrowser()
    private var currentCode: String = ""
    private var currentLanguage: String = ""

    init {
        setContent(browser.component)
        setupJavaScriptBridge()
        showWelcomeMessage()
    }

    private fun setupJavaScriptBridge() {
        val copyQuery = JBCefJSQuery.create(browser as JBCefBrowser)
        copyQuery.addHandler { request ->
            ApplicationManager.getApplication().invokeLater {
                when (request) {
                    "copy-html" -> copyHtmlToClipboard()
                    "copy-text" -> copyTextToClipboard()
                    "open-rayso" -> openInRaySo()
                }
            }
            return@addHandler null
        }

        // Inject the copy function into the browser
        browser.jbCefClient.addLoadHandler(object : org.cef.handler.CefLoadHandlerAdapter() {
            override fun onLoadEnd(browser: org.cef.browser.CefBrowser?, frame: org.cef.browser.CefFrame?, httpStatusCode: Int) {
                if (frame?.isMain == true) {
                    browser?.executeJavaScript(
                        """
                        window.copySnippet = function(type) {
                            ${copyQuery.inject("type")}
                        };
                        """.trimIndent(),
                        browser.url, 0
                    )
                }
            }
        }, browser.cefBrowser)
    }

    fun updatePreview(code: String, language: String) {
        currentCode = code
        currentLanguage = language

        val html = generatePreviewHtml(code, language)
        browser.loadHTML(html)
    }

    private fun generatePreviewHtml(code: String, language: String): String {
        val settings = service<ApplicationSettings>()
        val useRemote = settings.useRemoteForCodeImage

        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <title>Code Snippet Preview</title>
                <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/highlight.js/11.9.0/styles/github.min.css">
                <script src="https://cdnjs.cloudflare.com/ajax/libs/highlight.js/11.9.0/highlight.min.js"></script>
                <style>
                    body {
                        margin: 0;
                        padding: 20px;
                        font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
                        background: #f5f5f5;
                        color: #333;
                    }
                    .header {
                        background: white;
                        padding: 15px 20px;
                        border-radius: 8px;
                        margin-bottom: 20px;
                        box-shadow: 0 2px 4px rgba(0,0,0,0.1);
                        display: flex;
                        justify-content: space-between;
                        align-items: center;
                    }
                    .title {
                        font-size: 18px;
                        font-weight: 600;
                        color: #2c3e50;
                    }
                    .actions {
                        display: flex;
                        gap: 10px;
                    }
                    .btn {
                        padding: 8px 16px;
                        border: none;
                        border-radius: 6px;
                        cursor: pointer;
                        font-size: 14px;
                        font-weight: 500;
                        text-decoration: none;
                        display: inline-flex;
                        align-items: center;
                        gap: 6px;
                        transition: all 0.2s;
                    }
                    .btn-primary {
                        background: #3498db;
                        color: white;
                    }
                    .btn-primary:hover {
                        background: #2980b9;
                    }
                    .btn-secondary {
                        background: #95a5a6;
                        color: white;
                    }
                    .btn-secondary:hover {
                        background: #7f8c8d;
                    }
                    .btn-success {
                        background: #27ae60;
                        color: white;
                    }
                    .btn-success:hover {
                        background: #229954;
                    }
                    .code-container {
                        background: white;
                        border-radius: 12px;
                        padding: 24px;
                        box-shadow: 0 4px 12px rgba(0,0,0,0.1);
                        border: 1px solid #e1e8ed;
                        position: relative;
                        overflow: hidden;
                    }
                    .code-container::before {
                        content: '';
                        position: absolute;
                        top: 0;
                        left: 0;
                        right: 0;
                        height: 3px;
                        background: linear-gradient(90deg, #667eea 0%, #764ba2 100%);
                    }
                    .code-header {
                        display: flex;
                        justify-content: space-between;
                        align-items: center;
                        margin-bottom: 16px;
                        padding-bottom: 12px;
                        border-bottom: 1px solid #f0f0f0;
                    }
                    .language-badge {
                        background: #f8f9fa;
                        color: #495057;
                        padding: 4px 12px;
                        border-radius: 20px;
                        font-size: 12px;
                        font-weight: 600;
                        text-transform: uppercase;
                        letter-spacing: 0.5px;
                    }
                    .line-count {
                        font-size: 12px;
                        color: #6c757d;
                    }
                    pre {
                        margin: 0;
                        overflow: auto;
                        background: transparent;
                        border: none;
                        padding: 0;
                    }
                    code {
                        font-family: 'JetBrains Mono', 'Fira Code', 'SF Mono', Consolas, monospace;
                        font-size: 14px;
                        line-height: 1.6;
                        background: transparent;
                    }
                    .footer {
                        margin-top: 20px;
                        text-align: center;
                        color: #6c757d;
                        font-size: 12px;
                    }
                    @media (max-width: 768px) {
                        body { padding: 10px; }
                        .header { flex-direction: column; gap: 10px; }
                        .actions { width: 100%; justify-content: center; }
                    }
                </style>
            </head>
            <body>
                <div class="header">
                    <div class="title">Code Snippet Preview</div>
                    <div class="actions">
                        <button class="btn btn-secondary" onclick="copySnippet('copy-text')">
                            📋 Copy Text
                        </button>
                        <button class="btn btn-primary" onclick="copySnippet('copy-html')">
                            🎨 Copy HTML
                        </button>
                        ${if (useRemote) """
                        <button class="btn btn-success" onclick="copySnippet('open-rayso')">
                            🚀 Open Ray.so
                        </button>
                        """ else ""}
                    </div>
                </div>

                <div class="code-container">
                    <div class="code-header">
                        <span class="language-badge">${language.uppercase()}</span>
                        <span class="line-count">${code.lines().size} lines</span>
                    </div>
                    <pre><code class="language-$language" id="code-content">${escapeHtml(code)}</code></pre>
                </div>

                <div class="footer">
                    Generated by Beautiful GitLink • ${if (useRemote) "Ray.so mode enabled" else "Local mode enabled"}
                </div>

                <script>
                    hljs.highlightAll();

                    // Add copy feedback
                    function showCopyFeedback(button, message) {
                        const originalText = button.textContent;
                        button.textContent = message;
                        button.style.background = '#27ae60';
                        setTimeout(() => {
                            button.textContent = originalText;
                            button.style.background = '';
                        }, 2000);
                    }

                    document.addEventListener('click', function(e) {
                        if (e.target.tagName === 'BUTTON') {
                            showCopyFeedback(e.target, '✓ Copied!');
                        }
                    });
                </script>
            </body>
            </html>
        """.trimIndent()
    }

    private fun escapeHtml(text: String): String {
        return text
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#39;")
    }

    private fun copyHtmlToClipboard() {
        val html = generatePreviewHtml(currentCode, currentLanguage)
        Toolkit.getDefaultToolkit().systemClipboard.setContents(
            StringSelection(html),
            null
        )
        sendNotification(Notification(
            "HTML Copied",
            "Code snippet HTML has been copied to clipboard",
            type = Notification.Type.TRANSIENT
        ), project)
    }

    private fun copyTextToClipboard() {
        Toolkit.getDefaultToolkit().systemClipboard.setContents(
            StringSelection(currentCode),
            null
        )
        sendNotification(Notification(
            "Text Copied",
            "Code snippet text has been copied to clipboard",
            type = Notification.Type.TRANSIENT
        ), project)
    }

    private fun openInRaySo() {
        val encodedContent = java.util.Base64.getUrlEncoder().encodeToString(currentCode.toByteArray(Charsets.UTF_8))
        val mappedLanguage = mapLanguageId(currentLanguage)
        val rayUrl = "https://ray.so/#theme=candy&background=white&padding=128&code=$encodedContent&language=$mappedLanguage"
        BrowserUtil.browse(rayUrl)
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

    private fun showWelcomeMessage() {
        val html = """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <title>Code Snippet Preview</title>
                <style>
                    body {
                        margin: 0;
                        padding: 40px;
                        font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
                        background: #f5f5f5;
                        color: #333;
                        text-align: center;
                    }
                    .welcome {
                        background: white;
                        padding: 40px;
                        border-radius: 12px;
                        box-shadow: 0 4px 12px rgba(0,0,0,0.1);
                        max-width: 500px;
                        margin: 0 auto;
                    }
                    .icon {
                        font-size: 48px;
                        margin-bottom: 20px;
                    }
                    .title {
                        font-size: 24px;
                        font-weight: 600;
                        margin-bottom: 12px;
                        color: #2c3e50;
                    }
                    .subtitle {
                        font-size: 16px;
                        color: #6c757d;
                        line-height: 1.5;
                    }
                    .steps {
                        margin-top: 30px;
                        text-align: left;
                    }
                    .step {
                        display: flex;
                        align-items: center;
                        margin-bottom: 12px;
                        padding: 8px 0;
                    }
                    .step-number {
                        background: #3498db;
                        color: white;
                        width: 24px;
                        height: 24px;
                        border-radius: 50%;
                        display: flex;
                        align-items: center;
                        justify-content: center;
                        font-size: 12px;
                        font-weight: 600;
                        margin-right: 12px;
                        flex-shrink: 0;
                    }
                </style>
            </head>
            <body>
                <div class="welcome">
                    <div class="icon">🎨</div>
                    <div class="title">Code Snippet Preview</div>
                    <div class="subtitle">Select multiple lines of code and use "Create GitHub Snippet Image" to see a live preview here.</div>

                    <div class="steps">
                        <div class="step">
                            <div class="step-number">1</div>
                            <div>Select multiple lines of code in the editor</div>
                        </div>
                        <div class="step">
                            <div class="step-number">2</div>
                            <div>Right-click → GitLink Others → Create GitHub Snippet Image</div>
                        </div>
                        <div class="step">
                            <div class="step-number">3</div>
                            <div>View the rendered preview and copy or export as needed</div>
                        </div>
                    </div>
                </div>
            </body>
            </html>
        """.trimIndent()

        browser.loadHTML(html)
    }

    override fun dispose() {
        browser.dispose()
    }

    companion object {
        fun showPreview(project: Project, code: String, language: String) {
            val toolWindowManager = ToolWindowManager.getInstance(project)
            val toolWindow = toolWindowManager.getToolWindow("Code Snippet Preview")

            if (toolWindow != null) {
                val content = toolWindow.contentManager.getContent(0)
                val panel = content?.component as? CodeSnippetPreviewPanel
                panel?.updatePreview(code, language)
                toolWindow.show()
            }
        }
    }
}

class CodeSnippetPreviewToolWindowFactory : ToolWindowFactory {
    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val previewPanel = CodeSnippetPreviewPanel(project)
        val content = ContentFactory.getInstance().createContent(previewPanel, "", false)
        toolWindow.contentManager.addContent(content)
    }
}