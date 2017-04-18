package wifiIntensity

import org.scalajs.dom.MouseEvent
import org.scalajs.dom.html.Div

/**
  * User: Taoz
  * Date: 1/16/2017
  * Time: 11:20 AM
  */
object HelloPage extends Component[Div]{

  import scalatags.JsDom.short._

  val title = h1(*.textAlign := "center")("hello")
  val content = p(*.textAlign := "center")("welcome to 2017.")

  val counter = new Counter("hello", 10)

  val inputDom = input(*.placeholder := "counter name").render
  val changeNameButton = button("update").render
  changeNameButton.onclick = { e: MouseEvent =>
    println(s"change count name to ${inputDom.value}")
    counter.changeName(inputDom.value)
  }
  
  override def render(): Div = {
    println("render HelloPage")
    div(
      title,
      content,
      inputDom,
      changeNameButton,
      counter.render()
    ).render
  }

}
