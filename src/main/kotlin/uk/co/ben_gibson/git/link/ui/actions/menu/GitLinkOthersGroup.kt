package uk.co.ben_gibson.git.link.ui.actions.menu

import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.components.service
import uk.co.ben_gibson.git.link.platform.PlatformLocator
import uk.co.ben_gibson.git.link.settings.ApplicationSettings
import uk.co.ben_gibson.git.link.settings.MenuLevel

class GitLinkOthersGroup : DefaultActionGroup() {

    override fun update(event: AnActionEvent) {
        // First update child actions
        updateChildActions()

        // Then update presentation
        event.presentation.text = "GitLink Others"

        // Ensure the group is always enabled if it has children
        val hasChildren = childrenCount > 0
        event.presentation.isEnabledAndVisible = hasChildren

        // Set icon from platform (same as other GitLink actions)
        val project = event.project
        if (project != null) {
            val host = project.service<PlatformLocator>().locate()
            if (host != null) {
                event.presentation.icon = host.icon
            }
        }

        // Don't call super.update() to avoid interference
        println("[GitLinkOthersGroup] Update: hasChildren=$hasChildren, enabled=${event.presentation.isEnabled}")
    }

    private fun updateChildActions() {
        val settings = service<ApplicationSettings>()
        val actionManager = ActionManager.getInstance()

        // Clear current children
        removeAll()

        var addedCount = 0

        // Add actions that should be in submenu
        if (settings.copyGitHubMarkdownLinkMenuLevel == MenuLevel.SUBMENU) {
            actionManager.getAction("uk.co.ben_gibson.git.link.ui.actions.menu.MarkdownAction.Others")?.let {
                add(it)
                addedCount++
            }
        }

        if (settings.copyGitHubMarkdownSnippetMenuLevel == MenuLevel.SUBMENU) {
            actionManager.getAction("uk.co.ben_gibson.git.link.ui.actions.menu.CopyMarkdownSnippetAction")?.let {
                add(it)
                addedCount++
            }
        }

        if (settings.copyGitHubSnippetImageMenuLevel == MenuLevel.SUBMENU) {
            actionManager.getAction("uk.co.ben_gibson.git.link.ui.actions.menu.CreateGitHubSnippetImageAction")?.let {
                add(it)
                addedCount++
            }
        }

        // Debug logging
        println("[GitLinkOthersGroup] Added $addedCount actions to submenu")
    }

    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.BGT
    }
}