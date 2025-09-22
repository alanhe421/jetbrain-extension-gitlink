package cn.alanhe.git.link.pipeline

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import cn.alanhe.git.link.Context
import cn.alanhe.git.link.pipeline.middleware.*
import cn.alanhe.git.link.pipeline.middleware.Timer
import uk.co.ben_gibson.url.URL
import java.util.*
import kotlin.collections.Set

@Service(Service.Level.PROJECT)
class Pipeline(private val project: Project) {
    private val middlewares: Set<Middleware> = setOf(
        service<GenerateUrl>(),
        service<Timer>(),
        service<RecordHit>(),
        service<cn.alanhe.git.link.pipeline.middleware.ForceHttps>(),
        service<SendSupportNotification>(),
        service<ResolveContext>(),
    )

    fun accept(context: Context) : URL? {
        if (middlewares.isEmpty()) {
            throw IllegalStateException("No middleware registered")
        }

        val queue = PriorityQueue(middlewares)

        return next(queue, Pass(project, context))
    }

    private fun next(queue: PriorityQueue<Middleware>, pass: Pass) : URL? {
        val middleware = queue.remove()

        return middleware(pass) {
            return@middleware next(queue, pass)
        }
    }
}