package pages

import react.FC
import react.Props
import react.dom.html.ReactHTML

val HomePage = FC<Props> {
    ReactHTML.h2 { +"Home" }
    ReactHTML.br
    ReactHTML.p {
        +"Сервис, который будет рекурсивно обходить web-страницу, и предоставлять список из картинок с этой страницы и топ-5 слов."
    }
}
