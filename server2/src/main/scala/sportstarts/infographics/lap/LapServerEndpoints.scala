package sportstarts.infographics.lap

import cats.effect.IO
import cats.syntax.either.*
import sttp.tapir.server.ServerEndpoint

trait LapServerEndpoints:
  def endpoints: List[ServerEndpoint[Any, IO]]

object LapServerEndpoints:
  class Impl(lapHandlers: LapHandlers) extends LapServerEndpoints:

    val createLap =
      LapEndpoints.createLap.serverLogic[IO] { case (competitionId, createLap) =>
        lapHandlers.createLap(competitionId, createLap)
      }

    val editLap =
      LapEndpoints.editLap.serverLogic[IO] { case (lapId, updateLap) =>
        lapHandlers.editLap(lapId, updateLap)
      }

    val deleteLap =
      LapEndpoints.deleteLap.serverLogic[IO] { lapId =>
        lapHandlers.deleteLap(lapId).as(().asRight)
      }

    val listByCompetition =
      LapEndpoints.listByCompetition.serverLogic[IO] { competitionId =>
        lapHandlers.listLaps(competitionId).map(_.asRight)
      }

    val endpoints: List[ServerEndpoint[Any, IO]] = List(
      createLap,
      editLap,
      deleteLap,
      listByCompetition
    )
  end Impl

  def apply(lapHandlers: LapHandlers): LapServerEndpoints = new Impl(lapHandlers)
