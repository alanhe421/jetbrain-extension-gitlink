package uk.co.ben_gibson.git.link.settings

/**
 * Represents the menu level where an action should be displayed
 */
enum class MenuLevel {
    /**
     * Display action directly in the main context menu
     */
    MENU,

    /**
     * Display action in the GitLink Others submenu
     */
    SUBMENU
}

/**
 * Configuration for menu levels of different GitLink actions
 */
data class MenuLevelSettings(
    var openInGitHub: MenuLevel = MenuLevel.MENU,
    var copyGitHubLink: MenuLevel = MenuLevel.MENU,
    var copyGitHubMarkdownLink: MenuLevel = MenuLevel.SUBMENU,
    var copyGitHubMarkdownSnippet: MenuLevel = MenuLevel.SUBMENU,
    var copyGitHubSnippetImage: MenuLevel = MenuLevel.SUBMENU
)