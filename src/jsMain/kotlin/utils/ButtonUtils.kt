package utils

import org.w3c.dom.Element
import react.dom.events.MouseEvent
import react.dom.events.MouseEventHandler

fun <T : Element> withPreventDefault(action: (MouseEvent<T, *>) -> Unit): MouseEventHandler<T> =
    {
        it.preventDefault()
        action(it)
    }