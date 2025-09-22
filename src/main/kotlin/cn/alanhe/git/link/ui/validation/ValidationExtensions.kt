package cn.alanhe.git.link.ui.validation

import com.intellij.openapi.ui.ValidationInfo
import com.intellij.ui.layout.ValidationInfoBuilder
import cn.alanhe.git.link.GitLinkBundle.message
import cn.alanhe.git.link.git.Commit
import cn.alanhe.git.link.git.File
import cn.alanhe.git.link.ui.LineSelection
import cn.alanhe.git.link.url.*
import cn.alanhe.git.link.url.factory.TemplatedUrlFactory
import cn.alanhe.git.link.url.template.UrlTemplates
import uk.co.ben_gibson.url.Host
import uk.co.ben_gibson.url.URL
import java.lang.IllegalArgumentException

fun ValidationInfoBuilder.notBlank(value: String): ValidationInfo? = if (value.isEmpty()) error(message("validation.required")) else null

fun ValidationInfoBuilder.domain(value: String): ValidationInfo? {
    if (value.isEmpty()) {
        return null
    }

    return try {
        Host(value)
        null
    } catch (e: IllegalArgumentException) {
        error(message("validation.invalid-domain"))
    }
}

fun ValidationInfoBuilder.alphaNumeric(value: String): ValidationInfo? {
    if (value.isEmpty()) {
        return null
    }

    return if (!value.matches("[\\w\\s]+".toRegex())) error(message("validation.alpha-numeric")) else null
}

fun ValidationInfoBuilder.exists(value: String, existing: Collection<String>): ValidationInfo? {
    if (value.isEmpty()) {
        return null
    }

    return if (existing.contains(value)) return error(message("validation.exists")) else null
}

fun ValidationInfoBuilder.length(value: String, min: Int, max: Int): ValidationInfo? {
    if (value.isEmpty()) {
        return null
    }

    return when {
        value.length < min -> error(message("validation.min-length", min))
        value.length > max -> error(message("validation.max-length", max))
        else -> null
    }
}

fun ValidationInfoBuilder.fileAtCommitTemplate(value: String): ValidationInfo? {
    if (value.isEmpty()) {
        return null
    }

    val options = UrlOptions.UrlOptionsFileAtCommit(
        File("foo.kt", false, "src/main", false),
        "main",
        Commit("734232a3c18f0625843bd161c3f5da272b9d53c1"),
        LineSelection(10, 20)
    )

    return urlTemplate(options, fileAtCommit = value)
}

fun ValidationInfoBuilder.fileAtBranchTemplate(value: String): ValidationInfo? {
    if (value.isEmpty()) {
        return null
    }

    val options = UrlOptions.UrlOptionsFileAtBranch(
        File("foo.kt", false, "src/main", false),
        "master",
        LineSelection(10, 20)
    )

    return urlTemplate(options, fileAtBranch = value)
}

fun ValidationInfoBuilder.commitTemplate(value: String): ValidationInfo? {
    if (value.isEmpty()) {
        return null
    }

    val options = UrlOptions.UrlOptionsCommit(Commit("734232a3c18f0625843bd161c3f5da272b9d53c1"), "main")

    return urlTemplate(options, commit = value)
}

private fun ValidationInfoBuilder.urlTemplate(
    options: UrlOptions,
    fileAtBranch: String = "",
    fileAtCommit: String = "",
    commit: String = ""
) : ValidationInfo? {
    val factory = TemplatedUrlFactory(UrlTemplates(fileAtBranch, fileAtCommit, commit))

    return try {
        factory.createUrl(URL.fromString("https://example.com"), options)
        null
    } catch (e: Exception) {
        when(e) {
            is IllegalArgumentException -> error(message("validation.invalid-url-template"))
            else -> throw e
        }
    }
}