package sportstarts.infographics

import io.circe.{Decoder, Encoder}
import sttp.tapir.*
import sttp.tapir.Codec.PlainCodec

import java.time.LocalDate

opaque type CompetitionId = Int

object CompetitionId {
  def apply(value: Int): CompetitionId = value
  def value(id: CompetitionId): Int = id

  given PlainCodec[CompetitionId] = Codec.int.map(CompetitionId.apply)(CompetitionId.value)

  given Decoder[CompetitionId] = Decoder.decodeInt.map(CompetitionId.apply)
  given Encoder[CompetitionId] = Encoder.encodeInt.contramap(CompetitionId.value)
  given Schema[CompetitionId] = Schema.schemaForInt
}

opaque type CompetitionName = String
object CompetitionName {
  def apply(value: String): CompetitionName = value
  def value(name: CompetitionName): String = name

  given Decoder[CompetitionName] = Decoder.decodeString.map(CompetitionName.apply)
  given Encoder[CompetitionName] = Encoder.encodeString.contramap(CompetitionName.value)
  given Schema[CompetitionName] = Schema.string
}

opaque type CompetitionPlace = String
object CompetitionPlace {
  def apply(value: String): CompetitionPlace = value
  def value(place: CompetitionPlace): String = place

  given Decoder[CompetitionPlace] = Decoder.decodeString.map(CompetitionPlace.apply)
  given Encoder[CompetitionPlace] = Encoder.encodeString.contramap(CompetitionPlace.value)
  given Schema[CompetitionPlace] = Schema.string
}

case class CreateCompetition(
  name: CompetitionName,
  date: LocalDate,
  place: CompetitionPlace
) derives Decoder, Encoder, Schema

case class Competition(
  id: CompetitionId,
  name: CompetitionName,
  date: LocalDate,
  place: CompetitionPlace
) derives Decoder, Encoder, Schema
