from concurrent import futures
from pathlib import Path
import grpc
import sys
from grpc_reflection.v1alpha import reflection
from server.geograf import calculate_cumulative_distances

# Ensure proto folder is in sys.path
sys.path.insert(0, str(Path(__file__).parent / "proto"))
import server.proto.geograf_pb2 as pb2
import server.proto.geograf_pb2_grpc as pb2_grpc


class GeografServicer(pb2_grpc.GeografServicer):
    def CalculateLapDistance(self, request: pb2.LapDistanceRequest, context):
        cumulative_distances = calculate_cumulative_distances(request.lap.points)

        return pb2.LapDistanceResponse(distances=cumulative_distances)

    def FindTimesplitOnLap(self, request, context):
        raise NotImplementedError("FindTimesplitOnLap is not implemented")

    def MatchRoutes(self, request, context):
        # Extract recorded and predefined routes
        recorded_route = request.recorded_route
        predefined_route = request.predefined_route

        # Placeholder for matching logic
        matched_points = predefined_route  # Replace with actual matching logic
        distances = [pb2.CumulativeDistance(meters=1.0)] * len(
            predefined_route
        )  # Replace with actual distance calculation

        # Build the response
        response = pb2.MatchResponse(
            success=pb2.MatchSuccess(matched_points=matched_points, distances=distances)
        )
        return response


def serve():
    server = grpc.server(futures.ThreadPoolExecutor(max_workers=10))
    pb2_grpc.add_GeografServicer_to_server(GeografServicer(), server)

    # Enable reflection
    SERVICE_NAMES = (
        pb2.DESCRIPTOR.services_by_name["Geograf"].full_name,
        reflection.SERVICE_NAME,
    )
    reflection.enable_server_reflection(SERVICE_NAMES, server)

    server.add_insecure_port("[::]:50051")
    print("Server is running on port 50051...")
    server.start()
    server.wait_for_termination()


if __name__ == "__main__":
    serve()
