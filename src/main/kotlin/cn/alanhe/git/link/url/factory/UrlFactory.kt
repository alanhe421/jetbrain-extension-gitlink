package cn.alanhe.git.link.url.factory

import cn.alanhe.git.link.url.UrlOptions
import uk.co.ben_gibson.url.URL

interface UrlFactory {
    fun createUrl(baseUrl: URL, options: UrlOptions) : URL
}