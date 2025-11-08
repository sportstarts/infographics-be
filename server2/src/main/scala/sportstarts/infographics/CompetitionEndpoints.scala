package sportstarts.infographics

import sttp.tapir.*
import sttp.tapir.json.circe.*

object CompetitionEndpoints {
  val getCompetition =
    endpoint
      .get
      .in("api" / "v1" / "competition" / path[CompetitionId].name("competitionId"))
      .out(jsonBody[Competition])
      .tag("Competition")

  val createCompetition =
    endpoint
      .post
      .in("api" / "v1" / "competition")
      .in(jsonBody[CreateCompetition])
      .out(jsonBody[Competition])
      .tag("Competition")
}
