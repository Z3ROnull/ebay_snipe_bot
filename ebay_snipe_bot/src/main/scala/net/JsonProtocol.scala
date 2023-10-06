import com.github.nscala_time.time.Imports._
import spray.json.DefaultJsonProtocol._
import spray.json._

object JsonProtocol {
  implicit class SafeJsonConvertible[T](val obj: T) extends AnyVal {
    def safeJson(implicit writer: JsonWriter[T]): JsValue =
      Option(obj).fold[JsValue](JsNull)(writer.write)
  }

  implicit def optionWriter[T: JsonWriter] = new JsonWriter[Option[T]] {
    override def write(opt: Option[T]) = opt.fold[JsValue](JsNull)(_.toJson)
  }

  implicit object JDateJsonWriter extends JsonWriter[JDate] {
    override def write(date: JDate) = JsString(date.toLocalDateTime.toString)
  }

  // ... altri implicit JSON writers ...

  implicit object SnipeInfoJsonProtocol extends RootJsonFormat[SnipeInfo] {
    override def read(json: JsValue) = {
      val jObj = json.asJsObject.fields
      val auctionId = jObj.get("auctionId").collect { case JsString(aId) => aId }.getOrElse("")
      val description = jObj.get("description").collect { case JsString(desc) => desc }.getOrElse("")
      val bid = jObj.get("bid").collect {
        case JsNumber(b) => Currency("USD", b.toDouble)
        case JsString(str) => Currency.parse(str)
      }.getOrElse(throw new DeserializationException("A valid bid must be provided"))
      val quantity = jObj.get("quantity").collect { case JsNumber(qt) => qt.toInt }.getOrElse(1)
      val snipeTime = jObj.get("snipeTime").collect {
        case JsString(t) => Some(t.toDateTime.toDate)
        case JsNumber(n) => Some(new JDate(n.toLong))
      }.getOrElse(None)
      SnipeInfo(auctionId, description, bid, quantity, snipeTime)
    }

    override def write(info: SnipeInfo) = Map(
      "auctionId" -> info.auctionId.safeJson,
      "description" -> info.description.safeJson,
      "bid" -> info.bid.safeJson,
      "quantity" -> info.quantity.safeJson,
      "snipeTime" -> info.snipeTime.orNull.safeJson).toJson
  }
}