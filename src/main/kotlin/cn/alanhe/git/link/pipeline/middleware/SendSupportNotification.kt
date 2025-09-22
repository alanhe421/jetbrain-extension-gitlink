package cn.alanhe.git.link.pipeline.middleware

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import cn.alanhe.git.link.pipeline.Pass
import cn.alanhe.git.link.settings.ApplicationSettings
import cn.alanhe.git.link.ui.notification.Notification
import cn.alanhe.git.link.ui.notification.sendNotification
import uk.co.ben_gibson.url.URL

@Service
class SendSupportNotification : Middleware {
    override val priority = 10

    override fun invoke(pass: Pass, next: () -> URL?) : URL? {
        val url = next()

        val settings = service<ApplicationSettings>()

        if (settings.requestSupport && (settings.hits == 5 || settings.hits % 50 == 0)) {
            sendNotification(Notification.star())
        }

        return url
    }
}

