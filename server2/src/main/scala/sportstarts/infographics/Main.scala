package sportsparts.infographics

import cats.effect.{IO, IOApp}
import cats.effect.Resource
import cats.effect.std.Dispatcher
import cats.syntax.option.*
import com.comcast.ip4s.*
import doobie.hikari.HikariTransactor
import doobie.util.ExecutionContexts
import org.http4s.ember.server.EmberServerBuilder
import org.legogroup.woof.*
import org.legogroup.woof.slf4j2.*
import sportstarts.infographics.competition.*
import sportstarts.infographics.lap.*
import sportstarts.infographics.logHandler
import sttp.tapir.server.http4s.Http4sServerInterpreter
import sttp.tapir.swagger.bundle.SwaggerInterpreter

object Main extends IOApp.Simple:
  given Filter = Filter.atLeastLevel(LogLevel.Info)
  given Printer = ColorPrinter()

  val swaggerEndpoints = SwaggerInterpreter()
    .fromEndpoints[IO](CompetitionEndpoints.all ++ LapEndpoints.all, "Sportstarts Infographics", "1.0")

  case class Resources(transactor: HikariTransactor[IO], logger: Logger[IO])

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
    yield Resources(xa, logger)

  val run: IO[Unit] = resources.use { res =>
    given Logger[IO] = res.logger

    val competitionRepo = CompetitionRepo(res.transactor)
    val competitionHandlers = CompetitionHandlers(competitionRepo)
    val competitionServerEndpoints = CompetitionServerEndpoints(competitionHandlers)

    val lapRepo = LapRepo(res.transactor)
    val lapHandlers = LapHandlers(lapRepo)
    val lapServerEndpoints = LapServerEndpoints(lapHandlers)

    val routes = Http4sServerInterpreter[IO]().toRoutes(
      lapServerEndpoints.endpoints ++
        competitionServerEndpoints.endpoints ++
        swaggerEndpoints
    )

    EmberServerBuilder
      .default[IO]
      .withHost(host"0.0.0.0")
      .withPort(port"8080")
      .withHttpApp(routes.orNotFound)
      .build
      .useForever
  }
