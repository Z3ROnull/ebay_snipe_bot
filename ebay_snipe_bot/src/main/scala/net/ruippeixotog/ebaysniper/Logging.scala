import org.slf4j.LoggerFactory

trait Logging {
  lazy val log = LoggerFactory.getLogger(getClass.getName)
}

trait RoutingLogging extends Logging {
  val logServiceRequest = extractRequest.map { req =>
    val entityData = req.entity match {
      case HttpEntity.Strict(_, data) => data.decodeString("UTF-8")
      case _ => "<non-strict>"
    }
    log.info("{} {} {}", req.method.value, req.uri.path, entityData)
    ()
  }
}