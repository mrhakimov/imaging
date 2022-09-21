package pages

import ApplicationScope
import api.history
import api.httpClient
import kotlinx.coroutines.launch
import react.FC
import react.Props
import react.dom.html.ReactHTML
import react.useState
import ru.diamant.rabbit.common.model.StatisticResponse
import utils.withPreventDefault

data class HistoryProps(
    var results: List<StatisticResponse>,
    var header: String,
) : Props

val HistoryPage = FC<HistoryProps> { props ->
    ReactHTML.h2 { +"History" }

    props.results = listOf()
    props.header = ""
    val (results, setResults) = useState(props.results)
    val (header, setHeader) = useState(props.header)

    ReactHTML.button {
        +"Load History"
        onClick = withPreventDefault {
            ApplicationScope.launch {
                setHeader("Loading...")
                setResults(httpClient.history())
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
