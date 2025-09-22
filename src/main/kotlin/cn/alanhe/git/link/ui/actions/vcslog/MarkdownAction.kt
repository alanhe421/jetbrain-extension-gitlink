package cn.alanhe.git.link.ui.actions.vcslog

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.Project
import com.intellij.vcs.log.VcsLogDataKeys.VCS_LOG_COMMIT_SELECTION
import cn.alanhe.git.link.Context
import cn.alanhe.git.link.ContextCommit
import cn.alanhe.git.link.git.Commit
import cn.alanhe.git.link.ui.actions.Action

class MarkdownAction: Action(Type.COPY_MARKDOWN) {
    override fun buildContext(project: Project, event: AnActionEvent): Context? {
        val vcsCommit = event.getData(VCS_LOG_COMMIT_SELECTION)?.cachedFullDetails?.get(0) ?: return null

        return ContextCommit(vcsCommit.root, Commit(vcsCommit.id.toString()))
    }

    override fun shouldBeEnabled(event: AnActionEvent): Boolean {
        return event.getData(VCS_LOG_COMMIT_SELECTION)?.cachedFullDetails?.size == 1
    }
}