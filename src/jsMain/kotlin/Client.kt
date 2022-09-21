import kotlinx.browser.document
import react.createElement
import react.dom.render

fun main() {
    val container = document.createElement("div")
    document.body!!.appendChild(container)
    render(createElement(App), container)
}