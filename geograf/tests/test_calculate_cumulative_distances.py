from math import isclose
from server import geograf as g
from server.proto.geograf_pb2 import *


def assert_distances(actual, expected, rel_tol):
    assert len(actual) == len(expected)
    for a, e in zip(actual, expected):
        assert isclose(a, e, rel_tol=rel_tol)


def load_gpx_points(file_path: str) -> list[GpsPoint]:
    import gpxpy

    with open(file_path, "r") as gpx_file:
        gpx = gpxpy.parse(gpx_file)

    points = []
    for track in gpx.tracks:
        for segment in track.segments:
            for point in segment.points:
                gps_point = GpsPoint(
                    latitude=Lat(value=point.latitude), longitude=Lon(value=point.longitude)
                )
                points.append(gps_point)
    return points


def test_empty_list_returns_empty():
    """calculate_cumulative_distances should return an empty list for no points."""
    res = g.calculate_cumulative_distances([])
    assert res == []


def test_two_points_equator():
    gps = [
        GpsPoint(latitude=Lat(value=0.0), longitude=Lon(value=0.0)),
        GpsPoint(latitude=Lat(value=0.0), longitude=Lon(value=0.001)),
    ]

    result = [x.meters for x in g.calculate_cumulative_distances(gps)]

    # ~111.319 m at equator
    expected = [0.0, 111.319]

    assert_distances(result, expected, rel_tol=0.1 / 100)


def test_three_points_additive():
    gps = [
        GpsPoint(latitude=Lat(value=52.0), longitude=Lon(value=13.0)),
        GpsPoint(latitude=Lat(value=52.0), longitude=Lon(value=13.001)),
        GpsPoint(latitude=Lat(value=52.0), longitude=Lon(value=13.002)),
    ]

    result = g.calculate_cumulative_distances(gps)

    d1 = result[1].meters
    d2 = result[2].meters

    assert d2 > d1
    assert isclose(d2, 2 * d1, rel_tol=0.1 / 100)


def test_crossing_utm_zone():
    # Near zone boundary: lon ≈ 6°E (zones 31/32)
    gps = [
        GpsPoint(latitude=Lat(value=50.0), longitude=Lon(value=5.999)),
        GpsPoint(latitude=Lat(value=50.0), longitude=Lon(value=6.001)),
    ]

    result = [x.meters for x in g.calculate_cumulative_distances(gps)]

    # Ground truth from geodesic calculator ≈ 143.4 m
    expected = [0.0, 143.4]

    assert_distances(result, expected, rel_tol=0.1 / 100)


def test_real_route_1():
    points = load_gpx_points("./tests/data/euroservice.gpx")

    result = g.calculate_cumulative_distances(points)

    assert isclose(result[-1].meters, 13_460, rel_tol=0.1 / 100) # taken from garmin connect


def test_real_route_2():
    points = load_gpx_points("./tests/data/Moscow.gpx")

    result = g.calculate_cumulative_distances(points)

    assert isclose(result[-1].meters, 26_900, rel_tol=0.1 / 100) # taken from garmin connect


def test_real_route_3():
    points = load_gpx_points("./tests/data/Zayamashnae.gpx")

    result = g.calculate_cumulative_distances(points)

    assert isclose(result[-1].meters, 32_990, rel_tol=0.1 / 100) # taken from garmin connect


def test_real_route_4():
    points = load_gpx_points("./tests/data/Ovachik.gpx")

    result = g.calculate_cumulative_distances(points)

    assert isclose(result[-1].meters, 62_260, rel_tol=0.1 / 100) # taken from garmin connect


def test_real_route_5():
    points = load_gpx_points("./tests/data/Gonuyk_Canyon.gpx")

    result = g.calculate_cumulative_distances(points)

    assert isclose(result[-1].meters, 15930, rel_tol=0.1 / 100) # taken from garmin connect


def test_real_route_6():
    points = load_gpx_points("./tests/data/Naliboki_2024.gpx")

    result = g.calculate_cumulative_distances(points)

    assert isclose(result[-1].meters, 100_910, rel_tol=0.1 / 100) # taken from garmin connect

def test_real_route_7():
    points = load_gpx_points("./tests/data/Naliboki_2023.gpx")

    result = g.calculate_cumulative_distances(points)

    assert isclose(result[-1].meters, 101_470, rel_tol=0.1 / 100) # taken from garmin connect


def test_real_route_8():
    points = load_gpx_points("./tests/data/Kemer_Gedelme_Ovachik.gpx")

    result = g.calculate_cumulative_distances(points)

    assert isclose(result[-1].meters, 67_490, rel_tol=0.1 / 100) # taken from garmin connect


def test_real_route_9():
    points = load_gpx_points("./tests/data/Naliboki_2022.gpx")

    result = g.calculate_cumulative_distances(points)

    assert isclose(result[-1].meters, 99_410, rel_tol=0.1 / 100) # taken from garmin connect


def test_real_route_10():
    points = load_gpx_points("./tests/data/running_zg_21189507559.gpx")

    result = g.calculate_cumulative_distances(points)

    assert isclose(result[-1].meters, 8_230, rel_tol=0.7 / 100) # TODO why high tolerance? 


def test_real_route_11():
    points = load_gpx_points("./tests/data/running_zg_21252698158.gpx")

    result = g.calculate_cumulative_distances(points)

    assert isclose(result[-1].meters, 11_090, rel_tol=1.2 / 100) # TODO why high tolerance? 


def test_real_route_12():
    points = load_gpx_points("./tests/data/cycling_Raubichi_20526304516.gpx")

    result = g.calculate_cumulative_distances(points)

    assert isclose(result[-1].meters, 20_900, rel_tol=1 / 100) # TODO why high tolerance? 







