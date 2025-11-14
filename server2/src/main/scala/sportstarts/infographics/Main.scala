package sportsparts.infographics

import cats.effect.{IO, IOApp}
import cats.effect.Resource
import cats.effect.std.Dispatcher
import doobie.hikari.HikariTransactor
import doobie.util.ExecutionContexts
import org.legogroup.woof.{*, given}
import org.legogroup.woof.slf4j2.*
import sportstarts.infographics.competition.*
import sttp.tapir.server.netty.cats.NettyCatsServer
import sttp.tapir.swagger.bundle.SwaggerInterpreter

object Main extends IOApp.Simple:
  given Filter = Filter.atLeastLevel(LogLevel.Info)
  given Printer = ColorPrinter()

  val swaggerEndpoints = SwaggerInterpreter()
    .fromEndpoints[IO](CompetitionEndpoints.all, "Sportstarts Infographics", "1.0")

  case class Resources(woofSlf4jDispatcher: Dispatcher[IO], nettyServer: NettyCatsServer[IO], transactor: HikariTransactor[IO])

  val resources: Resource[IO, Resources] =
    for
      connectEC <- ExecutionContexts.fixedThreadPool[IO](32) // for JDBC operations
      xa <- HikariTransactor.newHikariTransactor[IO](
        "org.postgresql.Driver",
        url = "jdbc:postgresql://localhost:5432/infographics",
        user = "user",
        pass = "password",
        connectEC
      )
      woofSlf4jDispatcher <- Dispatcher.sequential[IO]
      nettyServer <- NettyCatsServer.io()
    yield Resources(woofSlf4jDispatcher, nettyServer, xa)

  val run: IO[Unit] = resources.use { res =>
    for {
      woofLogger <- DefaultLogger.makeIo(Output.fromConsole)
      _ <- IO.delay { // same as org.legogroup.woof.slf4j2.registerSlf4j, but passing dispatcher explicitly
        WoofLogger.logger = Some(woofLogger)
        WoofLogger.dispatcher = Some(res.woofSlf4jDispatcher)
      }

      logger <- DefaultLogger.makeIo(Output.fromConsole)

      competitionRepo = CompetitionRepo(res.transactor)
      competitionHandlers = CompetitionHandlers(competitionRepo)
      competitionServerEndpoints = CompetitionServerEndpoints(competitionHandlers)

      binding <- res.nettyServer
        .port(8080)
        .addEndpoints(competitionServerEndpoints.endpoints)
        .addEndpoints(swaggerEndpoints)
        .start()
      _ <- logger.info(s"Netty server started at ${binding.localSocket}")
      _ <- IO.never
    } yield ()
  }
