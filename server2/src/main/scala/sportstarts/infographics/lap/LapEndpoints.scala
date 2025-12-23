package sportstarts.infographics.lap

import sportstarts.infographics.competition.CompetitionId
import sportstarts.infographics.lap.LapHandlers.*
import sportstarts.infographics.lap.LapHandlers.CreateLapError.*
import sttp.model.StatusCode
import sttp.tapir.{ValidationError => _, *}
import sttp.tapir.json.circe.*

object LapEndpoints:
  val createLap =
    endpoint
      .post
      .in("api" / "v1" / "competition" / path[CompetitionId].name("competitionId") / "lap")
      .in(multipartBody[CreateLap])
      .out(jsonBody[Lap])
      .errorOut(
        oneOf[CreateLapError](
          oneOfVariant(
            StatusCode.UnprocessableEntity,
            jsonBody[ValidationError].description("Ошибки валидации")
          )
        )
      )
      .tag("Laps")

  val editLap =
    endpoint
      .put
      .in("api" / "v1" / "lap" / path[LapId].name("lapId"))
      .in(jsonBody[UpdateLap])
      .out(jsonBody[Lap])
      .errorOut(
        oneOf[LapDoesntExist.type](
          oneOfVariant(
            StatusCode.NotFound,
            emptyOutputAs(LapDoesntExist).description("Круг не найден")
          )
        )
      )
      .tag("Laps")

  val deleteLap =
    endpoint
      .delete
      .in("api" / "v1" / "lap" / path[LapId].name("lapId"))
      .out(statusCode(StatusCode.NoContent))
      .tag("Laps")

  val listByCompetition =
    endpoint
      .get
      .in("api" / "v1" / "competition" / path[CompetitionId].name("competitionId") / "laps")
      .out(jsonBody[List[Lap]])
      .tag("Laps")

  val all = List(createLap, editLap, deleteLap, listByCompetition)
