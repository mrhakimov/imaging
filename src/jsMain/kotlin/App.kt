import pages.*
import react.FC
import react.Props
import react.create
import react.createElement
import react.router.Route
import react.router.Routes
import react.router.dom.BrowserRouter
import security.AuthProvider
import security.useAuth
import utils.Resource

val App = FC<Props> {
    BrowserRouter {
        AuthProvider {
            AppRoutes {}
        }
    }
}

val AppRoutes = FC<Props> {
    val isAuthorized = useAuth().user is Resource.Ok

    Routes {
        Route {
            path = "/"
            element = CommonPage.create {
                menuItems =
                    mapOf("/" to "Home") +
                            if (isAuthorized) {
                                mapOf(
                                    "history" to "History",
                                    "favorites" to "Favorites"
                                )
                            }
                            else {
                                mapOf()
                            }
            }

            Route {
                index = true
                element = createElement(HomePage)
            }

            if (isAuthorized) {
                Route {
                    path = "logout"
                    element = createElement(LogoutPage)
                }

                Route {
                    path = "history"
                    element = createElement(HistoryPage)
                }

                Route {
                    path = "favorites"
                    element = createElement(FavoritesPage)
                }
            }
            else {
                Route {
                    path = "login"
                    element = createElement(LoginPage)
                }

                Route {
                    path = "register"
                    element = createElement(RegisterPage)
                }
            }

            Route {
                path = "*"
                element = createElement(NotFoundPage)
            }
        }
    }
}
