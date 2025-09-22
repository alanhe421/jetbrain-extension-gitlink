package cn.alanhe.git.link.ui

data class LineSelection(val start: Int, val end: Int) {
    constructor(start: Int) : this(start, start)
}