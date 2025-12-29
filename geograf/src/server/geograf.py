from pyproj import Geod
from server.proto.geograf_pb2 import GpsPoint, CumulativeDistance


def calculate_cumulative_distances(
    gps_points: list[GpsPoint],
) -> list[CumulativeDistance]:
    if not gps_points:
        return []

    geod = Geod(ellps="WGS84")

    cumulative_distance_meters = 0
    distances = [CumulativeDistance(meters=0.0)]  # First point has zero distance

    for i in range(1, len(gps_points)):
        point1 = gps_points[i - 1]
        point2 = gps_points[i]
        _, _, distance = geod.inv(
            point1.longitude.value,
            point1.latitude.value,
            point2.longitude.value,
            point2.latitude.value,
        )
        cumulative_distance_meters += distance
        distances.append(CumulativeDistance(meters=cumulative_distance_meters))

    return distances
