package uk.co.ben_gibson.git.link.ui.settings

import com.intellij.openapi.components.service
import com.intellij.openapi.options.BoundConfigurable
import com.intellij.ui.dsl.builder.bindSelected
import com.intellij.ui.dsl.builder.bindText
import com.intellij.ui.dsl.builder.panel
import uk.co.ben_gibson.git.link.GitLinkBundle
import uk.co.ben_gibson.git.link.GitLinkBundle.message
import uk.co.ben_gibson.git.link.settings.ApplicationSettings

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
        row {
            browserLink(message("actions.report-bug.title"), GitLinkBundle.URL_BUG_REPORT)
        }
    }
}