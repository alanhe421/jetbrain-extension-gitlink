package cn.alanhe.git.link.ui.actions.annotation

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.Project
import com.intellij.openapi.vcs.actions.ShowAnnotateOperationsPopup
import git4idea.annotate.GitFileAnnotation
import cn.alanhe.git.link.ui.actions.Action
import cn.alanhe.git.link.Context
import cn.alanhe.git.link.ContextCommit
import cn.alanhe.git.link.git.Commit

class CommitBrowserAction(private val annotation: GitFileAnnotation): Action(Type.BROWSER) {

    override fun buildContext(project: Project, event: AnActionEvent): Context? {
        val lineNumber = ShowAnnotateOperationsPopup.getAnnotationLineNumber(event.dataContext)

        val revision = annotation.getLineRevisionNumber(lineNumber) ?: return null

        return ContextCommit(annotation.file, Commit(revision.toString()))
    }
}