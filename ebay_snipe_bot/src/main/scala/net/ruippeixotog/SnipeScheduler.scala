import java.util.concurrent.TimeUnit
import scala.util.Random

import com.typesafe.config.ConfigFactory
import net.ruippeixotog.ebaysniper.ebay.BiddingClient

case class SnipeScheduler()(implicit ebay: BiddingClient) {
  val config = ConfigFactory.load.getConfig("ebay.sniper.scheduler")
  val meanMargin = config.getDuration("mean-margin", TimeUnit.MILLISECONDS)
  val stdevMargin = config.getDuration("stdev-margin", TimeUnit.MILLISECONDS)
  val maxMargin = config.getDuration("max-margin", TimeUnit.MILLISECONDS)

  def snipeTimeFor(auctionId: String, hintTime: Option[Date] = None): Option[Date] = {
    val endTime = ebay.auctionInfo(auctionId).endingAt.getMillis

    if (System.currentTimeMillis() > endTime) None
    else {
      val proposedTime = hintTime match {
        case None => endTime - meanMargin + (Random.nextGaussian() * stdevMargin).toLong
        case Some(t) => t.getTime
      }

      Some(new Date(math.min(proposedTime, endTime - maxMargin)))
    }
  }
}