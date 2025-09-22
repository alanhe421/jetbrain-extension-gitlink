package uk.co.ben_gibson.git.link.ui.actions.menu

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.components.service
import uk.co.ben_gibson.git.link.platform.PlatformLocator

class GitLinkOthersGroup : DefaultActionGroup() {

    override fun update(event: AnActionEvent) {
        super.update(event)

        event.presentation.text = "GitLink Others"

        // Set icon from platform (same as other GitLink actions)
        val project = event.project
        if (project != null) {
            val host = project.service<PlatformLocator>().locate()
            if (host != null) {
                event.presentation.icon = host.icon
            }
        }
    }

    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.BGT
    }
}