package wifiIntensity

import org.scalajs.dom.MouseEvent
import org.scalajs.dom.html.Div
import org.scalajs.dom.raw.Node


/**
  * User: Taoz
  * Date: 1/16/2017
  * Time: 11:21 AM
  */
class Counter(name: String = "No name", initCount: Int = 0) extends Component[Div] {


  import scalatags.JsDom.short._

  private var count = initCount

  private val countDom = p("count:" + count).render
  private val nameDom = div(h2(name)).render


  def changeName(newName: String): Node = {
    nameDom.innerHTML = ""
    nameDom.appendChild(
      h2(newName).render
    )
  }

  def getCount: Int = count

  private val plusFunc = { event: MouseEvent =>
    event.preventDefault()
    count += 1
    countDom.innerHTML = "count:" + count
  }


  private def changeCounter(c: Int) = { event: MouseEvent =>
    event.preventDefault()
    count += c
    countDom.innerHTML = "count:" + count
  }

  val plusButton = button(*.onclick := plusFunc)("plus").render

  val minusButton = button("minus").render

  minusButton.onclick = changeCounter(-1)


  override def render(): Div = {
    div(*.backgroundColor := "yellow", *.width := "200px")(
      p("this is a Counter"),
      nameDom,
      countDom,
      minusButton,
      plusButton
    ).render
  }


}
