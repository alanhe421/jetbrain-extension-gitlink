package cn.alanhe.git.link.url.factory

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import cn.alanhe.git.link.platform.*

@Service
class UrlFactoryLocator {
    fun locate(platform: Platform) : UrlFactory {
        return when(platform) {
            is Chromium -> service<ChromiumUrlFactory>()
            is Azure -> service<AzureUrlFactory>()
            is BitbucketServer -> service<BitbucketServerUrlFactory>()
            else -> service<TemplatedUrlFactoryProvider>().forPlatform(platform)
        }
    }
}