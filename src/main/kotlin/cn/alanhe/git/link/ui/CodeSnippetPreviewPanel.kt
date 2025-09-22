package cn.alanhe.git.link.ui

import com.intellij.ide.BrowserUtil
import com.intellij.openapi.Disposable
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.ActionPlaces
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.SimpleToolWindowPanel
import com.intellij.openapi.fileChooser.FileChooserFactory
import com.intellij.openapi.fileChooser.FileSaverDescriptor
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.ui.content.ContentFactory
import com.intellij.ui.jcef.JBCefBrowser
import com.intellij.ui.jcef.JBCefJSQuery
import cn.alanhe.git.link.settings.ApplicationSettings
import cn.alanhe.git.link.ui.notification.Notification
import cn.alanhe.git.link.ui.notification.sendNotification
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection
import javax.swing.JComponent
import java.io.File
import java.io.FileOutputStream
import java.util.Base64

class CodeSnippetPreviewPanel(private val project: Project) : SimpleToolWindowPanel(true, true), Disposable {

    private val browser: JBCefBrowser = JBCefBrowser()
    private var currentCode: String = ""
    private var currentLanguage: String = ""

    init {
        setContent(browser.component)
        setupJavaScriptBridge()
        setupDevTools()
        showWelcomeMessage()
    }

    private fun setupDevTools() {
        // Add Ctrl+Shift+I shortcut for DevTools (official method)
        browser.component.registerKeyboardAction(
            {
                openDevTools()
            },
            javax.swing.KeyStroke.getKeyStroke("ctrl shift I"),
            javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW
        )

        // Also add F12 shortcut
        browser.component.registerKeyboardAction(
            {
                openDevTools()
            },
            javax.swing.KeyStroke.getKeyStroke("F12"),
            javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW
        )

        println("[CodeSnippetPreview] DevTools shortcuts registered: Ctrl+Shift+I and F12")
    }

    private fun openDevTools() {
        try {
            // Official method from documentation - exact syntax
            val devTools = browser.cefBrowser.devTools
            val devToolsBrowser = JBCefBrowser.createBuilder()
                .setCefBrowser(devTools)
                .setClient(browser.jbCefClient)
                .build()

            // Create a dialog to show DevTools
            val dialog = com.intellij.openapi.ui.DialogBuilder()
            dialog.setTitle("Code Snippet Preview - DevTools")
            dialog.setCenterPanel(devToolsBrowser.component)
            dialog.setPreferredFocusComponent(devToolsBrowser.component)
            dialog.setDimensionServiceKey("CodeSnippetPreviewDevTools")
            dialog.show()

            println("[CodeSnippetPreview] DevTools opened successfully")
        } catch (e: Exception) {
            println("[CodeSnippetPreview] Failed to open DevTools: ${e.message}")
            e.printStackTrace()
        }
    }

    private fun setupJavaScriptBridge() {
        val copyQuery = JBCefJSQuery.create(browser as JBCefBrowser)
        copyQuery.addHandler { request ->
            ApplicationManager.getApplication().invokeLater {
                when {
                    request == "copy-html" -> copyHtmlToClipboard()
                    request == "copy-text" -> copyTextToClipboard()
                    request == "open-rayso" -> openInRaySo()
                    request == "open-devtools" -> openDevTools()
                    request == "copy-image-success" -> showCopyImageSuccessNotification()
                    request == "copy-image-error" -> showCopyImageErrorNotification()
                    request == "error" -> showGeneralErrorNotification()
                    request.startsWith("download-image:") -> downloadImage(request.substringAfter("download-image:"))
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

        // Load HTML template from resources
        val templateStream = javaClass.getResourceAsStream("/templates/code-snippet-preview.html")
            ?: throw IllegalStateException("Could not find code-snippet-preview.html template")

        val template = templateStream.bufferedReader().use { it.readText() }

        // Replace placeholders with actual values
        val watermark = settings.codeImageWatermark
        val showWatermark = watermark.isNotEmpty()
        val application = ApplicationManager.getApplication()
        val debugButton = if (application.isInternal || application.isUnitTestMode) DEBUG_BUTTON_HTML else ""

        return template
            .replace("%LANGUAGE%", language)
            .replace("%CODE%", escapeHtml(code))
            .replace("%FILENAME%", "code-snippet") // Default filename, could be made dynamic
            .replace("%SHOW_REMOTE%", if (useRemote) "block" else "none")
            .replace("%WATERMARK%", escapeHtml(watermark))
            .replace("%WATERMARK_DISPLAY%", if (showWatermark) "block" else "none")
            .replace("%DEVTOOLS_BUTTON%", debugButton)
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

    private fun showCopyImageSuccessNotification() {
        sendNotification(Notification(
            "Image Copied",
            "Code snippet image has been copied to clipboard",
            type = Notification.Type.TRANSIENT
        ), project)
    }

    private fun showCopyImageErrorNotification() {
        sendNotification(Notification(
            "Copy Failed",
            "Failed to copy image to clipboard",
            type = Notification.Type.PERSISTENT
        ), project)
    }

    private fun showGeneralErrorNotification() {
        sendNotification(Notification(
            "Error",
            "An error occurred while generating the image",
            type = Notification.Type.PERSISTENT
        ), project)
    }

    private fun downloadImage(dataUrl: String) {
        try {
            // Extract base64 data from data URL
            val base64Data = dataUrl.substringAfter("data:image/png;base64,")
            val imageBytes = Base64.getDecoder().decode(base64Data)

            // Show file save dialog
            val descriptor = FileSaverDescriptor("Save Code Snippet Image", "Save code snippet as PNG image", "png")
            val saveWrapper = FileChooserFactory.getInstance().createSaveFileDialog(descriptor, project)
            val fileToSave = saveWrapper.save("code-snippet.png")

            if (fileToSave != null) {
                FileOutputStream(fileToSave.file).use { fos ->
                    fos.write(imageBytes)
                }
                sendNotification(Notification(
                    "Image Saved",
                    "Code snippet image saved to ${fileToSave.file.absolutePath}",
                    type = Notification.Type.TRANSIENT
                ), project)
            }
        } catch (e: Exception) {
            sendNotification(Notification(
                "Save Failed",
                "Failed to save image: ${e.message}",
                type = Notification.Type.PERSISTENT
            ), project)
        }
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

private val DEBUG_BUTTON_HTML = """
            <button id="debugButton" data-tooltip="Open DevTools (Ctrl+Shift+I)" style="margin-left: 8px;" onclick="openDebugTools()">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <path d="M20 8h-2.81c-.45-.78-1.07-1.45-1.82-1.96L17 4.41 15.59 3l-2.17 2.17C12.96 5.06 12.49 5 12 5s-.96.06-1.42.17L8.41 3 7 4.41l1.62 1.63C7.88 6.55 7.26 7.22 6.81 8H4v2h2.09c-.05.33-.09.66-.09 1v1H4v2h2v1c0 .34.04.67.09 1H4v2h2.81c1.04 1.79 2.97 3 5.19 3s4.15-1.21 5.19-3H20v-2h-2.09c.05-.33.09-.66.09-1v-1h2v-2h-2v-1c0-.34-.04-.67-.09-1H20V8zm-6 8h-4v-2h4v2zm0-4h-4v-2h4v2z" fill="currentColor"/>
                </svg>
            </button>
""".trimIndent()
