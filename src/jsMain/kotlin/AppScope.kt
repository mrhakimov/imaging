import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlin.coroutines.CoroutineContext

object ApplicationScope : CoroutineScope {
    private val job = Job()
    override val coroutineContext: CoroutineContext
        get() = job
}