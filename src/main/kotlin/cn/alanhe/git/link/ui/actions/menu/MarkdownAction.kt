package cn.alanhe.git.link.ui.actions.menu

import com.intellij.openapi.actionSystem.AnActionEvent

open class MarkdownAction @JvmOverloads constructor(
    private val placement: Placement = Placement.MAIN,
) : MenuAction(Type.COPY_MARKDOWN) {

    enum class Placement {
        MAIN,
        SUBMENU,
    }

    override fun update(event: AnActionEvent) {
        super.update(event)

        if (placement == Placement.MAIN &&
            !MenuLevelChecker.shouldShowInMainMenu(event, MenuLevelChecker.ActionType.COPY_GITHUB_MARKDOWN_LINK)
        ) {
            event.presentation.isEnabledAndVisible = false
        }
    }

    override fun shouldShowIcon(event: AnActionEvent): Boolean {
        return placement == Placement.MAIN
    }
}

class MarkdownSubmenuAction : MarkdownAction(Placement.SUBMENU)
