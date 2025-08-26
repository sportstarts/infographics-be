curl -X GET "https://connect.garmin.com/activity-service/activity/20075051433/details?maxChartSize=200000" \
  -H "Host: connect.garmin.com" \
  -H "User-Agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10.15; rv:139.0) Gecko/20100101 Firefox/139.0" \
  -H "Accept: application/json" \
  -H "Accept-Language: en-US,en;q=0.5" \
  -H "NK: NT" \
  -H "X-app-ver: 5.14.1.2" \
  -H "X-lang: en-US" \
  -H "DI-Backend: connectapi.garmin.com" \
  -H "X-Requested-With: XMLHttpRequest" \
  -H "sentry-trace: 96db169340fc409e94aef626fa97f9ff-b563302c1931bbd5-1" \
  -H "baggage: sentry-environment=prod,sentry-release=connect%405.14.27,sentry-public_key=f0377f25d5534ad589ab3a9634f25e71,sentry-trace_id=96db169340fc409e94aef626fa97f9ff,sentry-sample_rate=1,sentry-transaction=%2Fmodern%2Factivity%2F%3Cdigits%3E,sentry-sampled=true" \
  -H "DNT: 1" \
  -H "Sec-GPC: 1" \
  -H "Connection: keep-alive" \
  -H "Referer: https://connect.garmin.com/modern/activity/19499583048" \
  -H "Sec-Fetch-Dest: empty" \
  -H "Sec-Fetch-Mode: cors" \
  -H "Sec-Fetch-Site: same-origin" \
  -H "TE: trailers" \
  -H "Authorization: Bearer $(curl -X POST https://connect.garmin.com/services/auth/token/public | jq -r '.access_token')" | jq > response.json

jq '[.activityDetailMetrics[] | {
  time: .metrics[1],
  distance: .metrics[3],
  heartRate: null,
  latitude: .metrics[8],
  longitude: .metrics[2],
  timestamp: .metrics[7]
}]' response.json