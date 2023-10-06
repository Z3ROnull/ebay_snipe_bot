import com.typesafe.config.Config

import net.ruippeixotog.ebaysniper.util.Implicits._
import net.ruippeixotog.ebaysniper.util.Logging
import net.ruippeixotog.scalascraper.browser.Browser
import net.ruippeixotog.scalascraper.config.dsl.DSL._
import net.ruippeixotog.scalascraper.dsl.DSL._

class EbayLoginManager(siteConf: Config, username: String, password: String)(implicit browser: Browser)
  extends Logging {

  def login(): Boolean = {
    if (browser.cookies(loginUrl).contains("shs")) true
    else forceLogin()
  }

  def loginUrl = siteConf.getString("login-form.uri-template").resolveVars()

  def forceLogin(): Boolean = {
    browser.clearCookies()
    log.debug(s"Getting the sign in cookie for ${siteConf.getString("name")}")

    val (formData, signInAction) = browser.get(loginUrl) >> signInFormExtractor
    val signInData = formData + ("userid" -> username) + ("pass" -> password)

    browser.post(signInAction, signInData) errorIf loginErrors match {
      case Left(status) =>
        log.error(s"A problem occurred while signing in ($status)")
        false

      case Right(doc) =>
        doc errorIf loginWarnings match {
          case Left(status) =>
            log.warn(s"A warning occurred while signing in ($status)")
          case _ =>
        }
        log.info("Login successful")
        true
    }
  }

  private[this] lazy val signInFormExtractor =
    Extract.formDataAndAction(siteConf.getString("login-form.form-query"))

  private[this] lazy val loginErrors = validatorsAt[String](siteConf, "login-confirm.error-statuses")
  private[this] lazy val loginWarnings = validatorsAt[String](siteConf, "login-confirm.warn-statuses")
}