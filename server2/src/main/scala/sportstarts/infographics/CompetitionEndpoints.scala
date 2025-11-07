package sportstarts.infographics

import sttp.shared.Identity
import sttp.tapir.*
import sttp.tapir.json.circe.*
import sttp.tapir.server.ServerEndpoint

object CompetitionEndpoints {
  val getCompetition =
    endpoint
      .get
      .in("api" / "v1" / "competition" / path[CompetitionId].name("competitionId"))
      .out(stringBody)
      .tag("Competition")

  val createCompetition =
    endpoint
      .post
      .in("api" / "v1" / "competition")
      .in(jsonBody[CreateCompetition])
      .out(jsonBody[Competition])
      .tag("Competition")
}
