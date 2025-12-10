package sportsparts.infographics

import cats.effect.{IO, IOApp}
import cats.effect.Resource
import cats.effect.std.Dispatcher
import cats.syntax.option.*
import doobie.hikari.HikariTransactor
import doobie.util.ExecutionContexts
import org.legogroup.woof.{*, given}
import org.legogroup.woof.slf4j2.*
import sportstarts.infographics.competition.*
import sportstarts.infographics.logHandler
import sttp.tapir.server.netty.cats.NettyCatsServer
import sttp.tapir.swagger.bundle.SwaggerInterpreter

object Main extends IOApp.Simple:
  given Filter = Filter.atLeastLevel(LogLevel.Info)
  given Printer = ColorPrinter()

  val swaggerEndpoints = SwaggerInterpreter()
    .fromEndpoints[IO](CompetitionEndpoints.all, "Sportstarts Infographics", "1.0")

  case class Resources(nettyServer: NettyCatsServer[IO], transactor: HikariTransactor[IO], logger: Logger[IO])

  val resources: Resource[IO, Resources] =
    for
      logger <- DefaultLogger.makeIo(Output.fromConsole).toResource

      woofSlf4jDispatcher <- Dispatcher.sequential[IO]
      _ <- IO.delay { // same as org.legogroup.woof.slf4j2.registerSlf4j, but passing dispatcher explicitly
        WoofLogger.logger = Some(logger)
        WoofLogger.dispatcher = Some(woofSlf4jDispatcher)
      }.toResource

      connectEC <- ExecutionContexts.fixedThreadPool[IO](32) // for JDBC operations
      xa <- HikariTransactor.newHikariTransactor[IO](
        "org.postgresql.Driver",
        url = "jdbc:postgresql://localhost:5432/infographics",
        user = "user",
        pass = "password",
        connectEC,
        logHandler = logHandler(logger).some
      )
      nettyServer <- NettyCatsServer.io()
    yield Resources(nettyServer, xa, logger)

  val run: IO[Unit] = resources.use { res =>
    val competitionRepo = CompetitionRepo(res.transactor)
    val competitionHandlers = CompetitionHandlers(competitionRepo)
    val competitionServerEndpoints = CompetitionServerEndpoints(competitionHandlers)

    for {

      binding <- res.nettyServer
        .port(8080)
        .addEndpoints(competitionServerEndpoints.endpoints)
        .addEndpoints(swaggerEndpoints)
        .start()
      _ <- res.logger.info(s"Netty server started at ${binding.localSocket}")
      _ <- IO.never
    } yield ()
  }
