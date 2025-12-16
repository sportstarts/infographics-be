import React, { useState } from "react";
import {
  LineChart,
  Line,
  XAxis,
  YAxis,
  Tooltip,
  Legend,
  ReferenceLine,
  ReferenceArea,
  ResponsiveContainer,
} from "recharts";

const data = [
  { distance: 0, A: 0, B: 0 },
  { distance: 1, A: 5, B: 3 },
  { distance: 2, A: 7, B: 6 },
  { distance: 3, A: 12, B: 8 },
  { distance: 4, A: 15, B: 10 },
];

const laps = [
  { start: 0, end: 1, name: "Lap 1", sport: "Run" },
  { start: 1, end: 4, name: "Lap 2", sport: "Bike" },
];

const LapTick = ({ x, y, payload }) => {
  const lap = laps.find(l => payload.value >= l.start && payload.value < l.end);
  if (!lap) return null;
  return (
    <g transform={`translate(${x},${y})`}>
      <text y={10} textAnchor="middle" fontSize={12} fill="#444">{lap.name}</text>
      <text y={26} textAnchor="middle" fontSize={10} fill="#888">{lap.sport}</text>
    </g>
  );
};

export default function TimeGapChart() {
  const [hoveredDistance, setHoveredDistance] = useState(null);

  const handleMouseMove = (state) => {
    if (state && state.activeLabel != null) {
      setHoveredDistance(state.activeLabel);
    } else {
      setHoveredDistance(null);
    }
  };

  const handleMouseLeave = () => setHoveredDistance(null);

  return (
    <ResponsiveContainer width="100%" height={400}>
      <LineChart
        data={data}
        margin={{ top: 20, right: 30, left: 20, bottom: 40 }}
        onMouseMove={handleMouseMove}
        onMouseLeave={handleMouseLeave}
      >
        <XAxis xAxisId="distance" dataKey="distance" type="number" domain={[0, "dataMax"]} tickFormatter={v => `${v} km`} />
        <XAxis xAxisId="lap" dataKey="distance" type="number" axisLine={true} tickLine={false} interval={0} ticks={laps.map(l => (l.start + l.end) / 2)} height={40} tick={<LapTick />} />
        <YAxis label={{ value: "Time Gap (s)", angle: -90, position: "insideLeft" }} />
        <Tooltip />
        <Legend />

        {/* Fill the hovered lap area */}
        {laps.map((lap, idx) => (
          hoveredDistance >= lap.start && hoveredDistance < lap.end ? (
            <ReferenceArea
              key={idx}
              x1={lap.start}
              x2={lap.end}
              y1="auto"
              y2="auto"
              fill="rgba(255,87,34,0.1)"
            />
          ) : null
        ))}

        {/* Vertical dashed lines for lap borders */}
        {laps.map((lap, idx) => (
          <ReferenceLine
            key={idx}
            x={lap.start}
            stroke="#999"
            strokeDasharray="4 4"
          />
        ))}

        <Line type="monotone" dataKey="A" name="Participant A" dot={false} />
        <Line type="monotone" dataKey="B" name="Participant B" dot={false} />
      </LineChart>
    </ResponsiveContainer>
  );
}
