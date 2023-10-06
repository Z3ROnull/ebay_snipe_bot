import java.io.File
import java.io.PrintStream
import java.util.concurrent.CancellationException
import scala.concurrent.{ExecutionContext, Future}
import scala.io.Source
import scala.util.{Failure, Success}

import net.ruippeixotog.ebaysniper.JsonProtocol._
import net.ruippeixotog.ebaysniper.SnipeServer._
import net.ruippeixotog.ebaysniper.ebay.BiddingClient
import spray.json._

trait SnipeManagement {
  implicit def ec: ExecutionContext
  implicit def ebay: BiddingClient
  val snipesFile: Option[File] = None

  // ...

  def loadSnipesFromFile(): Unit = {
    snipes.values.foreach(_.cancel())

    snipesFile.foreach { file =>
      log.info(s"Using $file for persisting snipe data")
      file.getParentFile.mkdirs()
      if (file.exists()) {
        for {
          line <- Source.fromFile(file).getLines()
          snipeInfo <- line.parseJson.convertTo[SnipeInfo].toList
        } {
          registerAndActivate(new Snipe(snipeInfo))
        }
      }
    }

    // ...
  }

  def saveSnipesToFile(): Unit = {
    snipesFile.foreach { file =>
      val out = new PrintStream(file)
      out.println(snipes.values.map(_.info).toJson.compactPrint)
      out.close()
    }
  }

  def registerAndActivate(snipe: Snipe): Unit = {
    val auctionId = snipe.info.auctionId
    snipes.get(auctionId).foreach(_.cancel())
    _snipes += (auctionId -> snipe)
    saveSnipesToFile()

    snipe.activate().onComplete {
      case Success(status) if BidStatus.isSuccess(status) =>
        log.info(s"Completed snipe ${snipe.info} successfully - ${BidStatus.statusMessage(status)}")

      case Success(status) =>
        log.warn(s"Completed snipe ${snipe.info} with errors - ${BidStatus.statusMessage(status)}")

      case Failure(e: CancellationException) =>
        log.info(s"The snipe ${snipe.info} was cancelled")

      case Failure(e) =>
        log.error(s"The snipe ${snipe.info} failed", e)

      case _ =>
    }
    if (snipes.get(auctionId) == Some(snipe)) {
      _snipes -= auctionId
      saveSnipesToFile()
    }
  }
}