package pages

import react.FC
import react.Props
import react.dom.html.ReactHTML

val NotFoundPage = FC<Props> {
    ReactHTML.h2 { +"No such page" }
}
