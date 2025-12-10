package sportstarts.infographics.competition

import CompetitionHandlers.CompetitionDoesntExist
import sttp.model.StatusCode
import sttp.tapir.*
import sttp.tapir.json.circe.*

import java.time.LocalDate

object CompetitionEndpoints {
  val getCompetition =
    endpoint
      .get
      .in("api" / "v1" / "competition" / path[CompetitionId].name("competitionId"))
      .out(jsonBody[Competition])
      .errorOut(
        oneOf[CompetitionDoesntExist.type](
          oneOfVariant(
            StatusCode.NotFound,
            emptyOutputAs(CompetitionDoesntExist).description("Соревнование не найдено")
          )
        )
      )
      .tag("Competitions")

  val createCompetition =
    endpoint
      .post
      .in("api" / "v1" / "competition")
      .in(jsonBody[CreateCompetition])
      .out(jsonBody[Competition])
      .tag("Competitions")

  val editCompetition =
    endpoint
      .put
      .in("api" / "v1" / "competition" / path[CompetitionId].name("competitionId"))
      .in(jsonBody[CreateCompetition])
      .out(jsonBody[Competition])
      .errorOut(
        oneOf[CompetitionDoesntExist.type](
          oneOfVariant(
            StatusCode.NotFound,
            emptyOutputAs(CompetitionDoesntExist).description("Соревнование не найдено")
          )
        )
      )
      .tag("Competitions")

  val deleteCompetition =
    endpoint
      .delete
      .in("api" / "v1" / "competition" / path[CompetitionId].name("competitionId"))
      .out(statusCode(StatusCode.NoContent))
      .tag("Competitions")

  val listCompetitions =
    endpoint
      .get
      .in("api" / "v1" / "competitions")
      .in(
        query[Option[CompetitionName]]("name")
          .and(query[Option[LocalDate]]("dateFrom"))
          .and(query[Option[LocalDate]]("dateTo"))
          .and(query[Option[CompetitionPlace]]("place"))
          .and(query[Offset]("offset").default(Offset(0)))
          .and(query[Limit]("limit").default(Limit(10)))
      )
      .out(jsonBody[List[Competition]])
      .tag("Competitions")

  val all = List(
    CompetitionEndpoints.getCompetition,
    CompetitionEndpoints.createCompetition,
    CompetitionEndpoints.editCompetition,
    CompetitionEndpoints.deleteCompetition,
    CompetitionEndpoints.listCompetitions
  )
}
