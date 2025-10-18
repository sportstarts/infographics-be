from concurrent import futures
import grpc
from grpc_reflection.v1alpha import reflection
import map_matcher_pb2
import map_matcher_pb2_grpc

class MapMatcherServicer(map_matcher_pb2_grpc.MapMatcherServicer):
    def MatchRoutes(self, request, context):
        # Extract recorded and predefined routes
        recorded_route = request.recorded_route
        predefined_route = request.predefined_route

        # Placeholder for matching logic
        matched_points = predefined_route  # Replace with actual matching logic
        distances = [0.0] * len(predefined_route)  # Replace with actual distance calculation

        # Build the response
        response = map_matcher_pb2.MatchResponse(
            success=map_matcher_pb2.Success(
                matched_points=matched_points,
                distances=distances
            )
        )
        return response

def serve():
    server = grpc.server(futures.ThreadPoolExecutor(max_workers=10))
    map_matcher_pb2_grpc.add_MapMatcherServicer_to_server(MapMatcherServicer(), server)

    # Enable reflection
    SERVICE_NAMES = (
        map_matcher_pb2.DESCRIPTOR.services_by_name['MapMatcher'].full_name,
        reflection.SERVICE_NAME,
    )
    reflection.enable_server_reflection(SERVICE_NAMES, server)

    server.add_insecure_port('[::]:50051')
    print("Server is running on port 50051...")
    server.start()
    server.wait_for_termination()

if __name__ == '__main__':
    serve()