package testtask.json

import java.text.SimpleDateFormat
import java.util.Date

import testtask.actors.Messages.Candle
import spray.json.{DefaultJsonProtocol, JsString, JsValue, JsonFormat, deserializationError}

import scala.util.Try

/**
 * Json serialization for candles
 **/
object MyJsonProtocol extends DefaultJsonProtocol {

  implicit val candleFormat = jsonFormat7(Candle)

  implicit object DateFormat extends JsonFormat[Date] {
    def write(date: Date) = JsString(dateToIsoString(date))

    def read(json: JsValue) = json match {
      case JsString(rawDate) =>
        parseIsoDateString(rawDate)
          .fold(deserializationError(s"Expected ISO Date format, got $rawDate"))(identity)
      case error => deserializationError(s"Expected JsString, got $error")

    }

    private val localIsoDateFormatter = new ThreadLocal[SimpleDateFormat] {
      override def initialValue() = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    }

    private def dateToIsoString(date: Date) =
      localIsoDateFormatter.get().format(date)

    private def parseIsoDateString(date: String): Option[Date] =
      Try {
        localIsoDateFormatter.get().parse(date)
      }.toOption
  }
}

