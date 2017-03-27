package wifiIntensity.utils

import java.util.concurrent.atomic.AtomicInteger

import org.joda.time.format.DateTimeFormatter

/**
  * User: Taoz
  * Date: 12/12/2015
  * Time: 4:18 PM
  */
object TimeTool {

  import com.github.nscala_time.time.Imports._

  val fmt_yyyyMMdd = DateTimeFormat.forPattern("yyyyMMdd")
  val fmt_yyyyMMddHHmmssSSS = DateTimeFormat.forPattern("yyyyMMddHHmmssSSS")
  val fmt_yyyysMMsdd = DateTimeFormat.forPattern("yyyy/MM/dd")
  val fmt_yyyysMM = DateTimeFormat.forPattern("yyyy/MM")

  def today_yyyyMMdd() = DateTime.now.toString(fmt_yyyyMMdd)


  def parseLong_yyyyMMdd(yyyyMMdd: Long) = {
    val y = yyyyMMdd.toInt / 10000
    val MMdd = yyyyMMdd.toInt % 10000
    val m = MMdd / 100
    val d = MMdd % 100

    val date =
      DateTime.now.withYear(y).withMonth(m).withDay(d)
        .withHour(0).withMinute(0).withSecond(0)
    date
  }

  def parse_yyyyMMdd(str: String) = fmt_yyyyMMdd.parseDateTime(str)

  def format_yyyysMMsdd(millis: Long) = format(millis, fmt_yyyysMMsdd)
  def format_yyyysMM(millis: Long) = format(millis, fmt_yyyysMM)

  def now_yyyy17ssss = DateTime.now().toString(fmt_yyyyMMddHHmmssSSS)


  def lastDayOfTheMonth(millis: Long, fmt: DateTimeFormatter) = {
    val d = new DateTime(millis)
    val e = d.plusMonths(1).withDay(1).minusDays(1)
    e.toString(fmt)
  }


  def format(millis: Long, fmt: DateTimeFormatter) = {
    val d = new DateTime(millis)
    d.toString(fmt)
  }

  private[this] val count = new AtomicInteger(0)

  private[this] def atomCount = count.getAndIncrement()

  def timeSerialNum() = {
    now_yyyy17ssss + (1000 + (atomCount%1000)).toString.substring(0)
  }


}
