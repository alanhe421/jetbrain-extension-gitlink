package cn.alanhe.git.link

import com.intellij.openapi.vfs.VirtualFile
import cn.alanhe.git.link.git.Commit
import cn.alanhe.git.link.ui.LineSelection

sealed class Context(val file: VirtualFile)

class ContextCommit(file: VirtualFile, val commit: Commit) : Context(file)
class ContextFileAtCommit(file: VirtualFile, val commit: Commit, val lineSelection: LineSelection? = null) : Context(file)
class ContextCurrentFile(file: VirtualFile, val lineSelection: LineSelection? = null) : Context(file)
