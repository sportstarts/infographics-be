package sportsparts.infographics

import sttp.tapir.server.netty.sync.NettySyncServer
import sportstarts.infographics.CompetitionEndpoints.{getCompetitionLogic}
import sttp.tapir.swagger.bundle.SwaggerInterpreter
import sttp.shared.Identity 

import sttp.tapir.*


@main def Main(): Unit = 
  val swaggerEndpoints = SwaggerInterpreter()
    .fromEndpoints[Identity](List(getCompetitionLogic.endpoint), "Sportstarts Infographics", "1.0")

  NettySyncServer()
    .port(8080)
    .addEndpoint(getCompetitionLogic)
    .addEndpoints(swaggerEndpoints)
    .startAndWait()