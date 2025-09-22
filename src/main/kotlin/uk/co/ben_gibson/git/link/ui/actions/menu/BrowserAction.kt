package uk.co.ben_gibson.git.link.ui.actions.menu

import com.intellij.openapi.actionSystem.AnActionEvent

class BrowserAction : MenuAction(Type.BROWSER) {

    override fun update(event: AnActionEvent) {
        super.update(event)

        // Check if this action should be visible in main menu based on settings
        if (!MenuLevelChecker.shouldShowInMainMenu(event, MenuLevelChecker.ActionType.OPEN_IN_GITHUB)) {
            event.presentation.isEnabledAndVisible = false
        }
    }
}