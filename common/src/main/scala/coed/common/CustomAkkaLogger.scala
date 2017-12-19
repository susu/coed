/*
 * Copyright (c) 2017 BalaBit
 * All rights reserved.
 */

package coed.common

import java.io.PrintWriter
import java.lang.management.ManagementFactory

import akka.actor.Actor
import akka.event.Logging._

class CustomAkkaLogger extends Actor {

  private var outputFile: Option[PrintWriter] = None

  def receive: Receive = {
    case InitializeLogger(_) => {
      try {
        outputFile = Some(new PrintWriter(logFilename, "UTF-8"))
      } catch {
        case exc: Exception =>
          Console.err.println(s"Could not set up logging: $exc, falling back to stderr.")
          exc.getStackTrace.foreach(Console.err.println)
          outputFile = Some(new PrintWriter(Console.err))
      }
      sender() ! LoggerInitialized
    }

    case Error(cause: Throwable, logSource, logClass: Class[_], message) =>
      emit(format("ERROR", logSource, logClass, message))
      if (cause != null) {
        emit(s"    CAUSE: ${cause.getClass.getSimpleName}: ${cause.getMessage}")
        emit(s"    BCKTR: ${cause.getStackTrace.mkString("\n    ")}")
      }

    case Warning(logSource, logClass, message) =>
      emit(format("WARN", logSource, logClass, message))

    case Info(logSource, logClass, message) =>
      emit(format("INFO", logSource, logClass, message))

    case Debug(logSource, logClass, message) =>
      emit(format("DEBUG", logSource, logClass, message))
  }

  private def format(level: String, logSource: String, logClass: Class[_], message: Any): String =
    s"[$level]: ($logSource) (${logClass.getSimpleName}): $message"


  private def emit(text: String): Unit = {
    outputFile.foreach(_.println(text))
    outputFile.foreach(_.flush())
  }

  private lazy val logFilename: String = {
    val appName = Option(System.getProperty("app.name")).getOrElse(ManagementFactory.getRuntimeMXBean.getName)
    s"akka.coed-$appName.log"
  }
}
