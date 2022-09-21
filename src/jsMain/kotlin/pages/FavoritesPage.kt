package pages

import ApplicationScope
import api.favorites
import api.httpClient
import kotlinx.coroutines.launch
import react.FC
import react.Props
import react.dom.html.ReactHTML
import react.useState
import ru.diamant.rabbit.common.model.StatisticResponse
import utils.withPreventDefault

data class FavoritesProps(
    var results: List<StatisticResponse>,
    var header: String,
) : Props

val FavoritesPage = FC<FavoritesProps> { props ->
    ReactHTML.h2 { +"Favorites" }

    props.results = listOf()
    props.header = ""
    val (results, setResults) = useState(props.results)
    val (header, setHeader) = useState(props.header)

    ReactHTML.button {
        +"Load Favorites"
        onClick = withPreventDefault {
            ApplicationScope.launch {
                setHeader("Loading...")
                setResults(httpClient.favorites())
                setHeader("Results:")
            }
        }
    }

    ReactHTML.h3 {
        +header
    }

    for (result in results) {
        ReactHTML.ul {
            for (word in result.topWorlds) {
                ReactHTML.li {
                    +word
                }
            }
        }

        ReactHTML.br {}

        for (image in result.images) {
            ReactHTML.img {
                src = image
            }
            ReactHTML.br {}
        }

        ReactHTML.hr {}
    }
}
