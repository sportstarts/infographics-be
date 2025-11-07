package sportstarts.infographics

import sttp.tapir.* 
import sttp.shared.Identity
import sttp.tapir.server.ServerEndpoint

object CompetitionEndpoints {
	val getCompetition = 
		endpoint
		.get
		.in("api" / "v1" / "competition" / path[CompetitionId].name("competitionId"))
		.out(stringBody)
		.tag("Competition")

	val getCompetitionLogic = getCompetition.handleSuccess(id => s"this is $id competition")
}
