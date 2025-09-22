package cn.alanhe.git.link.ui.settings

import com.intellij.openapi.components.service
import com.intellij.openapi.options.BoundConfigurable
import com.intellij.ui.dsl.builder.bindSelected
import com.intellij.ui.dsl.builder.bindText
import com.intellij.ui.dsl.builder.bindItem
import com.intellij.ui.dsl.builder.panel
import cn.alanhe.git.link.GitLinkBundle.message
import cn.alanhe.git.link.settings.ApplicationSettings
import cn.alanhe.git.link.settings.MenuLevel

class OtherSettingsConfigurable : BoundConfigurable(message("settings.other.group.title")) {

    override fun createPanel() = panel {
        val settings = service<ApplicationSettings>()

        group(message("settings.other.group.title")) {
            row {
                checkBox(message("settings.other.field.use-remote-for-code-image.label"))
                    .bindSelected(settings::useRemoteForCodeImage)
                    .comment(message("settings.other.field.use-remote-for-code-image.help"))
            }
            row("Code Image Watermark:") {
                textField()
                    .bindText(settings::codeImageWatermark)
                    .comment("Watermark for code snippet images. Leave empty for no watermark.")
            }
        }

        group("Menu Organization") {
            row("Open in GitHub:") {
                comboBox(MenuLevel.values().toList())
                    .bindItem(settings::openInGitHubMenuLevel)
                    .comment("MENU = main menu, SUBMENU = GitLink Others submenu")
            }
            row("Copy GitHub Link:") {
                comboBox(MenuLevel.values().toList())
                    .bindItem(settings::copyGitHubLinkMenuLevel)
                    .comment("MENU = main menu, SUBMENU = GitLink Others submenu")
            }
            row("Copy GitHub Markdown Link:") {
                comboBox(MenuLevel.values().toList())
                    .bindItem(settings::copyGitHubMarkdownLinkMenuLevel)
                    .comment("MENU = main menu, SUBMENU = GitLink Others submenu")
            }
            row("Copy GitHub Markdown Snippet:") {
                comboBox(MenuLevel.values().toList())
                    .bindItem(settings::copyGitHubMarkdownSnippetMenuLevel)
                    .comment("MENU = main menu, SUBMENU = GitLink Others submenu")
            }
            row("Copy GitHub Snippet Image:") {
                comboBox(MenuLevel.values().toList())
                    .bindItem(settings::copyGitHubSnippetImageMenuLevel)
                    .comment("MENU = main menu, SUBMENU = GitLink Others submenu")
            }
            row {
                text("Note: Changes will take effect after restarting the IDE.")
            }
        }
    }
}
