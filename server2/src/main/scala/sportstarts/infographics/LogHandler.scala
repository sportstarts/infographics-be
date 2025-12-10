package sportstarts.infographics

import cats.effect.IO
import doobie.util.log.*
import org.legogroup.woof.{*, given}
import org.legogroup.woof.Logger

// a copy of doobie's Slf4jLogHandler adapted for Woof
def logHandler(logger: Logger[IO]): LogHandler[IO] = new LogHandler[IO] {
  def run(logEvent: LogEvent): IO[Unit] =
    logEvent match {
      case Success(s, a, l, e1, e2) =>
        val paramsStr = a match {
          case nonBatch: Parameters.NonBatch => s"[${nonBatch.paramsAsList.mkString(", ")}]"
          case _: Parameters.Batch => "<batch arguments not rendered>"
        }
        logger.info(s"""Successful Statement Execution:
                              |
                              |  ${s.linesIterator.dropWhile(_.trim.isEmpty).mkString("\n  ")}
                              |
                              | parameters = $paramsStr
                              | label     = $l
                              | elapsed = ${e1.toMillis.toString} ms exec + ${e2.toMillis
                        .toString} ms processing (${(e1 + e2).toMillis.toString} ms total)
              """.stripMargin)

      case ProcessingFailure(s, a, l, e1, e2, t) =>
        val paramsStr = a.allParams.map(thisArgs => thisArgs.mkString("(", ", ", ")"))
          .mkString("[", ", ", "]")
        logger.error(s"""Failed Resultset Processing:
                                |
                                |  ${s.linesIterator.dropWhile(_.trim.isEmpty).mkString("\n  ")}
                                |
                                | parameters = $paramsStr
                                | label     = $l
                                | elapsed = ${e1.toMillis.toString} ms exec + ${e2.toMillis
                         .toString} ms processing (failed) (${(e1 + e2).toMillis.toString} ms total)
                                | failure = ${t.getMessage}
              """.stripMargin)

      case ExecFailure(s, a, l, e1, t) =>
        val paramsStr = a.allParams.map(thisArgs => thisArgs.mkString("(", ", ", ")"))
          .mkString("[", ", ", "]")
        logger.error(s"""Failed Statement Execution:
                                |
                                |  ${s.linesIterator.dropWhile(_.trim.isEmpty).mkString("\n  ")}
                                |
                                | parameters = $paramsStr
                                | label     = $l
                                | elapsed = ${e1.toMillis.toString} ms exec (failed)
                                | failure = ${t.getMessage}
              """.stripMargin)
    }
}
