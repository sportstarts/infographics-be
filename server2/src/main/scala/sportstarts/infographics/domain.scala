package sportstarts.infographics

import sttp.tapir.Codec
import sttp.tapir.Codec.PlainCodec

opaque type CompetitionId = Int

object CompetitionId {
	def apply(value: Int): CompetitionId = value
	def value(id: CompetitionId): Int = id

	given PlainCodec[CompetitionId] = Codec.int.map(CompetitionId.apply)(CompetitionId.value)
}