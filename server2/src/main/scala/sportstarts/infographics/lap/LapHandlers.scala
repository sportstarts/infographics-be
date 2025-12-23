package sportstarts.infographics.lap

import cats.data.EitherT
import cats.effect.IO
import cats.syntax.all.*
import io.circe.Decoder
import io.circe.Encoder
import io.jenetics.jpx.GPX
import org.legogroup.woof.{*, given}
import org.legogroup.woof.Logger
import sportstarts.infographics.competition.CompetitionId
import sportstarts.infographics.lap.LapHandlers.*
import sportstarts.infographics.lap.LapHandlers.CreateLapError.*
import sportstarts.infographics.lap.LapHandlers.LapDoesntExist
import sttp.tapir.Schema

import scala.jdk.StreamConverters._

trait LapHandlers:
  def createLap(competitionId: CompetitionId, lap: CreateLap): IO[Either[CreateLapError, Lap]]
  def editLap(lapId: LapId, lap: UpdateLap): IO[Either[LapDoesntExist.type, Lap]]
  def deleteLap(lapId: LapId): IO[Boolean]
  def listLaps(competitionId: CompetitionId): IO[List[Lap]]

object LapHandlers:
  sealed trait CreateLapError
  object CreateLapError:
    enum Code derives Encoder, Decoder:
      case CANT_READ_GPX extends Code
      case AT_LEAST_TWO_POINTS_REQUIRED extends Code

    given Schema[Code] = Schema.derivedEnumeration.defaultStringBased

    case class ValidationError(val code: Code) extends CreateLapError derives Encoder, Decoder, Schema

    val CantReadGpx = ValidationError(Code.CANT_READ_GPX)
    val AtLeastTwoPointsRequired = ValidationError(Code.AT_LEAST_TWO_POINTS_REQUIRED)

  object LapDoesntExist

  class Impl(lapRepo: LapRepo)(using Logger[IO]) extends LapHandlers:

    private val logger = Logger[IO]

    def createLap(competitionId: CompetitionId, lap: CreateLap): IO[Either[CreateLapError, Lap]] =
      (for
        _ <- EitherT.right[CreateLapError](logger.info(s"Received GPX file with content type: ${lap.gpx.contentType}"))
        gpx <- EitherT(IO(GPX.read(lap.gpx.body.toPath)).attempt.flatMap {
          case Right(gpx) => gpx.asRight.pure[IO]
          case Left(e) => logger.error(s"Error reading GPX file: ${e.getMessage}").as(Left(CantReadGpx))
        })
        waypoints = gpx.tracks()
          .flatMap(_.segments)
          .flatMap(_.points)
          .toScala(Iterator)
          .toList

        (lats, lons) <- EitherT.fromEither[IO] {
          Either.cond(
            waypoints.size >= 2,
            waypoints.map(wp => (wp.getLatitude().doubleValue(), wp.getLongitude().doubleValue())).unzip,
            AtLeastTwoPointsRequired
          )
        }

        stored <- EitherT.right(lapRepo.storeLap(competitionId,
          StoreLap(
            sportKindId = lap.sportKindId,
            lats = lats,
            lons = lons,
            description = lap.description,
            lapDistanceMeters = 0, // TODO calculate
            pointsDistancesMeters = List.empty // TODO calculate
          )))
      yield stored).value

    def editLap(lapId: LapId, lap: UpdateLap): IO[Either[LapDoesntExist.type, Lap]] =
      lapRepo.updateLap(lapId, lap).map {
        case Some(l) => Right(l)
        case None => Left(LapDoesntExist)
      }

    def deleteLap(lapId: LapId): IO[Boolean] =
      lapRepo.deleteLap(lapId)

    def listLaps(competitionId: CompetitionId): IO[List[Lap]] =
      lapRepo.listByCompetition(competitionId)

  def apply(lapRepo: LapRepo)(using Logger[IO]): LapHandlers = new Impl(lapRepo)
