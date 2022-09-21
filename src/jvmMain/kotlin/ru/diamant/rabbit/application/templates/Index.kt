package ru.diamant.rabbit.application.templates

import kotlinx.html.*

fun HTML.index() {
    head {
        title("crawler")
        styleLink(url = "/static/css/normalize.css")
    }
    body {
        div {
            id = "root"
        }
        script(src = "/static/template.js") {}
    }
}
