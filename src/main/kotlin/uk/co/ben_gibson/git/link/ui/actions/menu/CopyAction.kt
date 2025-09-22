package uk.co.ben_gibson.git.link.ui.actions.menu

import com.intellij.openapi.actionSystem.AnActionEvent

class CopyAction : MenuAction(Type.COPY) {

    override fun update(event: AnActionEvent) {
        super.update(event)

        // Check if this action should be visible in main menu based on settings
        if (!MenuLevelChecker.shouldShowInMainMenu(event, MenuLevelChecker.ActionType.COPY_GITHUB_LINK)) {
            event.presentation.isEnabledAndVisible = false
        }
    }
}