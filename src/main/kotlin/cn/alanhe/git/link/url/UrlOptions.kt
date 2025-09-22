package cn.alanhe.git.link.url

import cn.alanhe.git.link.git.Commit
import cn.alanhe.git.link.git.File
import cn.alanhe.git.link.ui.LineSelection

sealed interface UrlOptions {
    class UrlOptionsCommit(val commit: Commit, val currentBranch: String) : UrlOptions
    class UrlOptionsFileAtCommit(val file: File, val currentBranch: String, val commit: Commit, val lineSelection: LineSelection? = null) : UrlOptions
    class UrlOptionsFileAtBranch(val file: File, val branch: String, val lineSelection: LineSelection? = null) : UrlOptions
}