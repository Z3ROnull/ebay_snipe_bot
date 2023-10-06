package net.ruippeixotog.ebaysniper.util

import java.util.TimerTask

import com.typesafe.config.Config

object Implicits {
  implicit def functionToTimerTask(task: () => Unit): TimerTask = new TimerTask {
    def run(): Unit = task()
  }

  implicit class RichCloseable[T <: AutoCloseable](val resource: T) extends AnyVal {
    def use[U](block: T => U): Unit = {
      try {
        block(resource)
      } finally {
        resource.close()
      }
    }
  }

  implicit class RichString(val str: String) extends AnyVal {
    def resolveVars(map: Map[String, String] = Map())(implicit conf: Config): String = {
      def resolveKey(key: String): String = map.getOrElse(key, conf.getString(key))

      "\\{([^\\}]*)\\}".r.replaceAllIn(str, { m =>
        val key = m.group(1)
        resolveKey(key)
      })
    }
  }
}