package cn.alanhe.git.link.ui.actions.menu

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.components.service
import cn.alanhe.git.link.settings.ApplicationSettings
import cn.alanhe.git.link.settings.MenuLevel

/**
 * Utility class to check if an action should be visible based on menu level settings
 */
object MenuLevelChecker {

    /**
     * Check if an action should be visible in the main editor popup menu
     * based on its menu level setting
     */
    fun shouldShowInMainMenu(event: AnActionEvent, actionType: ActionType): Boolean {
        val settings = service<ApplicationSettings>()

        // Only hide from main menu if we're in EditorPopupMenu context and action is set to SUBMENU
        val isEditorPopup = isEditorPopupContext(event)
        if (!isEditorPopup) {
            return true // Always show in non-editor contexts
        }

        return when (actionType) {
            ActionType.OPEN_IN_GITHUB -> settings.openInGitHubMenuLevel != MenuLevel.SUBMENU
            ActionType.COPY_GITHUB_LINK -> settings.copyGitHubLinkMenuLevel != MenuLevel.SUBMENU
            ActionType.COPY_GITHUB_MARKDOWN_LINK -> settings.copyGitHubMarkdownLinkMenuLevel != MenuLevel.SUBMENU
            ActionType.COPY_GITHUB_MARKDOWN_SNIPPET -> settings.copyGitHubMarkdownSnippetMenuLevel != MenuLevel.SUBMENU
            ActionType.COPY_GITHUB_SNIPPET_IMAGE -> settings.copyGitHubSnippetImageMenuLevel != MenuLevel.SUBMENU
        }
    }

    /**
     * Check if we're in the editor popup menu context
     */
    private fun isEditorPopupContext(event: AnActionEvent): Boolean {
        // This is a simple heuristic - could be improved
        return event.place == "EditorPopup" || event.place == "EditorPopupMenu"
    }

    enum class ActionType {
        OPEN_IN_GITHUB,
        COPY_GITHUB_LINK,
        COPY_GITHUB_MARKDOWN_LINK,
        COPY_GITHUB_MARKDOWN_SNIPPET,
        COPY_GITHUB_SNIPPET_IMAGE
    }
}