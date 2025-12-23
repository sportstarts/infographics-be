package sportstarts.infographics.lap

import doobie.postgres.implicits.*
import doobie.util.Read
import doobie.util.meta.Meta
import io.circe.{Decoder, Encoder}
import sportstarts.infographics.competition.CompetitionId
import sttp.model.Part
import sttp.tapir.*
import sttp.tapir.Codec.PlainCodec
import sttp.tapir.generic.auto.*

opaque type LapId = Int
object LapId:
  def apply(value: Int): LapId = value
  def value(id: LapId): Int = id

  given PlainCodec[LapId] = Codec.int.map(LapId.apply)(LapId.value)
  given Decoder[LapId] = Decoder.decodeInt.map(LapId.apply)
  given Encoder[LapId] = Encoder.encodeInt.contramap(LapId.value)
  given Schema[LapId] = Schema.schemaForInt
  given Meta[LapId] = Meta[Int]

case class CreateLap(
  gpx: Part[TapirFile],
  sportKindId: Int,
  description: Option[String]
) derives Schema

case class UpdateLap(
  sportKindId: Int,
  description: Option[String],
) derives Decoder, Encoder, Schema

case class StoreLap(
  sportKindId: Int,
  lats: List[Double],
  lons: List[Double],
  description: Option[String],
  lapDistanceMeters: Double,
  pointsDistancesMeters: List[Double]
) derives Decoder, Encoder, Schema, Read

case class Lap(
  id: LapId,
  competitionId: CompetitionId,
  sportKindId: Int,
  lats: List[Double],
  lons: List[Double],
  description: Option[String],
  lapDistanceMeters: Double,
  pointsDistancesMeters: List[Double]
) derives Decoder, Encoder, Schema, Read
