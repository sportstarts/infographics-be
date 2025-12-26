from concurrent import futures
import grpc
from grpc_reflection.v1alpha import reflection
import geograf_pb2
import geograf_pb2_grpc

class GeografServicer(geograf_pb2_grpc.GeografServicer):
    def MatchRoutes(self, request, context):
        # Extract recorded and predefined routes
        recorded_route = request.recorded_route
        predefined_route = request.predefined_route

        # Placeholder for matching logic
        matched_points = predefined_route  # Replace with actual matching logic
        distances = [geograf_pb2.CumulativeDistance(meters=1.0)] * len(predefined_route)  # Replace with actual distance calculation

        # Build the response
        response = geograf_pb2.MatchResponse(
            success=geograf_pb2.MatchSuccess(
                matched_points=matched_points,
                distances=distances
            )
        )
        return response

def serve():
    server = grpc.server(futures.ThreadPoolExecutor(max_workers=10))
    geograf_pb2_grpc.add_GeografServicer_to_server(GeografServicer(), server)

    # Enable reflection
    SERVICE_NAMES = (
        geograf_pb2.DESCRIPTOR.services_by_name['Geograf'].full_name,
        reflection.SERVICE_NAME,
    )
    reflection.enable_server_reflection(SERVICE_NAMES, server)

    server.add_insecure_port('[::]:50051')
    print("Server is running on port 50051...")
    server.start()
    server.wait_for_termination()

if __name__ == '__main__':
    serve()