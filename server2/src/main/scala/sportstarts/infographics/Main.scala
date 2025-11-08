package sportsparts.infographics

import cats.effect.{IO, IOApp}
import cats.effect.std.Dispatcher
import cats.implicits.*
import org.legogroup.woof.{*, given}
import org.legogroup.woof.slf4j2.*
import sportstarts.infographics.Competition
import sportstarts.infographics.CompetitionEndpoints
import sportstarts.infographics.CompetitionId
import sportstarts.infographics.CompetitionName
import sportstarts.infographics.CompetitionPlace
import sttp.tapir.server.netty.cats.NettyCatsServer
import sttp.tapir.swagger.bundle.SwaggerInterpreter

import java.time.LocalDate

object Main extends IOApp.Simple:
  val swaggerEndpoints = SwaggerInterpreter()
    .fromEndpoints[IO](
      List(
        CompetitionEndpoints.getCompetition,
        CompetitionEndpoints.createCompetition
      ),
      "Sportstarts Infographics", "1.0"
    )

  given Filter = Filter.atLeastLevel(LogLevel.Info)
  given Printer = ColorPrinter()

  val getCompetitionLogic = CompetitionEndpoints.getCompetition.serverLogic[IO] { cid =>
    for
      log <- DefaultLogger.makeIo(Output.fromConsole)
      _ <- log.info(s"Received request for competition with id: $cid")
    yield Competition(CompetitionId(123), CompetitionName("Sample Competition"), LocalDate.now(), CompetitionPlace("Sample Place")).asRight
  }

  def withWoofSlf4jRegistered(f: IO[Unit]): IO[Unit] =
    Dispatcher.sequential[IO].use { implicit dispatcher =>
      for
        woofLogger <- DefaultLogger.makeIo(Output.fromConsole)
        _ <- woofLogger.registerSlf4j
        _ <- f
      yield ()
    }

  val run: IO[Unit] = withWoofSlf4jRegistered {
    NettyCatsServer.io().use { server =>
      for
        logger <- DefaultLogger.makeIo(Output.fromConsole)
        binding <- server
          .port(8080)
          .addEndpoint(getCompetitionLogic)
          .addEndpoints(swaggerEndpoints)
          .start()
        _ <- logger.info(s"Netty server started at ${binding.localSocket}")
        _ <- IO.never
      yield ()
    }

  }
