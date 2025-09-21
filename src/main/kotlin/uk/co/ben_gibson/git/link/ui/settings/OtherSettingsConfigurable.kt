package uk.co.ben_gibson.git.link.ui.settings

import com.intellij.openapi.options.BoundConfigurable
import com.intellij.ui.dsl.builder.panel
import uk.co.ben_gibson.git.link.GitLinkBundle
import uk.co.ben_gibson.git.link.GitLinkBundle.message

class OtherSettingsConfigurable : BoundConfigurable(message("settings.other.group.title")) {

    override fun createPanel() = panel {
        group(message("settings.other.group.title")) {
            row {
                comment(message("settings.other.empty.comment"))
            }
        }
        row {
            browserLink(message("actions.report-bug.title"), GitLinkBundle.URL_BUG_REPORT)
        }
    }
}