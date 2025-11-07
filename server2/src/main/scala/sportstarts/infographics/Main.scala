package sportsparts.infographics

import sportstarts.infographics.CompetitionEndpoints
import sttp.shared.Identity
import sttp.tapir.*
import sttp.tapir.server.netty.sync.NettySyncServer
import sttp.tapir.swagger.bundle.SwaggerInterpreter

@main def Main(): Unit =
  val swaggerEndpoints = SwaggerInterpreter()
    .fromEndpoints[Identity](
      List(
        CompetitionEndpoints.getCompetition,
        CompetitionEndpoints.createCompetition
      ),
      "Sportstarts Infographics", "1.0"
    )

  val getCompetitionLogic = CompetitionEndpoints.getCompetition.handleSuccess(id => s"this is $id competition")

  NettySyncServer()
    .port(8080)
    .addEndpoint(getCompetitionLogic)
    .addEndpoints(swaggerEndpoints)
    .startAndWait()
