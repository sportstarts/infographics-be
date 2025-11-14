package sportstarts.infographics.competition

import doobie.util.Read
import doobie.util.meta.Meta
import io.circe.{Decoder, Encoder}
import sttp.tapir.*
import sttp.tapir.Codec.PlainCodec

import java.sql.Date
import java.time.LocalDate

opaque type CompetitionId = Int

given Meta[LocalDate] = Meta[Date].imap(_.toLocalDate)(Date.valueOf)

object CompetitionId {
  def apply(value: Int): CompetitionId = value
  def value(id: CompetitionId): Int = id

  given PlainCodec[CompetitionId] = Codec.int.map(CompetitionId.apply)(CompetitionId.value)

  given Decoder[CompetitionId] = Decoder.decodeInt.map(CompetitionId.apply)
  given Encoder[CompetitionId] = Encoder.encodeInt.contramap(CompetitionId.value)
  given Schema[CompetitionId] = Schema.schemaForInt
  given Meta[CompetitionId] = Meta[Int]
}

opaque type CompetitionName = String
object CompetitionName {
  def apply(value: String): CompetitionName = value
  def value(name: CompetitionName): String = name

  given PlainCodec[CompetitionName] = Codec.string.map(CompetitionName.apply)(CompetitionName.value)
  given Decoder[CompetitionName] = Decoder.decodeString.map(CompetitionName.apply)
  given Encoder[CompetitionName] = Encoder.encodeString.contramap(CompetitionName.value)
  given Schema[CompetitionName] = Schema.string
  given Meta[CompetitionName] = Meta[String]
}

opaque type CompetitionPlace = String
object CompetitionPlace {
  def apply(value: String): CompetitionPlace = value
  def value(place: CompetitionPlace): String = place

  given PlainCodec[CompetitionPlace] = Codec.string.map(CompetitionPlace.apply)(CompetitionPlace.value)
  given Decoder[CompetitionPlace] = Decoder.decodeString.map(CompetitionPlace.apply)
  given Encoder[CompetitionPlace] = Encoder.encodeString.contramap(CompetitionPlace.value)
  given Schema[CompetitionPlace] = Schema.string
  given Meta[CompetitionPlace] = Meta[String]
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
) derives Decoder, Encoder, Schema, Read

opaque type Offset = Int
object Offset {
  def apply(value: Int): Offset = value
  def value(offset: Offset): Int = offset

  given PlainCodec[Offset] = Codec.int.map(Offset.apply)(Offset.value)
  given Meta[Offset] = Meta[Int]
}

opaque type Limit = Int
object Limit {
  def apply(value: Int): Limit = value
  def value(limit: Limit): Int = limit

  given PlainCodec[Limit] = Codec.int.map(Limit.apply)(Limit.value)
  given Meta[Limit] = Meta[Int]
}
