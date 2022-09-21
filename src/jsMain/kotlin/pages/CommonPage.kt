package pages

import components.AuthStatus
import react.FC
import react.Props
import react.dom.html.ReactHTML
import react.key
import react.router.Outlet
import react.router.dom.Link

external interface CommonPageProps : Props {
    var menuItems: Map<String, String>
}

val CommonPage = FC<CommonPageProps> { props ->
    ReactHTML.nav {
        props.menuItems.forEach {
            Link {
                key = it.key
                to = it.key

                +it.value
            }
            +" "
        }
    }

    AuthStatus()

    ReactHTML.hr()

    Outlet()
}
