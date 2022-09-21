package pages

import ApplicationScope
import api.addFavorite
import api.crawl
import api.httpClient
import kotlinx.coroutines.launch
import react.FC
import react.Props
import react.dom.html.InputType
import react.dom.html.ReactHTML
import react.router.useNavigate
import react.useState
import ru.diamant.rabbit.common.model.StatisticRequest
import ru.diamant.rabbit.common.model.StatisticResponse
import utils.withPreventDefault

data class HomeProps(
    var request: StatisticRequest,
    var results: StatisticResponse,
    var resultHeader: String
) : Props

val HomePage = FC<HomeProps> { props ->
    val navigate = useNavigate()

    props.request = StatisticRequest("", 1)
    props.results = StatisticResponse(listOf(), setOf())
    props.resultHeader = ""

    val (url, setUrl) = useState(props.request.url)
    val (level, setLevel) = useState(props.request.level)
    val (resultHeader, setResultHeader) = useState(props.resultHeader)
    val (results, setResults) = useState(props.results)

    ReactHTML.h2 { +"Home" }

    ReactHTML.form {
        className = "form"
        ReactHTML.label {
            htmlFor = "url"
        }
        +"URL:"

        ReactHTML.input {
            id = "url"
            name = "url"
            type = InputType.url
            value = url
            autoFocus = true
            minLength = 10 // http://a.a
            onChange = { event ->
                val target = event.target
                setUrl(target.value)
            }
        }

        ReactHTML.br {}

        ReactHTML.label {
            htmlFor = "level"
        }
        +"level:"

        ReactHTML.input {
            id = "level"
            name = "level"
            type = InputType.number
            min = 1.0
            max = 4.0
            value = level.toString()
            required = true
            onChange = { event ->
                val target = event.target
                setLevel(target.value.toInt())
            }
        }

        ReactHTML.br {}

        ReactHTML.input {
            type = InputType.submit
            value = "Crawl!"
            onClick = withPreventDefault {
                ApplicationScope.launch {
                    setResultHeader("Loading...")
                    setResults(httpClient.crawl(StatisticRequest(url, level)))

                    setResultHeader("Results:")
                }
            }
        }
    }

    ReactHTML.h2 {
        +resultHeader
    }

    ReactHTML.div {
        className = "one-line"

        ReactHTML.ul {
            for (word in results.topWorlds) {
                ReactHTML.li {
                    +word
                }
            }
        }

        ReactHTML.button {
            disabled = results.topWorlds.isEmpty()
            hidden = results.topWorlds.isEmpty()
            onClick = withPreventDefault {
                ApplicationScope.launch {
                    httpClient.addFavorite(StatisticRequest(url, level))
                }
                navigate("/")
            }
            +"Add to favorites"
        }
    }

    ReactHTML.div {
        for (image in results.images) {
            ReactHTML.img {
                src = image
            }
            ReactHTML.br {}
        }
    }
}
