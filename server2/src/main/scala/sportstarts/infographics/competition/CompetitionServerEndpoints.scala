package sportstarts.infographics.competition

import cats.effect.IO
import cats.syntax.either.*
import sttp.tapir.server.ServerEndpoint

trait CompetitionServerEndpoints:
  def endpoints: List[ServerEndpoint[Any, IO]]

object CompetitionServerEndpoints:
  class Impl(competitionHandlers: CompetitionHandlers) extends CompetitionServerEndpoints:

    val getCompetition =
      CompetitionEndpoints.getCompetition.serverLogic[IO] { competitionId =>
        competitionHandlers.getCompetitionById(competitionId)
      }

    val createCompetition =
      CompetitionEndpoints.createCompetition.serverLogic[IO] { createCompetition =>
        competitionHandlers.createCompetition(createCompetition).map(_.asRight)
      }

    val editCompetition =
      CompetitionEndpoints.editCompetition.serverLogic[IO] { case (competitionId, createCompetition) =>
        competitionHandlers.editCompetition(competitionId, createCompetition)
      }

    val deleteCompetition =
      CompetitionEndpoints.deleteCompetition.serverLogic[IO] { competitionId =>
        competitionHandlers.deleteCompetition(competitionId).as(().asRight)
      }

    val listCompetitions =
      CompetitionEndpoints.listCompetitions.serverLogic[IO] { case (name, dateFrom, dateTo, place, offset, limit) =>
        competitionHandlers.listCompetitions(name, dateFrom, dateTo, place, offset, limit).map(_.asRight)
      }

    val endpoints: List[ServerEndpoint[Any, IO]] =
      List(
        getCompetition,
        createCompetition,
        editCompetition,
        deleteCompetition,
        listCompetitions
      )
  end Impl

  def apply(competitionHandlers: CompetitionHandlers): CompetitionServerEndpoints =
    new Impl(competitionHandlers)
